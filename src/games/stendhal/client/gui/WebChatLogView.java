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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import games.stendhal.client.UserContext;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.sprite.EmojiStore.EmojiMatch;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * JavaFX powered chat log view rendering messages with HTML/CSS styling and
 * emoji-aware formatting.
 */
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 6623284543031880666L;

	private static final Logger LOGGER = Logger.getLogger(WebChatLogView.class);

	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());

	private final List<String> htmlLines = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();

	private final JFXPanel fxPanel;
	private final FxBridge bridge;

	private Color defaultBackground = new Color(0x3c, 0x1e, 0x00);
	private String channelName = "";

	WebChatLogView() {
		if (!isJavaFxAvailable()) {
			throw new UnsupportedOperationException("JavaFX modules not available");
		}

		setLayout(new BorderLayout());
		fxPanel = new JFXPanel();
		bridge = new FxBridge(fxPanel);
		add(fxPanel, BorderLayout.CENTER);

		installPopupMenu();
		refreshDocument();
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

		final String timestamp = TIMESTAMP_FORMAT.format(new Date());
		final String htmlLine = buildHtmlLine(timestamp, line);
		final String plainLine = buildPlainLine(timestamp, line);

		synchronized (htmlLines) {
			htmlLines.add(htmlLine);
			plainLines.add(plainLine);
		}

		refreshDocument();
	}

	@Override
	public void clear() {
		synchronized (htmlLines) {
			htmlLines.clear();
			plainLines.clear();
		}
		refreshDocument();
	}

	@Override
	public void setDefaultBackground(final Color color) {
		defaultBackground = (color != null) ? color : new Color(0x3c, 0x1e, 0x00);
		refreshDocument();
	}

	@Override
	public void setChannelName(final String name) {
		channelName = (name != null) ? name : "";
	}

	private void refreshDocument() {
		final String document = buildDocument();
		bridge.setContent(document);
	}

	private String buildDocument() {
		final StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>");
		builder.append("<style>").append(buildStylesheet()).append("</style></head><body class=\"chat-body\">");
		builder.append("<div id=\"chat\" class=\"chat-log\">");
		synchronized (htmlLines) {
			for (final String line : htmlLines) {
				builder.append(line);
			}
		}
		builder.append("</div>");
		builder.append("<script>window.setTimeout(function(){window.scrollTo(0, document.body.scrollHeight);},0);</script>");
		builder.append("</body></html>");
		return builder.toString();
	}

	private String buildStylesheet() {
		final StringBuilder css = new StringBuilder();
		final String background = toHex(defaultBackground);
		String emojiStack = buildEmojiFontStack(css);

		css.append("body.chat-body{margin:0;padding:0;background:")
			.append(background)
			.append(";color:#f3e9d6;font-family:'Segoe UI','Noto Sans',sans-serif;font-size:14px;}");
		css.append(".chat-log{padding:10px 16px;display:flex;flex-direction:column;gap:6px;}");
		css.append(".line{display:flex;flex-wrap:wrap;gap:6px;align-items:flex-start;padding:6px 8px;background:rgba(32,18,6,0.55);border-radius:6px;box-shadow:0 1px 0 rgba(0,0,0,0.35);}");
		css.append(".line.admin{border:1px solid rgba(255,220,120,0.85);background:rgba(58,34,4,0.7);}");
		css.append(".timestamp{font-family:'Consolas','Courier New',monospace;color:rgba(255,255,255,0.7);margin-right:6px;}");
		css.append(".header{color:#ffe9b0;font-weight:600;margin-right:6px;}");
		css.append(".message{flex:1 1 auto;white-space:pre-wrap;word-break:break-word;line-height:1.45;}");
		css.append(".line.type-error .message{color:#ffb4a2;}");
		css.append(".line.type-warning .message{color:#f5d491;}");
		css.append(".line.type-positive .message{color:#c9e5a3;}");
		css.append(".line.type-support .message{color:#b4e1ff;}");
		css.append(".line.type-client .message{color:#f2dec2;}");
		css.append(".emoji{font-family:").append(emojiStack).append(";font-size:1.1em;line-height:1.1em;}");
		return css.toString();
	}

	private String buildEmojiFontStack(final StringBuilder css) {
		String stack = "'Noto Color Emoji','Segoe UI Emoji','Apple Color Emoji','Twitter Color Emoji','EmojiOne Color',sans-serif";
		final String bundled = EmojiStore.get().getBundledFontDataUrl();
		if ((bundled != null) && !bundled.isEmpty()) {
			css.append("@font-face{font-family:'BundledEmoji';src:url(")
				.append(bundled)
				.append(") format('truetype');font-display:swap;}");
			stack = "'BundledEmoji'," + stack;
		}
		final String primary = EmojiStore.getFontFamily();
		if ((primary != null) && !primary.isEmpty()) {
			stack = "'" + escapeFontFamily(primary) + "'," + stack;
		}
		return stack;
	}

	private String buildHtmlLine(final String timestamp, final EventLine line) {
		final NotificationType type = (line != null) ? line.getType() : null;
		final boolean admin = isAdmin(type, line);
		final String cssClass = cssClassFor(type);

		final StringBuilder builder = new StringBuilder();
		builder.append("<div class=\"line");
		if (!cssClass.isEmpty()) {
			builder.append(' ').append(cssClass);
		}
		if (admin) {
			builder.append(" admin");
		}
		builder.append("\">");
		builder.append("<span class=\"timestamp\">").append(escapeHtml(timestamp)).append("</span>");

		final String header = (line != null) ? line.getHeader() : null;
		if ((header != null) && !header.isEmpty()) {
			builder.append("<span class=\"header\">").append(escapeHtml(header)).append("</span>");
		}

		final String text = (line != null) ? line.getText() : "";
		builder.append("<span class=\"message\">").append(formatMessageText(text)).append("</span>");
		builder.append("</div>");
		return builder.toString();
	}

	private String formatMessageText(final String text) {
		if ((text == null) || text.isEmpty()) {
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		final EmojiStore store = EmojiStore.get();
		int index = 0;
		while (index < text.length()) {
			final EmojiMatch match = store.matchEmoji(text, index);
			if (match != null) {
				final String glyph = (match.getGlyph() != null) ? match.getGlyph() : text.substring(index, index + match.getConsumedLength());
				builder.append("<span class=\"emoji\">").append(escapeHtml(glyph)).append("</span>");
				index += match.getConsumedLength();
				continue;
			}
			final int codePoint = text.codePointAt(index);
			appendEscapedCodePoint(builder, codePoint);
			index += Character.charCount(codePoint);
		}
		return builder.toString().replace("\n", "<br/>");
	}

	private void appendEscapedCodePoint(final StringBuilder builder, final int codePoint) {
		switch (codePoint) {
		case '<':
			builder.append("&lt;");
			break;
		case '>':
			builder.append("&gt;");
			break;
		case '"':
			builder.append("&quot;");
			break;
		case '&':
			builder.append("&amp;");
			break;
		case '\'':
			builder.append("&#39;");
			break;
		default:
			builder.append(Character.toChars(codePoint));
			break;
		}
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

	private String cssClassFor(final NotificationType type) {
		if (type == null) {
			return "";
		}
		switch (type) {
		case ERROR:
		case NEGATIVE:
		case DAMAGE:
		case POISON:
		case SIGNIFICANT_NEGATIVE:
			return "type-error";
		case WARNING:
			return "type-warning";
		case POSITIVE:
		case HEAL:
		case SIGNIFICANT_POSITIVE:
			return "type-positive";
		case SUPPORT:
		case RESPONSE:
			return "type-support";
		case CLIENT:
		case INFORMATION:
			return "type-client";
		default:
			return "";
		}
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

	private void installPopupMenu() {
		final JPopupMenu popup = new JPopupMenu();

		final JMenuItem saveItem = new JMenuItem("Zapisz");
		saveItem.addActionListener(e -> saveLog());
		popup.add(saveItem);

		final JMenuItem clearItem = new JMenuItem("Wyczyść");
		clearItem.addActionListener(e -> clear());
		popup.add(clearItem);

		fxPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				maybeShowPopup(e);
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}

	private void saveLog() {
		final String fileName = getSaveFileName();
		Writer writer = null;
		try {
			final File target = new File(fileName);
			final File parent = target.getParentFile();
			if ((parent != null) && !parent.exists()) {
				parent.mkdirs();
			}
			writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8);
			synchronized (htmlLines) {
				for (final String line : plainLines) {
					writer.write(line);
					writer.write(System.lineSeparator());
				}
			}
		} catch (final IOException ex) {
			LOGGER.error("Failed to save chat log", ex);
			return;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (final IOException ex) {
					LOGGER.warn("Failed to close chat log writer", ex);
				}
			}
		}

		addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + fileName, NotificationType.CLIENT));
	}

	private String getSaveFileName() {
		final String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		final StringBuilder builder = new StringBuilder();
		final String character = UserContext.get().getName();
		if ((character != null) && !character.isEmpty()) {
			builder.append(character).append('_');
		}
		if ((channelName != null) && !channelName.isEmpty()) {
			builder.append(channelName).append('_');
		}
		builder.append(timestamp).append('.').append("log");
		return stendhal.getGameFolder() + "chat/" + builder.toString();
	}

	private static boolean isJavaFxAvailable() {
		try {
			Class.forName("javafx.embed.swing.JFXPanel");
			Class.forName("javafx.scene.web.WebView");
			return true;
		} catch (final ClassNotFoundException ex) {
			return false;
		}
	}

	private static String escapeHtml(final String text) {
		if (text == null) {
			return "";
		}
		final StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			switch (ch) {
			case '<':
				builder.append("&lt;");
				break;
			case '>':
				builder.append("&gt;");
				break;
			case '"':
				builder.append("&quot;");
				break;
			case '&':
				builder.append("&amp;");
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

	private static String escapeFontFamily(final String family) {
		return (family == null) ? "" : family.replace("'", "\\'");
	}

	private static String toHex(final Color color) {
		final Color effective = (color != null) ? color : new Color(0x3c, 0x1e, 0x00);
		return String.format(Locale.ROOT, "#%02x%02x%02x", effective.getRed(), effective.getGreen(), effective.getBlue());
	}

	private static final class FxBridge {
		private final JFXPanel panel;
		private WebEngine engine;
		private boolean initialized;
		private String pendingContent;

		private FxBridge(final JFXPanel panel) {
			this.panel = panel;
			Platform.setImplicitExit(false);
			Platform.runLater(this::init);
		}

		private void init() {
			final WebView webView = new WebView();
			webView.setContextMenuEnabled(false);
			engine = webView.getEngine();
			engine.setJavaScriptEnabled(true);
			engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == Worker.State.SUCCEEDED) {
					engine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
				}
			});

			final BorderPane root = new BorderPane(webView);
			final Scene scene = new Scene(root);
			panel.setScene(scene);
			initialized = true;

			if (pendingContent != null) {
				engine.loadContent(pendingContent, "text/html");
				pendingContent = null;
			}
		}

		private void setContent(final String html) {
			Platform.runLater(() -> {
				if (!initialized) {
					pendingContent = html;
					return;
				}
				engine.loadContent(html, "text/html");
			});
		}
	}
}
