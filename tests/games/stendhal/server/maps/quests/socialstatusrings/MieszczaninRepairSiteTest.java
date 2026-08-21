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
import utilities.RPClass.BlockTestHelper;

public class MieszczaninRepairSiteTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void ownerRepairsCrossingDuringRepairStage() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setPosition(10, 10);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR);

		final MieszczaninRepairSite site = new MieszczaninRepairSite(player);
		site.setPosition(11, 10);

		assertTrue(site.onUsed(player));
		assertTrue(MieszczaninRepairProgress.isRepaired(player));
	}

	@Test
	public void repairSiteDoesNotAdvanceOtherStages() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setPosition(10, 10);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS);

		final MieszczaninRepairSite site = new MieszczaninRepairSite(player);
		site.setPosition(11, 10);

		assertFalse(site.onUsed(player));
		assertFalse(MieszczaninRepairProgress.isRepaired(player));
	}
}
