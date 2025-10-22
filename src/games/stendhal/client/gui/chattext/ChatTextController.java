/***************************************************************************
*                   (C) Copyright 2003-2015 - Stendhal                    *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui.chattext;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.stendhal;
import games.stendhal.common.constants.SoundLayer;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class ChatTextController {
    /** Maximum text length. Public chat is limited to 1000 server side. */
    private static final int MAX_TEXT_LENGTH = 1000;

    private static final Logger LOGGER = Logger.getLogger(ChatTextController.class);

    private final FXChatInputPane playerChatText;

    private ChatCache cache;
    private static ChatTextController instance;

    /**
     * Retrieves singleton instance.
     *
     * @return `ChatTextController` instance.
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
     * @return `true` if focus change is likely to succeed.
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
                event.consume();
                playerChatText.insertLineBreak();
                return;
            }
            if (event.isControlDown() || event.isAltDown() || event.isMetaDown()) {
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
            playerChatText.clear();
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
        final String content = playerChatText.getText();
        return (content != null) ? content : "";
    }

    private void setCache(final ChatCache cache) {
        this.cache = cache;
    }

    private void clearLine(final String originalText) {
        cache.addlinetoCache(originalText);
        playerChatText.clear();
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
        private static final String TEXT_FONT_STACK = "'Arial','Segoe UI','Segoe UI Emoji','Noto Color Emoji','Twemoji Mozilla','Apple Color Emoji','Liberation Sans','DejaVu Sans','Noto Sans','sans-serif'";
        private static final String[] BUNDLED_FX_FONT_RESOURCES = new String[] {
                "data/font/Carlito-Regular.ttf",
                "data/font/Carlito-Bold.ttf",
                "data/font/Carlito-Italic.ttf",
                "data/font/Carlito-BoldItalic.ttf",
                "data/font/Amaranth-Regular.ttf",
                "data/font/Amaranth-Bold.ttf",
                "data/font/Amaranth-Italic.ttf",
                "data/font/Amaranth-BoldItalic.ttf",
                "data/font/KonstytucjaPolska.ttf",
                "data/font/AntykwaTorunska.ttf",
                "data/font/NotoEmoji-Regular.ttf"
        };

        private static boolean fxFontsLoaded;

        private final int maxTextLength;
        private final Runnable lengthLimitHandler;
        private final List<KeyListener> listeners = new CopyOnWriteArrayList<>();
        private final CountDownLatch ready = new CountDownLatch(1);

        private volatile boolean documentReady;
        private volatile String pendingText = "";

        private WebView webView;
        private WebEngine engine;
        private Consumer<javafx.scene.input.KeyEvent> keyPressedHandler;

        FXChatInputPane(final int maxTextLength, final Runnable lengthLimitHandler) {
            this.maxTextLength = maxTextLength;
            this.lengthLimitHandler = lengthLimitHandler;
            setFocusable(true);
            setRequestFocusEnabled(true);
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            Platform.setImplicitExit(false);
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
            ensureFxFontsLoaded();
            webView = new WebView();
            webView.setContextMenuEnabled(false);
            webView.setFocusTraversable(true);
            webView.setStyle("-fx-background-color: transparent;");

            engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    if (documentReady) {
                        return;
                    }
                    documentReady = true;
                    injectJavaBridge();
                    if (pendingText != null) {
                        setTextInternal(pendingText);
                        pendingText = "";
                    }
                    enforceMaxLengthInternal();
                    moveCaretToEndInternal();
                    ready.countDown();
                }
            });

            webView.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, this::handleKeyPressed);
            webView.addEventFilter(javafx.scene.input.KeyEvent.KEY_RELEASED, this::handleKeyReleased);
            webView.addEventFilter(javafx.scene.input.KeyEvent.KEY_TYPED, this::handleKeyTyped);

            final BorderPane pane = new BorderPane(webView);
            pane.setStyle("-fx-background-color: transparent;");
            final Scene scene = new Scene(pane);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            setScene(scene);

            final String html = buildInputDocument();
            engine.loadContent(html, "text/html; charset=UTF-8");
        }

        private void injectJavaBridge() {
            try {
                final JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("javaBridge", new ChatInputBridge(lengthLimitHandler));
            } catch (final RuntimeException ex) {
                LOGGER.warn("Failed to inject chat input bridge", ex);
            }
        }

        private String buildInputDocument() {
            final int fontSize = resolveFontSize();
            final String emojiFontFace = buildEmojiFontFace();
            final String fontStack = buildFontStack();

            final StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>");
            html.append("<style>");
            html.append("html,body{margin:0;padding:0;background:transparent;overflow:hidden;}");
            if (!emojiFontFace.isEmpty()) {
                html.append(emojiFontFace);
            }
            html.append(".chat-input{min-height:48px;padding:6px 8px;box-sizing:border-box;border-radius:4px;outline:none;border:none;")
                    .append("font-family:").append(fontStack).append(";")
                    .append("font-size:").append(fontSize).append("px;")
                    .append("line-height:1.35;white-space:pre-wrap;word-break:break-word;")
                    .append("background:rgba(244,237,217,0.95);color:#2c1503;}");
            html.append(".chat-input:empty:before{content:attr(data-placeholder);pointer-events:none;color:rgba(44,21,3,0.55);}");
            html.append("::-webkit-scrollbar{width:12px;height:12px;}");
            html.append("::-webkit-scrollbar-thumb{background:rgba(0,0,0,0.25);border-radius:6px;}");
            html.append("::-webkit-scrollbar-track{background:rgba(0,0,0,0.1);}");
            html.append("</style></head><body>");
            html.append("<div id=\"editor\" class=\"chat-input\" contenteditable=\"true\" spellcheck=\"false\" data-placeholder=\"Wpisz wiadomość&#8230;\"></div>");
            html.append("<script>(function(){");
            html.append("var editor=document.getElementById('editor');");
            html.append("if(!editor){return;}");
            html.append("var maxLen=").append(maxTextLength).append(";");
            html.append("function getText(){var text=editor.innerText||'';text=text.replace(/\\u00a0/g,' ').replace(/\\r/g,'').replace(/\\u2028|\\u2029/g,'');if(text.endsWith('\n')){text=text.replace(/\n+$/,'');}return text;}");
            html.append("function escapeHtml(value){return value.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/'/g,'&#39;');}");
            html.append("function setText(value){if(value==null){value='';}var html=escapeHtml(value).replace(/\n/g,'<br>');editor.innerHTML=html;placeCaretAtEnd();}");
            html.append("function placeCaretAtEnd(){if(!editor){return;}editor.focus();var range=document.createRange();range.selectNodeContents(editor);range.collapse(false);var sel=window.getSelection();if(sel){sel.removeAllRanges();sel.addRange(range);}}");
            html.append("function notifyLimit(){if(window.javaBridge&&typeof window.javaBridge.onLengthLimitReached==='function'){window.javaBridge.onLengthLimitReached();}}");
            html.append("function clamp(fromEnforce){var text=getText();if(text.length>maxLen){var truncated=text.substring(0,maxLen);if(truncated!==text){setText(truncated);}if(!fromEnforce){notifyLimit();}return true;}return false;}");
            html.append("editor.addEventListener('input',function(){clamp(false);});");
            html.append("editor.addEventListener('keydown',function(ev){if(ev.key==='Tab'){ev.preventDefault();}});");
            html.append("editor.addEventListener('paste',function(ev){if(ev.clipboardData){ev.preventDefault();var data=ev.clipboardData.getData('text/plain');document.execCommand('insertText',false,data);}else if(window.clipboardData){ev.preventDefault();var legacy=window.clipboardData.getData('Text');document.execCommand('insertText',false,legacy);}});");
            html.append("window.chatInput={getText:getText,setText:setText,focus:function(){setTimeout(placeCaretAtEnd,0);},moveCaretToEnd:function(){setTimeout(placeCaretAtEnd,0);},insertLineBreak:function(){document.execCommand('insertLineBreak');},clear:function(){setText('');},enforceLimit:function(){clamp(true);}};");
            html.append("setTimeout(function(){editor.focus();},0);");
            html.append("})();</script></body></html>");
            return html.toString();
        }

        private String buildEmojiFontFace() {
            final String bundled = EmojiStore.get().getBundledFontDataUrl();
            if ((bundled == null) || bundled.isEmpty()) {
                return "";
            }
            return "@font-face{font-family:'BundledEmoji';src:url(" + bundled + ") format('truetype');font-display:swap;}";
        }

        private String buildFontStack() {
            final String bundled = EmojiStore.get().getBundledFontDataUrl();
            if ((bundled != null) && !bundled.isEmpty()) {
                return "'Arial','BundledEmoji','Segoe UI Emoji','Noto Color Emoji','Twemoji Mozilla','Apple Color Emoji','Segoe UI','Segoe UI Symbol','Liberation Sans','DejaVu Sans','Noto Sans','sans-serif'";
            }
            return TEXT_FONT_STACK;
        }

        private static synchronized void ensureFxFontsLoaded() {
            if (fxFontsLoaded) {
                return;
            }
            fxFontsLoaded = true;
            for (final String resource : BUNDLED_FX_FONT_RESOURCES) {
                if ((resource == null) || resource.isEmpty()) {
                    continue;
                }
                try (InputStream stream = DataLoader.getResourceAsStream(resource)) {
                    if (stream == null) {
                        continue;
                    }
                    final javafx.scene.text.Font font = javafx.scene.text.Font.loadFont(stream, 12);
                    if (font == null) {
                        LOGGER.warn("Unable to register chat font: " + resource);
                    }
                } catch (final IOException ex) {
                    LOGGER.warn("Failed to load chat font: " + resource, ex);
                }
            }
        }

        private static int resolveFontSize() {
            final Font textFieldFont = UIManager.getFont("TextField.font");
            if ((textFieldFont != null) && (textFieldFont.getSize() > 0)) {
                return Math.max(12, textFieldFont.getSize());
            }
            final Font textAreaFont = UIManager.getFont("TextArea.font");
            if ((textAreaFont != null) && (textAreaFont.getSize() > 0)) {
                return Math.max(12, textAreaFont.getSize());
            }
            final Font labelFont = UIManager.getFont("Label.font");
            if ((labelFont != null) && (labelFont.getSize() > 0)) {
                return Math.max(12, labelFont.getSize());
            }
            return 13;
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
                if (webView != null) {
                    webView.requestFocus();
                    if (documentReady) {
                        engine.executeScript("if(window.chatInput){window.chatInput.focus();}");
                    }
                }
            });
        }

        String getText() {
            return callFx(() -> {
                if (!documentReady || (engine == null)) {
                    return pendingText != null ? pendingText : "";
                }
                final Object result = engine.executeScript("window.chatInput ? window.chatInput.getText() : ''");
                return (result != null) ? result.toString() : "";
            });
        }

        void setText(final String text) {
            runFx(() -> {
                if (!documentReady || (engine == null)) {
                    pendingText = (text != null) ? text : "";
                    return;
                }
                setTextInternal(text);
                enforceMaxLengthInternal();
                moveCaretToEndInternal();
            });
        }

        void clear() {
            setText("");
        }

        void moveCaretToEnd() {
            runFx(this::moveCaretToEndInternal);
        }

        void insertLineBreak() {
            runFx(() -> {
                if (documentReady && (engine != null)) {
                    engine.executeScript("if(window.chatInput){window.chatInput.insertLineBreak();window.chatInput.enforceLimit();}");
                }
            });
        }

        void enforceMaxLength() {
            runFx(this::enforceMaxLengthInternal);
        }

        private void moveCaretToEndInternal() {
            if (documentReady && (engine != null)) {
                engine.executeScript("if(window.chatInput){window.chatInput.moveCaretToEnd();}");
            }
        }

        private void setTextInternal(final String text) {
            if (engine != null) {
                final String value = (text != null) ? text : "";
                engine.executeScript("if(window.chatInput){window.chatInput.setText(" + toJsString(value) + ");}");
            }
        }

        private void enforceMaxLengthInternal() {
            if (documentReady && (engine != null)) {
                engine.executeScript("if(window.chatInput){window.chatInput.enforceLimit();}");
            }
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

        private String toJsString(final String value) {
            final StringBuilder out = new StringBuilder();
            out.append('\'');
            if (value != null) {
                for (int i = 0; i < value.length(); i++) {
                    final char ch = value.charAt(i);
                    switch (ch) {
                    case '\\':
                        out.append("\\\\");
                        break;
                    case '\'':
                        out.append("\\'");
                        break;
                    case '"':
                        out.append("\\\"");
                        break;
                    case '\n':
                        out.append("\\n");
                        break;
                    case '\r':
                        out.append("\\r");
                        break;
                    case '\t':
                        out.append("\\t");
                        break;
                    default:
                        out.append(ch);
                        break;
                    }
                }
            }
            out.append('\'');
            return out.toString();
        }
    }

    private static final class ChatInputBridge {
        private final Runnable lengthLimitHandler;

        ChatInputBridge(final Runnable lengthLimitHandler) {
            this.lengthLimitHandler = lengthLimitHandler;
        }

        public void onLengthLimitReached() {
            if (lengthLimitHandler == null) {
                return;
            }
            SwingUtilities.invokeLater(() -> {
                try {
                    lengthLimitHandler.run();
                } catch (final RuntimeException ex) {
                    LOGGER.warn("Length limit handler failed", ex);
                }
            });
        }
    }
}
