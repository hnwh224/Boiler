/*
 * Copyright (c) 2023.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.api.display;

import com.google.gson.JsonObject;
import de.pianoman911.mapengine.api.clientside.IMapDisplay;
import de.pianoman911.mapengine.api.drawing.IDrawingSpace;
import net.somewhatcity.boiler.api.IBoilerSource;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.List;
import java.util.Set;

public interface IBoilerDisplay {

    /**
     * @return the id of the display
     */
    int id();

    /**
     *
     * @param name the name of the source to load
     * @param data additional data that gets provided to the source on load
     */
    void source(String name, JsonObject data);

    /**
     *
     * @return the name of the currently loaded source
     */
    String sourceName();

    /**
     *
     * @return the object of the currently loaded source
     */
    IBoilerSource source();

    /**
     *
     * @return the data of the currently loaded source
     */
    JsonObject sourceData();

    /**
     *
     * @return the first corner of the display
     */
    Location cornerA();

    /**
     *
     * @return the second corner of the display
     */
    Location cornerB();

    /**
     *
     * @return the center of the display
     */
    Location center();

    /**
     *
     * @return display settings
     */
    JsonObject settings();

    /**
     *
     * @param obj the settings for the display
     */
    void settings(JsonObject obj);

    /**
     *
     * @return the BlockFace the display is facing
     */
    BlockFace facing();

    /**
     *
     * @return the MapDisplay provided by MapEngine
     */
    IMapDisplay mapDisplay();

    /**
     *
     * @return the drawingSpace provided by MapEngine
     */
    IDrawingSpace drawingSpace();

    /**
     *
     * @return the width of the display in pixels
     */
    default int width() {
        return mapDisplay().pixelWidth();
    }

    /**
     *
     * @return the height of the display in pixels
     */
    default int height() {
        return mapDisplay().pixelHeight();
    }

    void viewport(Rectangle viewport);
    Rectangle viewport();

    /**
     * ticks the display for all players (recalculates which players can see the display)
     */
    void tick();
    boolean autoTick();
    void autoTick(boolean value);
    boolean persistent();
    void persistent(boolean value);
    void renderPaused(boolean value);
    boolean renderPaused();

    /**
     * forces rendering of the display
     */
    void render();

    /**
     * respawns the display
     */
    void respawn();
    void remove();

    /**
     *
     * @return the url you can publish rtsp streams to. These streams will then get played back on client using the client mod
     */
    String rtspPublishUrl();
    Set<Player> viewers();
    List<Location> speakers();
    void addSpeaker(Location location);
    void clearSpeakers();
    void onClick(CommandSender player, int x, int y, boolean right);
    void onScroll(CommandSender player, int x, int y, int delta);
    void onInput(CommandSender player, String string);
    void onKey(CommandSender player, String key);
    void save();
    void saveSourceData(JsonObject data);

}
