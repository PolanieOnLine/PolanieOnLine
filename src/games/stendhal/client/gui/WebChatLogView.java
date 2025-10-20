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
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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

/** Font stack mimicking Discord's UI typography. */
private static final String DISCORD_FONT_STACK = "'Whitney','Helvetica Neue','Helvetica','Arial',sans-serif";

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

       /** Whether the logged in user has admin permissions. */
       private final boolean adminUser;

       /** Cached chat lines in HTML and plain text form. */
       private final List<String> htmlLines = new ArrayList<>();
       private final List<String> plainLines = new ArrayList<>();
       private final List<NotificationType> lineTypes = new ArrayList<>();

        /** Cached HTML fragment waiting for the WebView shell. */
        private String pendingHtml = "";

        /** State holders. */
private volatile boolean autoScroll = true;
private volatile boolean shellLoaded;
private volatile boolean moderatorMode;
private Color defaultBackground = DEFAULT_BACKGROUND;
private String channelName = "";

WebChatLogView() throws Exception {
fx = FxBridge.tryCreate();
if (fx == null) {
throw new UnsupportedOperationException("JavaFX modules not available");
}

UserContext context = UserContext.get();
adminUser = (context != null) && context.isAdmin();
moderatorMode = adminUser;

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
                                loadShellDocument();
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
lineTypes.add(line.getType());
}

refreshHtml();
}

