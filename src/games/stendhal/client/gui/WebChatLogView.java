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
import games.stendhal.client.gui.textformat.AttributedTextSink;
import games.stendhal.client.gui.textformat.CssClassSet;
import games.stendhal.client.gui.textformat.StringFormatter;
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
	private static final char EMOJI_PLACEHOLDER_PREFIX = '\uFFF0';

	private static final StringFormatter<java.util.Set<String>, CssClassSet> FORMATTER
	= new StringFormatter<java.util.Set<String>, CssClassSet>();
	private static final CssClassSet DEFAULT_FRAGMENT = new CssClassSet().addClass("fragment");

	static {
		FORMATTER.addStyle('#', style("fragment", "fmt-hash"));
		FORMATTER.addStyle('§', style("fragment", "fmt-section"));
		FORMATTER.addStyle('~', style("fragment", "fmt-tilde"));
		FORMATTER.addStyle('¡', style("fragment", "fmt-admin"));
	}

	private final List<String> htmlLines = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();

	private final JFXPanel fxPanel;
	private final FxBridge bridge;

	private Color defaultBackground = new Color(0x3c, 0x1e, 0x00);
	private String channelName = "";

	private static CssClassSet style(final String... classes) {
		final CssClassSet set = new CssClassSet();
		if (classes != null) {
			for (final String cssClass : classes) {
				set.addClass(cssClass);
			}
		}
		return set;
	}

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

		bridge.appendLine(htmlLine);
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
		builder.append("<script>(function(){var c=document.getElementById('chat');window.chatlog={container:c,appendLine:function(html){if(!this.container){return;}var wrapper=document.createElement('div');wrapper.innerHTML=html;while(wrapper.firstChild){this.container.appendChild(wrapper.firstChild);}this.scrollToBottom();},clear:function(){if(this.container){this.container.innerHTML='';}},scrollToBottom:function(){if(this.container){this.container.scrollTop=this.container.scrollHeight;}window.scrollTo(0,document.body.scrollHeight);}};window.chatlog.scrollToBottom();})();</script>");
		builder.append("</body></html>");
		return builder.toString();
	}

	private String buildStylesheet() {
		final StringBuilder css = new StringBuilder();
		final String background = toHex(defaultBackground);
		final String emojiStack = buildEmojiFontStack(css);

		css.append("body.chat-body{margin:0;padding:0;background:")
			.append(background)
			.append(";color:#f4edd9;font-family:'Segoe UI','Noto Sans',sans-serif;font-size:14px;}");
		css.append(".chat-log{padding:10px 14px;display:flex;flex-direction:column;gap:4px;max-height:100%;overflow-y:auto;}");
		css.append(".line{display:flex;flex-wrap:wrap;gap:6px;align-items:flex-start;padding:4px 0;border-bottom:1px solid rgba(255,255,255,0.08);}");
		css.append(".line:last-child{border-bottom:none;}");
		css.append(".line.admin{border-left:3px solid rgba(240,200,120,0.9);padding-left:7px;}");
		css.append(".timestamp{font-family:'Consolas','Courier New',monospace;color:rgba(255,255,255,0.65);margin-right:6px;}");
		css.append(".header{color:#ffe6a1;font-weight:600;margin-right:6px;}");
		css.append(".message{flex:1 1 auto;white-space:pre-wrap;word-break:break-word;line-height:1.45;}");
		css.append(".fragment{color:inherit;}");
		css.append(".fmt-hash{color:#5aaaff;font-style:italic;font-weight:600;}");
		css.append(".fmt-section{text-decoration:underline;}");
		css.append(".fmt-tilde{color:#fe4c4c;text-decoration:underline;}");
		css.append(".fmt-admin{color:#e1b941;font-style:italic;}");
		css.append(".line.type-error .message{color:#ffb4a2;}");
		css.append(".line.type-warning .message{color:#f5d491;}");
		css.append(".line.type-positive .message{color:#c9e5a3;}");
		css.append(".line.type-support .message{color:#b4e1ff;}");
		css.append(".line.type-client .message{color:#f2dec2;}");
		css.append(".emoji{font-family:").append(emojiStack).append(";font-size:1.1em;line-height:1.1em;}");
		css.append(".emoji-span{display:inline-flex;align-items:center;gap:2px;}");
		css.append(".emoji-icon{height:1.25em;width:1.25em;vertical-align:middle;}");
		css.append(".chat-log{scrollbar-color:#8a5f34 #2c1503;}");
		css.append(".chat-log::-webkit-scrollbar{width:18px;background:#2c1503;}");
		css.append(".chat-log::-webkit-scrollbar-track{background:linear-gradient(180deg,#452308 0%,#331703 50%,#452308 100%);border-left:1px solid rgba(255,230,170,0.25);border-right:1px solid rgba(0,0,0,0.45);box-shadow:inset 0 0 4px rgba(0,0,0,0.6);}");
		css.append(".chat-log::-webkit-scrollbar-thumb{background:linear-gradient(180deg,#9a6d3a 0%,#7f4f20 50%,#9a6d3a 100%);border:1px solid rgba(60,30,0,0.85);border-radius:6px;box-shadow:inset 0 0 4px rgba(0,0,0,0.5);}");
		css.append(".chat-log::-webkit-scrollbar-thumb:hover{background:linear-gradient(180deg,#b78043 0%,#8f5a27 50%,#b78043 100%);}");
		css.append(".chat-log::-webkit-scrollbar-button{height:18px;background:linear-gradient(180deg,#4d2608 0%,#331703 100%);border:1px solid rgba(60,30,0,0.8);box-shadow:inset 0 0 3px rgba(0,0,0,0.6);}");
		css.append(".chat-log::-webkit-scrollbar-button:single-button:vertical:decrement{background-image:linear-gradient(180deg,rgba(255,230,170,0.45) 0%,rgba(255,230,170,0.1) 100%),url('data:image/svg+xml;base64,PHN2ZyBmaWxsPSIjZmZlNmE0IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMiIgaGVpZ2h0PSIxMiI+PHBhdGggZD0iTTYgMy4ybC0zLjQgNS42aDYuOHoiLz48L3N2Zz4=');background-repeat:no-repeat;background-position:center;background-size:12px 12px;}");
		css.append(".chat-log::-webkit-scrollbar-button:single-button:vertical:increment{background-image:linear-gradient(180deg,rgba(0,0,0,0.35) 0%,rgba(0,0,0,0.65) 100%),url('data:image/svg+xml;base64,PHN2ZyBmaWxsPSIjZmZlNmE0IiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMiIgaGVpZ2h0PSIxMiI+PHBhdGggZD0iTTIuNiAzLjRoNi44TDYgOS4yWiIvPjwvc3ZnPg==');background-repeat:no-repeat;background-position:center;background-size:12px 12px;}");
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

		final List<EmojiReplacement> replacements = new ArrayList<>();
		final String working = injectEmojiPlaceholders(text, replacements);
		final HtmlFragmentBuilder builder = new HtmlFragmentBuilder();
		FORMATTER.format(working, DEFAULT_FRAGMENT.copy(), builder);
		String html = builder.toHtml();
		for (final EmojiReplacement replacement : replacements) {
			html = html.replace(replacement.placeholder, replacement.html);
		}
		return html;
	}

	private String injectEmojiPlaceholders(final String text, final List<EmojiReplacement> replacements) {
		final EmojiStore store = EmojiStore.get();
		final StringBuilder working = new StringBuilder();
		int index = 0;
		int counter = 0;
		while (index < text.length()) {
			final EmojiMatch match = store.matchEmoji(text, index);
			if (match != null) {
				final int consumed = match.getConsumedLength();
				final String token = text.substring(index, index + consumed);
				final String placeholder = buildEmojiPlaceholder(counter++);
				replacements.add(new EmojiReplacement(placeholder, buildEmojiHtml(match, token)));
				working.append(placeholder);
				index += consumed;
				continue;
			}
			final int codePoint = text.codePointAt(index);
			working.appendCodePoint(codePoint);
			index += Character.charCount(codePoint);
		}
		return working.toString();
	}

	private String buildEmojiPlaceholder(final int index) {
		return new StringBuilder(16).append(EMOJI_PLACEHOLDER_PREFIX).append(index).append(EMOJI_PLACEHOLDER_PREFIX).toString();
	}

	private String buildEmojiHtml(final EmojiMatch match, final String token) {
		final String glyph = ((match != null) && (match.getGlyph() != null)) ? match.getGlyph() : ((token != null) ? token : "");
		final String dataUrl = (token != null) ? EmojiStore.get().dataUrlFor(token) : null;
		if ((dataUrl != null) && !dataUrl.isEmpty()) {
			return "<span class=\"emoji-span\"><img class=\"emoji-icon\" src=\"" + dataUrl + "\" alt=\"" + escapeHtml(glyph) + "\"/></span>";
		}
		return "<span class=\"emoji\">" + escapeHtml(glyph) + "</span>";
	}

	private static final class EmojiReplacement {
		private final String placeholder;
		private final String html;

		private EmojiReplacement(final String placeholder, final String html) {
			this.placeholder = placeholder;
			this.html = html;
		}
	}

	private static final class HtmlFragmentBuilder implements AttributedTextSink<CssClassSet> {
		private final StringBuilder out = new StringBuilder();

		@Override
		public void append(final String s, final CssClassSet attrs) {
			if ((s == null) || s.isEmpty()) {
				return;
			}
			final String escaped = escapeHtml(s).replace("\n", "<br/>");
			final String classes = (attrs != null) ? attrs.classString() : "";
			if ((classes != null) && !classes.isEmpty()) {
				out.append("<span class=\"").append(classes).append("\">")
				.append(escaped)
				.append("</span>");
			} else {
				out.append(escaped);
			}
		}

		String toHtml() {
			return out.toString();
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
		private boolean documentReady;
		private String pendingContent;
		private final List<String> pendingAppends = new ArrayList<String>();

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
					documentReady = true;
					flushPendingAppends();
				}
			});

			final BorderPane root = new BorderPane(webView);
			final Scene scene = new Scene(root);
			panel.setScene(scene);
			initialized = true;

			if (pendingContent != null) {
				documentReady = false;
				pendingAppends.clear();
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
				documentReady = false;
				pendingAppends.clear();
				engine.loadContent(html, "text/html");
			});
		}

		void appendLine(final String html) {
			if ((html == null) || html.isEmpty()) {
				return;
			}
			Platform.runLater(() -> {
				if (!initialized || !documentReady) {
					pendingAppends.add(html);
					return;
				}
				final StringBuilder script = new StringBuilder();
				script.append("if(window.chatlog){window.chatlog.appendLine(")
					.append(toJsString(html))
					.append(");window.chatlog.scrollToBottom();}");
				engine.executeScript(script.toString());
			});
		}

		private void flushPendingAppends() {
			if (engine == null) {
				pendingAppends.clear();
				return;
			}
			final StringBuilder script = new StringBuilder("if(window.chatlog){");
				for (final String line : pendingAppends) {
					script.append("window.chatlog.appendLine(")
					.append(toJsString(line))
					.append(");");
				}
				script.append("window.chatlog.scrollToBottom();}");
			pendingAppends.clear();
			engine.executeScript(script.toString());
		}

		private static String toJsString(final String value) {
			final StringBuilder out = new StringBuilder();
			out.append('"');
			if (value != null) {
				for (int i = 0; i < value.length(); i++) {
					final char ch = value.charAt(i);
					switch (ch) {
						case '\\':
						out.append("\\\\");
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
			out.append('"');
			return out.toString();
		}
	}
}
