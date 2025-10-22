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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import games.stendhal.client.UserContext;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.gui.textformat.AttributedTextSink;
import games.stendhal.client.gui.textformat.CssClassSet;
import games.stendhal.client.gui.textformat.StringFormatter;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.sprite.EmojiStore.EmojiMatch;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
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
	private static final String BUNDLED_CHAT_FONT_FAMILY = "StendhalChat";
	private static final char EMOJI_PLACEHOLDER_PREFIX = '\uFFF0';
	private static final char LAYERED_EMOJI_PLACEHOLDER_PREFIX = '\uFFF1';
	private static final Pattern LAYERED_EMOJI_PATTERN = Pattern.compile("\\[\\[(?:emoji|em):([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]", Pattern.CASE_INSENSITIVE);

	private static final StringFormatter<java.util.Set<String>, CssClassSet> FORMATTER
	= new StringFormatter<java.util.Set<String>, CssClassSet>();
	private static final CssClassSet DEFAULT_FRAGMENT = new CssClassSet().addClass("fragment");
	private static final char[] POLISH_SAMPLE_CHARS = { 'ą', 'ć', 'ę', 'ł', 'ń', 'ó', 'ś', 'ż', 'ź' };
        private static final BundledFontFace[] BUNDLED_CHAT_FONTS = new BundledFontFace[] {
                new BundledFontFace(BUNDLED_CHAT_FONT_FAMILY, "data/font/Carlito-Regular.ttf", "400", "normal"),
                new BundledFontFace(BUNDLED_CHAT_FONT_FAMILY, "data/font/Carlito-Italic.ttf", "400", "italic"),
                new BundledFontFace(BUNDLED_CHAT_FONT_FAMILY, "data/font/Carlito-Bold.ttf", "700", "normal"),
                new BundledFontFace(BUNDLED_CHAT_FONT_FAMILY, "data/font/Carlito-BoldItalic.ttf", "700", "italic"),
                new BundledFontFace("KonstytucjaPolska", "data/font/KonstytucjaPolska.ttf", "400", "normal"),
                new BundledFontFace("AntykwaTorunska", "data/font/AntykwaTorunska.ttf", "400", "normal"),
                new BundledFontFace("Amaranth", "data/font/Amaranth-Regular.ttf", "400", "normal"),
                new BundledFontFace("Amaranth", "data/font/Amaranth-Italic.ttf", "400", "italic"),
                new BundledFontFace("Amaranth", "data/font/Amaranth-Bold.ttf", "700", "normal"),
                new BundledFontFace("Amaranth", "data/font/Amaranth-BoldItalic.ttf", "700", "italic")
        };

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

        private final Font chatFont = resolveChatFont();

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

		EmojiStore.get().init();

		setOpaque(true);
		setBackground(defaultBackground);
		setLayout(new BorderLayout());
		fxPanel = new JFXPanel();
		bridge = new FxBridge(fxPanel, defaultBackground);
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
		if (bridge != null) {
			bridge.setBackground(defaultBackground);
		}
		final Color swingColor = defaultBackground;
		final Runnable update = () -> {
			setBackground(swingColor);
			repaint();
		};
		if (SwingUtilities.isEventDispatchThread()) {
			update.run();
		} else {
			SwingUtilities.invokeLater(update);
		}
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
		final String backgroundHex = toHex(defaultBackground);
		final String backgroundCss = toCssRgba(defaultBackground);
		final StringBuilder builder = new StringBuilder();
		builder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>");
		builder.append("<style>").append(buildStylesheet(backgroundHex, backgroundCss)).append("</style></head>");
		builder.append("<body class=\"chat-body\" style=\"background:")
			.append(backgroundHex)
			.append(";background-color:")
			.append(backgroundCss)
			.append(";color:#f4edd9;\">");
		builder.append("<div id=\"chat\" class=\"chat-log\">");
		synchronized (htmlLines) {
			for (final String line : htmlLines) {
				builder.append(line);
			}
		}
                builder.append("</div>");
                builder.append("<script>(function(){var c=document.getElementById('chat');");
                builder.append("function markFallbackApplied(img){if(!img){return;}if(img.dataset){img.dataset.emojiFallbackApplied='1';}else{img.setAttribute('data-emoji-fallback-applied','1');}}");
                builder.append("function tryAlternateSource(img){if(!img){return false;}var attr=img.getAttribute('data-alt-src');if(!attr){return false;}var sources=attr.split('|');if(!sources||!sources.length){return false;}var idxAttr=img.getAttribute('data-alt-index');var idx=idxAttr?parseInt(idxAttr,10):0;if(isNaN(idx)||idx<0){idx=0;}while(idx<sources.length){var candidate=sources[idx].trim();idx++;img.setAttribute('data-alt-index',idx);if(candidate&&candidate.length&&candidate!==img.src){img.src=candidate;return true;}}return false;}");
                builder.append("function applyFallback(img){if(!img){return;}var ds=img.dataset||{};if(ds.emojiFallbackApplied==='1'||img.getAttribute('data-emoji-fallback-applied')==='1'){return;}if(tryAlternateSource(img)){return;}var fallback=img.getAttribute('data-fallback');if(!fallback||fallback===img.src){return;}markFallbackApplied(img);img.src=fallback;}");
                builder.append("function primeFallbacks(root){if(!root||!root.querySelectorAll){return;}var imgs=root.querySelectorAll('img[data-fallback],img[data-alt-src]');for(var i=0;i<imgs.length;i++){var img=imgs[i];if(img.complete&&img.naturalWidth===0){applyFallback(img);}}}");
                builder.append("document.addEventListener('error',function(evt){var target=evt.target;if(!target||!target.classList){return;}if(target.classList.contains('emoji-icon')||target.classList.contains('emoji-layer-img')){applyFallback(target);}},true);");
                builder.append("window.chatlog={container:c,appendLine:function(html){if(!this.container){return;}var wrapper=document.createElement('div');wrapper.innerHTML=html;while(wrapper.firstChild){var node=wrapper.firstChild;this.container.appendChild(node);primeFallbacks(node);}this.scrollToBottom();},clear:function(){if(this.container){this.container.innerHTML='';}},scrollToBottom:function(){if(this.container){this.container.scrollTop=this.container.scrollHeight;}window.scrollTo(0,document.body.scrollHeight);},handleEmojiError:function(img){applyFallback(img);}};");
                builder.append("if(window.chatlog&&window.chatlog.container){primeFallbacks(window.chatlog.container);}window.chatlog.scrollToBottom();})();</script>");
		builder.append("</body></html>");
		return builder.toString();
	}

	private String buildStylesheet(final String backgroundHex, final String backgroundCss) {
		final StringBuilder css = new StringBuilder();
		appendBundledChatFonts(css);
		final String background = ((backgroundHex != null) && !backgroundHex.isEmpty()) ? backgroundHex : toHex(defaultBackground);
		final String backgroundColor = ((backgroundCss != null) && !backgroundCss.isEmpty()) ? backgroundCss
			: toCssRgba(defaultBackground);
		final String emojiStack = buildEmojiFontStack(css);
		final String fontStack = buildChatFontStack();
		final int bodyFontSize = chatBodyFontSize();
		final int timestampSize = timestampFontSize();

		css.append("body.chat-body{margin:0;padding:0;background:")
			.append(background)
			.append(";background-color:")
			.append(backgroundColor)
			.append(";color:#f4edd9;font-family:")
			.append(fontStack)
			.append(";font-size:")
			.append(bodyFontSize)
			.append("px;line-height:1.35;}");
		css.append("html{background:")
			.append(background)
			.append(";background-color:")
			.append(backgroundColor)
			.append(";}");
		css.append(".chat-log{padding:8px 12px;display:flex;flex-direction:column;gap:2px;max-height:100%;overflow-y:auto;background:")
			.append(background)
			.append(";background-color:")
			.append(backgroundColor)
			.append(";}");
                css.append(".line{display:flex;flex-wrap:wrap;gap:4px;align-items:flex-start;padding:2px 0;border-bottom:1px solid rgba(255,255,255,0.06);}");
                css.append(".line:last-child{border-bottom:none;}");
                css.append(".line.admin{border-left:3px solid rgba(240,200,120,0.9);padding-left:7px;}");
                css.append(".timestamp{font-style:italic;color:rgba(255,255,255,0.65);margin-right:6px;font-size:")
                        .append(timestampSize)
                        .append("px;}");
                css.append(".header{color:#ffe6a1;font-weight:600;margin-right:6px;font-style:italic;}");
                css.append(".message{flex:1 1 auto;white-space:pre-wrap;word-break:break-word;}");
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
                        css.append(".emoji{font-family:").append(emojiStack).append(";font-size:1.05em;line-height:1.05em;}");
                        css.append(".emoji-span{display:inline-flex;align-items:center;gap:2px;}");
                        css.append(".emoji-icon{height:1.25em;width:1.25em;vertical-align:middle;}");
                        css.append(".emoji-layered{position:relative;display:inline-flex;align-items:center;justify-content:center;width:1.65em;height:1.65em;margin:0 0.12em;--emoji-bg:transparent;--emoji-bg-opacity:1;--emoji-outline:rgba(0,0,0,0);--emoji-outline-width:0px;--emoji-shadow:0 0 0 0 transparent;}");
                        css.append(".emoji-layered::before{content:\"\";position:absolute;inset:0;border-radius:0.55em;background:var(--emoji-bg);opacity:var(--emoji-bg-opacity);box-shadow:0 0 0 var(--emoji-outline-width) var(--emoji-outline),var(--emoji-shadow);}");
                        css.append(".emoji-layered .emoji-layer{position:absolute;top:50%;left:50%;width:calc(100% - 0.2em);height:calc(100% - 0.2em);transform:translate(-50%,-50%);display:flex;align-items:center;justify-content:center;pointer-events:none;}");
                        css.append(".emoji-layered .emoji-layer-img{width:100%;height:100%;object-fit:contain;image-rendering:auto;}");
                        css.append(".emoji-layered .emoji-layer.glyph{font-size:0.95em;color:inherit;text-shadow:0 1px 2px rgba(0,0,0,0.35);}");
                        css.append(".emoji-layered .emoji-label{position:absolute;bottom:-0.3em;left:50%;transform:translateX(-50%);font-size:0.6em;padding:0.1em 0.35em;border-radius:999px;background:var(--emoji-label-bg,rgba(0,0,0,0.45));color:var(--emoji-label-color,#fff);line-height:1;font-weight:600;white-space:nowrap;pointer-events:none;}");
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
		final String withLayered = injectLayeredEmojiPlaceholders(text, replacements);
		final String working = injectEmojiPlaceholders(withLayered, replacements);
		final HtmlFragmentBuilder builder = new HtmlFragmentBuilder();
		FORMATTER.format(working, DEFAULT_FRAGMENT.copy(), builder);
		String html = builder.toHtml();
		for (final EmojiReplacement replacement : replacements) {
			html = html.replace(replacement.placeholder, replacement.html);
		}
		return html;
	}

	private String injectLayeredEmojiPlaceholders(final String text, final List<EmojiReplacement> replacements) {
		if ((text == null) || text.isEmpty()) {
			return text;
		}

		final Matcher matcher = LAYERED_EMOJI_PATTERN.matcher(text);
		final StringBuilder working = new StringBuilder();
		int index = 0;
		int counter = 0;
		while (matcher.find()) {
			working.append(text, index, matcher.start());
			final LayeredEmojiSpec spec = LayeredEmojiSpec.parse(matcher.group(1), matcher.group(2));
			if (spec != null) {
				final String html = buildLayeredEmojiHtml(spec);
				if ((html != null) && spec.hasRenderableLayers()) {
					final String placeholder = buildLayeredEmojiPlaceholder(counter++);
					replacements.add(new EmojiReplacement(placeholder, html));
					working.append(placeholder);
				} else {
					working.append(matcher.group(0));
				}
			} else {
				working.append(matcher.group(0));
			}
			index = matcher.end();
		}
		if (index < text.length()) {
			working.append(text.substring(index));
		}
		return working.toString();
	}

	private String injectEmojiPlaceholders(final String text, final List<EmojiReplacement> replacements) {
		final EmojiStore store = EmojiStore.get();
		final StringBuilder working = new StringBuilder();
		int index = 0;
		int counter = 0;
		while (index < text.length()) {
			final char current = text.charAt(index);
			if ((current == EMOJI_PLACEHOLDER_PREFIX) || (current == LAYERED_EMOJI_PLACEHOLDER_PREFIX)) {
				final int end = findPlaceholderEnd(text, index, current);
				working.append(text, index, end);
				index = end;
				continue;
			}
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
                        index += java.lang.Character.charCount(codePoint);
		}
		return working.toString();
	}

	private String buildEmojiPlaceholder(final int index) {
			return new StringBuilder(16).append(EMOJI_PLACEHOLDER_PREFIX).append(index).append(EMOJI_PLACEHOLDER_PREFIX).toString();
	}

	private String buildLayeredEmojiPlaceholder(final int index) {
			return new StringBuilder(20).append(LAYERED_EMOJI_PLACEHOLDER_PREFIX).append(index)
					.append(LAYERED_EMOJI_PLACEHOLDER_PREFIX).toString();
	}

	private int findPlaceholderEnd(final String text, final int start, final char prefix) {
			if ((text == null) || (start < 0) || (start >= text.length())) {
					return start + 1;
			}
			int offset = start + 1;
			while (offset < text.length()) {
					if (text.charAt(offset) == prefix) {
							return offset + 1;
					}
					offset++;
			}
			return text.length();
	}

                private String buildEmojiHtml(final EmojiMatch match, final String token) {
                                final EmojiStore store = EmojiStore.get();
                                final String glyph = ((match != null) && (match.getGlyph() != null)) ? match.getGlyph() : ((token != null) ? token : "");
                                final EmojiImageSource image = resolveEmojiImage(store, match, token, glyph);
                                if ((image != null) && image.hasPrimary()) {
                                                final StringBuilder builder = new StringBuilder();
                                                builder.append("<span class=\"emoji-span\"><img class=\"emoji-icon\"")
                                                                .append(" src=\"")
                                                                .append(escapeHtml(image.getPrimary()))
                                                                .append("\" alt=\"")
                                                                .append(escapeHtml(glyph))
                                                                .append("\" loading=\"lazy\" decoding=\"async\" referrerpolicy=\"no-referrer\"");
                                                if (image.hasFallback()) {
                                                                builder.append(" data-fallback=\"")
                                                                                .append(escapeHtml(image.getFallback()))
                                                                                .append("\"");
                                                }
                                                if (image.hasAlternateRemotes()) {
                                                                final String alternates = joinAlternateRemotes(image.getAlternateRemotes());
                                                                if (!alternates.isEmpty()) {
                                                                                builder.append(" data-alt-src=\"")
                                                                                                .append(escapeHtml(alternates))
                                                                                                .append("\" data-alt-index=\"0\"");
                                                                }
                                                }
                                                builder.append(" onerror=\"if(window.chatlog&&window.chatlog.handleEmojiError){window.chatlog.handleEmojiError(this);}\"");
                                                builder.append("/></span>");
                                                return builder.toString();
                                }
                                return "<span class=\"emoji\">" + escapeHtml(glyph) + "</span>";
                }

                private EmojiImageSource resolveEmojiImage(final EmojiStore store, final EmojiMatch match, final String token, final String glyph) {
                                if (store == null) {
                                                return null;
                                }
                                final LinkedHashSet<String> queries = new LinkedHashSet<String>();
                                if (match != null) {
                                                final String canonical = match.getName();
                                                if ((canonical != null) && !canonical.isEmpty()) {
                                                                queries.add(':' + canonical + ':');
                                                                queries.add(canonical);
                                                }
                                }
                                if ((token != null) && !token.isEmpty()) {
                                                queries.add(token);
                                }
                                if ((glyph != null) && !glyph.isEmpty()) {
                                                queries.add(glyph);
                                }
                                for (final String query : queries) {
                                                if ((query == null) || query.isEmpty()) {
                                                                continue;
                                                }
                                                final EmojiImageSource image = fetchEmojiImage(store, query);
                                                if ((image != null) && image.hasPrimary()) {
                                                                return image;
                                                }
                                }
                                return null;
                }

                private EmojiImageSource fetchEmojiImage(final EmojiStore store, final String query) {
                                if ((store == null) || (query == null) || query.isEmpty()) {
                                                return null;
                                }
                                final EmojiStore.EmojiImage image = store.imageFor(query);
                                if (image != null) {
                                                final String primary = image.getPrimaryUrl();
                                                final String fallback = image.getFallbackDataUrl();
                                                final List<String> alternateRemotes = image.getAlternateRemotes();
                                                final String effectivePrimary = ((primary != null) && !primary.isEmpty()) ? primary
                                                                : fallback;
                                                if ((effectivePrimary != null) && !effectivePrimary.isEmpty()) {
                                                                return new EmojiImageSource(effectivePrimary, fallback, alternateRemotes);
                                                }
                                }
                                final List<String> remoteUrls = store.remoteUrlsForToken(query);
                                final String fallbackData = (image != null) ? image.getFallbackDataUrl()
                                                : store.fallbackDataUrlFor(query);
                                if (!remoteUrls.isEmpty()) {
                                                final String primary = remoteUrls.get(0);
                                                final List<String> alternates = (remoteUrls.size() > 1)
                                                                ? remoteUrls.subList(1, remoteUrls.size())
                                                                : Collections.<String>emptyList();
                                                return new EmojiImageSource(primary, fallbackData, alternates);
                                }
                                if ((fallbackData != null) && !fallbackData.isEmpty()) {
                                                return new EmojiImageSource(fallbackData, fallbackData, Collections.<String>emptyList());
                                }
                                return null;
                }

                private String buildLayeredEmojiHtml(final LayeredEmojiSpec spec) {
                                if ((spec == null) || !spec.hasRenderableLayers()) {
                                                return null;
                                }

                                final LayerAsset baseAsset = resolveEmojiAsset(spec.getBaseToken());
				final List<LayerAsset> overlayAssets = new ArrayList<LayerAsset>();
				for (final String token : spec.getOverlays()) {
						final LayerAsset asset = resolveEmojiAsset(token);
						if (asset != null) {
								overlayAssets.add(asset);
						}
				}

				final StringBuilder builder = new StringBuilder();
				builder.append("<span class=\"emoji-layered");
				final String extraClasses = joinCssClasses(spec.getExtraClasses());
				if (!extraClasses.isEmpty()) {
						builder.append(' ').append(extraClasses);
				}
				builder.append("\"");

				final String style = buildContainerStyle(spec);
				if (!style.isEmpty()) {
						builder.append(" style=\"").append(style).append("\"");
				}

				final String tooltip = spec.getTooltip();
				if ((tooltip != null) && !tooltip.isEmpty()) {
						builder.append(" title=\"").append(escapeHtml(tooltip)).append("\"");
				}

				builder.append(" data-token=\"").append(escapeHtml(spec.getBaseDisplay())).append("\">");
                                final String baseSpan = buildLayerSpan(baseAsset, true, spec.getBaseDisplay());
                                if (baseSpan.isEmpty()) {
                                                builder.append("<span class=\"emoji-layer emoji-layer-base glyph\">")
                                                                .append(escapeHtml(spec.getBaseDisplay()))
                                                                .append("</span>");
                                } else {
                                                builder.append(baseSpan);
                                }
                                for (final LayerAsset asset : overlayAssets) {
                                                builder.append(buildLayerSpan(asset, false, null));
                                }
                                if (spec.getLabel() != null) {
                                                builder.append("<span class=\"emoji-label\">")
                                                                .append(escapeHtml(spec.getLabel()))
                                                                .append("</span>");
				}
				builder.append("</span>");
				return builder.toString();
		}

		private String buildContainerStyle(final LayeredEmojiSpec spec) {
				if (spec == null) {
						return "";
				}
				final StringBuilder style = new StringBuilder();
				if (spec.getBackground() != null) {
						style.append("--emoji-bg:").append(spec.getBackground()).append(';');
				}
				if (spec.getBackgroundOpacity() != null) {
						style.append("--emoji-bg-opacity:").append(spec.getBackgroundOpacity()).append(';');
				}
				if (spec.getOutline() != null) {
						style.append("--emoji-outline:").append(spec.getOutline()).append(';');
						final String outlineWidth = (spec.getOutlineWidth() != null) ? spec.getOutlineWidth() : "2px";
						style.append("--emoji-outline-width:").append(outlineWidth).append(';');
				} else if (spec.getOutlineWidth() != null) {
						style.append("--emoji-outline-width:").append(spec.getOutlineWidth()).append(';');
				}
				if (spec.getShadow() != null) {
						style.append("--emoji-shadow:").append(spec.getShadow()).append(';');
				}
				if (spec.getLabelColor() != null) {
						style.append("--emoji-label-color:").append(spec.getLabelColor()).append(';');
				}
				if (spec.getLabelBackground() != null) {
						style.append("--emoji-label-bg:").append(spec.getLabelBackground()).append(';');
				}
				return style.toString();
		}

                private String buildLayerSpan(final LayerAsset asset, final boolean baseLayer, final String altText) {
                                if (asset == null) {
                                                return "";
                                }
                                final StringBuilder builder = new StringBuilder("<span class=\"emoji-layer");
                                if (baseLayer) {
                                                builder.append(" emoji-layer-base");
                                }
                                final EmojiImageSource image = asset.getImage();
                                final boolean hasImage = (image != null) && image.hasPrimary();
                                if (!hasImage) {
                                                builder.append(" glyph");
                                }
                                builder.append("\"");
                                final String canonical = asset.getCanonicalToken();
                                if ((canonical != null) && !canonical.isEmpty()) {
                                                builder.append(" data-layer=\"")
                                                                .append(escapeHtml(canonical))
                                                                .append("\"");
                                }
                                builder.append('>');
                                final String effectiveAlt = (altText != null) ? altText : asset.getGlyph();
                                if (hasImage) {
                                                builder.append("<img class=\"emoji-layer-img\"")
                                                                .append(" src=\"")
                                                                .append(escapeHtml(image.getPrimary()))
                                                                .append("\" alt=\"")
                                                                .append(escapeHtml((effectiveAlt != null) ? effectiveAlt : ""))
                                                                .append("\" loading=\"lazy\" decoding=\"async\" referrerpolicy=\"no-referrer\"");
                                                if (image.hasFallback()) {
                                                                builder.append(" data-fallback=\"")
                                                                                .append(escapeHtml(image.getFallback()))
                                                                                .append("\"");
                                                }
                                                if (image.hasAlternateRemotes()) {
                                                                final String alternates = joinAlternateRemotes(image.getAlternateRemotes());
                                                                if (!alternates.isEmpty()) {
                                                                                builder.append(" data-alt-src=\"")
                                                                                                .append(escapeHtml(alternates))
                                                                                                .append("\" data-alt-index=\"0\"");
                                                                }
                                                }
                                                builder.append(" onerror=\"if(window.chatlog&&window.chatlog.handleEmojiError){window.chatlog.handleEmojiError(this);}\"");
                                                builder.append("/>");
                                } else {
                                                builder.append(escapeHtml((effectiveAlt != null) ? effectiveAlt : ""));
                                }
                                builder.append("</span>");
                                return builder.toString();
                }

		private LayerAsset resolveEmojiAsset(final String token) {
				if ((token == null) || token.trim().isEmpty()) {
						return null;
				}
				final EmojiStore store = EmojiStore.get();
				final String trimmed = token.trim();
				String canonicalName = store.check(trimmed);
                                if ((canonicalName == null) && !trimmed.startsWith(":") && store.isAvailable(trimmed)) {
                                                canonicalName = trimmed;
                                }
                                String queryToken = trimmed;
                                EmojiImageSource image = fetchEmojiImage(store, queryToken);
                                if (((image == null) || !image.hasPrimary()) && (canonicalName != null)) {
                                                final String colonToken = ":" + canonicalName + ":";
                                                final EmojiImageSource colonImage = fetchEmojiImage(store, colonToken);
                                                if (colonImage != null) {
                                                                image = colonImage;
                                                                queryToken = colonToken;
                                                }
                                }
                                String glyph = null;
                                if (canonicalName != null) {
						glyph = store.glyphFor(":" + canonicalName + ":");
				}
                                if ((glyph == null) && !queryToken.equals(trimmed)) {
                                                glyph = store.glyphFor(queryToken);
                                }
                                if (glyph == null) {
                                                glyph = store.glyphFor(trimmed);
                                }
                                if ((image == null) && (glyph != null) && !glyph.isEmpty() && !glyph.equals(trimmed)) {
                                                final EmojiImageSource glyphImage = fetchEmojiImage(store, glyph);
                                                if (glyphImage != null) {
                                                                image = glyphImage;
                                                }
                                }
                                if (glyph == null) {
                                                glyph = trimmed;
                                }
                                final String canonicalToken = (canonicalName != null) ? canonicalName : trimmed;
                                return new LayerAsset(trimmed, canonicalToken, image, glyph);
                }

		private static String joinCssClasses(final Iterable<String> classes) {
				if (classes == null) {
						return "";
				}
				final StringBuilder builder = new StringBuilder();
				for (final String css : classes) {
						if ((css == null) || css.isEmpty()) {
								continue;
						}
						if (builder.length() > 0) {
								builder.append(' ');
						}
						builder.append(css);
				}
				return builder.toString();
		}

		private static String sanitizeCssValue(final String value) {
				if (value == null) {
						return null;
				}
				final String trimmed = value.trim();
				if (trimmed.isEmpty()) {
						return null;
				}
				if (!trimmed.matches("[a-zA-Z0-9#.,%()\\s+\\-/*]+")) {
						return null;
				}
				return trimmed;
		}

                private static String sanitizeOpacity(final String value) {
                                if (value == null) {
                                                return null;
                                }
                                final String trimmed = value.trim();
				if (trimmed.isEmpty()) {
						return null;
				}
				try {
						final double numeric = Double.parseDouble(trimmed);
						final double clamped = Math.max(0d, Math.min(1d, numeric));
						return String.format(Locale.ROOT, "%.3f", clamped);
                                } catch (NumberFormatException ex) {
                                                final String sanitized = sanitizeCssValue(trimmed);
                                                return (sanitized != null) ? sanitized : null;
                                }
                }

                private static String joinAlternateRemotes(final List<String> remotes) {
                                if ((remotes == null) || remotes.isEmpty()) {
                                                return "";
                                }
                                final StringBuilder builder = new StringBuilder();
                                for (final String remote : remotes) {
                                                if ((remote == null) || remote.isEmpty()) {
                                                                continue;
                                                }
                                                final String trimmed = remote.trim();
                                                if (trimmed.isEmpty()) {
                                                                continue;
                                                }
                                                if (builder.length() > 0) {
                                                                builder.append('|');
                                                }
                                                builder.append(trimmed);
                                }
                                return builder.toString();
                }

		private static String sanitizePixels(final String value) {
				if (value == null) {
						return null;
				}
				final String trimmed = value.trim();
				if (trimmed.isEmpty()) {
						return null;
				}
				try {
						final double numeric = Double.parseDouble(trimmed);
						final double clamped = Math.max(0d, Math.min(64d, numeric));
						return String.format(Locale.ROOT, "%.2fpx", clamped);
				} catch (NumberFormatException ex) {
						final String sanitized = sanitizeCssValue(trimmed);
						return (sanitized != null) ? sanitized : null;
				}
		}

		private static String sanitizeText(final String value) {
				if (value == null) {
						return null;
				}
				final String cleaned = value.replaceAll("\\p{Cntrl}", "").trim();
				return cleaned.isEmpty() ? null : cleaned;
		}

		private static String sanitizeCssIdentifier(final String value) {
				if (value == null) {
						return "";
				}
				final String trimmed = value.trim();
				if (trimmed.isEmpty()) {
						return "";
				}
				if (!trimmed.matches("[a-zA-Z0-9_-]+")) {
						return "";
				}
				return trimmed;
		}

                private static final class LayerAsset {
                                private final String originalToken;
                                private final String canonicalToken;
                                private final EmojiImageSource image;
                                private final String glyph;

                                private LayerAsset(final String originalToken, final String canonicalToken, final EmojiImageSource image, final String glyph) {
                                                this.originalToken = originalToken;
                                                this.canonicalToken = canonicalToken;
                                                this.image = image;
                                                this.glyph = (glyph != null) ? glyph : originalToken;
                                }

                                EmojiImageSource getImage() {
                                                return image;
                                }

                                String getGlyph() {
                                                return glyph;
                                }

                                String getCanonicalToken() {
                                                return canonicalToken;
                                }
                }

		private static final class LayeredEmojiSpec {
				private final String baseToken;
				private final String baseDisplay;
				private final List<String> overlays = new ArrayList<String>();
				private final Set<String> extraClasses = new LinkedHashSet<String>();
				private String background;
				private String backgroundOpacity;
				private String outline;
				private String outlineWidth;
				private String shadow;
				private String label;
				private String labelColor;
				private String labelBackground;
				private String tooltip;

				private LayeredEmojiSpec(final String baseToken) {
						this.baseToken = baseToken;
						final String cleaned = sanitizeText(baseToken);
						this.baseDisplay = (cleaned != null) ? cleaned : baseToken;
				}

				static LayeredEmojiSpec parse(final String baseSegment, final String parameters) {
						if (baseSegment == null) {
								return null;
						}
						final String trimmed = baseSegment.trim();
						if (trimmed.isEmpty()) {
								return null;
						}
						final LayeredEmojiSpec spec = new LayeredEmojiSpec(trimmed);
						if ((parameters != null) && !parameters.isEmpty()) {
								final String[] segments = parameters.split("\\|");
								for (final String segment : segments) {
										spec.consume(segment);
								}
						}
						return spec;
				}

				private void consume(final String segment) {
						final String trimmed = (segment != null) ? segment.trim() : "";
						if (trimmed.isEmpty()) {
								return;
						}
						final int eq = trimmed.indexOf('=');
						if (eq <= 0) {
								addOverlay(trimmed);
								return;
						}
						final String key = trimmed.substring(0, eq).trim().toLowerCase(Locale.ROOT);
						final String value = trimmed.substring(eq + 1).trim();
						applyParameter(key, value);
				}

				private void applyParameter(final String key, final String value) {
						if ((value == null) || value.isEmpty()) {
								return;
						}
						switch (key) {
								case "bg":
								case "background":
								case "backgroundcolor":
								case "bgcolor":
										background = sanitizeCssValue(value);
										break;
								case "bgopacity":
								case "bgalpha":
								case "backgroundopacity":
										backgroundOpacity = sanitizeOpacity(value);
										break;
								case "outline":
								case "border":
								case "stroke":
										outline = sanitizeCssValue(value);
										break;
								case "outlinewidth":
								case "strokewidth":
								case "borderwidth":
										outlineWidth = sanitizePixels(value);
										break;
								case "shadow":
								case "glow":
										shadow = sanitizeCssValue(value);
										break;
								case "layers":
								case "layer":
								case "overlays":
										final String[] tokens = value.split("[,+;]");
										for (final String token : tokens) {
												addOverlay(token);
										}
										break;
								case "label":
										label = sanitizeText(value);
										break;
								case "tooltip":
								case "title":
										tooltip = sanitizeText(value);
										break;
								case "labelcolor":
								case "labelcolour":
								case "textcolor":
										labelColor = sanitizeCssValue(value);
										break;
								case "labelbg":
								case "labelbackground":
								case "labelcolour":
										labelBackground = sanitizeCssValue(value);
										break;
								case "class":
								case "classes":
										final String[] classTokens = value.split("[\\n\\r\\t, ]+");
										for (final String css : classTokens) {
												final String sanitized = sanitizeCssIdentifier(css);
												if (!sanitized.isEmpty()) {
														extraClasses.add(sanitized);
												}
										}
										break;
								default:
										addOverlay(value);
										break;
						}
				}

				private void addOverlay(final String token) {
						if (token == null) {
								return;
						}
						final String trimmed = token.trim();
						if (!trimmed.isEmpty()) {
								overlays.add(trimmed);
						}
				}

				boolean hasRenderableLayers() {
						return baseToken != null;
				}

				String getBaseToken() {
						return baseToken;
				}

				String getBaseDisplay() {
						return baseDisplay;
				}

				List<String> getOverlays() {
						return overlays;
				}

				Set<String> getExtraClasses() {
						return extraClasses;
				}

				String getBackground() {
						return background;
				}

				String getBackgroundOpacity() {
						return backgroundOpacity;
				}

				String getOutline() {
						return outline;
				}

				String getOutlineWidth() {
						return outlineWidth;
				}

				String getShadow() {
						return shadow;
				}

				String getLabel() {
						return label;
				}

				String getLabelColor() {
						return labelColor;
				}

				String getLabelBackground() {
						return labelBackground;
				}

				String getTooltip() {
						return tooltip;
				}
		}

        private static final class BundledFontFace {
                private final String family;
                private final String resource;
                private final String weight;
                private final String style;
                private String fileUrl;
                private String dataUrl;
                private boolean attempted;

                private BundledFontFace(final String family, final String resource, final String weight, final String style) {
                        this.family = (family != null) ? family : "";
                        this.resource = resource;
                        this.weight = (weight != null) ? weight : "400";
                        this.style = (style != null) ? style : "normal";
                }

                private synchronized void ensureLoaded() {
                        if (attempted) {
                                return;
                        }
                        attempted = true;
                        if ((resource == null) || resource.isEmpty()) {
                                return;
                        }
                        try (InputStream stream = DataLoader.getResourceAsStream(resource)) {
                                if (stream == null) {
                                        LOGGER.warn("Bundled chat font not found: " + resource);
                                        return;
                                }
                                final ByteArrayOutputStream memory = new ByteArrayOutputStream();
                                File tempFile = null;
                                FileOutputStream fileOut = null;
                                try {
                                        tempFile = File.createTempFile("stendhal-chat-font-", ".ttf");
                                        tempFile.deleteOnExit();
                                        fileOut = new FileOutputStream(tempFile);
                                } catch (final IOException ex) {
                                        LOGGER.warn("Unable to prepare chat font cache file: " + resource, ex);
                                        tempFile = null;
                                        fileOut = null;
                                }
                                final byte[] buffer = new byte[4096];
                                int read;
                                while ((read = stream.read(buffer)) != -1) {
                                        memory.write(buffer, 0, read);
                                        if (fileOut != null) {
                                                try {
                                                        fileOut.write(buffer, 0, read);
                                                } catch (final IOException ex) {
                                                        LOGGER.warn("Failed writing chat font cache file: " + resource, ex);
                                                        try {
                                                                fileOut.close();
                                                        } catch (final IOException closeEx) {
                                                                LOGGER.debug("Unable to close chat font cache stream", closeEx);
                                                        }
                                                        if ((tempFile != null) && !tempFile.delete()) {
                                                                LOGGER.debug("Unable to delete chat font cache file: " + tempFile);
                                                        }
                                                        tempFile = null;
                                                        fileOut = null;
                                                }
                                        }
                                }
                                if (fileOut != null) {
                                        try {
                                                fileOut.flush();
                                                fileOut.close();
                                        } catch (final IOException ex) {
                                                LOGGER.debug("Unable to finalize chat font cache file", ex);
                                        }
                                }
				if ((tempFile != null) && tempFile.isFile()) {
					fileUrl = tempFile.toURI().toASCIIString();
				}
				if (memory.size() > 0) {
					dataUrl = "data:font/ttf;base64," + Base64.getEncoder().encodeToString(memory.toByteArray());
				}
			} catch (final IOException ex) {
                                LOGGER.warn("Failed to load bundled chat font: " + resource, ex);
                        }
                }

                private String ensureFileUrl() {
                        ensureLoaded();
                        return fileUrl;
                }

                private String ensureDataUrl() {
                        ensureLoaded();
                        return dataUrl;
                }

                void appendCss(final StringBuilder css) {
                        if (css == null) {
                                return;
                        }
                        final String fileSource = ensureFileUrl();
                        final String dataSource = ensureDataUrl();
                        if (((fileSource == null) || fileSource.isEmpty())
                                        && ((dataSource == null) || dataSource.isEmpty())) {
                                return;
                        }
                        css.append("@font-face{font-family:'")
                                .append(escapeFontFamily(family))
                                .append("';font-style:")
                                .append(style)
                                .append(";font-weight:")
                                .append(weight)
                                .append(";src:");
                        boolean first = true;
                        if ((fileSource != null) && !fileSource.isEmpty()) {
                                css.append("url(").append(fileSource).append(") format('truetype')");
                                first = false;
                        }
                        if ((dataSource != null) && !dataSource.isEmpty()) {
                                if (!first) {
                                        css.append(',');
                                }
                                css.append("url(").append(dataSource).append(") format('truetype')");
                        }
                        css.append(";font-display:swap;}");
                }
        }

        private static final class EmojiReplacement {
                private final String placeholder;
                private final String html;

                private EmojiReplacement(final String placeholder, final String html) {
                        this.placeholder = placeholder;
                        this.html = html;
                }
        }

        private static final class EmojiImageSource {
                private final String primary;
                private final String fallback;
                private final List<String> alternateRemotes;

                private EmojiImageSource(final String primary, final String fallback, final List<String> alternateRemotes) {
                        this.primary = primary;
                        this.fallback = fallback;
                        if ((alternateRemotes == null) || alternateRemotes.isEmpty()) {
                                this.alternateRemotes = Collections.emptyList();
                        } else {
                                this.alternateRemotes = Collections.unmodifiableList(new ArrayList<String>(alternateRemotes));
                        }
                }

                String getPrimary() {
                        return primary;
                }

                String getFallback() {
                        return fallback;
                }

                boolean hasPrimary() {
                        return (primary != null) && !primary.isEmpty();
                }

                boolean hasFallback() {
                        return (fallback != null) && !fallback.isEmpty() && !fallback.equals(primary);
                }

                boolean hasAlternateRemotes() {
                        return !alternateRemotes.isEmpty();
                }

                List<String> getAlternateRemotes() {
                        return alternateRemotes;
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

        private static String toCssRgba(final Color color) {
                final Color effective = (color != null) ? color : new Color(0x3c, 0x1e, 0x00);
                final double alpha = Math.max(0d, Math.min(1d, effective.getAlpha() / 255d));
                return String.format(Locale.ROOT, "rgba(%d,%d,%d,%.3f)", effective.getRed(), effective.getGreen(), effective.getBlue(), alpha);
        }

        private static javafx.scene.paint.Color toFxColor(final Color color) {
                final Color effective = (color != null) ? color : new Color(0x3c, 0x1e, 0x00);
                final double alpha = Math.max(0d, Math.min(1d, effective.getAlpha() / 255d));
                return javafx.scene.paint.Color.rgb(effective.getRed(), effective.getGreen(), effective.getBlue(), alpha);
        }

        private static Font resolveChatFont() {
                final Font uiFont = findUiFont();
                final int size = ((uiFont != null) && (uiFont.getSize() > 0)) ? uiFont.getSize() : 13;

                final Font preferredArial = createFontIfAvailable("Arial", size);
                if (preferredArial != null) {
                        return preferredArial;
                }

                final Font unicodeArial = createFontIfAvailable("Arial Unicode MS", size);
                if (unicodeArial != null) {
                        return unicodeArial;
                }

                if ((uiFont != null) && supportsPolishCharacters(uiFont)) {
                        return uiFont.deriveFont(Font.PLAIN, size);
                }

                final Font fallbackSegoe = createFontIfAvailable("Segoe UI", size);
                if (fallbackSegoe != null) {
                        return fallbackSegoe;
                }

                final Font fallbackDejaVu = createFontIfAvailable("DejaVu Sans", size);
                if (fallbackDejaVu != null) {
                        return fallbackDejaVu;
                }

                final Font fallbackLiberation = createFontIfAvailable("Liberation Sans", size);
                if (fallbackLiberation != null) {
                        return fallbackLiberation;
                }

                return new Font("Dialog", Font.PLAIN, size);
        }

        private static Font findUiFont() {
                Font font = UIManager.getFont("TextPane.font");
                if (font == null) {
                        font = UIManager.getFont("TextArea.font");
                }
                if (font == null) {
                        font = UIManager.getFont("Label.font");
                }
                return font;
        }

        private static Font createFontIfAvailable(final String family, final int size) {
                if ((family == null) || family.isEmpty()) {
                        return null;
                }
                try {
                        final Font candidate = new Font(family, Font.PLAIN, size);
                        final String resolvedFamily = candidate.getFamily(Locale.ENGLISH);
                        if ((resolvedFamily != null) && resolvedFamily.equalsIgnoreCase(family)
                                        && supportsPolishCharacters(candidate)) {
                                return candidate;
                        }
                } catch (final Exception ex) {
                        LOGGER.debug("Unable to create preferred chat font: " + family, ex);
                }
                return null;
        }

        private static boolean supportsPolishCharacters(final Font font) {
                if (font == null) {
                        return false;
                }
                for (final char ch : POLISH_SAMPLE_CHARS) {
                        if (!font.canDisplay(ch)) {
                                return false;
                        }
                }
                return true;
        }

        private String buildChatFontStack() {
                final java.util.Set<String> families = new LinkedHashSet<String>();
                if (chatFont != null) {
                        addFontFamily(families, chatFont.getFamily());
                        addFontFamily(families, chatFont.getName());
                        addFontFamily(families, chatFont.getPSName());
                }
                addFontFamily(families, "Arial");
                addFontFamily(families, "Arial Unicode MS");
                addFontFamily(families, "Segoe UI");
                addFontFamily(families, "Segoe UI Variable");
                addFontFamily(families, "Segoe UI Symbol");
                addFontFamily(families, "Helvetica");
                addFontFamily(families, "Tahoma");
                addFontFamily(families, "Liberation Sans");
                addFontFamily(families, "DejaVu Sans");
                addFontFamily(families, "Noto Sans");
                addFontFamily(families, "Noto Sans Display");
                addFontFamily(families, "Roboto");
                addFontFamily(families, BUNDLED_CHAT_FONT_FAMILY);
                addFontFamily(families, "Carlito");
                addFontFamily(families, "Amaranth");
                addFontFamily(families, "KonstytucjaPolska");
                addFontFamily(families, "Konstytucja Polska");
                addFontFamily(families, "AntykwaTorunska");
                addFontFamily(families, "Antykwa Torunska");
                addFontFamily(families, "Dialog");

                final StringBuilder stack = new StringBuilder();
                for (final String family : families) {
                        if ((family == null) || family.isEmpty()) {
                                continue;
                        }
                        if (stack.length() > 0) {
                                stack.append(',');
                        }
                        stack.append('\'').append(escapeFontFamily(family)).append('\'');
                }
                if (stack.length() > 0) {
                        stack.append(',');
                }
                stack.append("sans-serif");
                return stack.toString();
        }

        private static void addFontFamily(final java.util.Set<String> families, final String family) {
                if ((families == null) || (family == null)) {
                        return;
                }
                final String trimmed = family.trim();
                if (!trimmed.isEmpty()) {
                        families.add(trimmed);
                }
        }

        private void appendBundledChatFonts(final StringBuilder css) {
                if (css == null) {
                        return;
                }
                for (final BundledFontFace face : BUNDLED_CHAT_FONTS) {
                        if (face != null) {
                                face.appendCss(css);
                        }
                }
        }

        private int chatBodyFontSize() {
                final int size = (chatFont != null) ? chatFont.getSize() - 1 : 13;
                return Math.max(11, size);
        }

        private int timestampFontSize() {
                return Math.max(10, chatBodyFontSize() - 1);
        }

        private static final class FxBridge {
                private final JFXPanel panel;
                private WebEngine engine;
                private WebView webView;
                private BorderPane root;
                private Scene scene;
                private boolean initialized;
                private boolean documentReady;
                private String pendingContent;
                private final List<String> pendingAppends = new ArrayList<String>();
                private String cssBackground;
                private javafx.scene.paint.Color fxBackground;

                private FxBridge(final JFXPanel panel, final Color initialBackground) {
                        this.panel = panel;
                        setSwingBackground(initialBackground);
                        cssBackground = toCssRgba(initialBackground);
                        fxBackground = toFxColor(initialBackground);
                        Platform.setImplicitExit(false);
                        Platform.runLater(this::init);
                }

                private void init() {
                        webView = new WebView();
                        webView.setContextMenuEnabled(false);
                        applyBackgroundStyles();
                        engine = webView.getEngine();
                        engine.setJavaScriptEnabled(true);
			engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue == Worker.State.SUCCEEDED) {
					documentReady = true;
					flushPendingAppends();
				}
			});

                        root = new BorderPane(webView);
                        applyBackgroundStyles();
                        scene = new Scene(root);
                        applyBackgroundStyles();
                        panel.setScene(scene);
                        initialized = true;

			if (pendingContent != null) {
				documentReady = false;
				pendingAppends.clear();
                                engine.loadContent(pendingContent, "text/html; charset=UTF-8");
                                pendingContent = null;
                        }
                }

		private void applyBackgroundStyles() {
			final String css = cssBackground;
			final javafx.scene.paint.Color fill = (fxBackground != null)
				? fxBackground
				: javafx.scene.paint.Color.TRANSPARENT;
			final Background backgroundFill = new Background(new BackgroundFill(fill, CornerRadii.EMPTY, Insets.EMPTY));
			if (webView != null) {
				if ((css != null) && !css.isEmpty()) {
					webView.setStyle("-fx-background-color: " + css + ";");
				} else {
					webView.setStyle("-fx-background-color: transparent;");
				}
				webView.setBackground(backgroundFill);
			}
			if (root != null) {
				if ((css != null) && !css.isEmpty()) {
					root.setStyle("-fx-background-color: " + css + ";");
				} else {
					root.setStyle("-fx-background-color: transparent;");
				}
				root.setBackground(backgroundFill);
			}
			if (scene != null) {
				scene.setFill(fill);
			}
		}

		private void setSwingBackground(final Color color) {
			final Color effective = (color != null) ? color : new Color(0x3c, 0x1e, 0x00);
			final Runnable update = () -> {
				panel.setOpaque(true);
				panel.setBackground(effective);
				panel.repaint();
			};
                        if (SwingUtilities.isEventDispatchThread()) {
                                update.run();
                        } else {
                                SwingUtilities.invokeLater(update);
                        }
                }

                private void updateBackgroundState(final Color color) {
                        cssBackground = toCssRgba(color);
                        fxBackground = toFxColor(color);
                        setSwingBackground(color);
                        Platform.runLater(this::applyBackgroundStyles);
                }

                void setBackground(final Color color) {
                        updateBackgroundState(color);
                }

                private void setContent(final String html) {
                        Platform.runLater(() -> {
                                if (!initialized) {
					pendingContent = html;
					return;
				}
				documentReady = false;
				pendingAppends.clear();
                                engine.loadContent(html, "text/html; charset=UTF-8");
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
