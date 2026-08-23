/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MieszczaninFinaleTest {

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void approvedPlayerHandsMaterialsToMarianekBeforeForgingStarts() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR);
		MieszczaninRepairProgress.markCommunityApproved(player);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka srebra", 2);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka miedzi", 1);
		PlayerTestHelper.equipWithStackableItem(player, "węgiel drzewny", 3);

		final Engine engine = marianek.getEngine();
		askQuest(marianek, player);
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		final String opening = getReply(marianek);
		assertTrue(opening.contains("#polegać"));
		assertFalse(opening.contains("ciupag"));
		assertFalse(player.isEquipped("pierścień mieszczanina"));

		engine.step(player, "polegać");
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(marianek).contains("#materiały"));

		engine.step(player, "materiały");
		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, engine.getCurrentState());
		assertTrue(getReply(marianek).contains("2 sztabki srebra"));

		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertTrue(player.getQuest(PierscienMieszczanina.QUEST_SLOT).startsWith("forging;"));
		assertFalse(player.isEquipped("sztabka srebra"));
		assertFalse(player.isEquipped("sztabka miedzi"));
		assertFalse(player.isEquipped("węgiel drzewny"));
		assertFalse(player.isEquipped("pierścień mieszczanina"));
		assertTrue(player.hasQuest(MieszczaninRepairProgress.SLOT));
	}

	@Test
	public void forgingDoesNotFinishBeforeOneHourPasses() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek waiting test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Bob");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				"forging;" + System.currentTimeMillis());

		askQuest(marianek, player);

		assertFalse(player.isEquipped("pierścień mieszczanina"));
		assertTrue(player.getQuest(PierscienMieszczanina.QUEST_SLOT).startsWith("forging;"));
		assertTrue(getReply(marianek).contains("Jeszcze nie"));
	}

	@Test
	public void playerReceivesRingAfterForgingTimePasses() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek ready test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Celina");
		MieszczaninRepairProgress.markCommunityApproved(player);
		MieszczaninHideoutProgress.markCleared(player);
		final long oldTimestamp = System.currentTimeMillis()
				- 61L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				"forging;" + oldTimestamp);

		askQuest(marianek, player);
		assertEquals(ConversationStates.INFORMATION_9,
				marianek.getEngine().getCurrentState());
		assertTrue(getReply(marianek).contains("#pierścień"));
		assertFalse(player.isEquipped("pierścień mieszczanina"));

		marianek.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_DONE));
		assertTrue(player.isEquipped("pierścień mieszczanina"));
		assertFalse(player.hasQuest(MieszczaninRepairProgress.SLOT));
		assertFalse(player.hasQuest(MieszczaninHideoutProgress.SLOT));
	}

	@Test
	public void missingMaterialsDoNotAdvanceOrConsumeAnything() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek materials test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Daria");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR);
		MieszczaninRepairProgress.markCommunityApproved(player);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka srebra", 2);

		final Engine engine = marianek.getEngine();
		askQuest(marianek, player);
		engine.step(player, "polegać");
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR));
		assertTrue(player.isEquipped("sztabka srebra", 2));
		assertFalse(player.isEquipped("pierścień mieszczanina"));
		assertTrue(getReply(marianek).contains("Brakuje"));
	}

	@Test
	public void malformedForgingTimestampDoesNotPermanentlyTrapPlayer() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek recovery test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Eryk");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT, "forging;broken");

		askQuest(marianek, player);
		assertEquals(ConversationStates.INFORMATION_9,
				marianek.getEngine().getCurrentState());
		marianek.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_DONE));
		assertTrue(player.isEquipped("pierścień mieszczanina"));
	}

	@Test
	public void finaleDoesNotAdvanceBeforeCommunityApproval() {
		final SpeakerNPC marianek = new SpeakerNPC("Marianek reminder test");
		MieszczaninFinale.attach(marianek);

		final Player player = PlayerTestHelper.createPlayer("Filip");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR);

		askQuest(marianek, player);

		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR));
		assertFalse(player.isEquipped("pierścień mieszczanina"));
	}

	private static void askQuest(final SpeakerNPC npc, final Player player) {
		final Engine engine = npc.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, ConversationPhrases.QUEST_MESSAGES.get(0));
	}
}
