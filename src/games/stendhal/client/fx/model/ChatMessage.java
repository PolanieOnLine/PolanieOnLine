package games.stendhal.client.fx.model;

import java.util.Objects;

/**
 * Simple chat message container used by the JavaFX shell.
 */
public final class ChatMessage {
        private final String sender;
        private final String message;
        private final boolean system;

        public ChatMessage(String sender, String message, boolean system) {
                this.sender = (sender == null) ? "" : sender;
                this.message = Objects.requireNonNull(message, "message");
                this.system = system;
        }

        public String getSender() {
                return sender;
        }

        public String getMessage() {
                return message;
        }

        public boolean isSystem() {
                return system;
        }
}
