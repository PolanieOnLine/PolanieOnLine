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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
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
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.apache.log4j.Logger;

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
* Chat log backed by JavaFX WebView when the JavaFX runtime is available. The
* implementation interacts with JavaFX through reflection so the client keeps
* compiling on systems without JavaFX modules.
*/
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 4101968159599867093L;

	private static final Logger LOGGER = Logger.getLogger(WebChatLogView.class);
	private static final Color DEFAULT_BACKGROUND = new Color(60, 30, 0);
	private static final String EMOJI_FONT_STACK = "'Segoe UI Emoji','Apple Color Emoji','Noto Color Emoji','Twitter Color Emoji','EmojiOne Color','Android Emoji','Noto Emoji',sans-serif";
	private static final String DISCORD_FONT_STACK = "'Whitney','Helvetica Neue','Helvetica','Arial',sans-serif";
	private static final String MESSAGE_TEMPLATE = "<article class=\\"message type-%s%s\\"><div class=\\"header\\">%s<span class=\\"author\\">%s</span><span class=\\"timestamp\\">%s</span></div><div class=\\"body\\">%s</div></article>";
	private static final String ADMIN_BADGE = "<span class=\\"admin-badge\\">ADMIN</span>";
	private static final long FX_INIT_TIMEOUT_SECONDS = 10L;

	private final Format dateFormatter = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());
	private final StringFormatter<Style, StyleSet> formatter = new StringFormatter<>();
	private final StyleContext styleContext = new StyleContext();
	private final Style regularStyle;
	private final Style italicStyle;
	private final Style underlineStyle;
	private final Style adminStyle;
	private final Style boldStyle;
	private final Style emojiStyle;
	private final FxBridge bridge;
	private final List<String> htmlLines = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();

	private Color defaultBackground = DEFAULT_BACKGROUND;
	private String channelName = "";

	WebChatLogView() throws Exception {
		bridge = FxBridge.tryCreate();
		if (bridge == null) {
			throw new UnsupportedOperationException("JavaFX modules not available");
		}

		setLayout(new BorderLayout());
		add(bridge.getComponent(), BorderLayout.CENTER);

		Font baseFont = UIManager.getFont("Label.font");
		int fontSize = (baseFont != null) ? Math.max(8, baseFont.getSize() - 1) : 12;

		regularStyle = styleContext.addStyle("regular", null);
		StyleConstants.setFontFamily(regularStyle, "Dialog");
		StyleConstants.setFontSize(regularStyle, fontSize);
		StyleConstants.setForeground(regularStyle, Color.WHITE);

		italicStyle = styleContext.addStyle("italic", regularStyle);
		StyleConstants.setItalic(italicStyle, true);
		StyleConstants.setForeground(italicStyle, new Color(65, 105, 225));

		underlineStyle = styleContext.addStyle("underline", regularStyle);
		StyleConstants.setUnderline(underlineStyle, true);

		adminStyle = styleContext.addStyle("admin", regularStyle);
		StyleConstants.setBold(adminStyle, true);
		StyleConstants.setForeground(adminStyle, new Color(225, 185, 65));

		boldStyle = styleContext.addStyle("bold", regularStyle);
		StyleConstants.setBold(boldStyle, true);
		StyleConstants.setForeground(boldStyle, new Color(90, 170, 255));
		StyleConstants.setFontSize(boldStyle, fontSize + 1);

		emojiStyle = styleContext.addStyle("emoji", regularStyle);
		StyleConstants.setFontFamily(emojiStyle, EmojiStore.getFontFamily());
		StyleConstants.setFontSize(emojiStyle, fontSize + 2);

		initFormatter();
		installPopupMenu();
		bridge.loadHtml(buildDocument(""), true);
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

		final NotificationType type = line.getType();
		final boolean admin = isAdminAlert(type);
		final String header = escape((line.getHeader() != null) ? line.getHeader() : "PolanieOnLine");
		final String timestamp = escape(dateFormatter.format(new Date()));
		final String body = formatBody(line, admin);

		final String article = String.format(Locale.ROOT, MESSAGE_TEMPLATE,
		cssClassFor(type), admin ? " admin" : "", admin ? ADMIN_BADGE : "", header, timestamp, body);

		synchronized (htmlLines) {
			htmlLines.add(article);
			plainLines.add(buildPlainLine(line));
		}

		updateDocument(true);
	}

	@Override
	public void clear() {
		synchronized (htmlLines) {
			htmlLines.clear();
			plainLines.clear();
		}
		updateDocument(false);
	}

	@Override
	public void setDefaultBackground(final Color color) {
		defaultBackground = (color != null) ? color : DEFAULT_BACKGROUND;
		updateDocument(false);
	}

	@Override
	public void setChannelName(final String name) {
		channelName = (name != null) ? name : "";
	}

	private void initFormatter() {
		StyleSet regular = new StyleSet(styleContext, regularStyle);
		StyleSet italic = new StyleSet(styleContext, italicStyle);
		StyleSet underline = new StyleSet(styleContext, underlineStyle);
		StyleSet admin = new StyleSet(styleContext, adminStyle);
		StyleSet bold = new StyleSet(styleContext, boldStyle);

		formatter.addStyle('#', italic);
		formatter.addStyle('§', underline);
		formatter.addStyle('¡', admin);
		formatter.addStyle('&', bold);
	}

	private void installPopupMenu() {
		final JPopupMenu popup = new JPopupMenu();

		JMenuItem item = new JMenuItem("Zapisz");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				save();
			}
		});
		popup.add(item);

		item = new JMenuItem("Wyczyść");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				clear();
			}
		});
		popup.add(item);

		bridge.getComponent().addMouseListener(new MousePopupAdapter() {
			@Override
			protected void showPopup(final MouseEvent e) {
				popup.show((Component) e.getSource(), e.getX(), e.getY());
			}
		});
	}

	private String formatBody(final EventLine line, final boolean admin) {
		final StringBuilder html = new StringBuilder();
		final HtmlSink sink = new HtmlSink(html, admin);
		final StyleSet base = new StyleSet(styleContext, regularStyle);
		formatter.format(line.getText(), base, sink);
		return html.toString();
	}

	private void updateDocument(final boolean autoScroll) {
		final String content;
		synchronized (htmlLines) {
			StringBuilder builder = new StringBuilder();
			for (String line : htmlLines) {
				builder.append(line);
			}
			content = builder.toString();
		}
		bridge.loadHtml(buildDocument(content), autoScroll);
	}

	private String buildPlainLine(final EventLine line) {
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormatter.format(new Date()));
		if (!(line instanceof HeaderLessEventLine) && (line.getHeader() != null) && !line.getHeader().isEmpty()) {
			sb.append(line.getHeader()).append(": ");
		}
		sb.append(line.getText());
		return sb.toString();
	}

	private String buildDocument(final String body) {
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
		String dataUrl = EmojiStore.get().getBundledFontDataUrl();
		if (dataUrl != null) {
			html.append("<style>@font-face{font-family:'Noto Color Emoji';src:url(").append(dataUrl).append(") format('truetype');}</style>");
		}
		html.append("<style>body{margin:0;padding:12px;background:")
			.append(cssColor(defaultBackground))
			.append(";color:#fff;font-family:")
			.append(DISCORD_FONT_STACK)
			.append(";font-size:14px;} .message{background:rgba(0,0,0,0.3);border-radius:8px;padding:8px 12px;margin-bottom:6px;}" +
		".message .header{display:flex;gap:6px;align-items:center;margin-bottom:4px;}" +
		".author{font-weight:600;} .timestamp{color:rgba(255,255,255,0.6);font-size:12px;}" +
		".body{white-space:pre-wrap;word-wrap:break-word;font-family:" + DISCORD_FONT_STACK + ";}" +
		".message.type-alert{border-left:3px solid #e67e22;}" +
		".message.type-server{border-left:3px solid #3498db;}" +
		".message.type-admin{border-left:3px solid #e74c3c;}" +
		".emoji{font-family:" + EMOJI_FONT_STACK + ";font-size:16px;}" +
		".admin-text{font-weight:700;color:#e1b941;}" +
		".admin-badge{background:#e74c3c;color:#fff;font-size:11px;font-weight:700;padding:1px 6px;border-radius:4px;margin-right:4px;}" +
		"</style></head><body>");
		html.append("<div id=\"chat\">").append(body).append("</div>");
		html.append("<script>const root=document.getElementById('chat');" +
		"function scrollToBottom(){window.scrollTo(0,document.body.scrollHeight);}" +
		"scrollToBottom();</script></body></html>");
		return html.toString();
	}

	private static String cssColor(final Color color) {
		Color c = (color != null) ? color : DEFAULT_BACKGROUND;
		return String.format(Locale.ROOT, "rgb(%d,%d,%d)", c.getRed(), c.getGreen(), c.getBlue());
	}

	private static boolean isAdminAlert(final NotificationType type) {
		return type == NotificationType.ADMIN || type == NotificationType.SUPPORT;
	}

	private static String cssClassFor(final NotificationType type) {
		if (type == null) {
			return "normal";
		}
		switch (type) {
		case ADMIN:
		case SUPPORT:
			return "admin";
		case SERVER:
			return "server";
		case ALERT:
			return "alert";
		default:
			return "normal";
		}
	}

	private static String escape(final String text) {
		if (text == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
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
			}
		}
		return builder.toString();
	}

	private void save() {
		String filename = buildLogFileName();
		File target = new File(filename);
		File parent = target.getParentFile();
		if ((parent != null) && !parent.exists()) {
			parent.mkdirs();
		}

		try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8)) {
			synchronized (htmlLines) {
				for (String line : plainLines) {
					writer.write(line);
					writer.write(System.lineSeparator());
				}
			}
			addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + target.getAbsolutePath(), NotificationType.CLIENT));
		} catch (IOException ex) {
			LOGGER.error("Failed to save chat log", ex);
		}
	}

	private String buildLogFileName() {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String name = (UserContext.get() != null) ? UserContext.get().getName() : null;
		StringBuilder builder = new StringBuilder();
		if ((name != null) && !name.isEmpty()) {
			builder.append(name).append('_');
		}
		if ((channelName != null) && !channelName.isEmpty()) {
			builder.append(channelName).append('_');
		}
		builder.append(timestamp).append(".log");
		return stendhal.getGameFolder() + "chat/" + builder.toString();
	}

	private final class HtmlSink implements AttributedTextSink<StyleSet> {
		private final StringBuilder html;
		private final boolean admin;

		HtmlSink(final StringBuilder html, final boolean admin) {
			this.html = html;
			this.admin = admin;
		}

		@Override
		public void append(final String value, final StyleSet styleSet) {
			if ((value == null) || value.isEmpty()) {
				return;
			}

			final javax.swing.text.AttributeSet attrs = styleSet.contents();
			final boolean italic = StyleConstants.isItalic(attrs);
			final boolean underline = StyleConstants.isUnderline(attrs);
			final boolean bold = StyleConstants.isBold(attrs);
			final Color fg = StyleConstants.getForeground(attrs);
			final String family = StyleConstants.getFontFamily(attrs);

			final StringBuilder style = new StringBuilder();
			final List<String> classes = new ArrayList<>();

			if (italic) {
				style.append("font-style:italic;");
			}
			if (underline) {
				style.append("text-decoration:underline;");
			}
			if (bold) {
				style.append("font-weight:bold;");
			}
			if (fg != null) {
				style.append("color:").append(cssColor(fg)).append(';');
			}
			if ((family != null) && family.equals(EmojiStore.getFontFamily())) {
				classes.add("emoji");
			}
			if (admin && classes.isEmpty() && (style.length() == 0)) {
				classes.add("admin-text");
			}

			final String escaped = escape(value);
			if (classes.isEmpty() && (style.length() == 0)) {
				html.append(escaped);
				return;
			}

			html.append("<span");
			if (!classes.isEmpty()) {
				html.append(" class=\"").append(String.join(" ", classes)).append("\"");
			}
			if (style.length() > 0) {
				html.append(" style=\"").append(style).append("\"");
			}
			html.append('>').append(escaped).append("</span>");
		}
	}

	private static final class FxBridge {
		private final JComponent component;
		private final Method runLater;
		private final Object webEngine;
		private final Method loadContent;
		private final Method executeScript;

		private FxBridge(final JComponent component, final Method runLater, final Object webEngine,
		final Method loadContent, final Method executeScript) {
			this.component = component;
			this.runLater = runLater;
			this.webEngine = webEngine;
			this.loadContent = loadContent;
			this.executeScript = executeScript;
		}

		static FxBridge tryCreate() {
			try {
				Class<?> panelClass = Class.forName("javafx.embed.swing.JFXPanel");
				Class<?> platformClass = Class.forName("javafx.application.Platform");
				Class<?> webViewClass = Class.forName("javafx.scene.web.WebView");
				Class<?> sceneClass = Class.forName("javafx.scene.Scene");
				Class<?> parentClass = Class.forName("javafx.scene.Parent");

				JComponent panel = (JComponent) panelClass.getConstructor().newInstance();
				Method runLater = platformClass.getMethod("runLater", Runnable.class);
				Method setScene = panelClass.getMethod("setScene", sceneClass);
				final Method getEngine = webViewClass.getMethod("getEngine");
				final Constructor<?> sceneCtor = sceneClass.getConstructor(parentClass);
				final Method loadContent = Class.forName("javafx.scene.web.WebEngine").getMethod("loadContent", String.class);
				final Method executeScript = Class.forName("javafx.scene.web.WebEngine").getMethod("executeScript", String.class);

				final Object[] engineHolder = new Object[1];
				final Exception[] errorHolder = new Exception[1];
				final CountDownLatch latch = new CountDownLatch(1);

				Runnable init = new Runnable() {
					@Override
					public void run() {
						try {
							Object webView = webViewClass.getConstructor().newInstance();
							Object scene = sceneCtor.newInstance(webView);
							setScene.invoke(panel, scene);
							engineHolder[0] = getEngine.invoke(webView);
						} catch (Exception ex) {
							errorHolder[0] = ex;
						} finally {
							latch.countDown();
						}
					}
				};

				runLater.invoke(null, init);
				if (!latch.await(FX_INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					LOGGER.warn("JavaFX initialization timed out");
					return null;
				}
				if (errorHolder[0] != null) {
					LOGGER.warn("Failed to initialize JavaFX WebView", errorHolder[0]);
					return null;
				}
				return new FxBridge(panel, runLater, engineHolder[0], loadContent, executeScript);
			} catch (ClassNotFoundException ex) {
				return null;
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException |
			SecurityException ex) {
				LOGGER.warn("JavaFX WebView unavailable", ex);
				return null;
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return null;
			}
		}

		JComponent getComponent() {
			return component;
		}

		void loadHtml(final String html, final boolean autoScroll) {
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						loadContent.invoke(webEngine, html);
						if (autoScroll) {
							executeScript.invoke(webEngine, "window.setTimeout(function(){window.scrollTo(0, document.body.scrollHeight);},0);");
						}
					} catch (IllegalAccessException | InvocationTargetException ex) {
						LOGGER.error("Failed to update WebView", ex);
					}
				}
			};
			try {
				runLater.invoke(null, task);
			} catch (IllegalAccessException | InvocationTargetException ex) {
				LOGGER.error("Failed to schedule WebView update", ex);
			}
		}
	}
}
