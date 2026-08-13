/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

import marauroa.common.game.RPObject;

public class UserContextCharacterSessionTest {
	private final UserContext context = UserContext.get();

	@After
	public void cleanup() {
		context.resetCharacterSession();
	}

	@Test
	public void resetClearsActiveCharacterIdentity() {
		RPObject player = new RPObject();
		player.put("name", "PierwszaPostac");
		context.setPlayer(player);

		context.resetCharacterSession();

		assertNull(context.getPlayer());
		assertNull(context.getName());
		assertEquals(0, context.getAdminLevel());
		assertEquals(0, context.getSheepID());
		assertEquals(0, context.getGoatID());
	}
}
