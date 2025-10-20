/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.chatlog;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import games.stendhal.client.gui.textformat.AttributedTextSink;
import games.stendhal.client.gui.textformat.StyleSet;
import games.stendhal.client.sprite.EmojiStore;

/**
 * AttributedTextSink for writing to a styled document.
 */
public class ChatTextSink implements AttributedTextSink<StyleSet> {
	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(ChatTextSink.class);

	/** Destination document. */
	private final Document document;

	/** Emoji style template. */
	private final Style emojiStyle;

	/** Styled document reference for inserting icons when available. */
	private final StyledDocument styledDocument;

	private static final class Segment {
		private final String text;
		private final EmojiStore.EmojiMatch match;
		private final String token;

		private Segment(final String text, final EmojiStore.EmojiMatch match, final String token) {
			this.text = text;
			this.match = match;
			this.token = token;
		}

		public static Segment text(final String text) {
			return new Segment(text, null, null);
		}

		public static Segment emoji(final EmojiStore.EmojiMatch match, final String token) {
			return new Segment(null, match, token);
		}

		public boolean isEmoji() {
			return match != null;
		}

		public String getText() {
			return text;
		}

		public EmojiStore.EmojiMatch getMatch() {
			return match;
		}

		public String getToken() {
			return token;
		}
	}

	/**
	* Create a new ChatTextSink.
	*
	* @param document destination document
	* @param emojiStyle base style to apply for emoji glyphs
	*/
	public ChatTextSink(Document document, Style emojiStyle) {
		this.document = document;
		this.emojiStyle = emojiStyle;
		this.styledDocument = (document instanceof StyledDocument) ? (StyledDocument) document : null;
	}

	@Override
	public void append(String s, StyleSet attrs) {
		try {
			if ((s == null) || s.isEmpty()) {
				return;
			}

			final EmojiStore store = EmojiStore.get();
			final List<Segment> segments = segmentText(store, s);
			StyleSet emojiAttrs = null;

			for (final Segment segment : segments) {
				if (!segment.isEmoji()) {
					document.insertString(document.getLength(), segment.getText(), attrs.contents());
					continue;
				}

				boolean insertedIcon = false;
				if (styledDocument != null) {
					final Icon icon = store.getIcon(segment.getToken());
					if (icon != null) {
						final SimpleAttributeSet iconAttrs = new SimpleAttributeSet();
						StyleConstants.setIcon(iconAttrs, icon);
						styledDocument.insertString(styledDocument.getLength(), "\uFFFC", iconAttrs);
						insertedIcon = true;
				}
			}

				if (!insertedIcon) {
					if (emojiAttrs == null) {
						emojiAttrs = buildEmojiAttributes(attrs);
					}
					final String glyph = segment.getMatch().getGlyph();
					document.insertString(document.getLength(), (glyph != null) ? glyph : segment.getToken(), emojiAttrs.contents());
				}
			}
		} catch (BadLocationException e) {
			logger.error("Failed to insert text.", e);
		}
	}

	private List<Segment> segmentText(final EmojiStore store, final String text) {
		final List<Segment> segments = new ArrayList<>();
		if ((text == null) || text.isEmpty()) {
			return segments;
		}

		final StringBuilder buffer = new StringBuilder();
		int index = 0;
		final int length = text.length();
		while (index < length) {
			final EmojiStore.EmojiMatch match = store.matchEmoji(text, index);
			if (match != null) {
				if (buffer.length() > 0) {
					segments.add(Segment.text(buffer.toString()));
					buffer.setLength(0);
				}
				final int consumed = match.getConsumedLength();
				final String token = text.substring(index, index + consumed);
				segments.add(Segment.emoji(match, token));
				index += consumed;
				continue;
			}
			final int codePoint = text.codePointAt(index);
			buffer.appendCodePoint(codePoint);
			index += Character.charCount(codePoint);
		}

		if (buffer.length() > 0) {
			segments.add(Segment.text(buffer.toString()));
		}

		return segments;
	}

	private StyleSet buildEmojiAttributes(final StyleSet attrs) {
		final StyleSet emojiAttrs = attrs.copy();

		emojiAttrs.setAttribute(StyleConstants.FontFamily, EmojiStore.getFontFamily());

		int fontSize = StyleConstants.getFontSize(attrs.contents());
		if (emojiStyle != null) {
			fontSize = StyleConstants.getFontSize(emojiStyle);
			emojiAttrs.setAttribute(StyleConstants.Underline, StyleConstants.isUnderline(emojiStyle));
		}
		emojiAttrs.setAttribute(StyleConstants.FontSize, fontSize);
		emojiAttrs.setAttribute(StyleConstants.Bold, Boolean.FALSE);
		emojiAttrs.setAttribute(StyleConstants.Italic, Boolean.FALSE);

		return emojiAttrs;
	}
}
