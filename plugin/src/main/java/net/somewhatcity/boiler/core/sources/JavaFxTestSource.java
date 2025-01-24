/*
 * Copyright (c) 2025.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.somewhatcity.boiler.core.sources;

import com.google.gson.JsonObject;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.scene.input.PickResultChooser;
import de.pianoman911.mapengine.api.drawing.IDrawingSpace;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.somewhatcity.boiler.api.IBoilerSource;
import net.somewhatcity.boiler.api.display.IBoilerDisplay;
import net.somewhatcity.boiler.core.Test;
import org.bukkit.command.CommandSender;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JavaFxTestSource implements IBoilerSource {

    private BufferedImage currentImage;

    @Override
    public void load(IBoilerDisplay display, JsonObject data) {
        AppStarter.renderCallback = new RenderCallback() {
            @Override
            public void onRender(BufferedImage image) {
                currentImage = image;
            }
        };
        Application.launch(AppStarter.class);


    }

    @Override
    public void unload() {

    }

    @Override
    public void draw(Graphics2D g2, Rectangle viewport) {
        if(currentImage != null) {
            g2.drawImage(currentImage, 0, 0, null);
        }
    }

    @Override
    public void onClick(CommandSender sender, int x, int y, boolean right) {
        AppStarter.click(x, y);
    }

    public static class AppStarter extends Application {

        private static RenderCallback renderCallback;
        private static Scene scene;
        private static StackPane root;

        public AppStarter() {

        }

        public static void click(int x, int y) {

            Node pick = pick(root, x, y);
            Event.fireEvent(pick, new ActionEvent());
            Event.fireEvent(pick, new MouseEvent(MouseEvent.MOUSE_CLICKED,
                    x, y, x, y, MouseButton.PRIMARY, 1,
                    false, false, false, false, false, false, false, false, false, false, null));

        }

        @Override
        public void start(Stage stage) throws Exception {
            root = new StackPane();

            javafx.scene.control.Button button = new Button("POMMES");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    System.out.println("BUTTON CLICK!!!");
                }
            });

            button.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    System.out.println("BUTTON CLICK 2!!!");
                }
            });



            root.getChildren().add(button);

            scene = new Scene(root, 300, 300);

            scene.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    System.out.println("MOUSE CLICK!!!");
                }
            });

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
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
                            renderCallback.onRender(bufferedImage);
                        }
                    });
                }
            }, 0, 20);
        }

        @Override
        public void init() throws Exception {
            super.init();
        }
    }

    interface RenderCallback {
        void onRender(BufferedImage image);
    }

    public static Node pick(Node node, double sceneX, double sceneY) {
        Point2D p = node.sceneToLocal(sceneX, sceneY, true /* rootScene */);

        // check if the given node has the point inside it, or else we drop out
        if (!node.contains(p)) return null;

        // at this point we know that _at least_ the given node is a valid
        // answer to the given point, so we will return that if we don't find
        // a better child option
        if (node instanceof Parent) {
            // we iterate through all children in reverse order, and stop when we find a match.
            // We do this as we know the elements at the end of the list have a higher
            // z-order, and are therefore the better match, compared to children that
            // might also intersect (but that would be underneath the element).
            Node bestMatchingChild = null;
            List<Node> children = ((Parent)node).getChildrenUnmodifiable();
            for (int i = children.size() - 1; i >= 0; i--) {
                Node child = children.get(i);
                p = child.sceneToLocal(sceneX, sceneY, true /* rootScene */);
                if (child.isVisible() && !child.isMouseTransparent() && child.contains(p)) {
                    bestMatchingChild = child;
                    break;
                }
            }

            if (bestMatchingChild != null) {
                return pick(bestMatchingChild, sceneX, sceneY);
            }
        }

        return node;
    }
}
