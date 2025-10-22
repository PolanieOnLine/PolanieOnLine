/***************************************************************************
*                   (C) Copyright 2003-2015 - Stendhal                    *
***************************************************************************
***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui.chattext;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.stendhal;
import games.stendhal.common.constants.SoundLayer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

public class ChatTextController {
    /** Maximum text length. Public chat is limited to 1000 server side. */
    private static final int MAX_TEXT_LENGTH = 1000;

    private final FXChatInputPane playerChatText;

    private ChatCache cache;
    private static ChatTextController instance;

    /**
     * Retrieves singleton instance.
     *
     * @return
     *   `ChatTextController` instance.
     */
    public static ChatTextController get() {
        if (ChatTextController.instance == null) {
            ChatTextController.instance = new ChatTextController();
        }
        return ChatTextController.instance;
    }

    /**
     * Private singleton constructor.
     */
    private ChatTextController() {
        playerChatText = new FXChatInputPane(MAX_TEXT_LENGTH, this::notifyLengthLimit);
        playerChatText.setOnKeyPressed(this::handleFxKeyPressed);
        playerChatText.awaitInitialization();

        StendhalClient client = StendhalClient.get();
        String logFile = null;
        if (client != null) {
            // StendhalClient is null during test runs
            logFile = stendhal.getGameFolder() + "chat/out-" + client.getCharacter() + ".log";
        }
        cache = new ChatCache(logFile);
        cache.loadChatCache();
        setCache(cache);
    }

    /**
     * Sets focus to chat input.
     *
     * @return
     *   `true` if focus change is likely to succeed.
     */
    public boolean setFocus() {
        return playerChatText.requestFocusInWindow();
    }

    public JComponent getPlayerChatText() {
        return playerChatText;
    }

    public void setChatLine(final String text) {
        playerChatText.setText(text != null ? text : "");
    }

    private void handleFxKeyPressed(final javafx.scene.input.KeyEvent event) {
        final KeyCode code = event.getCode();
        if (code == KeyCode.ENTER) {
            if (event.isShiftDown()) {
                return;
            }
            event.consume();
            submitCurrentLine();
            return;
        }

        if ((code == KeyCode.UP) && event.isShiftDown()) {
            event.consume();
            if (cache.hasPrevious()) {
                setChatLine(cache.previous());
                playerChatText.moveCaretToEnd();
            }
            return;
        }

        if ((code == KeyCode.DOWN) && event.isShiftDown()) {
            event.consume();
            if (cache.hasNext()) {
                setChatLine(cache.next());
                playerChatText.moveCaretToEnd();
            }
            return;
        }

        if (code == KeyCode.F1) {
            event.consume();
            SlashActionRepository.get("manual").execute(null, null);
            return;
        }

        if (code == KeyCode.TAB) {
            event.consume();
        }
    }

    private void submitCurrentLine() {
        final String text = getText();
        if (text.isEmpty()) {
            playerChatText.setText("");
            return;
        }
        if (ChatLineParser.parseAndHandle(text)) {
            clearLine(text);
        }
    }

    public void addKeyListener(final KeyListener l) {
        playerChatText.addAwtKeyListener(l);
    }

    public String getText() {
        return playerChatText.getText();
    }

    private void setCache(final ChatCache cache) {
        this.cache = cache;
    }

    private void clearLine(final String originalText) {
        cache.addlinetoCache(originalText);
        playerChatText.setText("");
    }

    public void saveCache() {
        cache.save();
    }

    private void notifyLengthLimit() {
        ClientSingletonRepository.getSound().getGroup(SoundLayer.USER_INTERFACE.groupName)
                .play("click-1", 0, null, null, false, true);
    }

    private static final class FXChatInputPane extends JFXPanel {
        private static final long serialVersionUID = 885350581860244944L;

        private final int maxTextLength;
        private final Runnable lengthLimitHandler;
        private final List<KeyListener> listeners = new CopyOnWriteArrayList<>();
        private final CountDownLatch ready = new CountDownLatch(1);

        private TextArea textArea;
        private Consumer<javafx.scene.input.KeyEvent> keyPressedHandler;

        FXChatInputPane(final int maxTextLength, final Runnable lengthLimitHandler) {
            this.maxTextLength = maxTextLength;
            this.lengthLimitHandler = lengthLimitHandler;
            setFocusable(true);
            setRequestFocusEnabled(true);
            Platform.runLater(this::initializeFx);
        }

        void awaitInitialization() {
            try {
                ready.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void initializeFx() {
            textArea = new TextArea();
            textArea.setWrapText(true);
            textArea.setPrefRowCount(2);
            textArea.setFocusTraversable(true);
            textArea.setTextFormatter(new TextFormatter<>(change -> {
                if ((maxTextLength > 0) && (change.getControlNewText().length() > maxTextLength)) {
                    if (lengthLimitHandler != null) {
                        lengthLimitHandler.run();
                    }
                    return null;
                }
                return change;
            }));

            textArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKeyPressed);
            textArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, this::handleKeyReleased);
            textArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_TYPED, this::handleKeyTyped);

            final BorderPane pane = new BorderPane(textArea);
            setScene(new Scene(pane));
            ready.countDown();
        }

        void setOnKeyPressed(final Consumer<javafx.scene.input.KeyEvent> handler) {
            keyPressedHandler = handler;
        }

        void addAwtKeyListener(final KeyListener listener) {
            if (listener != null) {
                listeners.add(listener);
            }
        }

        @Override
        public boolean requestFocusInWindow() {
            final boolean focused = super.requestFocusInWindow();
            requestFxFocus();
            return focused;
        }

        private void requestFxFocus() {
            runFx(() -> {
                if (textArea != null) {
                    textArea.requestFocus();
                    textArea.positionCaret(textArea.getText().length());
                }
            });
        }

        String getText() {
            return callFx(() -> textArea != null ? textArea.getText() : "");
        }

        void setText(final String text) {
            runFx(() -> {
                if (textArea != null) {
                    textArea.setText(text);
                    textArea.positionCaret(textArea.getText().length());
                }
            });
        }

        void moveCaretToEnd() {
            runFx(() -> {
                if (textArea != null) {
                    textArea.positionCaret(textArea.getText().length());
                }
            });
        }

        private void handleKeyPressed(final javafx.scene.input.KeyEvent event) {
            if (keyPressedHandler != null) {
                keyPressedHandler.accept(event);
            }
            dispatchToAwt(KeyEvent.KEY_PRESSED, event, KeyEvent.CHAR_UNDEFINED);
        }

        private void handleKeyReleased(final javafx.scene.input.KeyEvent event) {
            dispatchToAwt(KeyEvent.KEY_RELEASED, event, KeyEvent.CHAR_UNDEFINED);
        }

        private void handleKeyTyped(final javafx.scene.input.KeyEvent event) {
            final String character = event.getCharacter();
            final char keyChar = (character != null && !character.isEmpty()) ? character.charAt(0)
                    : KeyEvent.CHAR_UNDEFINED;
            dispatchToAwt(KeyEvent.KEY_TYPED, event, keyChar);
        }

        private void dispatchToAwt(final int id, final javafx.scene.input.KeyEvent fxEvent, final char keyChar) {
            if (listeners.isEmpty()) {
                return;
            }

            final int keyCode = (id == KeyEvent.KEY_TYPED) ? KeyEvent.VK_UNDEFINED : toAwtKeyCode(fxEvent);
            final int modifiers = toAwtModifiers(fxEvent);
            final KeyEvent awtEvent = new KeyEvent(
                    this,
                    id,
                    System.currentTimeMillis(),
                    modifiers,
                    keyCode,
                    keyChar);

            SwingUtilities.invokeLater(() -> {
                for (final KeyListener listener : listeners) {
                    switch (id) {
                    case KeyEvent.KEY_PRESSED:
                        listener.keyPressed(awtEvent);
                        break;
                    case KeyEvent.KEY_RELEASED:
                        listener.keyReleased(awtEvent);
                        break;
                    case KeyEvent.KEY_TYPED:
                        listener.keyTyped(awtEvent);
                        break;
                    default:
                        break;
                    }
                }
            });
        }

        private int toAwtKeyCode(final javafx.scene.input.KeyEvent fxEvent) {
            final KeyCode fxCode = fxEvent.getCode();
            if (fxCode == null) {
                return KeyEvent.VK_UNDEFINED;
            }
            final int code = fxCode.getCode();
            return (code != 0) ? code : KeyEvent.VK_UNDEFINED;
        }

        private int toAwtModifiers(final javafx.scene.input.KeyEvent fxEvent) {
            int modifiers = 0;
            if (fxEvent.isShiftDown()) {
                modifiers |= InputEvent.SHIFT_DOWN_MASK;
            }
            if (fxEvent.isControlDown()) {
                modifiers |= InputEvent.CTRL_DOWN_MASK;
            }
            if (fxEvent.isAltDown()) {
                modifiers |= InputEvent.ALT_DOWN_MASK;
            }
            if (fxEvent.isMetaDown()) {
                modifiers |= InputEvent.META_DOWN_MASK;
            }
            if (fxEvent.isShortcutDown()) {
                modifiers |= InputEvent.CTRL_DOWN_MASK;
            }
            return modifiers;
        }

        private void runFx(final Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                runnable.run();
            } else {
                Platform.runLater(runnable);
            }
        }

        private <T> T callFx(final Supplier<T> supplier) {
            if (Platform.isFxApplicationThread()) {
                return supplier.get();
            }
            final FutureTask<T> task = new FutureTask<>(supplier::get);
            Platform.runLater(task);
            try {
                return task.get();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (final ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }
}
