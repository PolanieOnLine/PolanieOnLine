/***************************************************************************
*                   (C) Copyright 2003-2015 - Stendhal                    *
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.stendhal;
import games.stendhal.client.actions.SlashActionRepository;
import games.stendhal.client.scripting.ChatLineParser;
import games.stendhal.common.constants.SoundLayer;

public class ChatTextController {
	/** Maximum text length. Public chat is limited to 1000 server side. */
	private static final int MAX_TEXT_LENGTH = 1000;

	private final EmojiTextPane playerChatText = new EmojiTextPane();

	private ChatCache cache;
	private static ChatTextController instance;

	/**
	* Retrieves singleton instance.
	*
	* @return
	*   `ChatTextController` instance.
	*/
	public static ChatTextController get() {
		if (ChatTextController.instance == null) {
			ChatTextController.instance = new ChatTextController();
		}
		return ChatTextController.instance;
	}

	/**
	* Private singleton constructor.
	*/
	private ChatTextController() {
		Document doc = playerChatText.getDocument();
		if (doc instanceof AbstractDocument) {
			((AbstractDocument) doc).setDocumentFilter(new SizeFilter(MAX_TEXT_LENGTH));
		}
		setupKeys();
		StendhalClient client = StendhalClient.get();
		String logFile = null;
		if (client != null) {
			// StendhalClient is null during test runs
			logFile = stendhal.getGameFolder() + "chat/out-" + client.getCharacter() + ".log";
		}
		cache = new ChatCache(logFile);
		cache.loadChatCache();
		setCache(cache);
	}

	/**
	* Sets focus to chat input.
	*
	* @return
	*   `true` if focus change is likely to succeed.
	*/
	public boolean setFocus() {
		return playerChatText.requestFocusInWindow();
	}

	public JTextComponent getPlayerChatText() {
		return playerChatText;
	}

	public void setChatLine(final String text) {
		playerChatText.setText(text);
	}

	/**
	* Add the special key bindings.
	*/
	private void setupKeys() {
		final KeyStroke enterPress = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
		final KeyStroke enterRelease = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
		final KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
		final KeyStroke shiftEnterRelease = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK, true);

		bindSubmitKey(JComponent.WHEN_FOCUSED, enterPress);
		bindSubmitKey(JComponent.WHEN_FOCUSED, enterRelease);
		bindSubmitKey(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, enterPress);
		bindSubmitKey(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, enterRelease);
		bindSubmitKey(JComponent.WHEN_IN_FOCUSED_WINDOW, enterPress);
		bindSubmitKey(JComponent.WHEN_IN_FOCUSED_WINDOW, enterRelease);

		bindLineBreakKey(JComponent.WHEN_FOCUSED, shiftEnter);
		bindLineBreakKey(JComponent.WHEN_FOCUSED, shiftEnterRelease);
		bindLineBreakKey(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, shiftEnter);
		bindLineBreakKey(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, shiftEnterRelease);

		final InputMap focused = playerChatText.getInputMap(JComponent.WHEN_FOCUSED);
		focused.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK), "history_previous");
		focused.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK), "history_next");
		focused.put(KeyStroke.getKeyStroke("F1"), "manual");

		final ActionMap actions = playerChatText.getActionMap();
		actions.put("submit", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				submitCurrentLine();
			}
		});
		actions.put("insert_line_break", new DefaultEditorKit.InsertBreakAction());
		actions.put("history_previous", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (cache.hasPrevious()) {
					setChatLine(cache.previous());
				}
			}
		});
		actions.put("history_next", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (cache.hasNext()) {
					setChatLine(cache.next());
				}
			}
		});
		actions.put("manual", new AbstractAction() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SlashActionRepository.get("manual").execute(null, null);
			}
		});
	}

	private void bindSubmitKey(final int condition, final KeyStroke stroke) {
		final InputMap map = playerChatText.getInputMap(condition);
		if (map != null) {
			map.put(stroke, "submit");
		}
	}

	private void bindLineBreakKey(final int condition, final KeyStroke stroke) {
		final InputMap map = playerChatText.getInputMap(condition);
		if (map != null) {
			map.put(stroke, "insert_line_break");
		}
	}

	private void submitCurrentLine() {
		final String text = playerChatText.getText();
		if (ChatLineParser.parseAndHandle(text)) {
			clearLine();
		}
	}

	public void addKeyListener(final KeyListener l) {
		playerChatText.addKeyListener(l);
	}

	public String getText() {
		return playerChatText.getText();
	}

	private void setCache(final ChatCache cache) {
		this.cache = cache;
	}

	private void clearLine() {
		cache.addlinetoCache(getText());

		setChatLine("");
	}

	public void saveCache() {
		cache.save();
	}

	/**
	* A document filter that limits the maximum allowed length of a document.
	*/
	private static class SizeFilter extends DocumentFilter {
		/** Sound to play if the user tries to enter too long string. */
		private static final String sound = "click-1";
		/** Maximum length of the document. */
		final int maxSize;

		/**
		* Create a new SizeFilter.
		*
		* @param maxSize maximum length of the document
		*/
		SizeFilter(int maxSize) {
			this.maxSize = maxSize;
		}

		@Override
		public void insertString(FilterBypass fb, int offs, String str,
		AttributeSet a) throws BadLocationException {
			if (allowsChange(fb, offs, 0, str)) {
				super.insertString(fb, offs, str, a);
			} else {
				fail();
			}
		}

		@Override
		public void replace(FilterBypass fb, int offs, int length, String str,
		AttributeSet a) throws BadLocationException {
			if (allowsChange(fb, offs, length, str)) {
				super.replace(fb, offs, length, str, a);
			} else {
				fail();
			}
		}

		private boolean allowsChange(final FilterBypass fb, final int offs, final int length, final String str)
		throws BadLocationException {
			final Document document = fb.getDocument();
			int current = document.getLength();
			int removal = length;
			if (document instanceof EmojiTextPane.PlainTextSupport) {
				final EmojiTextPane.PlainTextSupport support = (EmojiTextPane.PlainTextSupport) document;
				current = support.getPlainLength();
				removal = support.plainLengthForRange(offs, length);
			}
			int addition = (str != null) ? str.length() : 0;
			if (document instanceof EmojiTextPane.PlainTextSupport) {
				addition = ((EmojiTextPane.PlainTextSupport) document).plainLengthForText(str);
			}
			return ((current - removal) + addition) <= maxSize;
		}

		/**
		* Called when the document change is rejected. Notify the user.
		*/
		private void fail() {
			ClientSingletonRepository.getSound().getGroup(SoundLayer.USER_INTERFACE.groupName).play(sound, 0, null, null, false, true);
		}
	}
}
