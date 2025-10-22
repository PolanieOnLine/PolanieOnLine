package games.stendhal.client.fx.view;

import java.util.Collection;

import games.stendhal.client.fx.ChatView;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Primary in-game layout that hosts the map, chat and side panels.
 */
public class GameRootView extends BorderPane {

        private final StatusBar statusBar = new StatusBar();
        private final GameCanvas gameCanvas = new GameCanvas();
        private final PlayerListView playerList = new PlayerListView();
        private final ChatView chatView = new ChatView();
        private final VBox inventoryPane = new VBox();

        public GameRootView() {
                initialize();
        }

        private void initialize() {
                setStyle("-fx-background-color: linear-gradient(#020617, #0f172a);");

                statusBar.setStatus("Łączenie...");
                setTop(statusBar);

                inventoryPane.setPadding(new Insets(16));
                inventoryPane.setSpacing(12);
                inventoryPane.setStyle("-fx-background-color: rgba(8,11,19,0.7);");
                Label inventoryHeader = new Label("Ekwipunek");
                inventoryHeader.setTextFill(Color.web("#f3f4f6"));
                inventoryPane.getChildren().addAll(inventoryHeader,
                                createPlaceholder("Panel ekwipunku w trakcie migracji"));

                StackPane canvasWrapper = new StackPane(gameCanvas);
                canvasWrapper.setPadding(new Insets(10));
                gameCanvas.widthProperty().bind(canvasWrapper.widthProperty().subtract(20));
                gameCanvas.heightProperty().bind(canvasWrapper.heightProperty().subtract(20));

                BorderPane centerPane = new BorderPane();
                centerPane.setCenter(canvasWrapper);
                centerPane.setLeft(inventoryPane);
                centerPane.setRight(playerList);
                BorderPane.setMargin(inventoryPane, new Insets(10));
                BorderPane.setMargin(playerList, new Insets(10));

                chatView.setStyle("-fx-border-color: rgba(59,130,246,0.25); -fx-border-width: 1 0 0 0;");

                setCenter(centerPane);
                setBottom(chatView);
        }

        public void startSession(String zoneName) {
                statusBar.setLocation(zoneName);
                statusBar.setStatus("Witamy w świecie PolanieOnLine");
                gameCanvas.start();
        }

        public void stopSession() {
                gameCanvas.stop();
        }

        public void setPlayers(Collection<String> players) {
                playerList.setPlayers(players);
        }

        public void postSystemMessage(String message) {
                chatView.addMessage("System", message, true);
        }

        public void bindChat(java.util.function.Consumer<String> messageConsumer) {
                chatView.setMessageHandler(text -> {
                        if (messageConsumer != null) {
                                messageConsumer.accept(text);
                        }
                });
        }

        public void addChatMessage(String sender, String text, boolean system) {
                chatView.addMessage(sender, text, system);
        }

        private static Label createPlaceholder(String message) {
                Label label = new Label(message);
                label.setWrapText(true);
                label.setTextFill(Color.web("#cbd5f5"));
                label.setStyle("-fx-background-color: rgba(30,41,59,0.4); -fx-padding: 12; -fx-background-radius: 8;");
                return label;
        }

        public ChatView getChatView() {
                return chatView;
        }
}
