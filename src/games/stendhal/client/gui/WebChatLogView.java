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
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

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
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
* Chat log implementation backed by a JavaFX WebView. Messages are rendered as
* HTML so color emoji fonts are respected.
*/
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 4101968159599867093L;

	private static final Logger logger = Logger.getLogger(WebChatLogView.class);

	/** Default chat background. */
	private static final Color DEFAULT_BACKGROUND = new Color(60, 30, 0);

	/** Timestamp format. */
	private final Format dateFormatter = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());

	/** Formatter for markup text. */
	private final StringFormatter<Style, StyleSet> formatter = new StringFormatter<Style, StyleSet>();

	/** Style context used for building HTML spans. */
	private final StyleContext styleContext = new StyleContext();

	/** Base styles matching the legacy Swing chat rendering. */
	private final Style regularStyle;
	private final Style headerStyle;
	private final Style timestampStyle;
	private final Style boldStyle;
	private final Style emojiStyle;

	/** JavaFX bridge. */
	private final JFXPanel fxPanel = new JFXPanel();
	private WebView webView;
	private WebEngine webEngine;

	/** Cached chat lines in HTML and plain text form. */
	private final List<String> htmlLines = new ArrayList<String>();
	private final List<String> plainLines = new ArrayList<String>();

	/** State holders. */
	private volatile boolean autoScroll = true;
	private volatile boolean unread;
	private Color defaultBackground = DEFAULT_BACKGROUND;
	private String channelName = "";

	/** Guard to avoid repeated scroll-bar lookup. */
	private final AtomicBoolean scrollSetup = new AtomicBoolean(false);

	WebChatLogView() {
		setLayout(new BorderLayout());
		add(fxPanel, BorderLayout.CENTER);
		fxPanel.setFocusable(false);

		Font baseFont = UIManager.getFont("Label.font");
		int mainSize = (baseFont != null) ? Math.max(8, baseFont.getSize() - 1) : 12;

		regularStyle = styleContext.addStyle("regular", null);
		StyleConstants.setFontFamily(regularStyle, "Dialog");
		StyleConstants.setFontSize(regularStyle, mainSize);

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

		Platform.runLater(() -> initializeFxScene());
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

	private void initializeFxScene() {
		webView = new WebView();
		webView.setContextMenuEnabled(false);
		webEngine = webView.getEngine();
		webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
			if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
				exposeBridge();
				if (autoScroll) {
					scrollToBottom();
				}
			}
		});

		fxPanel.setScene(new Scene(webView));
		refreshHtml();
		setupScrollTracking();
	}

	private void setupScrollTracking() {
		if (!scrollSetup.compareAndSet(false, true)) {
			return;
		}

		Platform.runLater(() -> {
			if (webView == null) {
				return;
			}
			for (javafx.scene.Node node : webView.lookupAll(".scroll-bar")) {
				if (node instanceof ScrollBar) {
					ScrollBar bar = (ScrollBar) node;
					if (bar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
						bar.valueProperty().addListener((o, oldVal, newVal) -> {
							boolean atBottom = newVal.doubleValue() >= bar.getMax();
							autoScroll = atBottom;
							if (atBottom) {
								setUnreadLinesWarning(false);
							}
						});
					}
				}
			}
		});
	}

	private void exposeBridge() {
		try {
			JSObject window = (JSObject) webEngine.executeScript("window");
			window.setMember("chatBridge", new ChatBridge());
		webEngine.executeScript("if (typeof chatInit === 'function') { chatInit(); }");
	} catch (RuntimeException ex) {
	logger.warn("Failed to expose chat bridge", ex);
}
}

@Override
public void setFont(Font font) {
	super.setFont(font);
	if (font == null) {
		return;
	}
	int mainSize = Math.max(8, font.getSize() - 1);
	StyleConstants.setFontSize(regularStyle, mainSize);
	StyleConstants.setFontSize(headerStyle, mainSize);
	StyleConstants.setFontSize(timestampStyle, Math.max(6, mainSize - 1));
	StyleConstants.setFontSize(boldStyle, mainSize + 1);
	StyleConstants.setFontSize(emojiStyle, mainSize + 2);
	refreshHtml();
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

	Runnable task = () -> appendLine(line.getHeader(), line.getText(), line.getType());
	if (SwingUtilities.isEventDispatchThread()) {
		task.run();
	} else {
	SwingUtilities.invokeLater(task);
}
}

