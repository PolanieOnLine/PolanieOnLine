package games.stendhal.client.fx.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Simple HUD status bar for the FX client.
 */
public class StatusBar extends HBox {

        private final Label locationLabel = new Label("Brak strefy");
        private final Label statusLabel = new Label("Oczekiwanie na połączenie");

        public StatusBar() {
                setSpacing(20);
                setPadding(new Insets(10, 18, 10, 18));
                setAlignment(Pos.CENTER_LEFT);
                setStyle("-fx-background-color: rgba(17,24,39,0.85);");

                locationLabel.setTextFill(Color.web("#facc15"));
                locationLabel.setFont(Font.font("SansSerif", 16));

                statusLabel.setTextFill(Color.web("#f9fafb"));

                getChildren().addAll(locationLabel, statusLabel);
        }

        public void setLocation(String location) {
                locationLabel.setText(location == null ? "Brak strefy" : location);
        }

        public void setStatus(String status) {
                statusLabel.setText(status == null ? "" : status);
        }
}
