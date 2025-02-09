/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core;

import com.google.gson.Gson;
import net.somewhatcity.boiler.api.util.GraphicUtils;
import net.somewhatcity.boiler.core.keyboard.KeyModifier;
import net.somewhatcity.boiler.core.keyboard.KeyboardKey;
import net.somewhatcity.boiler.core.keyboard.Keyboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

public class Test {
    public static void main(String[] args) throws IOException {
        String json = Files.readString(Path.of("./kb_layout_de.json"));

        Gson gson = new Gson();

        Keyboard keyboard = gson.fromJson(json, Keyboard.class);

        System.out.println(keyboard.layout);

        BufferedImage img = new BufferedImage(128 * 3, 128, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        g.setFont(new Font("Arial", Font.PLAIN, 11));

        for(KeyboardKey key : keyboard.keys) {
            System.out.println(Arrays.toString(key.bounds));
            Rectangle rect = new Rectangle(key.bounds[0], key.bounds[1], key.bounds[2], key.bounds[3]);
            g.setColor(Color.decode(key.backgroundColor));
            g.fill(rect);
            g.setColor(Color.decode(key.color));

            if(key.modifiers != null) {
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

        ImageIO.write(img, "png", new File("kb.png"));
    }
}
