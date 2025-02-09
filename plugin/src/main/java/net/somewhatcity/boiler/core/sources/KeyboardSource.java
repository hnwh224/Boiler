/*
 * Copyright (c) 2024.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.sources;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.somewhatcity.boiler.api.CreateArgument;
import net.somewhatcity.boiler.api.CreateCommandArguments;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.api.util.CommandArgumentType;
import net.somewhatcity.boiler.api.util.GraphicUtils;
import net.somewhatcity.boiler.core.BoilerPlugin;
import net.somewhatcity.boiler.core.keyboard.KeyModifier;
import net.somewhatcity.boiler.core.keyboard.Keyboard;
import net.somewhatcity.boiler.core.keyboard.KeyboardKey;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CreateCommandArguments(arguments = {
        @CreateArgument(name = "link", type = CommandArgumentType.INTEGER)
})
public class KeyboardSource implements IBoilerSource {

    private BufferedImage currentFrame = null;
    private int linkedId;
    private CommandSender lastInteractor;
    private String json;
    private Gson gson = new Gson();

    private boolean altKey = false;
    private boolean ctrlKey = false;
    private boolean shiftKey = false;
    //private boolean capsLoc = false;

    private Keyboard keyboard;

    @Override
    public void load(IBoilerDisplay display, JsonObject data) {
        linkedId = data.get("link").getAsInt();

        try {
            InputStream inputStream = BoilerPlugin.getPlugin().getResource("assets/keyboards/kb_layout_de.json");
            json = new String(inputStream.readAllBytes());

            loadFromJson();
            renderKeyboard();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void loadFromJson() {
        keyboard = gson.fromJson(json, Keyboard.class);
    }

    public void renderKeyboard() {

        BufferedImage img = new BufferedImage(128 * 3, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setFont(new Font("Arial", Font.PLAIN, 11));

        for(KeyboardKey key : keyboard.keys) {
            Rectangle rect = new Rectangle(key.bounds[0], key.bounds[1], key.bounds[2], key.bounds[3]);

            Color bgColor = Color.decode(key.backgroundColor);
            if(System.currentTimeMillis() - key.lastPressed < 200) {
                bgColor = bgColor.darker().darker();
            }
            g.setColor(bgColor);
            g.fill(rect);
            g.setColor(Color.decode(key.color));

            if(key.modifiers != null && shiftKey) {
                Optional<KeyModifier> optionalModifier = key.modifiers.stream().filter(mod -> mod.value.equals("shift")).findFirst();

                if(optionalModifier.isPresent()) {
                    KeyModifier modifier = optionalModifier.get();
                    GraphicUtils.centeredString(g, rect, modifier.text);
                }
            } else {
                GraphicUtils.centeredString(g, rect, key.text);
            }
        }

        g.dispose();

        currentFrame = img;
    }

    @Override
    public void unload() {

    }

    @Override
    public void draw(Graphics2D g2, Rectangle viewport) {
        renderKeyboard();
        if(currentFrame != null) g2.drawImage(currentFrame, 0, 0, 128 * 3, 128, null);
    }

    public void sendKeystroke(int keycode) {
        IBoilerDisplay display = BoilerPlugin.getPlugin().displayManager().display(linkedId);
        if(display == null || lastInteractor == null) return;

        switch (keycode) {
            case KeyEvent.VK_CAPS_LOCK -> {
                shiftKey = !shiftKey;
            }
        }

        display.onKey(lastInteractor, KeyEvent.getKeyText(keycode));
    }

    @Override
    public void onClick(CommandSender sender, int x, int y, boolean right) {
        lastInteractor = sender;

        for(KeyboardKey key : keyboard.keys) {
            Rectangle rect = new Rectangle(key.bounds[0], key.bounds[1], key.bounds[2], key.bounds[3]);
            if(rect.contains(x, y)) {
                key.lastPressed = System.currentTimeMillis();
                System.out.println("key: " + KeyEvent.getKeyText(key.action));
                sendKeystroke(key.action);
                break;
            }
        }
    }

    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }

    public class KeyboardAction {

    }
}
