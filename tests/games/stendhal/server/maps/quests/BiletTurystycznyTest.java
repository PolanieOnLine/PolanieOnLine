/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class BiletTurystycznyTest {
	private static final String QUEST_SLOT = "bilet_turystyczny";

	@Test
	public void tripRequiresRecordedUseOfTicket() {
		final Player player = PlayerTestHelper.createPlayer("traveller");

		assertFalse(BiletTurystyczny.hasCompletedTrip(player));

		player.setQuest(QUEST_SLOT, "bought;100;taken;-1");
		assertFalse(BiletTurystyczny.hasCompletedTrip(player));

		player.setQuest(QUEST_SLOT, "bought;100;taken;200");
		assertTrue(BiletTurystyczny.hasCompletedTrip(player));
	}
}
