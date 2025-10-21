/***************************************************************************
 *                  (C) Copyright 2003-2023 Faiumoni e.V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.chattext;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Icon;

import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.client.sprite.EmojiStore;

/**
 * A drop down menu for selecting special characters that players may want to
 * use in chat.
 */
public class CharacterMap extends JButton {
	private final static EmojiStore emojis = EmojiStore.get();
	private final static String DEFAULT_BUTTON_TEXT = "\u263A";

	/**
	 * Create a new CharacterMap.
	 */
	public CharacterMap() {
		super();
		setFocusable(false);
		setToolTipText("Emotikony");

		final Icon buttonIcon = emojis.getIcon(":grin:");
		if (buttonIcon != null) {
			setIcon(buttonIcon);
			setText("");
		} else {
			final String glyph = emojis.glyphFor(":grin:");
			setText(glyph != null ? glyph : DEFAULT_BUTTON_TEXT);
		}

		final JPopupMenu menu = new JPopupMenu();

		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Place the menu right justified to the button
				menu.show(CharacterMap.this, getWidth() - menu.getPreferredSize().width, getHeight());
			}
		});

		ActionListener selectionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				Object source = ev.getSource();
				if (source instanceof EmojiButton) {
					ChatLineParser.parseAndHandle(((EmojiButton) source).getEmojiText());
				}
			}
		};

		fillMenu(menu, selectionListener);
	}

	/**
	 * Fill the popup menu with characters.
	 *
	 * @param menu popup menu
	 * @param listener action listener that should be attached to the menu items
	 */
	private void fillMenu(JComponent menu, ActionListener listener) {
		menu.setLayout(new GridLayout(0, 13));

		Insets insets = new Insets(1, 1, 1, 1);
		setMargin(insets);
		for (String st: emojis.getEmojiList()) {
			st = ":" + st + ":";
			final Icon icon = emojis.getIcon(st);
			final String glyph = emojis.glyphFor(st);
			if (icon != null || glyph != null) {
				EmojiButton item = new EmojiButton(icon, glyph != null ? glyph : st, st);
				item.setMargin(insets);
				item.addActionListener(listener);
				item.setBorder(null);
				menu.add(item);
			}
		}
	}

	private class EmojiButton extends JMenuItem {
		private final String emojiText;

		public EmojiButton(final Icon icon, final String fallbackLabel, final String text) {
			super();
			emojiText = text;
			if (icon != null) {
				setIcon(icon);
				setText("");
			} else {
				setText(fallbackLabel);
			}
			setIconTextGap(0);
			setToolTipText(text);
		}

		public String getEmojiText() {
			return emojiText;
		}
	}
}
