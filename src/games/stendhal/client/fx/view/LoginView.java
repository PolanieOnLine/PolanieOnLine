package games.stendhal.client.fx.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Login screen for the JavaFX client shell.
 */
public class LoginView extends BorderPane {

        private final TextField hostField = new TextField("localhost");
        private final TextField portField = new TextField("32160");
        private final TextField usernameField = new TextField();
        private final PasswordField passwordField = new PasswordField();
        private final Button loginButton = new Button("Połącz");
        private final Button offlineButton = new Button("Tryb offline");
        private final Label feedbackLabel = new Label();

        private LoginHandler loginHandler;
        private Runnable offlineHandler;

        public LoginView() {
                initialize();
        }

        private void initialize() {
                getStyleClass().add("polanie-login-view");
                setPadding(new Insets(40));
                setStyle("-fx-background-color: linear-gradient(#080a0f, #111827);");

                Label title = new Label("PolanieOnLine");
                title.setFont(Font.font("SansSerif", FontWeight.BOLD, 36));
                title.setTextFill(Color.web("#f3f4f6"));

                Label subtitle = new Label("Nowy klient JavaFX");
                subtitle.setTextFill(Color.web("#9ca3af"));

                VBox header = new VBox(4, title, subtitle);
                header.setAlignment(Pos.CENTER);

                setTop(header);
                BorderPane.setAlignment(header, Pos.CENTER);

                GridPane form = new GridPane();
                form.setHgap(12);
                form.setVgap(12);
                form.setPadding(new Insets(30));
                form.setStyle("-fx-background-color: rgba(17,24,39,0.65); -fx-background-radius: 12;" +
                                "-fx-border-color: rgba(148,163,184,0.2); -fx-border-radius: 12; -fx-border-width: 1;");

                configureField(hostField, "Serwer");
                configureField(portField, "Port");
                configureField(usernameField, "Użytkownik");
                configurePassword(passwordField, "Hasło");

                form.add(new Label("Serwer"), 0, 0);
                form.add(hostField, 1, 0);
                form.add(new Label("Port"), 0, 1);
                form.add(portField, 1, 1);
                form.add(new Label("Użytkownik"), 0, 2);
                form.add(usernameField, 1, 2);
                form.add(new Label("Hasło"), 0, 3);
                form.add(passwordField, 1, 3);

                loginButton.getStyleClass().add("login-primary-button");
                offlineButton.getStyleClass().add("login-secondary-button");

                loginButton.setOnAction(event -> triggerLogin());
                offlineButton.setOnAction(event -> triggerOffline());

                passwordField.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                                triggerLogin();
                                event.consume();
                        }
                });

                usernameField.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                                triggerLogin();
                                event.consume();
                        }
                });

                HBox buttons = new HBox(12, loginButton, offlineButton);
                buttons.setAlignment(Pos.CENTER_RIGHT);

                form.add(buttons, 1, 4);

                feedbackLabel.setTextFill(Color.web("#f87171"));

                VBox container = new VBox(20, form, feedbackLabel);
                container.setAlignment(Pos.TOP_CENTER);

                setCenter(container);

                BorderPane.setAlignment(container, Pos.CENTER);
        }

        private static void configureField(TextField field, String prompt) {
                field.setPromptText(prompt);
                field.setStyle("-fx-background-color: rgba(30,41,59,0.7); -fx-text-fill: #f9fafb;"
                                + "-fx-prompt-text-fill: #9ca3af; -fx-background-radius: 8; -fx-border-radius: 8;"
                                + "-fx-border-color: rgba(148,163,184,0.2);");
        }

        private static void configurePassword(PasswordField field, String prompt) {
                field.setPromptText(prompt);
                field.setStyle("-fx-background-color: rgba(30,41,59,0.7); -fx-text-fill: #f9fafb;"
                                + "-fx-prompt-text-fill: #9ca3af; -fx-background-radius: 8; -fx-border-radius: 8;"
                                + "-fx-border-color: rgba(148,163,184,0.2);");
        }

        private void triggerLogin() {
                feedbackLabel.setText("");
                if (loginHandler == null) {
                        return;
                }
                String host = hostField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                int port;
                try {
                        port = Integer.parseInt(portField.getText().trim());
                } catch (NumberFormatException ex) {
                        feedbackLabel.setText("Nieprawidłowy numer portu");
                        return;
                }
                loginHandler.onLogin(host, port, username, password);
        }

        private void triggerOffline() {
                if (offlineHandler != null) {
                        offlineHandler.run();
                }
        }

        public void setOnLogin(LoginHandler handler) {
                this.loginHandler = handler;
        }

        public void setOnOffline(Runnable handler) {
                this.offlineHandler = handler;
        }

        public void setBusy(boolean busy) {
                loginButton.setDisable(busy);
                offlineButton.setDisable(busy);
                hostField.setDisable(busy);
                portField.setDisable(busy);
                usernameField.setDisable(busy);
                passwordField.setDisable(busy);
        }

        public void showError(String message) {
                feedbackLabel.setText(message == null ? "" : message);
        }

        public void focusUsername() {
                usernameField.requestFocus();
        }

        @FunctionalInterface
        public interface LoginHandler {
                void onLogin(String host, int port, String username, String password);
        }
}
