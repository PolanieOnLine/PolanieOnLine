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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import org.json.simple.JSONObject;
import org.apache.log4j.Logger;

import games.stendhal.client.util.JSONLoader;
import games.stendhal.client.sprite.DataLoader;

public class EmojiStore {
	private static EmojiStore instance;

	private List<String> emojilist;
	private Map<String, String> emojimap;
	private Map<String, String> emojiGlyphs;

	private static final String pathPrefix = "data/sprites/emoji/";
	private static final String fontPrefix = "data/font/";
	private static final Logger logger = Logger.getLogger(EmojiStore.class);

	private static final String PRIMARY_EMOJI_FONT = "Noto Color Emoji";
	private static final String FALLBACK_EMOJI_FONT = "Noto Emoji";
	private static final int EMOJI_FONT_SIZE = 28;
	private static final Font EMOJI_FONT = resolveEmojiFont();
	private static final FontRenderContext FONT_CONTEXT;

	static {
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FONT_CONTEXT = graphics.getFontRenderContext();
		graphics.dispose();
	}

	private static Font resolveEmojiFont() {
		Font font = createSystemFont(PRIMARY_EMOJI_FONT);
		if (font != null) {
			return font;
		}
		font = loadBundledFont("NotoColorEmoji-Regular.ttf");
		if (font != null) {
			return font;
		}
		font = createSystemFont(FALLBACK_EMOJI_FONT);
		if (font != null) {
			return font;
		}
		font = loadBundledFont("NotoEmoji-Regular.ttf");
		if (font != null) {
			return font;
		}
		return new Font(Font.SANS_SERIF, Font.PLAIN, EMOJI_FONT_SIZE);
	}

	private static boolean hasFontFamily(final String name) {
		final String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		for (final String family: families) {
			if (name.equals(family)) {
				return true;
			}
		}
		return false;
	}

	private static Font createSystemFont(final String family) {
		if (hasFontFamily(family)) {
			return new Font(family, Font.PLAIN, EMOJI_FONT_SIZE);
		}
		return null;
	}

	private static Font loadBundledFont(final String resource) {
		final String path = fontPrefix + resource;
		try (InputStream stream = DataLoader.getResourceAsStream(path)) {
			if (stream == null) {
				logger.warn("Emoji font resource not found: " + path);
				return null;
			}
			Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
			return font.deriveFont(Font.PLAIN, (float) EMOJI_FONT_SIZE);
		} catch (FontFormatException | IOException e) {
			logger.error("Failed to load emoji font '" + resource + "'", e);
			return null;
		}
	}

	public static String getFontFamily() {
		return EMOJI_FONT.getFamily();
	}

	public static Font deriveFont(final int style, final int size) {
		return EMOJI_FONT.deriveFont(style, (float) size);
	}

	private static final class EmojiFontSprite implements Sprite {
		private final String glyph;
		private final int width;
		private final int height;
		private final int ascent;

		EmojiFontSprite(final String glyph) {
			this.glyph = glyph;
			Rectangle2D bounds = EMOJI_FONT.getStringBounds(glyph, FONT_CONTEXT);
			LineMetrics metrics = EMOJI_FONT.getLineMetrics(glyph, FONT_CONTEXT);
			width = (int) Math.ceil(bounds.getWidth());
			height = (int) Math.ceil(metrics.getHeight());
			ascent = Math.round(metrics.getAscent());
		}

		@Override
		public Sprite createRegion(final int x, final int y, final int width,
				final int height, final Object ref) {
			return this;
		}

		@Override
		public void draw(final java.awt.Graphics g, final int x, final int y) {
			draw(g, x, y, 0, 0, width, height);
		}

		@Override
		public void draw(final java.awt.Graphics g, final int destx, final int desty,
				final int x, final int y, final int w, final int h) {
			Graphics2D g2d = (Graphics2D) g;
			Font oldFont = g2d.getFont();
			Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2d.setFont(EMOJI_FONT);
			g2d.drawString(glyph, destx, desty + ascent);
			g2d.setFont(oldFont);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldAA);
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public Object getReference() {
			return glyph;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public boolean isConstant() {
			return true;
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
				if (el != null && el instanceof List<?>) {
					for (final Object k: (List<?>) el) {
						emojilist.add((String) k);
					}
				}
				if (em != null && em instanceof Map<?, ?>) {
					for (final Map.Entry<?, ?> e: ((Map<?, ?>) em).entrySet()) {
						final String key = (String) e.getKey();
						final String value = (String) e.getValue();
						emojimap.put(key, value);
						if (looksLikeGlyph(key)) {
							emojiGlyphs.putIfAbsent(value, key);
						}
					}
				}
				buildFallbackGlyphs();
			}
		};
		loader.load(pathPrefix + "emojis.json");
	}

	private void buildFallbackGlyphs() {
		for (final String name: emojilist) {
			if (!emojiGlyphs.containsKey(name)) {
				emojiGlyphs.put(name, ":" + name + ":");
			}
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
	 * Creates an emoji sprite backed by a font glyph.
	 *
	 * @param text
	 *     Text representing emoji.
	 * @return
	 *     Sprite that renders the emoji or <code>null</code> if emoji isn't available.
	 */
	public Sprite create(final String text) {
		final String glyph = glyphFor(text);
		if (glyph == null) {
			return null;
		}
		return new EmojiFontSprite(glyph);
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
		return emojiGlyphs.get(name);
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
}
