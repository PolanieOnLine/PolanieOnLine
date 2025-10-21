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
package games.stendhal.client.gui.chattext;

import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.apache.log4j.Logger;

import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.sprite.EmojiStore.EmojiMatch;

/**
* Text component that renders emoji as inline icons while keeping the
* underlying text available for message processing.
*/
class EmojiTextPane extends JTextPane {
	private static final long serialVersionUID = 2052826065122130468L;

	private static final Logger LOGGER = Logger.getLogger(EmojiTextPane.class);

	/** Placeholder character Swing uses for icon elements. */
	private static final char ICON_PLACEHOLDER = '\uFFFC';

	/** Attribute key storing the original emoji token. */
	private static final String TOKEN_ATTRIBUTE = "emojiToken";

	interface PlainTextSupport {
		int getPlainLength();

		int plainLengthForRange(int docOffset, int docLength);

		int plainLengthForText(String text);
	}

	private final EmojiDocument document;

	EmojiTextPane() {
		document = new EmojiDocument();
		setDocument(document);
		setMargin(new Insets(2, 4, 2, 4));
		setFocusTraversalKeysEnabled(false);
	}

	EmojiDocument getEmojiDocument() {
		return document;
	}

	@Override
	public String getText() {
		return document.getPlainText();
	}

	@Override
	public void setText(final String text) {
		document.replaceAll(text);
	}

	static final class EmojiDocument extends DefaultStyledDocument implements PlainTextSupport {
		private static final long serialVersionUID = 2550027924476934076L;

		private final StringBuilder plainText = new StringBuilder();

		EmojiDocument() {
			super();
		}

		@Override
		public void insertString(final int offset, final String str, final AttributeSet attr) throws BadLocationException {
			if ((str == null) || str.isEmpty()) {
				return;
			}

			final int safeOffset = Math.max(0, Math.min(offset, getLength()));
			final int plainOffset = plainIndexForDocumentOffset(safeOffset);
			final EmojiStore store = EmojiStore.get();
			int docPos = safeOffset;
			int plainPos = plainOffset;
			int index = 0;
			while (index < str.length()) {
				final int codePoint = str.codePointAt(index);
				if ((codePoint == '\n') || (codePoint == '\r')) {
					index += Character.charCount(codePoint);
					continue;
				}
				final EmojiMatch match = store.matchEmoji(str, index);
				if (match != null) {
					final int consumed = match.getConsumedLength();
					final String token = str.substring(index, index + consumed);
					if (!insertEmoji(docPos, plainPos, token)) {
						insertPlainText(docPos, plainPos, token);
						final int length = token.length();
						docPos += length;
						plainPos += length;
					} else {
						docPos += 1;
						plainPos += token.length();
					}
					index += consumed;
					continue;
				}
				final String segment = new String(Character.toChars(codePoint));
				if (insertEmoji(docPos, plainPos, segment)) {
					docPos += 1;
					plainPos += segment.length();
				} else {
					insertPlainText(docPos, plainPos, segment);
					final int length = segment.length();
					docPos += length;
					plainPos += length;
				}
				index += Character.charCount(codePoint);
			}
		}

		@Override
		public void remove(final int offs, final int len) throws BadLocationException {
			if ((len <= 0) || (offs < 0) || (offs >= getLength())) {
				return;
			}

			final int boundedLen = Math.min(len, getLength() - offs);
			final int plainOffset = plainIndexForDocumentOffset(offs);
			final int plainLength = plainLengthForRange(offs, boundedLen);
			super.remove(offs, boundedLen);
			if (plainLength > 0) {
				plainText.delete(plainOffset, Math.min(plainOffset + plainLength, plainText.length()));
			}
		}

		void replaceAll(final String text) {
			try {
				if (getLength() > 0) {
					remove(0, getLength());
				}
				plainText.setLength(0);
				if ((text != null) && !text.isEmpty()) {
					insertString(0, text, null);
				}
			} catch (final BadLocationException e) {
				LOGGER.warn("Failed to reset emoji text", e);
			}
		}

		String getPlainText() {
			return plainText.toString();
		}

		@Override
		public int getPlainLength() {
			return plainText.length();
		}

		@Override
		public int plainLengthForRange(final int docOffset, final int docLength) {
			if ((docOffset < 0) || (docLength <= 0)) {
				return 0;
			}
			final int end = Math.min(getLength(), docOffset + docLength);
			int plain = 0;
			for (int i = docOffset; i < end; i++) {
				plain += plainLengthAtDocumentIndex(i);
			}
			return plain;
		}

		@Override
		public int plainLengthForText(final String text) {
			if ((text == null) || text.isEmpty()) {
				return 0;
			}
			return text.length();
		}

		private boolean insertEmoji(final int docPos, final int plainPos, final String token) throws BadLocationException {
			if ((token == null) || token.isEmpty()) {
				return false;
			}
			final Icon icon = EmojiStore.get().getIcon(token);
			if (icon == null) {
				return false;
			}
			final SimpleAttributeSet attrs = new SimpleAttributeSet();
			StyleConstants.setIcon(attrs, icon);
			attrs.addAttribute(TOKEN_ATTRIBUTE, token);
			super.insertString(docPos, String.valueOf(ICON_PLACEHOLDER), attrs);
			plainText.insert(plainPos, token);
			return true;
		}

		private void insertPlainText(final int docPos, final int plainPos, final String text) throws BadLocationException {
			if ((text == null) || text.isEmpty()) {
				return;
			}
			super.insertString(docPos, text, SimpleAttributeSet.EMPTY);
			plainText.insert(plainPos, text);
		}

		private int plainIndexForDocumentOffset(final int docOffset) {
			int plainIndex = 0;
			for (int i = 0; i < docOffset; i++) {
				plainIndex += plainLengthAtDocumentIndex(i);
			}
			return plainIndex;
		}

		private int plainLengthAtDocumentIndex(final int index) {
			if ((index < 0) || (index >= getLength())) {
				return 0;
			}
			final Element element = getCharacterElement(index);
			final Object value = (element != null) ? element.getAttributes().getAttribute(TOKEN_ATTRIBUTE) : null;
			if (value instanceof String) {
				return ((String) value).length();
			}
			try {
				final String text = getText(index, 1);
				if ((text != null) && !text.isEmpty()) {
					return text.length();
				}
			} catch (final BadLocationException e) {
				LOGGER.debug("Unable to read text at index " + index, e);
			}
			return 0;
		}
	}
}
