package games.stendhal.client.fx;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Chat component backed by a WebView instance.
 */
public class ChatView extends BorderPane {

        private static final String CHAT_TEMPLATE =
                        "<html>\n" +
                        "<head>\n" +
                        "  <style>\n" +
                        "    body { background:#111; color:#eee; font-family:sans-serif; margin:5px; }\n" +
                        "    .msg { margin:2px 0; }\n" +
                        "    .player { color:#ffcc33; }\n" +
                        "    .system { color:#66aaff; }\n" +
                        "    .emoji { width:16px; height:16px; display:inline-block; vertical-align:middle; }\n" +
                        "  </style>\n" +
                        "  <script>\n" +
                        "    function addMessage(msg) {\n" +
                        "      var div = document.createElement('div');\n" +
                        "      div.className = 'msg';\n" +
                        "      div.innerHTML = msg;\n" +
                        "      document.body.appendChild(div);\n" +
                        "      window.scrollTo(0, document.body.scrollHeight);\n" +
                        "    }\n" +
                        "  </script>\n" +
                        "</head>\n" +
                        "<body></body>\n" +
                        "</html>";

        private final WebView webView;
        private final WebEngine webEngine;
        private final TextField inputField;
        private final List<String> pendingMessages = new ArrayList<String>();
        private boolean documentReady;
        private MessageHandler messageHandler;

        public ChatView() {
                webView = new WebView();
                webView.setContextMenuEnabled(false);
                webEngine = webView.getEngine();
                webEngine.loadContent(CHAT_TEMPLATE);
                webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                                        Worker.State newValue) {
                                if (newValue == Worker.State.SUCCEEDED) {
                                        documentReady = true;
                                        flushPendingMessages();
                                }
                        }
                });

                inputField = new TextField();
                inputField.setPromptText("Napisz wiadomość...");
                inputField.setOnAction(event -> handleLocalInput());
                inputField.setStyle("-fx-background-color: #222; -fx-text-fill: #eee; -fx-prompt-text-fill: #888;");

                setCenter(webView);
                setBottom(inputField);
                setStyle("-fx-background-color: #111;");
        }

        private void handleLocalInput() {
                String text = inputField.getText();
                if ((text == null) || text.trim().isEmpty()) {
                        inputField.clear();
                        return;
                }
                String normalized = text.trim();
                addMessage("Ty", normalized, false);
                if (messageHandler != null) {
                        messageHandler.onMessage(normalized);
                }
                inputField.clear();
        }

        /**
         * Append a message to the chat view.
         *
         * @param sender message author
         * @param message text content
         * @param system true if the message comes from the system
         */
        public void addMessage(String sender, String message, boolean system) {
                String effectiveSender = (sender == null) ? "" : sender;
                String escapedSender = escapeHtml(effectiveSender);
                String textWithEmoji = EmojiManager.replaceShortcodes(message == null ? "" : message);
                String escapedMessage = escapeHtml(textWithEmoji);
                String cssClass = system ? "system" : "player";
                String html = "<span class='" + cssClass + "'>" + escapedSender + ":</span> " + escapedMessage;
                enqueueMessage(html);
        }

        private void enqueueMessage(final String html) {
                synchronized (pendingMessages) {
                        if (!documentReady) {
                                pendingMessages.add(html);
                                return;
                        }
                }
                runOnFxThread(new Runnable() {
                        @Override
                        public void run() {
                                webEngine.executeScript("addMessage(" + toJavaScriptString(html) + ");");
                        }
                });
        }

        private void flushPendingMessages() {
                List<String> messages;
                synchronized (pendingMessages) {
                        if (pendingMessages.isEmpty()) {
                                return;
                        }
                        messages = new ArrayList<String>(pendingMessages);
                        pendingMessages.clear();
                }
                for (final String message : messages) {
                        runOnFxThread(new Runnable() {
                                @Override
                                public void run() {
                                        webEngine.executeScript("addMessage(" + toJavaScriptString(message) + ");");
                                }
                        });
                }
        }

        private void runOnFxThread(Runnable action) {
                if (Platform.isFxApplicationThread()) {
                        action.run();
                } else {
                        Platform.runLater(action);
                }
        }

        private static String escapeHtml(String value) {
                if (value == null) {
                        return "";
                }
                StringBuilder builder = new StringBuilder(value.length());
                for (int i = 0; i < value.length(); i++) {
                        char ch = value.charAt(i);
                        switch (ch) {
                        case '&':
                                builder.append("&amp;");
                                break;
                        case '<':
                                builder.append("&lt;");
                                break;
                        case '>':
                                builder.append("&gt;");
                                break;
                        case '"':
                                builder.append("&quot;");
                                break;
                        case '\'':
                                builder.append("&#39;");
                                break;
                        default:
                                builder.append(ch);
                                break;
                        }
                }
                return builder.toString();
        }

        private static String toJavaScriptString(String value) {
                StringBuilder builder = new StringBuilder();
                builder.append('"');
                for (int i = 0; i < value.length(); i++) {
                        char ch = value.charAt(i);
                        switch (ch) {
                        case '\\':
                                builder.append("\\\\");
                                break;
                        case '\"':
                                builder.append("\\\"");
                                break;
                        case '\'':
                                builder.append("\\'");
                                break;
                        case '\n':
                                builder.append("\\n");
                                break;
                        case '\r':
                                break;
                        default:
                                builder.append(ch);
                                break;
                        }
                }
                builder.append('"');
                return builder.toString();
        }

        /**
         * Register a handler to receive local chat submissions.
         *
         * @param handler callback invoked after the message was enqueued locally
         */
        public void setMessageHandler(MessageHandler handler) {
                this.messageHandler = handler;
        }

        /**
         * Listener for chat submissions originating from the local player.
         */
        public interface MessageHandler {
                void onMessage(String message);
        }
}
