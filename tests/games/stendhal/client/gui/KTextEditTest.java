/***************************************************************************
 *                (C) Copyright 2003-2014 - Faiumoni e.V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.awt.Color;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import games.stendhal.common.NotificationType;

import org.junit.Test;

/** Tests for KTextEdit. */
public class KTextEditTest {
	@Test
	public void questInvitationsMatchExperienceButNotHighlightedKeywords()
			throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			KTextEdit editor = new KTextEdit();
			editor.insertText("PD", NotificationType.SIGNIFICANT_POSITIVE);
			editor.insertText("NPC", NotificationType.QUEST_INVITATION);
			editor.insertText("#hasło", NotificationType.NORMAL);
			StyledDocument document = editor.textPane.getStyledDocument();
			Color experience = StyleConstants.getForeground(
					document.getCharacterElement(0).getAttributes());
			Color invitation = StyleConstants.getForeground(
					document.getCharacterElement(2).getAttributes());
			Color keyword = StyleConstants.getForeground(
					document.getCharacterElement(5).getAttributes());
			assertEquals(experience, invitation);
			assertNotEquals(keyword, invitation);
		});
	}

	/**
	 * Test that the chatlog click listener limits URLs as desired.
	 */
	@Test
	public void testURLPattern() {
		KTextEdit ed = new KTextEdit();
		Pattern p = (ed.new LinkListener()).whitelist;
		assertFalse(p.matcher("justsomething").matches());
		assertTrue(p.matcher("http://stendhalgame.org").matches());
		assertTrue(p.matcher("http://stendhalgame.org/").matches());
		assertTrue(p.matcher("https://stendhalgame.org").matches());
		assertTrue(p.matcher("https://stendhalgame.org/").matches());
		assertFalse(p.matcher("stendhalgame.org").matches());
		assertTrue(p.matcher(
				"https://stendhalgame.org/player-guide/ask-for-help.html").matches());
		assertFalse(p.matcher("https://stendhalgame.org.com").matches());
		assertFalse(p.matcher("https://stendhalgame.org.com/").matches());
		assertFalse(p.matcher(
				"https://stendhalgame.org.com/trojan.html").matches());
		assertFalse("Line break within URL", p.matcher(
				"https://stendhalgame.org/player-guide\n/ask-for-help.html").matches());
	}
}
