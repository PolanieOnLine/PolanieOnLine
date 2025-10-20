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
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.json.simple.JSONObject;
import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.util.JSONLoader;
import games.stendhal.client.sprite.DataLoader;

public class EmojiStore {
	private static EmojiStore instance;

	private List<String> emojilist;
	private Map<String, String> emojimap;
	private Map<String, String> emojiGlyphs;
	private Map<String, Icon> emojiIcons;
	private int longestKeyLength;

	private static final String pathPrefix = "data/sprites/emoji/";
	private static final Logger logger = Logger.getLogger(EmojiStore.class);
	private static final String DEFAULT_EMOJI_FONT = Font.SANS_SERIF;

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
		emojiIcons = new HashMap<>();
		longestKeyLength = 0;
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
						recordKeyLength(key);
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
	*     <code>Sprite</code> backed by the emoji image or <code>null</code> if unavailable.
	*/
	public Sprite create(final String text) {
		final String name = check(text);
		if (name == null) {
			return null;
		}
		return ClientSingletonRepository.getSpriteStore().getSprite(pathPrefix + name + ".png");
	}

	public Icon getIcon(final String text) {
		final String name = check(text);
		if (name == null) {
			return null;
		}
		final Icon cached = emojiIcons.get(name);
		if (cached != null) {
			return cached;
		}
		final String path = pathPrefix + name + ".png";
		final URL resource = DataLoader.getResource(path);
		if (resource == null) {
			logger.warn("Emoji icon not found: " + path);
			return null;
		}
		final ImageIcon icon = new ImageIcon(resource);
		emojiIcons.put(name, icon);
		return icon;
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
		if (glyph == null) {
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

	public String absPath(final String name) {
		final String checked = check(name);
		if (checked != null) {
			return pathPrefix + checked + ".png";
		}
		return null;
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
		return DEFAULT_EMOJI_FONT;
	}

	public static Font deriveFont(final int style, final int size) {
		return new Font(DEFAULT_EMOJI_FONT, style, size);
	}
}
