/***************************************************************************
 *                    Copyright © 2003-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sprite;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.util.JSONLoader;

public class EmojiStore {
	private static EmojiStore instance;

	private List<String> emojilist;
	private Map<String, String> emojimap;
	private Map<String, String> emojiGlyphs;
	private Map<String, RenderedEmoji> emojiCache;
	private int longestKeyLength;

	private Font baseEmojiFont;
	private String emojiFontFamily;
	private FontRenderContext fontRenderContext;
	private byte[] bundledFontBytes;
	private boolean usingBundledFont;
	private EmojiBitmapExtractor bitmapExtractor;

	private static final Logger logger = Logger.getLogger(EmojiStore.class);
	private static final String DEFAULT_EMOJI_FONT = Font.SANS_SERIF;
	private static final String EMOJI_JSON_PATH = "data/sprites/emoji/emojis.json";
	private static final String EMOJI_IMAGE_PATH = "data/sprites/emoji/";
	private static final String BUNDLED_FONT_PATH = "data/font/NotoColorEmoji-Regular.ttf";
	private static final String SAMPLE_GLYPH = "\uD83D\uDE03";
	private static final String[] FALLBACK_FONT_FAMILIES = {
		"Segoe UI Emoji",
		"Apple Color Emoji",
		"Noto Color Emoji",
		"Twitter Color Emoji",
		"EmojiOne Color",
		"Android Emoji"
	};
	private static final float DEFAULT_EMOJI_SIZE = 28f;
	private static final float ICON_POINT_SIZE = 22f;
	private static final float SPRITE_POINT_SIZE = 48f;
	private static final int ICON_PADDING = 4;
	private static final char VARIATION_SELECTOR_TEXT = '\uFE0E';
	private static final char VARIATION_SELECTOR_EMOJI = '\uFE0F';

	private static final class RenderedEmoji {
		private final String glyph;
		private final Icon icon;
		private final Sprite sprite;

		private RenderedEmoji(final String glyph, final Icon icon, final Sprite sprite) {
			this.glyph = glyph;
			this.icon = icon;
			this.sprite = sprite;
		}
	}

	public static final class EmojiMatch {
		private final String name;
		private final String glyph;
		private final int consumedLength;

		private EmojiMatch(final String name, final String glyph, final int consumedLength) {
			this.name = name;
			this.glyph = glyph;
			this.consumedLength = consumedLength;
		}

		public String getName() {
			return name;
		}

		public String getGlyph() {
			return glyph;
		}

		public int getConsumedLength() {
			return consumedLength;
		}
	}

	public static EmojiStore get() {
		if (instance == null) {
			instance = new EmojiStore();
		}
		return instance;
	}

	/**
	* Singleton.
	*/
	private EmojiStore() {
		emojilist = new LinkedList<>();
		emojimap = new HashMap<>();
		emojiGlyphs = new LinkedHashMap<>();
		emojiCache = new HashMap<>();
		longestKeyLength = 0;
			fontRenderContext = new FontRenderContext(null, true, true);
	}

	/**
	* Loads emoji data from JSON.
	*/
	public void init() {
		final JSONLoader loader = new JSONLoader();
		loader.onDataReady = new Runnable() {
			@Override
			public void run() {
				final JSONObject document = (JSONObject) loader.data;
				final Object el = document.get("emojilist");
				final Object em = document.get("emojimap");
				if ((el != null) && (el instanceof List<?>)) {
					for (final Object k: (List<?>) el) {
						emojilist.add((String) k);
					}
				}
				if ((em != null) && (em instanceof Map<?, ?>)) {
					for (final Map.Entry<?, ?> entry: ((Map<?, ?>) em).entrySet()) {
						final String key = (String) entry.getKey();
						final String value = (String) entry.getValue();
						emojimap.put(key, value);
						recordKeyLength(key);
						if (looksLikeGlyph(key)) {
							emojiGlyphs.putIfAbsent(value, key);
						}
					}
				}
				buildFallbackGlyphs();
				ensureEmojiFont();
			}
		};
		loader.load(EMOJI_JSON_PATH);
	}

	private void buildFallbackGlyphs() {
		for (final String name: emojilist) {
			if (!emojiGlyphs.containsKey(name)) {
				emojiGlyphs.put(name, ":" + name + ":");
			}
		}
	}

	private void ensureEmojiFont() {
		if (baseEmojiFont != null) {
			return;
		}

		boolean bundled = false;
		Font loaded = loadBundledEmojiFont();
		if (loaded != null) {
			if (isValidEmojiFont(loaded)) {
				bundled = true;
			} else {
				logger.warn("Bundled emoji font cannot display sample glyphs; ignoring bundled font");
				loaded = null;
			}
		}

		if (loaded == null) {
			loaded = findSystemEmojiFont();
		}

		if (loaded == null) {
			loaded = new Font(DEFAULT_EMOJI_FONT, Font.PLAIN, Math.round(DEFAULT_EMOJI_SIZE));
		}

		usingBundledFont = bundled;
		baseEmojiFont = loaded.deriveFont(Font.PLAIN, DEFAULT_EMOJI_SIZE);
		emojiFontFamily = baseEmojiFont.getFontName(Locale.ENGLISH);
		initializeBitmapExtractor(baseEmojiFont);
	}

	private void initializeBitmapExtractor(final Font font) {
		if ((font == null) || (bitmapExtractor != null)) {
			return;
		}

		if (!usingBundledFont || (bundledFontBytes == null)) {
			return;
		}

		final byte[] cblc = extractBundledFontTable("CBLC");
		final byte[] cbdt = extractBundledFontTable("CBDT");
		if ((cblc != null) && (cbdt != null)) {
			bitmapExtractor = new EmojiBitmapExtractor(font, fontRenderContext, cblc, cbdt);
		} else {
			logger.debug("Bundled emoji font does not expose CBLC/CBDT tables; skipping bitmap extraction");
		}
	}

	private Font findSystemEmojiFont() {
		try {
			final String[] availableFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			final List<String> families = Arrays.asList(availableFamilies);
			for (final String family: FALLBACK_FONT_FAMILIES) {
				if (families.contains(family)) {
					final Font candidate = new Font(family, Font.PLAIN, Math.round(DEFAULT_EMOJI_SIZE));
					if (isValidEmojiFont(candidate)) {
						return candidate;
					}
				}
			}
		} catch (HeadlessException e) {
			logger.warn("Unable to query system emoji fonts", e);
		}
		return null;
	}


	private boolean isValidEmojiFont(final Font font) {
		if (font == null) {
			return false;
		}
		return font.canDisplayUpTo(SAMPLE_GLYPH) == -1;
	}

	private Font loadBundledEmojiFont() {
		if (bundledFontBytes == null) {
			try (InputStream stream = DataLoader.getResourceAsStream(BUNDLED_FONT_PATH)) {
				if (stream == null) {
					logger.warn("Bundled emoji font not found: " + BUNDLED_FONT_PATH);
					return null;
				}
				bundledFontBytes = readAllBytes(stream);
			} catch (IOException e) {
				logger.warn("Unable to read bundled emoji font", e);
				bundledFontBytes = null;
			}
		}

		if (bundledFontBytes == null) {
			return null;
		}

		try {
			final Font base = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(bundledFontBytes));
			try {
				GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(base);
			} catch (Exception e) {
				logger.debug("Failed to register bundled emoji font", e);
			}
			return base;
		} catch (FontFormatException | IOException e) {
			logger.warn("Unable to load bundled emoji font", e);
			return null;
		}
	}

	private static byte[] readAllBytes(final InputStream stream) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		final byte[] buffer = new byte[4096];
		int read;
		while ((read = stream.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
		return out.toByteArray();
	}

	private byte[] extractBundledFontTable(final String tag) {
		if ((bundledFontBytes == null) || (tag == null) || (tag.length() != 4)) {
			return null;
		}

		final ByteBuffer buffer = ByteBuffer.wrap(bundledFontBytes).order(ByteOrder.BIG_ENDIAN);
		if (buffer.remaining() < 12) {
			return null;
		}

		buffer.getInt();
		final int numTables = Short.toUnsignedInt(buffer.getShort());
		buffer.getShort();
		buffer.getShort();
		buffer.getShort();

		final int tableRecordsOffset = 12;
		final int targetTag = tagToInt(tag);
		for (int index = 0; index < numTables; index++) {
			final int entryOffset = tableRecordsOffset + (index * 16);
			if ((entryOffset + 16) > bundledFontBytes.length) {
				break;
			}
			buffer.position(entryOffset);
			final int entryTag = buffer.getInt();
			buffer.getInt();
			final int offset = buffer.getInt();
			final int length = buffer.getInt();
			if (entryTag == targetTag) {
				if ((offset < 0) || (length <= 0) || (((long) offset + length) > bundledFontBytes.length)) {
					return null;
				}
				return Arrays.copyOfRange(bundledFontBytes, offset, offset + length);
			}
		}
		return null;
	}

	private static int tagToInt(final String tag) {
		return ((tag.charAt(0) & 0xFF) << 24)
			| ((tag.charAt(1) & 0xFF) << 16)
			| ((tag.charAt(2) & 0xFF) << 8)
			| (tag.charAt(3) & 0xFF);
	}

	private void recordKeyLength(final String key) {
		if (key != null) {
			longestKeyLength = Math.max(longestKeyLength, key.length());
		}
	}

	private boolean looksLikeGlyph(final String key) {
		if (key == null || key.isEmpty()) {
			return false;
		}
		final int codePoint = key.codePointAt(0);
		return codePoint > 0xFF;
	}

	/**
	* Creates an emoji sprite.
	*
	* @param text
	*     Text representing emoji.
	* @return
	*     <code>Sprite</code> backed by a rasterized emoji glyph or <code>null</code> if unavailable.
	*/
	public Sprite create(final String text) {
		final RenderedEmoji rendered = renderEmoji(text);
		if ((rendered == null) || (rendered.sprite == null)) {
			return null;
		}
		return rendered.sprite;
	}

	public Icon getIcon(final String text) {
		final RenderedEmoji rendered = renderEmoji(text);
		if ((rendered == null) || (rendered.icon == null)) {
			return null;
		}
		return rendered.icon;
	}

	private String ensureEmojiPresentation(final String glyph) {
		if ((glyph == null) || glyph.isEmpty()) {
			return glyph;
		}
		if ((glyph.indexOf(VARIATION_SELECTOR_TEXT) != -1) || (glyph.indexOf(VARIATION_SELECTOR_EMOJI) != -1)) {
			return glyph;
		}
		if (glyph.codePointCount(0, glyph.length()) != 1) {
			return glyph;
		}
		final int codePoint = glyph.codePointAt(0);
		if (codePoint < 0x1F000) {
			return glyph + VARIATION_SELECTOR_EMOJI;
		}
		return glyph;
	}

	private RenderedEmoji renderEmoji(final String text) {
		if (text == null) {
			return null;
		}
		final String name = check(text);
		if (name == null) {
			return null;
		}
		RenderedEmoji cached = emojiCache.get(name);
		if (cached != null) {
			return cached;
		}

		ensureEmojiFont();
		final String glyph = ensureEmojiPresentation(emojiGlyphs.getOrDefault(name, ":" + name + ":"));
		BufferedImage iconImage = null;
		BufferedImage spriteImage = null;
		if (bitmapExtractor != null) {
			iconImage = bitmapExtractor.renderGlyph(glyph, ICON_POINT_SIZE, ICON_PADDING);
			spriteImage = bitmapExtractor.renderGlyph(glyph, SPRITE_POINT_SIZE, ICON_PADDING);
		}
		if (iconImage == null) {
			iconImage = rasterizeGlyph(glyph, ICON_POINT_SIZE);
		}
		if (spriteImage == null) {
			spriteImage = rasterizeGlyph(glyph, SPRITE_POINT_SIZE);
		}
		if ((iconImage == null) || (spriteImage == null)) {
			final BufferedImage fallback = loadEmojiAsset(name);
			if (fallback != null) {
				if (iconImage == null) {
					iconImage = fallback;
				}
				if (spriteImage == null) {
					spriteImage = fallback;
				}
			}
		}
		final Icon icon = (iconImage != null) ? new ImageIcon(iconImage) : null;
		final Sprite sprite = (spriteImage != null) ? new ImageSprite(spriteImage, name) : null;
		cached = new RenderedEmoji(glyph, icon, sprite);
		emojiCache.put(name, cached);
		return cached;
	}
	private BufferedImage loadEmojiAsset(final String name) {
		final String resource = EMOJI_IMAGE_PATH + name + ".png";
		try (InputStream stream = DataLoader.getResourceAsStream(resource)) {
			if (stream == null) {
				return null;
			}
			return ImageIO.read(stream);
		} catch (IOException e) {
			logger.warn("Unable to load fallback emoji asset: " + resource, e);
			return null;
		}
	}

	private BufferedImage rasterizeGlyph(final String glyph, final float fontSize) {
		if ((glyph == null) || glyph.isEmpty()) {
			return null;
		}
		ensureEmojiFont();
		if (baseEmojiFont == null) {
			return null;
		}

		final Font font = baseEmojiFont.deriveFont(Font.PLAIN, fontSize);
		final TextLayout layout = new TextLayout(glyph, font, fontRenderContext);
		Rectangle pixelBounds = layout.getPixelBounds(fontRenderContext, 0f, 0f);
		if ((pixelBounds.width <= 0) || (pixelBounds.height <= 0)) {
			final Rectangle2D visualBounds = layout.getBounds();
			if ((visualBounds.getWidth() > 0d) && (visualBounds.getHeight() > 0d)) {
				pixelBounds = new Rectangle(
					(int) Math.floor(visualBounds.getX()),
					(int) Math.floor(visualBounds.getY()),
					Math.max(1, (int) Math.ceil(visualBounds.getWidth())),
					Math.max(1, (int) Math.ceil(visualBounds.getHeight()))
				);
			} else {
				final float ascent = layout.getAscent();
				final float descent = layout.getDescent();
				final float advance = layout.getAdvance();
				final float fallbackWidth = (advance > 0f) ? advance : fontSize;
				final float fallbackHeight = ((ascent + descent) > 0f) ? (ascent + descent) : fontSize;
				pixelBounds = new Rectangle(
					0,
					(int) Math.floor(-ascent),
					Math.max(1, (int) Math.ceil(fallbackWidth)),
					Math.max(1, (int) Math.ceil(fallbackHeight))
				);
			}
		}
		final int width = Math.max(1, pixelBounds.width + (ICON_PADDING * 2));
		final int height = Math.max(1, pixelBounds.height + (ICON_PADDING * 2));
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();
		try {
			g2d.setComposite(AlphaComposite.Clear);
			g2d.fillRect(0, 0, width, height);
			g2d.setComposite(AlphaComposite.SrcOver);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
			g2d.setFont(font);
			final float drawX = ICON_PADDING - pixelBounds.x;
			final float drawY = ICON_PADDING - pixelBounds.y;
			layout.draw(g2d, drawX, drawY);
			} finally {
				g2d.dispose();
		}
		return image;
	}

	/**
	* Retrieves glyph string for the provided emoji text.
	*
	* @param text
	*     Text representing emoji.
	* @return
	*     Glyph string or <code>null</code> if unavailable.
	*/
	public String glyphFor(final String text) {
		final String name = check(text);
		if (name == null) {
			return null;
		}
		final String glyph = emojiGlyphs.get(name);
		if (glyph == null) {
			return null;
		}
		return ensureEmojiPresentation(glyph);
	}

	public EmojiMatch matchEmoji(final CharSequence text, final int index) {
		if ((text == null) || (index < 0) || (index >= text.length()) || (longestKeyLength == 0)) {
			return null;
		}

		final int maxEnd = Math.min(text.length(), index + longestKeyLength);
		for (int end = maxEnd; end > index; end--) {
			final String candidate = text.subSequence(index, end).toString();
			final int consumed = end - index;
			final EmojiMatch direct = matchCandidate(candidate, consumed);
			if (direct != null) {
				return direct;
			}
			final String normalized = stripVariationSelectors(candidate);
			if (!normalized.isEmpty() && !normalized.equals(candidate)) {
				final EmojiMatch normalizedMatch = matchCandidate(normalized, consumed);
				if (normalizedMatch != null) {
					return normalizedMatch;
				}
			}
		}

		return null;
	}

	private EmojiMatch matchCandidate(final String candidate, final int consumedChars) {
		final String name = emojimap.get(candidate);
		if (name == null) {
			return null;
		}
		String glyph = emojiGlyphs.get(name);
		if (glyph != null) {
			glyph = ensureEmojiPresentation(glyph);
		} else {
			glyph = ":" + name + ":";
		}
		return new EmojiMatch(name, glyph, consumedChars);
	}

	private static String stripVariationSelectors(final String text) {
		if ((text == null) || (text.indexOf("\uFE0E") == -1 && text.indexOf("\uFE0F") == -1)) {
			return text;
		}
		final StringBuilder builder = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			final char ch = text.charAt(i);
			if ((ch == '\uFE0E') || (ch == '\uFE0F')) {
				continue;
			}
			builder.append(ch);
		}
		return builder.toString();
	}

	/**
	* Checks if text represents an emoji.
	*
	* @param text
	*     Text to be checked.
	* @return
	*     String representing emoji identifier or <code>null</code>.
	*/
	public String check(String text) {
		text = text.replace("\\\\", "\\");
		String name = emojimap.containsKey(text) ? emojimap.get(text) : null;
		if (name == null && (text.startsWith(":") && text.endsWith(":"))) {
			text = text.substring(0, text.length() - 1).substring(1);
			if (isAvailable(text)) {
				name = text;
			}
		}
		return name;
	}

	/**
	* Checks if an emoji is registered.
	*
	* @param name
	*     Text representing emoji image filename.
	* @return
	*     <code>true</code> if name is registered.
	*/
	public boolean isAvailable(String name) {
		if (name.startsWith(":") && name.endsWith(":")) {
			name = name.substring(1, name.length()-1);
		}
		return emojilist.contains(name);
	}

	/**
	* Get a list of available emojis.
	*
	* @return
	*     A copy of the emoji list.
	*/
	public List<String> getEmojiList() {
		return new LinkedList<String>() {{ addAll(emojilist); }};
	}

	public static String getFontFamily() {
		final EmojiStore store = get();
		store.ensureEmojiFont();
		if ((store.emojiFontFamily != null) && !store.emojiFontFamily.isEmpty()) {
			return store.emojiFontFamily;
		}
		return DEFAULT_EMOJI_FONT;
	}

	public static Font deriveFont(final int style, final int size) {
		final EmojiStore store = get();
		store.ensureEmojiFont();
		if (store.baseEmojiFont != null) {
			return store.baseEmojiFont.deriveFont(style, (float) size);
		}
		return new Font(DEFAULT_EMOJI_FONT, style, size);
	}
}
