/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class MieszczaninHideoutProgressTest {

	@BeforeClass
	public static void beforeClass() {
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void progressSurvivesAsMonotonicInternalStages() {
		final Player player = PlayerTestHelper.createPlayer("Tracker");

		assertFalse(MieszczaninHideoutProgress.isCleared(player));
		MieszczaninHideoutProgress.markCleared(player);
		assertTrue(MieszczaninHideoutProgress.isCleared(player));
		assertFalse(MieszczaninHideoutProgress.isMessengerFreed(player));

		MieszczaninHideoutProgress.markMessengerFreed(player);
		assertTrue(MieszczaninHideoutProgress.isMessengerFreed(player));
		assertFalse(MieszczaninHideoutProgress.areToolsRecovered(player));

		MieszczaninHideoutProgress.markToolsRecovered(player);
		MieszczaninHideoutProgress.markCleared(player);
		MieszczaninHideoutProgress.markMessengerFreed(player);
		assertTrue(MieszczaninHideoutProgress.areToolsRecovered(player));
		assertTrue(player.isQuestInState(MieszczaninHideoutProgress.SLOT,
				MieszczaninHideoutProgress.TOOLS_RECOVERED));

		MieszczaninHideoutProgress.clear(player);
		assertFalse(player.hasQuest(MieszczaninHideoutProgress.SLOT));
	}
}