@Override
public void clear() {
synchronized (htmlLines) {
htmlLines.clear();
plainLines.clear();
lineTypes.clear();
}
refreshHtml();
}

			@Override
        public void setDefaultBackground(Color color) {
                defaultBackground = (color != null) ? color : DEFAULT_BACKGROUND;
			if (fx != null) {
                        loadShellDocument();
                }
        }

	@Override
	public void setChannelName(String name) {
		channelName = (name != null) ? name : "";
	}

        private void refreshHtml() {
			if (fx == null) {
			return;
                }

                final String fragment;
                synchronized (htmlLines) {
                        fragment = buildLinesFragment();
                }

                synchronized (this) {
                        pendingHtml = fragment;
                }

                pushPendingHtml();
        }

        private void pushPendingHtml() {
                final String html;
                synchronized (this) {
                        html = pendingHtml;
                }
			if (!shellLoaded || (fx == null)) {
			return;
                }
                fx.updateContent(html, autoScroll);
        }

        private void loadShellDocument() {
                final String initial;
                synchronized (htmlLines) {
                        initial = buildLinesFragment();
                }

                synchronized (this) {
                        pendingHtml = initial;
                }

                shellLoaded = false;
                final String shell = buildShellDocument(initial);
fx.loadShell(shell, new Runnable() {
@Override
public void run() {
shellLoaded = true;
pushPendingHtml();
setModeratorMode(moderatorMode);
}
});
}

	private String buildShellDocument(final String initialContent) {
	StringBuilder html = new StringBuilder();
	final EmojiStore store = EmojiStore.get();
	final String embeddedFont = store.getBundledFontDataUrl();
	final String emojiFamily = store.getFontFamily();
	final String emojiCssFamily = cssFontFamilyName(emojiFamily);
	final boolean hasEmbeddedFont = (embeddedFont != null) && !embeddedFont.isEmpty();
	final String emojiFontStack = emojiCssFamily + "," + EMOJI_FONT_STACK;
	html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><style>");
	html.append("html,body{height:100%;margin:0;padding:0;}");
	html.append("body{background:").append(cssColor(defaultBackground)).append(";margin:0;padding:0;color:#dbdee1;font-family:")
	.append(DISCORD_FONT_STACK).append(";display:flex;flex-direction:column;overflow:hidden;}");
	html.append(".chat-root{display:flex;flex-direction:column;height:100%;width:100%;}");
	html.append("#chat-container{flex:1;overflow-y:auto;box-sizing:border-box;padding:16px 18px;background:rgba(15,15,18,0.55);backdrop-filter:blur(2px);}");
	html.append(".message{display:flex;flex-direction:column;gap:4px;margin-bottom:6px;padding:8px 12px;border-radius:10px;background:rgba(255,255,255,0.02);transition:background 0.2s ease;border:1px solid rgba(255,255,255,0.04);}");
	html.append(".message:hover{background:rgba(255,255,255,0.06);}");
	html.append(".message.mod-muted{opacity:0.35;}");
	html.append(".message.admin-alert{border-left:3px solid #f8a532;background:rgba(248,165,50,0.12);}");
	html.append(".message.admin-alert:hover{background:rgba(248,165,50,0.2);}");
	html.append(".header-line{display:flex;align-items:baseline;gap:8px;font-size:13px;line-height:1.35;}");
	html.append(".author{font-weight:600;color:#f4f5f7;}");
	html.append(".admin-badge{background:linear-gradient(135deg,#e36414,#f97316);color:#fff;padding:1px 6px;border-radius:6px;font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:0.04em;}");
	html.append(".timestamp{margin-left:auto;color:#949ba4;font-size:11px;}");
	html.append(".message-body{white-space:pre-wrap;word-break:break-word;font-size:")
	.append(StyleConstants.getFontSize(regularStyle)).append("px;line-height:1.5;color:#dbdee1;}");
	html.append(".message-body a{color:#5da9ff;text-decoration:none;font-weight:500;}");
	html.append(".message-body a:hover{text-decoration:underline;}");
	html.append(".mention{background:rgba(88,101,242,0.25);color:#dee0ff;padding:0 4px;border-radius:4px;font-weight:600;}");
	html.append(".mention-global{background:rgba(237,66,69,0.3);color:#ffe0e5;}");
	html.append(".emoji{font-family:").append(emojiFontStack).append(";font-style:normal;font-weight:normal;}");
	html.append(".message.type-warning .author,.message.type-error .author{color:#ffb347;}");
	html.append(".message.type-support .author,.message.type-server .author{color:#7cc5ff;}");
	html.append("body.mod-mode .message{opacity:0.25;}");
	html.append("body.mod-mode .message.admin-alert{opacity:1;}");
	if (adminUser) {
	html.append("#admin-banner{padding:10px 18px;background:rgba(88,101,242,0.15);border-bottom:1px solid rgba(88,101,242,0.3);color:#dee0ff;font-size:12px;font-weight:600;display:flex;gap:12px;align-items:center;}");
	html.append("#admin-banner button{background:#5865f2;color:#f8f9ff;border:none;border-radius:6px;padding:4px 10px;font-size:12px;font-weight:600;cursor:pointer;transition:opacity 0.2s ease;}");
	html.append("#admin-banner button:hover{opacity:0.85;}");
	}
	if (hasEmbeddedFont) {
	html.append("@font-face{font-family:").append(emojiCssFamily).append(";src:url(").append(embeddedFont).append(") format('truetype');font-display:swap;}");
	}
	html.append("</style><script>");
html.append("window.__stendhalChat={autoScroll:true,moderatorMode:false,decoder:null,ensureDecoder:function(){if(this.decoder!==null){return;}if(typeof TextDecoder!=='undefined'){try{this.decoder=new TextDecoder('utf-8');return;}catch(e){this.decoder=false;}}this.decoder=false;},decode:function(b64){this.ensureDecoder();var binary=atob(b64);if(this.decoder&&this.decoder!==false){var length=binary.length;var bytes=new Uint8Array(length);for(var i=0;i<length;i++){bytes[i]=binary.charCodeAt(i);}try{return this.decoder.decode(bytes);}catch(e){}}var escaped='';for(var j=0;j<binary.length;j++){var code=binary.charCodeAt(j).toString(16);if(code.length<2){code='0'+code;}escaped+='%'+code;}try{return decodeURIComponent(escaped);}catch(e){return binary;}},setContent:function(b64,scroll){var container=document.getElementById('chat-container');if(!container){return;}var html=this.decode(b64);container.innerHTML=html;if(scroll&&window.__stendhalChat.autoScroll){window.requestAnimationFrame(function(){container.scrollTop=container.scrollHeight;});}},setModeratorMode:function(enabled){var body=document.body;if(!body){return;}this.moderatorMode=!!enabled;if(enabled){body.classList.add('mod-mode');}else{body.classList.remove('mod-mode');}}};");
	html.append("window.addEventListener('wheel',function(){var container=document.getElementById('chat-container');if(!container){return;}var atBottom=(container.scrollHeight-container.scrollTop-container.clientHeight)<=4;window.__stendhalChat.autoScroll=atBottom;},{passive:true});");
	html.append("window.addEventListener('touchmove',function(){var container=document.getElementById('chat-container');if(!container){return;}var atBottom=(container.scrollHeight-container.scrollTop-container.clientHeight)<=4;window.__stendhalChat.autoScroll=atBottom;},{passive:true});");
	html.append("window.addEventListener('keydown',function(ev){if(ev && (ev.key==='End'||ev.key==='PageDown')){var container=document.getElementById('chat-container');if(container){container.scrollTop=container.scrollHeight;window.__stendhalChat.autoScroll=true;}}});");
	if (adminUser) {
	html.append("function __stendhalToggleMod(){var current=window.__stendhalChat && window.__stendhalChat.moderatorMode;window.__stendhalChat.setModeratorMode(!current);}");
	html.append("function __stendhalJumpBottom(){var container=document.getElementById('chat-container');if(container){container.scrollTop=container.scrollHeight;window.__stendhalChat.autoScroll=true;}}");
	}
	html.append("</script></head><body");
	if (moderatorMode) {
	html.append(" class=\"mod-mode\"");
	}
	html.append(">");
	html.append("<div class=\"chat-root\">");
	if (adminUser) {
	html.append("<div id=\"admin-banner\"><span>Tryb eksperymentalny WebView – panel moderatora.</span><button type=\"button\" onclick=\"__stendhalToggleMod()\">Przełącz tryb moderatora</button><button type=\"button\" onclick=\"__stendhalJumpBottom()\">Przewiń na dół</button></div>");
	}
	html.append("<div id=\"chat-container\">");
	html.append(initialContent);
	html.append("</div></div></body></html>");
	return html.toString();
	}

	private String buildLinesFragment() {
	StringBuilder builder = new StringBuilder();
	for (String line : htmlLines) {
	builder.append(line);
	}
	return builder.toString();
	}

	private String buildLineHtml(EventLine line) {
	NotificationType type = (line != null) ? line.getType() : NotificationType.NORMAL;
	String typeClass = cssType(type);
	boolean adminAlert = isAdminAlert(type);
	String header = (line != null) ? line.getHeader() : "";
	String timestamp = escape(dateFormatter.format(new Date()));
	String plain = (line != null) ? escape(buildPlainLine(line)) : "";
	StringBuilder body = new StringBuilder();
	HtmlTextSink sink = new HtmlTextSink(body, adminAlert);
	StyleSet base = new StyleSet(styleContext, regularStyle);
	formatter.format(line.getText(), base, sink);
	StringBuilder html = new StringBuilder();
	html.append("<article class=\"message type-").append(typeClass);
	if (adminAlert) {
	html.append(" admin-alert");
	}
	html.append("\" data-plain=\"").append(plain).append("\">");
	html.append("<div class=\"header-line\">");
	if ((header != null) && !header.isEmpty()) {
	html.append("<span class=\"author\">").append(escape(header)).append("</span>");
	} else {
	html.append("<span class=\"author\">PolanieOnLine</span>");
	}
	if (adminAlert) {
	html.append("<span class=\"admin-badge\">ADMIN</span>");
	}
	html.append("<span class=\"timestamp\">").append(timestamp).append("</span>");
	html.append("</div>");
	html.append("<div class=\"message-body\">").append(body).append("</div>");
	html.append("</article>");
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

	private static String cssFontFamilyName(String family) {
	if ((family == null) || family.isEmpty()) {
	family = "Noto Color Emoji";
	}
	return '\'' + family.replace("'", "\\'") + '\'';
	}

	private Pattern buildMentionPattern() {
	StringBuilder regex = new StringBuilder("(?i)(@everyone|@here|!report|!helpmod|!alert");
	UserContext context = UserContext.get();
	String player = (context != null) ? context.getName() : null;
	if ((player != null) && !player.isEmpty()) {
	String escaped = Pattern.quote(player);
	regex.append("|@").append(escaped).append("\\b");
	regex.append("|\\b").append(escaped).append("\\b");
	}
	regex.append(')');
	return Pattern.compile(regex.toString());
	}

	private String highlightMentions(final String text, final boolean adminLine) {
	if ((text == null) || text.isEmpty()) {
	return "";
	}
	Pattern pattern = buildMentionPattern();
	Matcher matcher = pattern.matcher(text);
	StringBuilder out = new StringBuilder();
	int lastIndex = 0;
	while (matcher.find()) {
	int start = matcher.start();
	if (start > lastIndex) {
	out.append(escape(text.substring(lastIndex, start)));
	}
	String mention = text.substring(start, matcher.end());
	String cssClass = "mention";
	String normalized = mention.toLowerCase(Locale.ROOT);
	if (normalized.startsWith("@everyone") || normalized.startsWith("@here")
	|| normalized.startsWith("!report") || normalized.startsWith("!helpmod")
	|| normalized.startsWith("!alert")) {
	cssClass += " mention-global";
	} else if (adminLine) {
	cssClass += " mention-global";
	}
	out.append("<span class=\"").append(cssClass).append("\">")
	.append(escape(mention)).append("</span>");
	lastIndex = matcher.end();
	}
	if (lastIndex < text.length()) {
	out.append(escape(text.substring(lastIndex)));
	}
	return out.toString();
	}

	private static String cssType(final NotificationType type) {
	if (type == null) {
	return "normal";
	}
	return type.name().toLowerCase(Locale.ROOT).replace('_', '-');
	}

	private static boolean isAdminAlert(final NotificationType type) {
	if (type == null) {
	return false;
	}
	switch (type) {
	case SUPPORT:
	case WARNING:
	case ERROR:
	case SERVER:
	case TELLALL:
	case CLIENT:
	return true;
	default:
	return false;
	}
	}

	private void copyLastLine() {
	String latest = null;
	synchronized (htmlLines) {
	if (!plainLines.isEmpty()) {
	latest = plainLines.get(plainLines.size() - 1);
	}
	}
	copyToClipboard(latest, "Skopiowano ostatnią wiadomość do schowka.");
	}

	private void copyAdminHighlights() {
	StringBuilder highlights = new StringBuilder();
	synchronized (htmlLines) {
	for (int i = 0; i < plainLines.size(); i++) {
	if ((i < lineTypes.size()) && isAdminAlert(lineTypes.get(i))) {
	if (highlights.length() > 0) {
	highlights.append(System.lineSeparator());
	}
	highlights.append(plainLines.get(i));
	}
	}
	}
	if (highlights.length() == 0) {
	logger.info("Brak wiadomości administratora do skopiowania.");
	return;
	}
	copyToClipboard(highlights.toString(), "Skopiowano ostrzeżenia moderatora do schowka.");
	}

	private void copyToClipboard(final String text, final String infoMessage) {
	if ((text == null) || text.isEmpty()) {
	return;
	}
	try {
	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	clipboard.setContents(new StringSelection(text), null);
	if (infoMessage != null) {
	logger.info(infoMessage);
	}
	} catch (HeadlessException ex) {
	logger.warn("Schowek systemowy jest niedostępny", ex);
	}
	}

	private void setModeratorMode(final boolean enabled) {
	moderatorMode = enabled;
	if (fx != null) {
	fx.setModeratorMode(enabled);
	}
	}

	private void syncModeratorMode() {
	if (fx == null) {
	return;
	}
	Boolean current = fx.queryModeratorMode();
	if (current != null) {
	moderatorMode = current.booleanValue();
	}
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
\t\t@Override
		protected void showPopup(MouseEvent e) {
		final JPopupMenu popup = new JPopupMenu("zapisz");
		if (adminUser) {
		syncModeratorMode();
		}

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

		menuItem = new JMenuItem("Skopiuj ostatnią wiadomość");
		menuItem.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		copyLastLine();
		}
		});
		popup.add(menuItem);

		if (adminUser) {
		popup.addSeparator();
		menuItem = new JMenuItem("Skopiuj alerty moderatora");
		menuItem.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
		copyAdminHighlights();
		}
		});
		popup.add(menuItem);

		JCheckBoxMenuItem modToggle = new JCheckBoxMenuItem("Tryb moderatora (eksperymentalny)", moderatorMode);
		modToggle.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent event) {
		setModeratorMode(modToggle.isSelected());
		}
		});
		popup.add(modToggle);
		}

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
private final boolean adminAlert;

