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
package games.stendhal.client;

import java.awt.Dimension;

import org.apache.log4j.Logger;

import games.stendhal.client.gui.GameCanvasFX;
import games.stendhal.client.gui.HUDPaneFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * JavaFX front-end for the PolanieOnline client.
 */
public class PolanieClientFX extends Application {

    private static final Logger LOGGER = Logger.getLogger(PolanieClientFX.class);

    private GameCanvasFX gameCanvas;
    private HUDPaneFX hudPane;

    @Override
    public void start(Stage primaryStage) {
        Dimension displaySize = stendhal.getDisplaySize();

        gameCanvas = new GameCanvasFX(displaySize.getWidth(), displaySize.getHeight());
        hudPane = new HUDPaneFX();

        StackPane gameLayer = new StackPane(gameCanvas);
        gameLayer.setPadding(new Insets(8));

        BorderPane root = new BorderPane();
        root.setCenter(gameLayer);
        BorderPane.setMargin(gameLayer, new Insets(4, 8, 8, 8));
        root.setRight(wrapHudPane());

        double preferredWidth = displaySize.getWidth() + hudPane.getPrefWidth() + 32;
        double preferredHeight = Math.max(displaySize.getHeight() + 16, hudPane.getPrefHeight() + 16);
        Scene scene = new Scene(root, preferredWidth, preferredHeight);

        primaryStage.setTitle(stendhal.GAME_NAME + " " + stendhal.VERSION);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();

        gameCanvas.bindToParent(gameLayer);
        gameCanvas.start();
        gameCanvas.requestFocus();

        hudPane.appendSystemMessage("Witaj w prototypie klienta JavaFX PolanieOnline!");
        hudPane.appendSystemMessage("Sterowanie i logika sieciowa zostaną dodane w kolejnych iteracjach.");

        installInputHandlers(scene);

        primaryStage.setOnCloseRequest(event -> {
            LOGGER.info("Stage closed, shutting down JavaFX runtime");
            Platform.exit();
        });
    }

    private Region wrapHudPane() {
        VBox wrapper = new VBox();
        wrapper.setPadding(new Insets(8, 12, 8, 0));
        wrapper.getChildren().add(hudPane);
        VBox.setVgrow(hudPane, Priority.ALWAYS);
        return wrapper;
    }

    private void installInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.F11) {
                Stage stage = (Stage) scene.getWindow();
                stage.setFullScreen(!stage.isFullScreen());
            } else {
                hudPane.appendSystemMessage("Naciśnięto klawisz: " + code.getName());
            }
        });

        scene.setOnMouseClicked(event -> gameCanvas.requestFocus());
    }

    @Override
    public void stop() {
        if (gameCanvas != null) {
            gameCanvas.stop();
        }
    }

    /**
     * Helper that starts the JavaFX runtime from the legacy entry point.
     *
     * @param args command line arguments passed to {@link Application#launch(String...)}
     */
    public static void launchClient(String... args) {
        Application.launch(PolanieClientFX.class, args);
    }
}
