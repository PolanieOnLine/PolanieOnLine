/***************************************************************************
 *                   (C) Copyright 2003-2024 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.stendhal;
import javafx.animation.AnimationTimer;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * JavaFX canvas responsible for rendering the world view.
 */
public class GameCanvasFX extends Canvas {

    private static final double GRID_SIZE = 32.0;

    private final AnimationTimer animationTimer;
    private long lastFrameTime;
    private double orbitAngle;
    private double playerX;
    private double playerY;

    public GameCanvasFX(double width, double height) {
        super(width, height);
        setFocusTraversable(true);

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                renderFrame(now);
            }
        };

        widthProperty().addListener((observable, oldValue, newValue) -> drawStaticElements());
        heightProperty().addListener((observable, oldValue, newValue) -> drawStaticElements());

        drawStaticElements();
    }

    private void renderFrame(long now) {
        if (lastFrameTime == 0) {
            lastFrameTime = now;
            updatePlayerPosition(0);
            drawFrame();
            return;
        }

        double deltaSeconds = (now - lastFrameTime) / 1_000_000_000.0;
        lastFrameTime = now;

        updatePlayerPosition(deltaSeconds);
        drawFrame();
    }

    private void updatePlayerPosition(double deltaSeconds) {
        orbitAngle += deltaSeconds;
        double radiusX = Math.max(64, getWidth() / 4.0);
        double radiusY = Math.max(48, getHeight() / 4.0);
        playerX = getWidth() / 2.0 + Math.cos(orbitAngle) * radiusX;
        playerY = getHeight() / 2.0 + Math.sin(orbitAngle) * radiusY;
    }

    private void drawStaticElements() {
        drawFrame();
    }

    private void drawFrame() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.web("#1b1e2b"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setStroke(Color.web("#2f3347"));
        gc.setLineWidth(1.0);
        for (double x = 0; x <= getWidth(); x += GRID_SIZE) {
            gc.strokeLine(x, 0, x, getHeight());
        }
        for (double y = 0; y <= getHeight(); y += GRID_SIZE) {
            gc.strokeLine(0, y, getWidth(), y);
        }

        gc.setFill(Color.web("#8f5b2b"));
        double tileSize = GRID_SIZE - 6;
        double markerX = Math.floor(playerX / GRID_SIZE) * GRID_SIZE + 3;
        double markerY = Math.floor(playerY / GRID_SIZE) * GRID_SIZE + 3;
        gc.fillRoundRect(markerX, markerY, tileSize, tileSize, 8, 8);

        gc.setFill(Color.web("#ffe082"));
        double playerRadius = 18;
        gc.fillOval(playerX - playerRadius, playerY - playerRadius, playerRadius * 2, playerRadius * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Verdana", 14));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.TOP);
        gc.fillText(String.format("FPS limit: %d", stendhal.getFpsLimit()), 10, 10);
        gc.fillText(String.format("Canvas: %.0f x %.0f", getWidth(), getHeight()), 10, 30);
    }

    /**
     * Start the render loop.
     */
    public void start() {
        lastFrameTime = 0;
        animationTimer.start();
    }

    /**
     * Stop the render loop.
     */
    public void stop() {
        animationTimer.stop();
    }

    /**
     * Bind the canvas size to the provided parent region.
     *
     * @param parent parent region hosting the canvas
     */
    public void bindToParent(Region parent) {
        parent.widthProperty().addListener((obs, oldWidth, newWidth) -> setWidth(newWidth.doubleValue()));
        parent.heightProperty().addListener((obs, oldHeight, newHeight) -> setHeight(newHeight.doubleValue()));
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }
}