HtmlTextSink(StringBuilder html, boolean adminAlert) {
this.html = html;
this.adminAlert = adminAlert;
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

final StringBuilder span = new StringBuilder();
span.append("<span");
if (emoji) {
span.append(" class=\"emoji\"");
}
span.append(" style=\"");

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
span.append("font-family:").append(DISCORD_FONT_STACK).append(';');
}
span.append('\"');
span.append('>');
if (emoji) {
String glyph = (store != null) ? store.glyphFor(text) : null;
if ((glyph == null) || glyph.isEmpty()) {
glyph = text;
}
span.append(escape(glyph));
} else {
span.append(renderContent(text));
}
span.append("</span>");
html.append(span);
}

private String renderContent(final String text) {
return highlightMentions(text, adminAlert);
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

		private Object createLoadListener(final Object property, final Runnable afterLoad) {
			if ((property == null) || (changeListenerClass == null)) {
				return null;
			}
			try {
				final Object listener = Proxy.newProxyInstance(changeListenerClass.getClassLoader(),
				new Class<?>[] { changeListenerClass }, new InvocationHandler() {
					@Override
					public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
						if ((method != null) && "changed".equals(method.getName()) && (args != null) && (args.length >= 3)) {
							Object newState = args[2];
							boolean terminal = isTerminalState(newState);
							if (terminal && (afterLoad != null)) {
								SwingUtilities.invokeLater(afterLoad);
							}
							if (propertyRemoveListener != null) {
								try {
									propertyRemoveListener.invoke(property, proxy);
								} catch (Exception removeEx) {
									logger.debug("Unable to detach WebView load listener", removeEx);
								}
							}
						}
						return null;
					}
				});
				return listener;
			} catch (IllegalArgumentException ex) {
				logger.debug("Unable to create WebView load listener", ex);
				return null;
			}
		}

		private boolean isTerminalState(final Object state) {
			if (state == null) {
				return false;
			}
			return state.equals(succeededState) || state.equals(failedState) || state.equals(cancelledState);
		}

		private void prepareWorkerMetadata(final Object worker) {
			if (worker == null) {
				return;
			}
			if ((propertyAddListener == null) || (propertyRemoveListener == null)) {
				try {
					Object property = (workerStateProperty != null) ? workerStateProperty.invoke(worker) : null;
					if ((property != null) && (changeListenerClass != null)) {
						propertyAddListener = property.getClass().getMethod("addListener", changeListenerClass);
						propertyRemoveListener = property.getClass().getMethod("removeListener", changeListenerClass);
					}
				} catch (Exception ex) {
					logger.debug("Unable to resolve WebView listener methods", ex);
				}
			}
			if (workerStateClass != null) {
				succeededState = resolveStateField(succeededState, "SUCCEEDED");
				failedState = resolveStateField(failedState, "FAILED");
				cancelledState = resolveStateField(cancelledState, "CANCELLED");
			}
		}

		private Object resolveStateField(Object currentValue, final String fieldName) {
			if (currentValue != null || (workerStateClass == null)) {
				return currentValue;
			}
			try {
				Field field = workerStateClass.getField(fieldName);
				return field.get(null);
			} catch (NoSuchFieldException | IllegalAccessException ex) {
				logger.debug("Unable to resolve WebView worker state: " + fieldName, ex);
				return null;
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

                void loadShell(final String html, final Runnable afterLoad) {
                        loadContent(html, afterLoad);
                }

void updateContent(final String html, final boolean scroll) {
if (html == null) {
return;
}
final String encoded = Base64.getEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));
                        final String script = "if(window.__stendhalChat){window.__stendhalChat.setContent('" + encoded + "',"
                                + (scroll ? "true" : "false") + ");}";
                        Runnable task = new Runnable() {
			@Override
                                public void run() {
			try {
			if (!awaitReady()) {
			return;
                                                }
                                                executeScript.invoke(webEngine, script);
                                        } catch (Throwable ex) {
                                                logger.warn("Failed to update chat content", ex);
                                        }
                                }
                        };
invokeLater(task);
}

