/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.query;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class InfoActionTest {
	@Test
	public void reportsWarsawTimeWhenServerRunsInUtc() throws Exception {
		final TimeZone previous = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			final SimpleDateFormat expected = new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss");
			expected.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
			final String before = expected.format(new Date());
			final Method method = InfoAction.class.getDeclaredMethod("getGametime");
			method.setAccessible(true);
			final String actual = (String) method.invoke(new InfoAction());
			final String after = expected.format(new Date());
			assertTrue(actual, actual.equals(before) || actual.equals(after));
		} finally {
			TimeZone.setDefault(previous);
		}
	}

	/**
	 * Tests for execute.
	 */
	@Test
	public void testExecute() {
		Player bob = PlayerTestHelper.createPlayer("bob");
		InfoAction info = new InfoAction();
		info.onAction(bob, null);
		assertFalse(bob.events().isEmpty());
		//assertEquals(null,bob.events().get(0));

	}
}
