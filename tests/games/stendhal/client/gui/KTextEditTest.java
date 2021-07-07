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

import java.util.regex.Pattern;

import org.junit.Test;

/** Tests for KTextEdit. */
public class KTextEditTest {
	/**
	 * Test that the chatlog click listener limits URLs as desired.
	 */
	@Test
	public void testURLPattern() {
		KTextEdit ed = new KTextEdit();
		Pattern p = (ed.new LinkListener()).whitelist;
		assertFalse(p.matcher("justsomething").matches());
		assertTrue(p.matcher("http://polanieonline.eu").matches());
		assertTrue(p.matcher("http://polanieonline.eu/").matches());
		assertTrue(p.matcher("https://polanieonline.eu").matches());
		assertTrue(p.matcher("https://polanieonline.eu/").matches());
		assertFalse(p.matcher("polanieonline.eu").matches());
		assertTrue(p.matcher("https://polanieonline.eu/player-guide/ask-for-help.html").matches());
		assertFalse(p.matcher("https://polanieonline.eu.com").matches());
		assertFalse(p.matcher("https://polanieonline.eu.com/").matches());
		assertFalse(p.matcher("https://polanieonline.eu.com/trojan.html").matches());
		assertFalse("Line break within URL", p.matcher("https://polanieonline.eu/player-guide\n/ask-for-help.html").matches());
	}
}
