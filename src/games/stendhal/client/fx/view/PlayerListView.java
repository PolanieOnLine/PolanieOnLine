package games.stendhal.client.fx.view;

import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Display of nearby players in the JavaFX shell.
 */
public class PlayerListView extends BorderPane {

        private final ListView<String> listView = new ListView<String>();

        public PlayerListView() {
                setPadding(new Insets(12));
                setStyle("-fx-background-color: rgba(15,23,42,0.75);");

                Label title = new Label("Gracze");
                title.setFont(Font.font("SansSerif", 18));
                title.setTextFill(Color.web("#f3f4f6"));
                setTop(title);

                listView.setStyle("-fx-control-inner-background: rgba(15,23,42,0.75);"
                                + "-fx-text-fill: #e5e7eb; -fx-border-color: rgba(148,163,184,0.2);"
                                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-background-insets: 0;"
                                + "-fx-padding: 6;");
                setCenter(listView);
        }

        public void setPlayers(Collection<String> players) {
                listView.setItems(FXCollections.observableArrayList(players));
        }
}
