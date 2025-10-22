package games.stendhal.client.fx.view;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Lightweight animated canvas acting as a placeholder for the future renderer.
 */
public class GameCanvas extends Canvas {

        private final AnimationTimer timer;
        private long startNanos;

        public GameCanvas() {
                timer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                                if (startNanos == 0) {
                                        startNanos = now;
                                }
                                drawFrame((now - startNanos) / 1_000_000_000.0);
                        }
                };

                widthProperty().addListener((obs, oldVal, newVal) -> drawFrame(0));
                heightProperty().addListener((obs, oldVal, newVal) -> drawFrame(0));
        }

        public void start() {
                timer.start();
        }

        public void stop() {
                timer.stop();
        }

        private void drawFrame(double seconds) {
                GraphicsContext gc = getGraphicsContext2D();
                double width = getWidth();
                double height = getHeight();

                gc.setFill(Color.rgb(12, 17, 26));
                gc.fillRect(0, 0, width, height);

                double gridSize = 48;
                gc.setStroke(Color.rgb(30, 41, 59, 0.4));
                for (double x = 0; x < width; x += gridSize) {
                        gc.strokeLine(x, 0, x, height);
                }
                for (double y = 0; y < height; y += gridSize) {
                        gc.strokeLine(0, y, width, y);
                }

                double radius = 60 + 30 * Math.sin(seconds * 0.7);
                double centerX = width / 2.0;
                double centerY = height / 2.0;

                gc.setFill(Color.rgb(56, 189, 248, 0.45));
                gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

                gc.setFill(Color.rgb(14, 165, 233, 0.4));
                gc.fillOval(centerX - radius * 0.7 + Math.cos(seconds) * 40,
                                centerY - radius * 0.7 + Math.sin(seconds) * 40, radius * 1.4, radius * 1.4);

                gc.setFill(Color.rgb(224, 242, 254, 0.9));
                gc.fillText("Mapa świata w przebudowie", centerX - 120, centerY + radius + 24);
        }
}
