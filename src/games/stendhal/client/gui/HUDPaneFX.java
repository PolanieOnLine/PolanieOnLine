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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Heads-up display containing chat, status, and utility panels for the JavaFX client.
 */
public class HUDPaneFX extends BorderPane {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TextArea chatLog;
    private final TextField chatInput;
    private final ListView<String> playerList;
    private final ProgressBar healthBar;
    private final ProgressBar manaBar;
    private final Label locationLabel;

    public HUDPaneFX() {
        getStyleClass().add("hud-pane");
        setPrefWidth(320);
        setMinWidth(280);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: rgba(22,25,35,0.92); "
                + "-fx-border-color: #2f3347; "
                + "-fx-border-width: 0 0 0 2; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 16, 0.2, 0, 0);");

        VBox content = new VBox(18);
        content.setFillWidth(true);
        Region statusSection = buildStatusSection();
        Region chatSection = buildChatSection();
        Region playerSection = buildPlayerSection();
        content.getChildren().addAll(statusSection, chatSection, playerSection);
        VBox.setVgrow(chatSection, Priority.ALWAYS);
        VBox.setVgrow(playerSection, Priority.SOMETIMES);

        setCenter(content);
    }

    private Region buildStatusSection() {
        VBox statusBox = new VBox(6);
        statusBox.getStyleClass().add("hud-section");

        Label title = createSectionTitle("Status postaci");
        healthBar = new ProgressBar(0.82);
        manaBar = new ProgressBar(0.54);
        configureBar(healthBar, "hud-health-bar");
        configureBar(manaBar, "hud-mana-bar");

        Label healthLabel = new Label("Zdrowie");
        Label manaLabel = new Label("Mana");
        locationLabel = new Label("Pozycja: (128, 64)");
        locationLabel.getStyleClass().add("hud-location-label");

        statusBox.getChildren().addAll(title, healthLabel, healthBar, manaLabel, manaBar, locationLabel);
        return statusBox;
    }

    private Region buildChatSection() {
        VBox chatBox = new VBox(6);
        chatBox.getStyleClass().add("hud-section");

        Label title = createSectionTitle("Czat");
        chatLog = new TextArea();
        chatLog.setEditable(false);
        chatLog.setWrapText(true);
        chatLog.setPrefRowCount(10);
        chatLog.setFocusTraversable(false);
        chatLog.getStyleClass().add("hud-chat-log");

        chatInput = new TextField();
        chatInput.setPromptText("Wpisz wiadomość...");
        chatInput.setOnAction(event -> sendChatMessage());

        Button sendButton = new Button("Wyślij");
        sendButton.setDefaultButton(true);
        sendButton.setOnAction(event -> sendChatMessage());

        HBox inputRow = new HBox(6, chatInput, sendButton);
        HBox.setHgrow(chatInput, Priority.ALWAYS);

        chatBox.getChildren().addAll(title, chatLog, inputRow);
        VBox.setVgrow(chatLog, Priority.ALWAYS);
        return chatBox;
    }

    private Region buildPlayerSection() {
        VBox playerBox = new VBox(6);
        playerBox.getStyleClass().add("hud-section");

        Label title = createSectionTitle("Gracze w pobliżu");
        playerList = new ListView<>();
        playerList.setPrefHeight(160);
        playerList.getStyleClass().add("hud-player-list");
        playerList.getItems().addAll("Wojownik", "Mag", "Łowca");

        playerBox.getChildren().addAll(title, playerList);
        VBox.setVgrow(playerList, Priority.ALWAYS);
        return playerBox;
    }

    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("hud-section-title");
        return label;
    }

    private void configureBar(ProgressBar bar, String styleClass) {
        bar.setPrefWidth(Double.MAX_VALUE);
        bar.getStyleClass().add(styleClass);
    }

    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        appendChatMessage("Ty", message);
        chatInput.clear();
    }

    public void appendSystemMessage(String message) {
        appendChatMessage("System", message);
    }

    public void appendChatMessage(String author, String message) {
        String timestamp = LocalTime.now().withNano(0).format(TIME_FORMAT);
        chatLog.appendText(String.format("[%s] %s: %s%n", timestamp, author, message));
        chatLog.setScrollTop(Double.MAX_VALUE);
    }

    public void updateStatus(double health, double mana, String locationText) {
        healthBar.setProgress(clamp01(health));
        manaBar.setProgress(clamp01(mana));
        locationLabel.setText(locationText);
    }

    public void setPlayerList(Collection<String> players) {
        if (players == null) {
            playerList.getItems().clear();
        } else {
            playerList.getItems().setAll(players);
        }
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
