package games.stendhal.client.fx;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import games.stendhal.client.fx.model.CharacterSlot;
import games.stendhal.client.fx.model.ChatMessage;

/**
 * Backend abstraction for the JavaFX client shell.
 */
public interface FxClientBackend {

        /**
         * Attempt to authenticate against a server.
         *
         * @param host server hostname
         * @param port server port
         * @param username login name
         * @param password login password
         * @return future resolving to the login outcome
         */
        CompletableFuture<LoginResult> login(String host, int port, String username, String password);

        /**
         * Fetch available characters for the authenticated account.
         *
         * @return future resolving to available characters
         */
        CompletableFuture<List<CharacterSlot>> loadCharacters();

        /**
         * Select an active character to enter the game world.
         *
         * @param characterName name of the character to join with
         * @return future completing when the selection succeeds
         */
        CompletableFuture<Void> selectCharacter(String characterName);

        /**
         * Submit a chat message to the backend.
         *
         * @param message message text
         */
        void sendChatMessage(String message);

        /**
         * Register a listener for chat messages produced by the backend.
         *
         * @param listener consumer invoked for each incoming message
         */
        void addChatListener(Consumer<ChatMessage> listener);

        /**
         * Shutdown and release backend resources.
         */
        void shutdown();

        /**
         * Result of a login attempt.
         */
        final class LoginResult {
                private final boolean success;
                private final String message;

                public LoginResult(boolean success, String message) {
                        this.success = success;
                        this.message = message;
                }

                public boolean isSuccess() {
                        return success;
                }

                public String getMessage() {
                        return message;
                }
        }
}