@Override
public void clear() {
	Runnable task = () -> {
		htmlLines.clear();
		plainLines.clear();
		refreshHtml();
	};
	if (SwingUtilities.isEventDispatchThread()) {
		task.run();
	} else {
	SwingUtilities.invokeLater(task);
}
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

private void appendLine(String header, String text, NotificationType type) {
	insertNewLine(header, text, type);
	refreshHtml();
	if (!autoScroll) {
		setUnreadLinesWarning(true);
	}
}

private void insertNewLine(String header, String text, NotificationType type) {
	String timestamp = dateFormatter.format(new Date());
	StringBuilder html = new StringBuilder();
	html.append("<div class="chat-line">");
	html.append("<span class="chat-timestamp">");
	html.append(escapeHtml(timestamp));
	html.append("</span>");

	if ((header != null) && !header.isEmpty()) {
		html.append("<span class="chat-header">");
		html.append(escapeHtml("<" + header + ">"));
		html.append("</span>");
	}

	NotificationType effectiveType = (type != null) ? type : NotificationType.CLIENT;
	String rendered = renderText(text, effectiveType);
	String color = toCssColor(effectiveType.getColor());
	html.append("<span class="chat-message" style="color:");
	html.append(color);
	html.append(";">");
	html.append(rendered);
	html.append("</span></div>");

	htmlLines.add(html.toString());
	plainLines.add(buildPlainLine(timestamp, header, text));
}

private String renderText(String text, NotificationType type) {
	if (text == null) {
		return "";
	}
	String normalized = text;
	Style baseStyle = regularStyle;
	if (type != null) {
		if (NotificationType.EMOJI.equals(type)) {
			normalized = normalizeEmojiText(text);
			baseStyle = emojiStyle;
		} else {
		String description = type.getStyleDescription();
		if (NotificationType.NORMALSTYLE.equals(description)) {
			baseStyle = boldStyle;
		} else if (NotificationType.REGULAR.equals(description)) {
		baseStyle = regularStyle;
	}
}
}

StyleSet base = new StyleSet(styleContext, baseStyle);
if (type != null) {
	base.setAttribute(StyleConstants.Foreground, type.getColor());
}

HtmlTextSink sink = new HtmlTextSink();
formatter.format(normalized, base, sink);
return sink.toHtml();
}

private String normalizeEmojiText(final String text) {
	if (text == null) {
		return null;
	}
	final String trimmed = text.trim();
	if (trimmed.isEmpty()) {
		return trimmed;
	}

	final EmojiStore store = EmojiStore.get();
	if (store.check(trimmed) != null) {
		return trimmed;
	}

	String normalized = trimmed.replace('\', '/');
	final int slash = normalized.lastIndexOf('/');
	if (slash != -1) {
		normalized = normalized.substring(slash + 1);
	}
	final int dot = normalized.lastIndexOf('.');
	if (dot != -1) {
		normalized = normalized.substring(0, dot);
	}

	if (normalized.isEmpty()) {
		return trimmed;
	}

	if (!normalized.startsWith(":")) {
		normalized = ":" + normalized;
	}
	if (!normalized.endsWith(":")) {
		normalized = normalized + ":";
	}

	if ((store.check(normalized) != null) || store.isAvailable(normalized)) {
		return normalized;
	}

	return trimmed;
}

private String buildPlainLine(String timestamp, String header, String text) {
	StringBuilder sb = new StringBuilder();
	sb.append(timestamp);
	if ((header != null) && !header.isEmpty()) {
		sb.append("<").append(header).append("> ");
	}
	sb.append(text != null ? text : "");
	return sb.toString();
}

private void refreshHtml() {
	if (webEngine == null) {
		return;
	}
	String html = buildHtmlDocument();
	Platform.runLater(() -> webEngine.loadContent(html, "text/html"));
}

private void scrollToBottom() {
	if (webEngine == null) {
		return;
	}
	try {
		webEngine.executeScript("window.scrollTo(0, document.body.scrollHeight);");
	} catch (RuntimeException ex) {
	logger.debug("Failed to scroll chat", ex);
}
}

private String buildHtmlDocument() {
	StringBuilder sb = new StringBuilder();
	sb.append("<!DOCTYPE html><html><head><meta charset="UTF-8"><style>");
	sb.append(generateCss());
	sb.append("</style><script>");
sb.append("function chatInit(){document.addEventListener('click',function(e){var target=e.target.closest('[data-link]');if(target){e.preventDefault();if(window.chatBridge){window.chatBridge.openLink(target.getAttribute('data-link'));}}});}");
sb.append("</script></head><body class="chat-body");
if (unread) {
	sb.append(" unread");
}
sb.append(""><div class="chat-log">");
for (String line : htmlLines) {
	sb.append(line);
}
sb.append("</div></body></html>");
return sb.toString();
}

private String generateCss() {
	StringBuilder css = new StringBuilder();
	css.append("body.chat-body{margin:0;padding:0;background:");
		css.append(toCssColor(defaultBackground));
	css.append(";font-family:'Dialog',sans-serif;color:#f0f0f0;}");
css.append("body.chat-body.unread{background:#3c003c;}");
css.append(".chat-log{padding:6px;box-sizing:border-box;}");
css.append(".chat-line{display:flex;flex-wrap:wrap;gap:4px;margin-bottom:2px;}");
css.append(".chat-timestamp{color:#dcdcdc;font-style:italic;font-size:0.85em;white-space:pre;}");
css.append(".chat-header{color:#ffffdc;font-style:italic;white-space:pre;}");
css.append(".chat-message{flex:1 1 auto;white-space:pre-wrap;word-break:break-word;}");
css.append(".chat-fragment{white-space:pre-wrap;}");
css.append(".chat-fragment.emoji{font-family:'");
	css.append(EmojiStore.getFontFamily());
css.append("','Noto Color Emoji','Segoe UI Emoji','Apple Color Emoji','sans-serif';}");
css.append(".chat-link{text-decoration:underline;cursor:pointer;}");
return css.toString();
}

private String toCssColor(Color color) {
	if (color == null) {
		return "#ffffff";
	}
	return String.format(Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
}

private String escapeHtml(String text) {
	if (text == null) {
		return "";
	}
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < text.length(); i++) {
		char ch = text.charAt(i);
		switch (ch) {
			case '<':
			sb.append("&lt;");
			break;
			case '>':
			sb.append("&gt;");
			break;
			case '&':
			sb.append("&amp;");
			break;
			case ''':
			sb.append("&#39;");
			break;
			case '"':
			sb.append("&quot;");
			break;
			case '
			':
			sb.append("<br/>");
			break;
			case '
			':
			break;
			case '	':
			sb.append("&emsp;");
			break;
			default:
			sb.append(ch);
		}
	}
	return sb.toString();
}

private void setUnreadLinesWarning(boolean warn) {
	if (warn == unread) {
		return;
	}
	unread = warn;
	refreshHtml();
}

private void save() {
	String fname = getSaveFileName();
	File target = new File(fname);
	File parent = target.getParentFile();
	if (parent != null) {
		parent.mkdirs();
	}
	try (Writer out = new OutputStreamWriter(new FileOutputStream(target), java.nio.charset.StandardCharsets.UTF_8)) {
		for (String line : plainLines) {
			out.write(line);
			out.write(System.lineSeparator());
		}
		addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + fname, NotificationType.CLIENT));
	} catch (IOException ex) {
	logger.error(ex, ex);
}
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
	protected void showPopup(final MouseEvent e) {
		final JPopupMenu popup = new JPopupMenu("zapisz");

		JMenuItem saveItem = new JMenuItem("Zapisz");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				save();
			}
		});
		popup.add(saveItem);

		JMenuItem clearItem = new JMenuItem("Wyczyść");
		clearItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				clear();
			}
		});
		popup.add(clearItem);

		popup.show(e.getComponent(), e.getX(), e.getY());
	}
}

private class ChatBridge {
	public void openLink(String text) {
		if (text == null) {
			return;
		}
		LinkListener listener = new LinkListener();
		listener.linkClicked(text);
	}
}

private class LinkListener {
	final java.util.regex.Pattern whitelist = java.util.regex.Pattern.compile("^https?://stendhalgame\.org(/.*)*$");

	void linkClicked(String text) {
		if (!whitelist.matcher(text).matches()) {
			return;
		}
		SwingUtilities.invokeLater(() -> {
			addLine(new HeaderLessEventLine("Próbuję otworzyć #'" + text + "' w twojej przeglądarce.", NotificationType.CLIENT));
			BareBonesBrowserLaunch.openURL(text);
			ClientSingletonRepository.getChatTextController().setFocus();
		});
	}
}

private class HtmlTextSink implements AttributedTextSink<StyleSet> {
	private final StringBuilder builder = new StringBuilder();

	@Override
	public void append(String s, StyleSet attrs) {
		if ((s == null) || s.isEmpty()) {
			return;
		}
		Style style = attrs.contents();
		String text = escapeHtml(s);

		StringBuilder span = new StringBuilder();
		span.append("<span class="chat-fragment");

		Object fontFamily = StyleConstants.getFontFamily(style);
		boolean isEmoji = (fontFamily != null) && fontFamily.equals(EmojiStore.getFontFamily());
		if (isEmoji) {
			span.append(" emoji");
		}
		Object linkAttr = style.getAttribute("linkact");
		if (linkAttr != null) {
			span.append(" chat-link" data-link="").append(escapeAttribute(s.trim())).append(""");
		} else {
		span.append(""");
	}
	span.append(" style="");

		Color fg = StyleConstants.getForeground(style);
		if (fg != null) {
			span.append("color:").append(toCssColor(fg)).append(';');
		}
		int size = StyleConstants.getFontSize(style);
		if (size > 0) {
			span.append("font-size:").append(size).append("px;");
		}
		if (fontFamily != null) {
			span.append("font-family:'").append(fontFamily).append("','Noto Color Emoji','Segoe UI Emoji','Apple Color Emoji',sans-serif;");
		} else if (isEmoji) {
			span.append("font-family:'").append(EmojiStore.getFontFamily()).append("','Noto Color Emoji','Segoe UI Emoji','Apple Color Emoji',sans-serif;");
		}
		if (StyleConstants.isBold(style)) {
			span.append("font-weight:bold;");
		}
		if (StyleConstants.isItalic(style)) {
			span.append("font-style:italic;");
		}
		if (StyleConstants.isUnderline(style)) {
			span.append("text-decoration:underline;");
		}
		span.append("\">");
		span.append(text);
		span.append("</span>");
		builder.append(span);
}

String toHtml() {
	return builder.toString();
}

	private String escapeAttribute(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "&quot;");
	}
}
}
}