void setModeratorMode(final boolean enabled) {
final String script = "if(window.__stendhalChat){window.__stendhalChat.setModeratorMode(" + (enabled ? "true" : "false") + ");}";
Runnable task = new Runnable() {
@Override
public void run() {
try {
if (!awaitReady()) {
return;
}
executeScript.invoke(webEngine, script);
} catch (Throwable ex) {
logger.debug("Nie udało się przełączyć trybu moderatora", ex);
}
}
};
invokeLater(task);
}

Boolean queryModeratorMode() {
final CountDownLatch latch = new CountDownLatch(1);
final Boolean[] holder = new Boolean[1];
Runnable task = new Runnable() {
@Override
public void run() {
try {
if (!awaitReady()) {
return;
}
Object value = executeScript.invoke(webEngine,
"return (window.__stendhalChat && window.__stendhalChat.moderatorMode===true);");
holder[0] = coerceBoolean(value);
} catch (Throwable ex) {
logger.debug("Unable to query moderator mode", ex);
} finally {
latch.countDown();
}
}
};
invokeLater(task);
try {
latch.await(1, TimeUnit.SECONDS);
} catch (InterruptedException ex) {
Thread.currentThread().interrupt();
}
return holder[0];
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

private void invokeLater(Runnable runnable) {
try {
runLater.invoke(null, runnable);
} catch (IllegalAccessException | InvocationTargetException ex) {
logger.error("Failed to schedule JavaFX task", ex);
}
}

private static Boolean coerceBoolean(final Object value) {
if (value instanceof Boolean) {
return (Boolean) value;
}
if (value instanceof Number) {
return ((Number) value).intValue() != 0;
}
if (value instanceof String) {
return Boolean.parseBoolean((String) value);
}
return null;
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
