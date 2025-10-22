package games.stendhal.client.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import games.stendhal.client.fx.EmojiManager;
import games.stendhal.client.fx.model.CharacterSlot;
import games.stendhal.client.fx.model.ChatMessage;
import games.stendhal.client.fx.view.CharacterSelectionView;
import games.stendhal.client.fx.view.GameRootView;
import games.stendhal.client.fx.view.LoginView;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller that coordinates the new JavaFX client shell.
 */
public class FxClientController {

        private final Stage stage;
        private final FxClientBackend backend;
        private final StackPane root = new StackPane();
        private final Scene scene;

        private LoginView loginView;
        private CharacterSelectionView characterView;
        private GameRootView gameView;

        public FxClientController(Stage stage) {
                this(stage, new DummyClientBackend());
        }

        public FxClientController(Stage stage, FxClientBackend backend) {
                this.stage = Objects.requireNonNull(stage, "stage");
                this.backend = Objects.requireNonNull(backend, "backend");
                this.scene = new Scene(root, 1180, 760);
                stage.setScene(scene);
                stage.setTitle("PolanieOnLine – klient JavaFX");
        }

        public void start() {
                showLogin();
                stage.show();
        }

        public void stop() {
                if (gameView != null) {
                        gameView.stopSession();
                }
                backend.shutdown();
        }

        private void showLogin() {
                if (loginView == null) {
                        loginView = new LoginView();
                        loginView.setOnLogin(this::handleLoginAttempt);
                        loginView.setOnOffline(() -> stage.close());
                        loginView.focusUsername();
                }
                setView(loginView);
        }

        private void showCharacters(List<CharacterSlot> slots) {
                if (characterView == null) {
                        characterView = new CharacterSelectionView();
                        characterView.setOnJoin(this::handleCharacterJoin);
                        characterView.setOnBack(() -> {
                                showLogin();
                                if (loginView != null) {
                                        loginView.setBusy(false);
                                        loginView.showError("");
                                        loginView.focusUsername();
                                }
                        });
                        characterView.setOnCreate(() -> characterView.showInfo("Tworzenie postaci będzie dostępne w następnej iteracji."));
                }
                characterView.setCharacters(slots);
                characterView.setBusy(false);
                characterView.showInfo("Wybierz bohatera i dołącz do świata");
                setView(characterView);
        }

        private void showGame(CharacterSlot slot) {
                if (gameView == null) {
                        gameView = new GameRootView();
                        gameView.bindChat(message -> backend.sendChatMessage(message));
                }
                setView(gameView);
                gameView.startSession(slot.getName());
                gameView.setPlayers(Arrays.asList(slot.getName(), "Przyjaciel", "Kupiec"));
                backend.addChatListener(this::handleIncomingChat);
                gameView.postSystemMessage("Połączono z sesją demonstracyjną. Emoji działają: "
                                + EmojiManager.replaceShortcodes(":smile: :fire: :heart:"));
        }

        private void setView(javafx.scene.Node node) {
                Platform.runLater(() -> {
                        root.getChildren().setAll(node);
                });
        }

        private void handleLoginAttempt(String host, int port, String username, String password) {
                loginView.setBusy(true);
                CompletableFuture<FxClientBackend.LoginResult> future = backend.login(host, port, username, password);
                future.thenCompose(result -> {
                        if (!result.isSuccess()) {
                                Platform.runLater(() -> {
                                        loginView.setBusy(false);
                                        loginView.showError(result.getMessage());
                                });
                                return CompletableFuture.completedFuture(null);
                        }
                        return backend.loadCharacters();
                }).thenAccept(characters -> {
                        if (characters != null) {
                                Platform.runLater(() -> showCharacters(characters));
                        }
                }).exceptionally(ex -> {
                        Platform.runLater(() -> {
                                loginView.setBusy(false);
                                loginView.showError("Błąd logowania: " + ex.getMessage());
                        });
                        return null;
                });
        }

        private void handleCharacterJoin(CharacterSlot slot) {
                characterView.setBusy(true);
                backend.selectCharacter(slot.getName()).thenRun(() -> {
                        Platform.runLater(() -> {
                                characterView.setBusy(false);
                                showGame(slot);
                        });
                }).exceptionally(ex -> {
                        Platform.runLater(() -> {
                                characterView.setBusy(false);
                                characterView.showInfo("Nie udało się wybrać postaci: " + ex.getMessage());
                        });
                        return null;
                });
        }

        private void handleIncomingChat(ChatMessage message) {
                Platform.runLater(() -> {
                        if (gameView != null) {
                                gameView.addChatMessage(message.getSender(), message.getMessage(), message.isSystem());
                        }
                });
        }
}
