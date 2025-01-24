/*
 * Copyright (c) 2023.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.display;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import de.pianoman911.mapengine.api.clientside.IMapDisplay;
import de.pianoman911.mapengine.api.drawing.IDrawingSpace;
import de.pianoman911.mapengine.api.util.Converter;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.SourceConfig;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.api.util.SourceType;
import net.somewhatcity.boiler.core.BoilerConfig;
import net.somewhatcity.boiler.core.BoilerPlugin;
import net.somewhatcity.boiler.core.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.somewhatcity.boiler.core.BoilerPlugin.MAP_ENGINE;

public class ImplBoilerDisplay implements IBoilerDisplay, Listener {

    private static final Executor SCENE_EXECUTOR = Executors.newCachedThreadPool();
    private final int ID;
    private IMapDisplay MAP_DISPLAY;
    private final Location CORNER_A;
    private final Location CORNER_B;
    private final BlockFace FACING;
    private IDrawingSpace drawingSpace;
    private BufferedImage image;
    private Graphics2D g2;
    private IBoilerSource source;
    private String sourceName;
    private Rectangle viewport;
    private boolean autoTick = true;
    private boolean persistent = true;
    private boolean renderPaused = false;
    private JsonObject settings = new JsonObject();
    private JsonObject sourceData = new JsonObject();
    private List<Location> speakers = new ArrayList<>();
    private HashMap<UUID, Long> lastUpdates = new HashMap<>();
    private final Set<Player> receivers = new HashSet<>();
    private long lastRender;
    private int renderPeriod = 20;
    private Timer renderTimer;

    public ImplBoilerDisplay(int id, Location cornerA, Location cornerB, BlockFace facing) {
        this.ID = id;
        this.CORNER_A = cornerA;
        this.CORNER_B = cornerB;
        this.FACING = facing;

        this.MAP_DISPLAY = MAP_ENGINE.displayProvider().createBasic(cornerA.toVector().toBlockVector(), cornerB.toVector().toBlockVector(), facing);
        this.viewport = new Rectangle(0, 0, width(), height());
        this.drawingSpace = MAP_ENGINE.pipeline().createDrawingSpace(MAP_DISPLAY);
        this.image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
        this.g2 = this.image.createGraphics();

        this.settings.addProperty("buffer", true);

        MAP_DISPLAY.frameAt(0, 0).frameEntityId();

        BoilerPlugin.getPlugin().getServer().getPluginManager().registerEvents(this, BoilerPlugin.getPlugin());

        save();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        receivers.remove(e.getPlayer());
    }

    @Override
    public void tick() {

        SourceConfig sourceConfig = null;
        if(source != null) sourceConfig = source.getClass().getAnnotation(SourceConfig.class);

        int viewDistance = BoilerConfig.viewDistance;
        if(settings.has("viewDistance")) {
            viewDistance = settings.get("viewDistance").getAsInt();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(CORNER_A.getWorld().equals(player.getWorld()) && CORNER_A.distance(player.getLocation()) < viewDistance) {
                if(!receivers.contains(player)) {
                    receivers.add(player);

                    if(BoilerConfig.clientEnabled && sourceConfig != null && sourceConfig.sourceType().equals(SourceType.CLIENT)) {
                        System.out.println("DISPLAY_CREATE " + id());

                        String host = BoilerConfig.rtmpHost;
                        String bind = BoilerConfig.rtmpBind;
                        int port = BoilerConfig.rtmpPort;

                        String stream = String.format("rtsp://{user}:{password}@%s:%s/live/display_%s", host, port, id());
                        String username = "bs_" + player.getName().replace("_", "").toLowerCase(Locale.ROOT);
                        String password = UUID.randomUUID().toString().replace("-", "");

                        BoilerPlugin.getPlugin().mediaMtxAuthServer().registerUser(username, password);

                        String userStream = stream
                                .replace("{user}", username)
                                .replace("{password}", password);

                        BoilerClientUtil.sendCreateDisplay(player, this, userStream);
                    } else {
                        MAP_DISPLAY.spawn(player);
                        if(settings.has("itemRotation")) MAP_DISPLAY.itemRotation(player, settings.get("itemRotation").getAsInt());
                        if(settings.has("visualDirection")) MAP_DISPLAY.visualDirection(player, BlockFace.valueOf(settings.get("visualDirection").getAsString()));
                        if(settings.has("rotation")) MAP_DISPLAY.rotation(player, settings.getAsJsonObject("rotation").get("yaw").getAsFloat(), settings.getAsJsonObject("rotation").get("pitch").getAsFloat());
                    }
                }
            }else {
                if(receivers.contains(player)) {
                    receivers.remove(player);

                    if(BoilerConfig.clientEnabled && sourceConfig != null && sourceConfig.sourceType().equals(SourceType.CLIENT)) {
                        BoilerClientUtil.sendRemoveDisplay(player, this);
                    } else {
                        MAP_DISPLAY.despawn(player);
                    }
                }
            }
        }

        if(sourceConfig == null || sourceConfig.sourceType().equals(SourceType.SERVER)) {
            if(receivers.isEmpty() && renderTimer != null) {
                renderTimer.cancel();
                renderTimer = null;
            } else if(renderTimer == null && !receivers.isEmpty()) {
                renderTimer = new Timer();
                renderTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        render();
                    }
                }, 0, renderPeriod);
            }
        }
    }

    @Override
    public boolean autoTick() {
        return autoTick;
    }

    @Override
    public void autoTick(boolean value) {
        this.autoTick = value;
    }

    @Override
    public boolean persistent() {
        return persistent;
    }

    @Override
    public void persistent(boolean value) {
        this.persistent = value;
    }

    @Override
    public void renderPaused(boolean value) {
        this.renderPaused = value;
    }

    @Override
    public boolean renderPaused() {
        return renderPaused;
    }

    @Override
    public void render() {

        if(renderPaused) return;
        if(receivers.isEmpty()) return;

        if(source == null) {
            source("default", null);
            return;
        }

        Set<Player> actualReceivers = new HashSet<>();
        actualReceivers.addAll(receivers);
        Set<Player> toRemove = new HashSet<>();

        actualReceivers.forEach(player -> {

            int interval = BoilerPlugin.getPlugin().intervalManager().getInterval(player);
            if(interval > 0 && lastUpdates.containsKey(player.getUniqueId())) {
                long lastUpdate = lastUpdates.get(player.getUniqueId());
                if(lastUpdate + interval > System.currentTimeMillis()) {
                    toRemove.add(player);
                }
            }
        });
        actualReceivers.removeAll(toRemove);

        drawingSpace.ctx().receivers(actualReceivers);
        source.draw(drawingSpace);
        drawingSpace.flush();

        actualReceivers.forEach(player -> {
            lastUpdates.put(player.getUniqueId(), System.currentTimeMillis());
        });

        lastRender = System.currentTimeMillis();
    }

    @Override
    public void respawn() {
        for(Player player : receivers) {
            MAP_DISPLAY.despawn(player);
        }
        receivers.clear();
    }

    @Override
    public void remove() {
        if(this.source != null) {
            try {
                source.unload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        MAP_DISPLAY = null;

        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @Override
    public String rtspPublishUrl() {
        return "rtsp://localhost:8554/live/display_%s".formatted(id());
    }

    @Override
    public Set<Player> viewers() {
        return receivers;
    }

    @Override
    public List<Location> speakers() {
        return Collections.unmodifiableList(speakers);
    }

    @Override
    public void addSpeaker(Location location) {
        speakers.add(location);
    }

    @Override
    public void clearSpeakers() {
        speakers.clear();
    }

    @Override
    public void onClick(CommandSender sender, int x, int y, boolean right) {
        if(source != null) source.onClick(sender, x, y, right);
    }

    @Override
    public void onScroll(CommandSender sender, int x, int y, int delta) {
        if(source != null) source.onScroll(sender, x, y, delta);
    }

    @Override
    public void onInput(CommandSender sender, String string) {
        if(source != null) source.onInput(sender, string);
    }

    @Override
    public void onKey(CommandSender sender, String key) {
        if(source != null) source.onKey(sender, key);
    }

    @Override
    public void save() {
        BoilerPlugin.getPlugin().displayManager().saveDisplay(this);

        drawingSpace.ctx().converter(settings.get("dither") != null && settings.get("dither").getAsBoolean() ? Converter.FLOYD_STEINBERG : Converter.DIRECT);
        drawingSpace.ctx().buffering(settings.get("buffer") != null && settings.get("buffer").getAsBoolean());
        drawingSpace.ctx().bundling(settings.get("bundle") != null && settings.get("bundle").getAsBoolean());
        mapDisplay().glowing(settings.get("glowing") != null && settings.get("glowing").getAsBoolean());

        renderPeriod = settings.get("renderPeriod") != null ? settings.get("renderPeriod").getAsInt() : 20;
    }

    @Override
    public void saveSourceData(JsonObject data) {
        this.sourceData = data;
        save();
    }

    @Override
    public int id() {
        return this.ID;
    }

    @Override
    public void source(String name, JsonObject data) {
        Class<? extends IBoilerSource> sourceClass = BoilerPlugin.getPlugin().sourceManager().source(name);
        if(sourceClass == null) {
            BoilerPlugin.getPlugin().getLogger().log(Level.WARNING, "Failed to load source for map display " + this.id() + " (source not found)");
            return;
        }

        if(this.source != null) {
            try {
                this.source.unload();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        boolean keepLastSourceData = data != null && data.has("keepLastSourceData") && data.get("keepLastSourceData").getAsBoolean();

        this.sourceData = data;
        this.sourceName = name;

        g2.clearRect(0, 0, width(), height());

        if(persistent && !keepLastSourceData) {
            save();
        }

        BoilerPlugin.EXECUTOR.execute(() -> {
            try {
                source = sourceClass.getDeclaredConstructor().newInstance();
                source.load(this, data);

                receivers.forEach(player -> {
                    MAP_DISPLAY.despawn(player);
                    BoilerClientUtil.sendRemoveDisplay(player, this);
                });
                receivers.clear();

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public String sourceName() {
        return sourceName;
    }

    @Override
    public IBoilerSource source() {
        return source;
    }

    @Override
    public JsonObject sourceData() {
        return sourceData;
    }

    @Override
    public Location cornerA() {
        return CORNER_A;
    }

    @Override
    public Location cornerB() {
        return CORNER_B;
    }

    @Override
    public Location center() {
        return CORNER_A.clone().add(CORNER_B).multiply(0.5);
    }

    @Override
    public JsonObject settings() {
        return settings;
    }

    @Override
    public void settings(JsonObject obj) {
        settings = obj;
        save();
    }

    @Override
    public BlockFace facing() {
        return FACING;
    }

    @Override
    public IMapDisplay mapDisplay() {
        return MAP_DISPLAY;
    }

    @Override
    public IDrawingSpace drawingSpace() {
        return drawingSpace;
    }

    @Override
    public void viewport(Rectangle viewport) {
        if(viewport == null) throw new NullPointerException("Viewport cannot be null");
        this.viewport = viewport;
    }

    @Override
    public Rectangle viewport() {
        return viewport;
    }

}
