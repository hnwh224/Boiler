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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class Test {
    public static void main(String[] args) throws IOException, InterruptedException {




        Application.launch(AppStarter.class);
    }

    public static class AppStarter extends Application {

        public AppStarter() {

        }

        @Override
        public void start(Stage stage) throws Exception {
            //stage.setTitle("JavaFX Without Extending Application");

            StackPane root = new StackPane();


            Button button = new Button("POMMES");

            root.getChildren().add(button);

            Scene scene = new Scene(root, 300, 300);

            WritableImage writableImage = new WritableImage(300, 300);
            root.snapshot(null, writableImage);

            BufferedImage bufferedImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bufferedImage.createGraphics();

            for(int x = 0; x < 300; x++) {
                for(int y = 0; y < 300; y++) {
                    int color = writableImage.getPixelReader().getArgb(x, y);
                    g.setColor(new Color(color));
                    g.fillRect(x, y, 1, 1);
                }
            }

            g.dispose();
            ImageIO.write(bufferedImage, "png", new File("javafx.png"));



            //stage.setScene(scene);
            //stage.show();
        }
    }
}
