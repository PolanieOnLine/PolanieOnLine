/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.BlockTestHelper;

public class MieszczaninStolenToolsTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void stolenToolsUseDedicatedReadableQuestGraphic() {
		final Player player = PlayerTestHelper.createPlayer("ToolsVisualTester");
		final MieszczaninStolenTools tools = new MieszczaninStolenTools(player);

		assertEquals("item/dropped_tools",
				tools.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE));
		assertEquals(0, tools.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
		assertEquals(1, tools.getInt(PlayerPrivateQuestProp.TILESET_COLUMNS_ATTRIBUTE));
		assertEquals("Zabierz|Użyj", tools.get("menu"));
	}

	@Test
	public void toolsRequireFreedMessengerAndRememberRecovery() {
		final Player player = PlayerTestHelper.createPlayer("Tracker");
		player.setPosition(10, 10);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS);

		final MieszczaninStolenTools tools = new MieszczaninStolenTools(player);
		tools.setPosition(11, 10);
		assertFalse(tools.onUsed(player));

		MieszczaninHideoutProgress.markCleared(player);
		MieszczaninHideoutProgress.markMessengerFreed(player);
		assertTrue(tools.onUsed(player));
		assertTrue(MieszczaninHideoutProgress.areToolsRecovered(player));
	}
}
