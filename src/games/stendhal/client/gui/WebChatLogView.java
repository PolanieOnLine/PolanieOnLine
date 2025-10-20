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
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.UserContext;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.gui.textformat.AttributedTextSink;
import games.stendhal.client.gui.textformat.StringFormatter;
import games.stendhal.client.gui.textformat.StyleSet;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;

/**
 * Chat log implementation backed by JavaFX WebView when available. The class
 * interacts with JavaFX through reflection so the client continues to compile
 * even when the JavaFX modules are missing from the runtime. When the modules
 * cannot be found the constructor throws an exception which is caught by the
 * caller and the legacy Swing implementation is used instead.
 */
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 4101968159599867093L;

	private static final Logger logger = Logger.getLogger(WebChatLogView.class);

	/** Shared emoji font stack for HTML rendering. */
	private static final String EMOJI_FONT_STACK = "'Segoe UI Emoji','Apple Color Emoji','Noto Color Emoji','Twitter Color Emoji','EmojiOne Color','Android Emoji','Noto Emoji',sans-serif";

	/** Default chat background. */
	private static final Color DEFAULT_BACKGROUND = new Color(60, 30, 0);

	/** Timestamp format. */
	private final Format dateFormatter = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());

	/** Formatter for markup text. */
	private final StringFormatter<Style, StyleSet> formatter = new StringFormatter<>();

	/** Style context used for building HTML spans. */
	private final StyleContext styleContext = new StyleContext();

	/** Base styles matching the legacy Swing chat rendering. */
	private final Style regularStyle;
	private final Style headerStyle;
	private final Style timestampStyle;
	private final Style boldStyle;
	private final Style emojiStyle;

	/** JavaFX bridge accessed via reflection. */
	private final FxBridge fx;

	/** Cached chat lines in HTML and plain text form. */
	private final List<String> htmlLines = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();

	/** State holders. */
	private volatile boolean autoScroll = true;
	private Color defaultBackground = DEFAULT_BACKGROUND;
	private String channelName = "";

	WebChatLogView() throws Exception {
		fx = FxBridge.tryCreate();
		if (fx == null) {
			throw new UnsupportedOperationException("JavaFX modules not available");
		}

		setLayout(new BorderLayout());
		add(fx.getComponent(), BorderLayout.CENTER);

		Font baseFont = UIManager.getFont("Label.font");
		int mainSize = (baseFont != null) ? Math.max(8, baseFont.getSize() - 1) : 12;

		regularStyle = styleContext.addStyle("regular", null);
		StyleConstants.setFontFamily(regularStyle, "Dialog");
		StyleConstants.setFontSize(regularStyle, mainSize);
		StyleConstants.setForeground(regularStyle, Color.WHITE);

		headerStyle = styleContext.addStyle("header", regularStyle);
		StyleConstants.setItalic(headerStyle, true);
		StyleConstants.setForeground(headerStyle, new Color(255, 255, 220));

		timestampStyle = styleContext.addStyle("timestamp", regularStyle);
		StyleConstants.setItalic(timestampStyle, true);
		StyleConstants.setForeground(timestampStyle, new Color(220, 220, 220));
		StyleConstants.setFontSize(timestampStyle, Math.max(6, mainSize - 1));

		boldStyle = styleContext.addStyle("bold", regularStyle);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setItalic(boldStyle, true);
		StyleConstants.setForeground(boldStyle, new Color(90, 170, 255));
		StyleConstants.setFontSize(boldStyle, mainSize + 1);

		emojiStyle = styleContext.addStyle("emoji", regularStyle);
		StyleConstants.setFontFamily(emojiStyle, EmojiStore.getFontFamily());
		StyleConstants.setFontSize(emojiStyle, mainSize + 2);

		initFormatterStyles();
		addMouseListener(new TextPaneMouseListener());

		fx.whenReady(new Runnable() {
			@Override
			public void run() {
				refreshHtml();
			}
		});
	}

	private void initFormatterStyles() {
		StyleSet regular = new StyleSet(styleContext, regularStyle);

		StyleSet italics = regular.copy();
		italics.setAttribute(StyleConstants.Italic, Boolean.TRUE);
		italics.setAttribute(StyleConstants.Foreground, new Color(65, 105, 225));
		italics.setAttribute("linkact", new LinkListener());
		formatter.addStyle('#', italics);

		StyleSet underline = regular.copy();
		underline.setAttribute(StyleConstants.Underline, Boolean.TRUE);
		formatter.addStyle('§', underline);

		StyleSet redUnderline = regular.copy();
		redUnderline.setAttribute(StyleConstants.Underline, Boolean.TRUE);
		redUnderline.setAttribute(StyleConstants.Foreground, new Color(254, 76, 76));
		formatter.addStyle('~', redUnderline);

		StyleSet admin = regular.copy();
		admin.setAttribute(StyleConstants.Italic, Boolean.TRUE);
		admin.setAttribute(StyleConstants.Foreground, new Color(225, 185, 65));
		formatter.addStyle('¡', admin);
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void addLine(EventLine line) {
		if (line == null) {
			return;
		}

		String html = buildLineHtml(line);
		synchronized (htmlLines) {
			htmlLines.add(html);
			plainLines.add(buildPlainLine(line));
		}

		refreshHtml();
	}

	@Override
	public void clear() {
		synchronized (htmlLines) {
			htmlLines.clear();
			plainLines.clear();
		}
		refreshHtml();
	}

	@Override
	public void setDefaultBackground(Color color) {
		defaultBackground = (color != null) ? color : DEFAULT_BACKGROUND;
		refreshHtml();
	}

	@Override
	public void setChannelName(String name) {
		channelName = (name != null) ? name : "";
	}

	private void refreshHtml() {
		final String document;
		synchronized (htmlLines) {
			document = buildDocument();
		}

		fx.loadContent(document, new Runnable() {
			@Override
			public void run() {
				if (autoScroll) {
					fx.scrollToEnd();
				}
			}
		});
	}

	private String buildDocument() {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>");
		html.append("body { background:").append(cssColor(defaultBackground)).append("; margin:0; padding:4px; font-family: ")
			.append(cssFontFamily(regularStyle)).append("; font-size:").append(StyleConstants.getFontSize(regularStyle))
			.append("px; color:").append(cssColor(StyleConstants.getForeground(regularStyle))).append("; }");
		html.append(".line { white-space: pre-wrap; word-break: break-word; }");
		html.append(".timestamp { color:").append(cssColor(StyleConstants.getForeground(timestampStyle))).append("; font-style: italic; margin-right: 4px; }");
		html.append(".header { color:").append(cssColor(StyleConstants.getForeground(headerStyle))).append("; font-style: italic; margin-right: 4px; }");
		html.append(".bold { color:").append(cssColor(StyleConstants.getForeground(boldStyle))).append("; font-style: italic; font-weight: bold; }");
		html.append(".emoji { font-family: ").append(EMOJI_FONT_STACK).append("; font-style: normal; font-weight: normal; }");
		html.append(".emoji-img { height: 1.2em; width: auto; vertical-align: middle; }");
		html.append("a { color: ").append(cssColor(new Color(65, 105, 225))).append("; }");
		html.append("</style></head><body>");

		for (String line : htmlLines) {
			html.append(line);
		}

		html.append("</body></html>");
		return html.toString();
	}

	private String buildLineHtml(EventLine line) {
		StringBuilder html = new StringBuilder();
		html.append("<div class=\"line\">");
		html.append("<span class=\"timestamp\">").append(escape(dateFormatter.format(new Date()))).append("</span>");

		String header = line.getHeader();
		if ((header != null) && !header.isEmpty()) {
			html.append("<span class=\"header\">").append(escape(header)).append(": </span>");
		}

		HtmlTextSink sink = new HtmlTextSink(html);
		StyleSet base = new StyleSet(styleContext, regularStyle);
		formatter.format(line.getText(), base, sink);
		html.append("</div>");
		return html.toString();
	}

	private String buildPlainLine(EventLine line) {
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormatter.format(new Date()));
		if (!(line instanceof HeaderLessEventLine) && (line.getHeader() != null) && !line.getHeader().isEmpty()) {
			sb.append(line.getHeader()).append(": ");
		}
		sb.append(line.getText());
		return sb.toString();
	}

	private static String cssColor(Color color) {
		Color c = (color != null) ? color : Color.WHITE;
		return "rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
	}

	private static String cssFontFamily(Style style) {
		String family = StyleConstants.getFontFamily(style);
		if ((family == null) || family.isEmpty()) {
			family = "Dialog";
		}
		return '\'' + family.replace("'", "\\'") + '\'';
	}

	private static String escape(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
			case '&':
				out.append("&amp;");
				break;
			case '<':
				out.append("&lt;");
				break;
			case '>':
				out.append("&gt;");
				break;
			case '\"':
				out.append("&quot;");
				break;
			default:
				out.append(c);
			}
		}
		return out.toString();
	}

	private void save() {
		String fname = getSaveFileName();
		Writer writer = null;
		try {
			File target = new File(fname);
			target.getParentFile().mkdirs();
			writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8");
			synchronized (htmlLines) {
				for (String line : plainLines) {
					writer.write(line);
					writer.write(System.lineSeparator());
				}
			}
		} catch (IOException ex) {
			logger.error("Failed to save chat log", ex);
			return;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.warn("Failed to close chat log writer", e);
				}
			}
		}

		addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + fname, NotificationType.CLIENT));
	}

	private String getSaveFileName() {
		String savename = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		if (!"".equals(channelName)) {
			savename = channelName + "_" + savename;
		}

		String charname = UserContext.get().getName();
		if (charname != null) {
			savename = charname + "_" + savename;
		}

		return stendhal.getGameFolder() + "chat/" + savename + ".log";
	}

	private class TextPaneMouseListener extends MousePopupAdapter {
		@Override
		protected void showPopup(MouseEvent e) {
			final JPopupMenu popup = new JPopupMenu("zapisz");

			JMenuItem menuItem = new JMenuItem("Zapisz");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			});
			popup.add(menuItem);

			menuItem = new JMenuItem("Wyczyść");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					clear();
				}
			});
			popup.add(menuItem);

			popup.show(e.getComponent(), e.getX(), e.getY());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			Object link = e.getSource();
			if (link instanceof LinkListener) {
				((LinkListener) link).linkClicked("");
			}
		}
	}

	class LinkListener {
		void linkClicked(String text) {
			ClientSingletonRepository.getChatTextController().setFocus();
		}
	}

	private class HtmlTextSink implements AttributedTextSink<StyleSet> {
		private final StringBuilder html;

		HtmlTextSink(StringBuilder html) {
			this.html = html;
		}

		@Override
		public void append(String s, StyleSet attrs) {
			if ((s == null) || s.isEmpty()) {
				return;
			}

			final Style style = attrs.contents();
			final EmojiStore store = EmojiStore.get();
			final StringBuilder buffer = new StringBuilder();
			int index = 0;
			final int length = s.length();
			while (index < length) {
				final EmojiStore.EmojiMatch match = store.matchEmoji(s, index);
				if (match != null) {
					if (buffer.length() > 0) {
						appendSpan(store, buffer.toString(), style, false);
						buffer.setLength(0);
					}
					final int consumed = Math.max(1, match.getConsumedLength());
					String glyph = match.getGlyph();
					if ((glyph == null) || glyph.isEmpty()) {
						glyph = s.substring(index, Math.min(length, index + consumed));
					}
					appendSpan(store, glyph, style, true);
					index += consumed;
					continue;
				}
				final int codePoint = s.codePointAt(index);
				buffer.appendCodePoint(codePoint);
                               index += java.lang.Character.charCount(codePoint);
			}
			if (buffer.length() > 0) {
				appendSpan(store, buffer.toString(), style, false);
			}
		}

		private void appendSpan(final EmojiStore store, final String text, final Style style, final boolean emoji) {
			if ((text == null) || text.isEmpty()) {
				return;
			}

			final String dataUrl = (emoji && (store != null)) ? store.dataUrlFor(text) : null;
			final StringBuilder span = new StringBuilder();
			span.append("<span");
			if (emoji) {
				span.append(" class=\"emoji\"");
			}
			span.append(" style=\"");

			if (dataUrl != null) {
				span.append("display:inline-block;");
			} else {
				final Color fg = StyleConstants.getForeground(style);
				if (fg != null) {
					span.append("color:").append(cssColor(fg)).append(';');
				}
				if (!emoji && StyleConstants.isBold(style)) {
					span.append("font-weight:bold;");
				}
				if (!emoji && StyleConstants.isItalic(style)) {
					span.append("font-style:italic;");
				} else if (emoji) {
					span.append("font-style:normal;");
				}
				if (StyleConstants.isUnderline(style)) {
					span.append("text-decoration:underline;");
				}
				span.append("font-size:").append(StyleConstants.getFontSize(style)).append("px;");
				if (emoji) {
					span.append("font-family:").append(EMOJI_FONT_STACK).append(';');
					span.append("font-weight:normal;");
				} else {
					final String family = StyleConstants.getFontFamily(style);
					if ((family != null) && !family.isEmpty()) {
						span.append("font-family:").append(cssFontFamily(style)).append(';');
					}
				}
			}
			span.append('\"');
			span.append('>');
			if ((dataUrl != null) && emoji) {
				span.append("<img class=\"emoji-img\" src=\"").append(dataUrl).append("\" alt=\"")
					.append(escape(text)).append("\"/>");
			} else {
				span.append(escape(text));
			}
			span.append("</span>");
			html.append(span);
		}
	}

	/**
	 * Reflection based bridge to JavaFX WebView.
	 */
	private static final class FxBridge {
		private final Object panel;
		private final Method runLater;
		private final Constructor<?> webViewCtor;
		private final Method setContextMenuEnabled;
private final Method getEngine;
private final Constructor<?> sceneCtor;
private final Method setScene;
private final Method loadContent;
private final Method executeScript;
		private final Method getLoadWorker;
		private final Method workerStateProperty;
		private final Method workerGetState;
		private final Class<?> changeListenerClass;
		private final Class<?> workerStateClass;
		private final CountDownLatch ready = new CountDownLatch(1);

		private volatile Object webEngine;
		private volatile Throwable startupFailure;
		private volatile Method propertyAddListener;
		private volatile Method propertyRemoveListener;
		private volatile Object succeededState;
		private volatile Object failedState;
		private volatile Object cancelledState;

		private FxBridge(Object panel, Method runLater, Constructor<?> webViewCtor, Method setContextMenuEnabled,
				Method getEngine, Constructor<?> sceneCtor, Method setScene, Method loadContent, Method executeScript,
				Method getLoadWorker, Method workerStateProperty, Method workerGetState, Class<?> changeListenerClass, Class<?> workerStateClass) {
			this.panel = panel;
			this.runLater = runLater;
			this.webViewCtor = webViewCtor;
			this.setContextMenuEnabled = setContextMenuEnabled;
			this.getEngine = getEngine;
			this.sceneCtor = sceneCtor;
			this.setScene = setScene;
			this.loadContent = loadContent;
			this.executeScript = executeScript;
			this.getLoadWorker = getLoadWorker;
			this.workerStateProperty = workerStateProperty;
			this.workerGetState = workerGetState;
			this.changeListenerClass = changeListenerClass;
			this.workerStateClass = workerStateClass;
		}

		private static final Object fxGuard = new Object();
		private static volatile boolean fxChecked;
		private static volatile boolean fxAvailable;
		private static volatile boolean fxMissingLogged;
		private static volatile boolean fxInitLogged;
		private static volatile boolean softwarePipelineForced;

		static FxBridge tryCreate() {
			if (!ensureJavaFxPresent()) {
				return null;
			}
			boolean forcedSoftware = false;
			for (int attempt = 0; attempt < 2; attempt++) {
				try {
					Class<?> jfxPanelClass = Class.forName("javafx.embed.swing.JFXPanel");
					Object panel = jfxPanelClass.getConstructor().newInstance();

					Class<?> platformClass = Class.forName("javafx.application.Platform");
					Method runLater = platformClass.getMethod("runLater", Runnable.class);

					Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
					Constructor<?> webViewCtor = webViewClass.getConstructor();
					Method setContextMenuEnabled = webViewClass.getMethod("setContextMenuEnabled", boolean.class);
					Method getEngine = webViewClass.getMethod("getEngine");

					Class<?> parentClass = Class.forName("javafx.scene.Parent");
					Class<?> sceneClass = Class.forName("javafx.scene.Scene");
					Constructor<?> sceneCtor = sceneClass.getConstructor(parentClass);
					Method setScene = jfxPanelClass.getMethod("setScene", sceneClass);

Class<?> webEngineClass = Class.forName("javafx.scene.web.WebEngine");
Method loadContent = webEngineClass.getMethod("loadContent", String.class, String.class);
Method executeScript = webEngineClass.getMethod("executeScript", String.class);
Method getLoadWorker = webEngineClass.getMethod("getLoadWorker");

Class<?> workerClass = Class.forName("javafx.concurrent.Worker");
Method workerStateProperty = workerClass.getMethod("stateProperty");
Method workerGetState = workerClass.getMethod("getState");
Class<?> changeListenerClass = Class.forName("javafx.beans.value.ChangeListener");
Class<?> workerStateClass = Class.forName("javafx.concurrent.Worker$State");

FxBridge bridge = new FxBridge(panel, runLater, webViewCtor, setContextMenuEnabled, getEngine, sceneCtor,
setScene, loadContent, executeScript, getLoadWorker, workerStateProperty, workerGetState,
changeListenerClass, workerStateClass);
					bridge.init();
					return bridge;
				} catch (Throwable ex) {
					if (!forcedSoftware && shouldRetryWithSoftwarePipeline(ex)) {
						forcedSoftware = forceSoftwarePipeline();
						if (forcedSoftware) {
							continue;
						}
					}
					logInitializationFailure(ex);
					markUnavailable();
					return null;
				}
			}
			markUnavailable();
			return null;
		}

		private static boolean ensureJavaFxPresent() {
			if (fxChecked) {
				return fxAvailable;
			}
			synchronized (fxGuard) {
				if (!fxChecked) {
					fxAvailable = areRequiredClassesPresent();
					fxChecked = true;
					if (!fxAvailable && !fxMissingLogged) {
						logger.info("JavaFX modules not found; using legacy chat log.");
						fxMissingLogged = true;
					}
				}
			}
			return fxAvailable;
		}

		private static boolean areRequiredClassesPresent() {
			return isClassPresent("javafx.embed.swing.JFXPanel")
				&& isClassPresent("javafx.application.Platform")
				&& isClassPresent("javafx.scene.web.WebView")
				&& isClassPresent("javafx.scene.Scene");
		}

		private static boolean isClassPresent(String className) {
			try {
				Class.forName(className, false, WebChatLogView.class.getClassLoader());
				return true;
			} catch (ClassNotFoundException ex) {
				return false;
			} catch (LinkageError ex) {
				if (!fxMissingLogged) {
					logger.warn("JavaFX class present but incompatible: " + className + " (" + ex.getMessage() + " ). Install a JavaFX build that matches this runtime.");
					fxMissingLogged = true;
				}
				return false;
			}
		}

		private static void markUnavailable() {
			synchronized (fxGuard) {
				fxAvailable = false;
				fxChecked = true;
			}
		}

		private static void logInitializationFailure(Throwable ex) {
			Throwable cause = rootCause(ex);
			String message = (cause != null) ? cause.getMessage() : null;
			if (message == null) {
				message = "";
			}
			if (!fxInitLogged) {
				if (message.contains("QuantumRenderer") || message.contains("no suitable pipeline")) {
					logger.info("JavaFX WebView disabled: renderer pipeline unavailable. Update graphics drivers or launch the client with -Dprism.order=sw.");
				} else if (message.contains("No toolkit found")) {
					logger.info("JavaFX WebView disabled: JavaFX toolkit failed to start. Ensure the OpenJFX runtime matches the JRE.");
				} else if (message.contains("Unsupported JavaFX configuration")) {
					logger.info("JavaFX WebView disabled: unsupported JavaFX configuration detected. Verify module-path and graphics stack settings.");
				} else {
					String causeName = (cause != null) ? cause.getClass().getSimpleName() : ex.getClass().getSimpleName();
					if (!message.isEmpty()) {
						logger.warn("JavaFX WebView initialization failed: " + causeName + ": " + message);
					} else {
						logger.warn("JavaFX WebView initialization failed: " + causeName);
					}
				}
				fxInitLogged = true;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("JavaFX initialization stack trace", ex);
			}
		}

		private static Throwable rootCause(Throwable ex) {
			Throwable current = ex;
			while ((current != null) && (current.getCause() != null) && (current.getCause() != current)) {
				current = current.getCause();
			}
			return current;
		}

		private void init() throws IllegalAccessException, InvocationTargetException {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						Object webView = webViewCtor.newInstance();
						setContextMenuEnabled.invoke(webView, Boolean.FALSE);
						Object engine = getEngine.invoke(webView);
						Object scene = sceneCtor.newInstance(webView);
						setScene.invoke(panel, scene);
						webEngine = engine;
						Object worker = null;
						try {
							worker = getLoadWorker.invoke(engine);
						} catch (Throwable metadataEx) {
							logger.debug("Unable to query WebView worker during init", metadataEx);
						}
						prepareWorkerMetadata(worker);
					} catch (Throwable ex) {
						startupFailure = ex;
						logInitializationFailure(ex);
					} finally {
						ready.countDown();
					}
				}
			};
			startupFailure = null;
			runLater.invoke(null, task);
			try {
				if (!ready.await(5, TimeUnit.SECONDS) || (webEngine == null)) {
					Throwable failure = startupFailure;
					if (failure != null) {
						throw new IllegalStateException("JavaFX WebView failed to initialize", failure);
					}
					throw new IllegalStateException("JavaFX WebView initialization did not complete");
				}
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while waiting for JavaFX initialization", ex);
			}
		}

		Component getComponent() {
			return (Component) panel;
		}

		void whenReady(final Runnable runnable) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						ready.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						return;
					}
					runnable.run();
				}
			};
			SwingUtilities.invokeLater(task);
		}

		void loadContent(final String html, final Runnable afterLoad) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						if (!awaitReady()) {
							return;
						}
						Object worker = (webEngine != null) ? getLoadWorker.invoke(webEngine) : null;
						Object property = null;
						if (worker != null) {
							property = workerStateProperty.invoke(worker);
						}
						Object listener = null;
						boolean listenerAttached = false;
						try {
							if ((property != null) && (changeListenerClass != null)) {
								prepareWorkerMetadata(worker);
								listener = createLoadListener(property, afterLoad);
								if ((listener != null) && (propertyAddListener != null)) {
									propertyAddListener.invoke(property, listener);
									listenerAttached = true;
								}
							}
						} catch (Throwable attachEx) {
							logger.debug("Unable to attach WebView load listener", attachEx);
						}
						loadContent.invoke(webEngine, html, "text/html");
						if (!listenerAttached && (afterLoad != null)) {
							SwingUtilities.invokeLater(afterLoad);
						}
					} catch (Throwable ex) {
						logger.error("Failed to load chat HTML", ex);
					}
				}
			};
			invokeLater(task);
		}

		private void prepareWorkerMetadata(final Object worker) {
			if ((worker == null) || (changeListenerClass == null)) {
				return;
			}
			try {
				Object property = workerStateProperty.invoke(worker);
				if (property == null) {
					return;
				}
				if ((propertyAddListener == null) || (propertyRemoveListener == null)) {
					Class<?> propertyClass = property.getClass();
					propertyAddListener = propertyClass.getMethod("addListener", changeListenerClass);
					propertyRemoveListener = propertyClass.getMethod("removeListener", changeListenerClass);
				}
				if (succeededState == null) {
					@SuppressWarnings("rawtypes")
					Class enumClass = workerStateClass;
					succeededState = Enum.valueOf(enumClass, "SUCCEEDED");
					failedState = Enum.valueOf(enumClass, "FAILED");
					cancelledState = Enum.valueOf(enumClass, "CANCELLED");
				}
			} catch (Throwable ex) {
				logger.debug("Unable to initialize WebView worker metadata", ex);
			}
		}

		private Object createLoadListener(final Object property, final Runnable afterLoad) {
			if ((propertyAddListener == null) || (propertyRemoveListener == null) || (changeListenerClass == null)) {
				return null;
			}
			final Method removeMethod = propertyRemoveListener;
			final Runnable callback = afterLoad;
			final Object successState = succeededState;
			final Object failed = failedState;
			final Object cancelled = cancelledState;
			InvocationHandler handler = new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (!"changed".equals(method.getName()) || (args == null) || (args.length < 3)) {
						return null;
					}
					Object newValue = args[2];
					boolean completed = false;
					if ((successState != null) && successState.equals(newValue)) {
						completed = true;
						if (callback != null) {
							SwingUtilities.invokeLater(callback);
						}
					} else if (((failed != null) && failed.equals(newValue)) || ((cancelled != null) && cancelled.equals(newValue))) {
						completed = true;
					}
					if (completed && (removeMethod != null)) {
						try {
							removeMethod.invoke(property, proxy);
						} catch (Throwable removalEx) {
							logger.debug("Failed to remove WebView load listener", removalEx);
						}
					}
					return null;
				}
			};
			return Proxy.newProxyInstance(changeListenerClass.getClassLoader(), new Class<?>[] { changeListenerClass }, handler);
		}

		void scrollToEnd() {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						if (!awaitReady()) {
							return;
						}
						executeScript.invoke(webEngine,
								"window.requestAnimationFrame(function(){window.scrollTo(0, document.body.scrollHeight);});");
					} catch (Throwable ex) {
						logger.warn("Failed to scroll chat", ex);
					}
				}
			};
			invokeLater(task);
		}

		private void prepareWorkerMetadata(final Object worker) {
			if ((worker == null) || (changeListenerClass == null)) {
				return;
			}
			try {
				Object property = workerStateProperty.invoke(worker);
				if (property == null) {
					return;
				}
				if ((propertyAddListener == null) || (propertyRemoveListener == null)) {
					Class<?> propertyClass = property.getClass();
					propertyAddListener = propertyClass.getMethod("addListener", changeListenerClass);
					propertyRemoveListener = propertyClass.getMethod("removeListener", changeListenerClass);
				}
				if (succeededState == null) {
					@SuppressWarnings("rawtypes")
					Class enumClass = workerStateClass;
					succeededState = Enum.valueOf(enumClass, "SUCCEEDED");
					failedState = Enum.valueOf(enumClass, "FAILED");
					cancelledState = Enum.valueOf(enumClass, "CANCELLED");
				}
			} catch (Throwable ex) {
				logger.debug("Unable to initialize WebView worker metadata", ex);
			}
		}

		private Object createLoadListener(final Object property, final Runnable afterLoad) {
			if ((propertyAddListener == null) || (propertyRemoveListener == null) || (changeListenerClass == null)) {
				return null;
			}
			final Method removeMethod = propertyRemoveListener;
			final Runnable callback = afterLoad;
			final Object successState = succeededState;
			final Object failed = failedState;
			final Object cancelled = cancelledState;
			InvocationHandler handler = new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					if (!"changed".equals(method.getName()) || (args == null) || (args.length < 3)) {
						return null;
					}
					Object newValue = args[2];
					boolean completed = false;
					if ((successState != null) && successState.equals(newValue)) {
						completed = true;
						if (callback != null) {
							SwingUtilities.invokeLater(callback);
						}
					} else if (((failed != null) && failed.equals(newValue)) || ((cancelled != null) && cancelled.equals(newValue))) {
						completed = true;
					}
					if (completed && (removeMethod != null)) {
						try {
							removeMethod.invoke(property, proxy);
						} catch (Throwable removalEx) {
							logger.debug("Failed to remove WebView load listener", removalEx);
						}
					}
					return null;
				}
			};
			return Proxy.newProxyInstance(changeListenerClass.getClassLoader(), new Class<?>[] { changeListenerClass }, handler);
		}

		private void invokeLater(Runnable runnable) {
			try {
				runLater.invoke(null, runnable);
			} catch (IllegalAccessException | InvocationTargetException ex) {
				logger.error("Failed to schedule JavaFX task", ex);
			}
		}

		private boolean awaitReady() {
			try {
				return ready.await(5, TimeUnit.SECONDS) && (webEngine != null);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}

		private static boolean shouldRetryWithSoftwarePipeline(Throwable ex) {
			Throwable cause = rootCause(ex);
			if (cause == null) {
				cause = ex;
			}
			String message = cause.getMessage();
			if (message == null) {
				return false;
			}
			String normalized = message.toLowerCase(Locale.ROOT);
			return normalized.contains("quantumrenderer")
					|| normalized.contains("no suitable pipeline")
					|| normalized.contains("no toolkit found");
		}

		private static boolean forceSoftwarePipeline() {
			synchronized (fxGuard) {
				if (softwarePipelineForced) {
					return false;
				}
				softwarePipelineForced = true;
			}
			System.setProperty("prism.order", "sw");
			System.setProperty("prism.text", "t2k");
			logger.info("Retrying JavaFX WebView initialization using the software renderer (prism.order=sw).");
			return true;
		}
	}
}
