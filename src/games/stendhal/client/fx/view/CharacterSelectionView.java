package games.stendhal.client.fx.view;

import java.util.List;
import java.util.function.Consumer;

import games.stendhal.client.fx.model.CharacterSlot;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Character selection panel for the JavaFX client shell.
 */
public class CharacterSelectionView extends BorderPane {

        private final ListView<CharacterSlot> listView = new ListView<CharacterSlot>();
        private final Button playButton = new Button("Wejdź do gry");
        private final Button backButton = new Button("Wyloguj");
        private final Button createButton = new Button("Nowa postać");
        private final Label infoLabel = new Label();

        private Consumer<CharacterSlot> onJoin;
        private Runnable onBack;
        private Runnable onCreate;

        public CharacterSelectionView() {
                initialize();
        }

        private void initialize() {
                setPadding(new Insets(32));
                setStyle("-fx-background-color: linear-gradient(#111827, #0b1120);");

                Label title = new Label("Wybierz postać");
                title.setFont(Font.font("SansSerif", FontWeight.BOLD, 30));
                title.setTextFill(Color.web("#f3f4f6"));

                Label subtitle = new Label("Twoi bohaterowie czekają na przygodę");
                subtitle.setTextFill(Color.web("#9ca3af"));

                VBox header = new VBox(6, title, subtitle);
                header.setAlignment(Pos.CENTER_LEFT);
                setTop(header);

                listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                listView.setCellFactory(list -> new CharacterCell());
                listView.setPrefWidth(420);
                listView.setStyle("-fx-background-color: rgba(15,23,42,0.7); -fx-control-inner-background: rgba(15,23,42,0.7);"
                                + "-fx-text-fill: #e5e7eb; -fx-border-color: rgba(148,163,184,0.25); -fx-border-radius: 10;"
                                + "-fx-background-insets: 0; -fx-background-radius: 10;");

                playButton.getStyleClass().add("character-primary-button");
                backButton.getStyleClass().add("character-secondary-button");
                createButton.getStyleClass().add("character-secondary-button");

                playButton.setOnAction(event -> triggerJoin());
                listView.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                                triggerJoin();
                        }
                });

                backButton.setOnAction(event -> {
                        if (onBack != null) {
                                onBack.run();
                        }
                });

                createButton.setOnAction(event -> {
                        if (onCreate != null) {
                                onCreate.run();
                        }
                });

                infoLabel.setTextFill(Color.web("#facc15"));

                HBox actions = new HBox(12, playButton, createButton, backButton);
                actions.setAlignment(Pos.CENTER_RIGHT);

                VBox center = new VBox(16, listView, infoLabel, actions);
                center.setAlignment(Pos.CENTER_LEFT);
                center.setPadding(new Insets(20, 0, 0, 0));

                setCenter(center);
        }

        private void triggerJoin() {
                if (onJoin == null) {
                        return;
                }
                CharacterSlot slot = listView.getSelectionModel().getSelectedItem();
                if (slot != null) {
                        onJoin.accept(slot);
                } else {
                        showInfo("Wybierz postać, aby kontynuować");
                }
        }

        public void setCharacters(List<CharacterSlot> slots) {
                listView.setItems(FXCollections.observableArrayList(slots));
                if (!listView.getItems().isEmpty()) {
                        listView.getSelectionModel().selectFirst();
                }
        }

        public void setOnJoin(Consumer<CharacterSlot> consumer) {
                this.onJoin = consumer;
        }

        public void setOnBack(Runnable action) {
                this.onBack = action;
        }

        public void setOnCreate(Runnable action) {
                this.onCreate = action;
        }

        public void setBusy(boolean busy) {
                listView.setDisable(busy);
                playButton.setDisable(busy);
                backButton.setDisable(busy);
                createButton.setDisable(busy);
        }

        public void showInfo(String message) {
                infoLabel.setText(message == null ? "" : message);
        }

        private static class CharacterCell extends ListCell<CharacterSlot> {
                @Override
                protected void updateItem(CharacterSlot item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || (item == null)) {
                                setText(null);
                                setGraphic(null);
                        } else {
                                StringBuilder builder = new StringBuilder();
                                builder.append(item.getName());
                                builder.append("  •  poziom ");
                                builder.append(item.getLevel());
                                if (!item.getProfession().isEmpty()) {
                                        builder.append("  •  ");
                                        builder.append(item.getProfession());
                                }
                                setText(builder.toString());
                        }
                }
        }
}
