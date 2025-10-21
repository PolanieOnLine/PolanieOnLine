/***************************************************************************
*                   (C) Copyright 2003-2024 - Stendhal                    *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import games.stendhal.client.UserContext;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.sprite.EmojiStore.EmojiMatch;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;

/**
* Chat log backed by JavaFX WebView. Falls back to {@link KTextEdit} when the
* JavaFX runtime is unavailable.
*/
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 4101968159599867093L;

	private static final Logger LOGGER = Logger.getLogger(WebChatLogView.class);
	private static final Color DEFAULT_BACKGROUND = new Color(60, 30, 0);
	private static final String DEFAULT_FONT_STACK = "'Noto Color Emoji','Segoe UI Emoji','Apple Color Emoji','Twemoji Mozilla','EmojiOne Color','Noto Emoji',sans-serif";
	private static final String DOCUMENT_TEMPLATE =
		"<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style id=\"chat-style\">%s</style></head>"
			+ "<body class=\"chat-root\" style=\"background:%s\"><main id=\"chat-log\" class=\"chat-log\"></main>"
			+ "<script>(function(){const log=document.getElementById('chat-log');const styleTag=document.getElementById('chat-style');const pending=[];const container=document.scrollingElement||document.body||document.documentElement;"
			+ "function scrollToBottom(){if(!container){return;}container.scrollTop=container.scrollHeight;}"
			+ "function append(html,scroll){if(!log){return;}const template=document.createElement('template');template.innerHTML=html;log.appendChild(template.content);if(scroll){requestAnimationFrame(scrollToBottom);}}"
			+ "function clearMessages(){if(log){log.innerHTML='';}}"
			+ "function applyStyle(styleText,background){if(styleTag&&typeof styleText==='string'&&styleText.length){styleTag.textContent=styleText;}if(typeof background==='string'&&background.length&&document.body){document.body.style.background=background;}}"
			+ "function flushPending(){if(!log){return;}while(pending.length){try{pending.shift()();}catch(ex){console.error(ex);}}}"
			+ "window.ChatBridge={append:function(html,scroll){pending.push(function(){append(html,scroll);});if(document.readyState==='complete'){flushPending();}},"
			+ "clear:function(){pending.push(clearMessages);if(document.readyState==='complete'){flushPending();}},"
			+ "applyStyle:function(styleText,background){pending.push(function(){applyStyle(styleText,background);});if(document.readyState==='complete'){flushPending();}},"
			+ "flush:flushPending};"
			+ "if(document.readyState==='complete'){flushPending();}else{window.addEventListener('load',flushPending);}})();</script></body></html>";
	private static final String STYLE_TEMPLATE =
		"body{margin:0;padding:0;background:%2$s;color:#f0f0f0;font-family:%1$s;font-size:%3$dpx;}"
			+ ".chat-root{padding:12px;overflow:auto;}"
			+ ".chat-log{display:flex;flex-direction:column;gap:8px;}"
			+ ".message{background:rgba(0,0,0,0.35);border-radius:10px;padding:6px 12px;box-shadow:0 2px 4px rgba(0,0,0,0.35);}"
			+ ".message.type-error{border-left:4px solid #ff5050;}"
			+ ".message.type-warning{border-left:4px solid #ffae42;}"
			+ ".message.type-positive{border-left:4px solid #4caf50;}"
			+ ".message.type-support{border-left:4px solid #5ad4ff;}"
			+ ".message-header{display:flex;align-items:center;gap:8px;font-size:0.85em;color:rgba(255,255,255,0.7);margin-bottom:4px;}"
			+ ".message-author{font-weight:600;color:#ffffcc;}"
			+ ".message-body{white-space:pre-wrap;word-break:break-word;font-size:1em;line-height:1.4;}"
			+ ".message-body .emoji{display:inline-flex;align-items:center;justify-content:center;font-family:%1$s;font-size:1.2em;line-height:1;vertical-align:-0.2em;margin:0 2px;}"
			+ ".message-admin{background:rgba(255,215,64,0.15);border-left:4px solid #ffd740;}"
			+ ".admin-badge{display:inline-flex;align-items:center;justify-content:center;background:#ffd740;color:#2a2000;font-weight:700;font-size:0.75em;padding:0 6px;border-radius:6px;margin-right:6px;}";
	private static final long FX_INIT_TIMEOUT_SECONDS = 10L;

	private final Format dateFormatter = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());
	private final List<String> htmlLines = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();
	private final FxBridge bridge;

	private Color defaultBackground = DEFAULT_BACKGROUND;
	private String channelName = "";
	private int fontSize;
	private String backgroundCss;
	private String styleSheet;

	WebChatLogView() throws Exception {
		bridge = FxBridge.tryCreate();

		setLayout(new BorderLayout());
		add(bridge.getComponent(), BorderLayout.CENTER);

		Font baseFont = UIManager.getFont("Label.font");
		fontSize = (baseFont != null) ? Math.max(8, baseFont.getSize() - 1) : 12;

		installPopupMenu();
		refreshStyle();
		bridge.loadShell(buildShellDocument());
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void addLine(final EventLine line) {
		if (line == null) {
			return;
		}

		final String timestamp = dateFormatter.format(new Date());
		final String author = escape((line.getHeader() != null) ? line.getHeader() : "PolanieOnLine");
		final String body = formatBody(line.getText());
		final NotificationType type = line.getType();
		final boolean admin = isAdmin(type, line);

		final String cssClass = cssClassFor(type, admin);
		final String adminBadge = admin ? "<span class=\"admin-badge\">ADMIN</span>" : "";
		final String article = String.format(
			Locale.ROOT,
			"<article class=\"message %s\"><div class=\"message-header\">%s<span class=\"message-author\">%s</span><span>%s</span></div><div class=\"message-body\">%s</div></article>",
				cssClass,
				adminBadge,
				author,
				escape(timestamp),
				body);

		synchronized (htmlLines) {
			htmlLines.add(article);
			plainLines.add(buildPlainLine(timestamp, line));
		}

		bridge.appendMessage(article, true);
	}

	@Override
	public void clear() {
		synchronized (htmlLines) {
			htmlLines.clear();
			plainLines.clear();
		}
		bridge.clearMessages();
	}

	@Override
	public void setDefaultBackground(final Color color) {
		defaultBackground = (color != null) ? color : DEFAULT_BACKGROUND;
		refreshStyle();
		bridge.applyStyle(styleSheet, backgroundCss);
	}

	@Override
	public void setChannelName(final String name) {
		channelName = (name != null) ? name : "";
	}

	private boolean isAdmin(final NotificationType type, final EventLine line) {
		if (type == NotificationType.SUPPORT) {
			return true;
		}
		if (line != null) {
			final String header = line.getHeader();
			if ((header != null) && header.toLowerCase(Locale.ROOT).contains("admin")) {
				return true;
			}
		}
		return false;
	}

	private String cssClassFor(final NotificationType type, final boolean admin) {
		final StringBuilder builder = new StringBuilder("message");
		if (admin) {
			builder.append(" message-admin");
		}
		if (type == null) {
			return builder.toString();
		}
		switch (type) {
		case ERROR:
		case NEGATIVE:
		case DAMAGE:
		case POISON:
		case SIGNIFICANT_NEGATIVE:
			builder.append(" type-error");
			break;
		case WARNING:
			builder.append(" type-warning");
			break;
		case POSITIVE:
		case HEAL:
		case SIGNIFICANT_POSITIVE:
			builder.append(" type-positive");
			break;
		case SUPPORT:
			builder.append(" type-support");
			break;
		default:
			break;
		}
		return builder.toString();
	}

	private String buildPlainLine(final String timestamp, final EventLine line) {
		final StringBuilder builder = new StringBuilder();
		if (timestamp != null) {
			builder.append(timestamp);
		}
		if (line != null) {
			final String header = line.getHeader();
			if ((header != null) && !header.isEmpty()) {
				builder.append('<').append(header).append("> ");
			}
			final String text = line.getText();
			if (text != null) {
				builder.append(text);
			}
		}
		return builder.toString();
	}

	private void installPopupMenu() {
		final JPopupMenu popup = new JPopupMenu();

		JMenuItem saveItem = new JMenuItem("Zapisz");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				save();
			}
		});
		popup.add(saveItem);

		JMenuItem clearItem = new JMenuItem("Wyczyść");
		clearItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				clear();
			}
		});
		popup.add(clearItem);

		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent event) {
				showPopup(event);
			}

			@Override
			public void mouseReleased(final MouseEvent event) {
				showPopup(event);
			}

			private void showPopup(final MouseEvent event) {
				if (event.isPopupTrigger()) {
					popup.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		};

		bridge.getComponent().addMouseListener(adapter);
	}

	private void save() {
		final String fileName = getSaveFileName();
		Writer writer = null;
		try {
			File target = new File(fileName);
			File parent = target.getParentFile();
			if ((parent != null) && !parent.exists()) {
				parent.mkdirs();
			}
			writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8");
			synchronized (htmlLines) {
				for (String line : plainLines) {
					writer.write(line);
					writer.write(System.lineSeparator());
				}
			}
		} catch (IOException ex) {
			LOGGER.error("Failed to save chat log", ex);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOGGER.warn("Failed to close chat log writer", ex);
				}
			}
		}

		addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + fileName, NotificationType.CLIENT));
	}

	private String getSaveFileName() {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		StringBuilder nameBuilder = new StringBuilder();
		String character = UserContext.get().getName();
		if (character != null) {
			nameBuilder.append(character).append('_');
		}
		if (!channelName.isEmpty()) {
			nameBuilder.append(channelName).append('_');
		}
		nameBuilder.append(timestamp).append('.').append("log");
		return stendhal.getGameFolder() + "chat/" + nameBuilder.toString();
	}

	private void refreshStyle() {
		backgroundCss = toCssColor(defaultBackground);
		styleSheet = buildStyleSheet(buildFontStack(), backgroundCss);
	}

	private String buildShellDocument() {
		return String.format(Locale.ROOT, DOCUMENT_TEMPLATE, styleSheet, backgroundCss);
	}

	private String buildStyleSheet(final String fontStack, final String background) {
		StringBuilder css = new StringBuilder();
		String fontFace = buildFontFaceRule();
		if (!fontFace.isEmpty()) {
			css.append(fontFace);
		}
		css.append(String.format(Locale.ROOT, STYLE_TEMPLATE, fontStack, background, fontSize));
		return css.toString();
	}

	private String buildFontStack() {
		String family = EmojiStore.getFontFamily();
		if ((family == null) || family.isEmpty()) {
			return DEFAULT_FONT_STACK;
		}
		String quoted = quoteFontFamily(family);
		if (DEFAULT_FONT_STACK.contains(quoted) || quoted.isEmpty()) {
			return DEFAULT_FONT_STACK;
		}
		return quoted + "," + DEFAULT_FONT_STACK;
	}

	private String buildFontFaceRule() {
		EmojiStore store = EmojiStore.get();
		String dataUrl = store.getBundledFontDataUrl();
		if ((dataUrl == null) || dataUrl.isEmpty()) {
			return "";
		}
		String family = store.getFontFamily();
		if ((family == null) || family.isEmpty()) {
			family = "Noto Color Emoji";
		}
		return String.format(Locale.ROOT, "@font-face{font-family:%s;src:url('%s');}", quoteFontFamily(family), dataUrl);
	}

	private String quoteFontFamily(final String family) {
		if ((family == null) || family.isEmpty()) {
			return "";
		}
		String sanitized = family.replace("\\", "\\\\").replace("'", "\\'");
		return "'" + sanitized + "'";
	}

	private String formatBody(final String text) {
		if ((text == null) || text.isEmpty()) {
			return "";
		}
		EmojiStore store = EmojiStore.get();
		StringBuilder html = new StringBuilder();
		StringBuilder plain = new StringBuilder();
		int length = text.length();
		int index = 0;
		while (index < length) {
			char ch = text.charAt(index);
			if (ch == '\r') {
				index++;
				continue;
			}
			if (ch == '\n') {
				if (plain.length() > 0) {
					html.append(escape(plain.toString()));
					plain.setLength(0);
				}
				html.append("<br>");
				index++;
				continue;
			}
			EmojiMatch match = store.matchEmoji(text, index);
			if (match != null) {
				if (plain.length() > 0) {
					html.append(escape(plain.toString()));
					plain.setLength(0);
				}
				int consumed = match.getConsumedLength();
				String token = text.substring(index, index + consumed);
				String glyph = match.getGlyph();
				if ((glyph != null) && !glyph.isEmpty()) {
					html.append("<span class=\"emoji\">")
						.append(escape(glyph))
						.append("</span>");
				} else {
					String dataUrl = store.dataUrlFor(token);
					if ((dataUrl != null) && !dataUrl.isEmpty()) {
						html.append("<img class=\"emoji\" src=\"")
							.append(dataUrl)
							.append("\" alt=\"")
							.append(escape(token))
							.append("\">");
					} else {
						html.append("<span class=\"emoji\">")
							.append(escape(token))
							.append("</span>");
					}
				}
				index += consumed;
				continue;
			}
			plain.append(ch);
			index++;
		}
		if (plain.length() > 0) {
			html.append(escape(plain.toString()));
		}
		return html.toString();
	}
	private static String escape(final String value) {
		if (value == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		int length = value.length();
		for (int i = 0; i < length; i++) {
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
	
	private static String toJsString(final String value) {
		if (value == null) {
			return "''";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('\'');
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
			case '\':
				builder.append("\\");
				break;
			case '\'':
				builder.append("\'");
				break;
			case '"':
				builder.append("\\"");
				break;
			case '\n':
				builder.append("\\n");
				break;
			case '\r':
				builder.append("\\r");
				break;
			case '\t':
				builder.append("\\t");
				break;
			case '\f':
				builder.append("\\f");
				break;
			case '\b':
				builder.append("\\b");
				break;
			default:
				if ((ch < 0x20) || (ch > 0x7e)) {
					builder.append(String.format(Locale.ROOT, "\\u%04x", (int) ch));
				} else {
					builder.append(ch);
				}
				break;
			}
		}
		builder.append('\'');
		return builder.toString();
	}
	
	private static String toCssColor(final Color color) {
		return String.format(Locale.ROOT, "rgb(%d,%d,%d)", color.getRed(), color.getGreen(), color.getBlue());
	}

		private static final class FxBridge {
		private final JComponent component;
		private final Object engine;
		private final Method loadContent;
		private final Method executeScript;
		private final Method runLater;
		private final List<String> pendingScripts = new ArrayList<>();
		private volatile boolean ready;

		private FxBridge(final JComponent component, final Object engine, final Method loadContent, final Method executeScript, final Method runLater) {
			this.component = component;
			this.engine = engine;
			this.loadContent = loadContent;
			this.executeScript = executeScript;
			this.runLater = runLater;
		}

		static FxBridge tryCreate() throws InterruptedException {
			try {
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				if (loader == null) {
					loader = FxBridge.class.getClassLoader();
				}

				Class<?> jfxPanelClass = Class.forName("javafx.embed.swing.JFXPanel", true, loader);
				Object jfxPanel = jfxPanelClass.getConstructor().newInstance();

				Class<?> platformClass = Class.forName("javafx.application.Platform", true, loader);
				Method runLater = platformClass.getMethod("runLater", Runnable.class);

				Class<?> webViewClass = Class.forName("javafx.scene.web.WebView", false, loader);
				Class<?> webEngineClass = Class.forName("javafx.scene.web.WebEngine", false, loader);
				Class<?> sceneClass = Class.forName("javafx.scene.Scene", false, loader);
				Class<?> parentClass = Class.forName("javafx.scene.Parent", false, loader);
				Class<?> changeListenerClass = Class.forName("javafx.beans.value.ChangeListener", false, loader);
				Class<?> workerStateClass = Class.forName("javafx.concurrent.Worker$State", false, loader);

				Method getEngine = webViewClass.getMethod("getEngine");
				Method loadContent = webEngineClass.getMethod("loadContent", String.class);
				Method executeScript = webEngineClass.getMethod("executeScript", String.class);
				Method getLoadWorker = webEngineClass.getMethod("getLoadWorker");
				Constructor<?> webViewCtor = webViewClass.getConstructor();
				Constructor<?> sceneCtor = sceneClass.getConstructor(parentClass);
				Method setScene = jfxPanelClass.getMethod("setScene", sceneClass);

				Enum<?> succeededState = Enum.valueOf((Class) workerStateClass, "SUCCEEDED");
				Enum<?> failedState = Enum.valueOf((Class) workerStateClass, "FAILED");

				CountDownLatch latch = new CountDownLatch(1);
				Object[] engineHolder = new Object[1];
				Throwable[] failureHolder = new Throwable[1];
				runLater.invoke(null, new Runnable() {
					@Override
					public void run() {
						try {
							Object webView = webViewCtor.newInstance();
							Object engine = getEngine.invoke(webView);
							Object scene = sceneCtor.newInstance(webView);
							setScene.invoke(jfxPanel, scene);
							engineHolder[0] = engine;
						} catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
							failureHolder[0] = ex;
						} finally {
							latch.countDown();
						}
					}
				});

				if (!latch.await(FX_INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					throw new UnsupportedOperationException("Timed out waiting for JavaFX WebView initialization");
				}
				if (engineHolder[0] == null) {
					Throwable cause = failureHolder[0];
					if (LOGGER.isDebugEnabled() && cause != null) {
						LOGGER.debug("JavaFX WebView initialization failed", cause);
					}
					throw new UnsupportedOperationException(describeFailure(cause), cause);
				}

				FxBridge bridge = new FxBridge((JComponent) jfxPanel, engineHolder[0], loadContent, executeScript, runLater);
				bridge.attachLoadListener(getLoadWorker, changeListenerClass, succeededState, failedState, loader);
				return bridge;
			} catch (ClassNotFoundException ex) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("JavaFX classes not found", ex);
				}
				throw new UnsupportedOperationException("JavaFX modules not available", ex);
			} catch (UnsupportedOperationException ex) {
				throw ex;
			} catch (ReflectiveOperationException ex) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("JavaFX reflection failure", ex);
				}
				throw new UnsupportedOperationException(describeFailure(ex), ex);
			} catch (RuntimeException | LinkageError ex) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("JavaFX runtime failure", ex);
				}
				throw new UnsupportedOperationException(describeFailure(ex), ex);
			}
		}

		private void attachLoadListener(final Method getLoadWorker, final Class<?> changeListenerClass, final Object succeededState, final Object failedState, final ClassLoader loader) throws ReflectiveOperationException {
			Object worker = getLoadWorker.invoke(engine);
			Method stateProperty = worker.getClass().getMethod("stateProperty");
			Object property = stateProperty.invoke(worker);
			Method addListener = property.getClass().getMethod("addListener", changeListenerClass);
			Object listener = Proxy.newProxyInstance(loader, new Class<?>[] { changeListenerClass }, (proxy, method, args) -> {
				if ("changed".equals(method.getName()) && (args != null) && (args.length == 3)) {
					Object newValue = args[2];
					if ((newValue != null) && newValue.equals(succeededState)) {
						ready = true;
						flushScripts();
					} else if ((failedState != null) && failedState.equals(newValue)) {
						LOGGER.warn("JavaFX WebView load failed");
					}
				}
				return null;
			});
			addListener.invoke(property, listener);
		}

		JComponent getComponent() {
			return component;
		}

		void loadShell(final String html) {
			if (html == null) {
				return;
			}
			ready = false;
			synchronized (pendingScripts) {
				pendingScripts.clear();
			}
			try {
				runLater.invoke(null, new Runnable() {
					@Override
					public void run() {
						try {
							loadContent.invoke(engine, html);
						} catch (IllegalAccessException | InvocationTargetException ex) {
							LOGGER.warn("Failed to render chat HTML", ex);
						}
					}
				});
			} catch (IllegalAccessException | InvocationTargetException ex) {
				LOGGER.warn("Failed to schedule JavaFX update", ex);
			}
		}

		void appendMessage(final String html, final boolean scrollToBottom) {
			if (html == null) {
				return;
			}
			final String script = "if(window.ChatBridge){ChatBridge.append(" + WebChatLogView.toJsString(html) + "," + (scrollToBottom ? "true" : "false") + ");}";
			enqueueScript(script);
		}

		void clearMessages() {
			enqueueScript("if(window.ChatBridge){ChatBridge.clear();}");
		}

		void applyStyle(final String styleText, final String background) {
			final String script = "if(window.ChatBridge){ChatBridge.applyStyle(" + WebChatLogView.toJsString(styleText) + "," + WebChatLogView.toJsString(background) + ");}";
			enqueueScript(script);
		}

		private void enqueueScript(final String script) {
			if ((script == null) || script.isEmpty()) {
				return;
			}
			synchronized (pendingScripts) {
				pendingScripts.add(script);
			}
			if (ready) {
				flushScripts();
			}
		}

		private void flushScripts() {
			final List<String> commands;
			synchronized (pendingScripts) {
				if (pendingScripts.isEmpty()) {
					return;
				}
				commands = new ArrayList<>(pendingScripts);
				pendingScripts.clear();
			}
			try {
				runLater.invoke(null, new Runnable() {
					@Override
					public void run() {
						for (String command : commands) {
							try {
								executeScript.invoke(engine, command);
							} catch (IllegalAccessException | InvocationTargetException ex) {
								LOGGER.warn("Failed to execute chat script", ex);
							}
						}
					}
				});
			} catch (IllegalAccessException | InvocationTargetException ex) {
				LOGGER.warn("Failed to schedule chat script execution", ex);
			}
		}

		private static String describeFailure(final Throwable cause) {
			if (cause == null) {
				return "JavaFX initialization failed";
			}
			Throwable root = unwrap(cause);
			String message = (root.getMessage() != null) ? root.getMessage() : root.getClass().getSimpleName();
			String lower = message.toLowerCase(Locale.ROOT);
			if (lower.contains("quantumrenderer") || lower.contains("internal graphics not initialized")) {
				return "JavaFX runtime failed to initialize graphics pipeline";
			}
			return "JavaFX initialization failed: " + message;
		}

		private static Throwable unwrap(final Throwable cause) {
			Throwable current = cause;
			while (true) {
				Throwable next = null;
				if (current instanceof InvocationTargetException && ((InvocationTargetException) current).getCause() != null) {
					next = ((InvocationTargetException) current).getCause();
				} else if (current instanceof ExceptionInInitializerError && ((ExceptionInInitializerError) current).getCause() != null) {
					next = ((ExceptionInInitializerError) current).getCause();
				} else if ((current.getCause() != null) && (current.getCause() != current)) {
					next = current.getCause();
				}
				if (next == null) {
				return current;
				}
				current = next;
			}
		}
	}

}
