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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

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

	/**
	* Create a new ChatTextSink.
	*
	* @param document destination document
	* @param emojiStyle base style to apply for emoji glyphs
	*/
	public ChatTextSink(Document document, Style emojiStyle) {
		this.document = document;
		this.emojiStyle = emojiStyle;
	}

	@Override
	public void append(String s, StyleSet attrs) {
		try {
			if ((s == null) || s.isEmpty()) {
				return;
			}

			final EmojiStore store = EmojiStore.get();
			final int length = s.length();
			final StringBuilder plain = new StringBuilder();
			StyleSet emojiAttrs = null;
			int index = 0;

			while (index < length) {
				final EmojiStore.EmojiMatch match = store.matchEmoji(s, index);
				if (match != null) {
					if (plain.length() > 0) {
						document.insertString(document.getLength(), plain.toString(), attrs.contents());
						plain.setLength(0);
					}

					if (emojiAttrs == null) {
						emojiAttrs = buildEmojiAttributes(attrs);
					}

					document.insertString(document.getLength(), match.getGlyph(), emojiAttrs.contents());
					index += match.getConsumedLength();
					continue;
				}

				final int codePoint = s.codePointAt(index);
				plain.appendCodePoint(codePoint);
				index += Character.charCount(codePoint);
			}

			if (plain.length() > 0) {
				document.insertString(document.getLength(), plain.toString(), attrs.contents());
			}
		} catch (BadLocationException e) {
			logger.error("Failed to insert text.", e);
		}
	}

	/**
	* Builds a copy of the provided attributes with emoji-specific font settings applied.
	*
	* @param attrs
	*     Style set to copy.
	* @return
	*     Style set with emoji font attributes.
	*/
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
