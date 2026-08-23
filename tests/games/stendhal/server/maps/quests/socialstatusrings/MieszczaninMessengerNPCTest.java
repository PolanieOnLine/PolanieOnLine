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

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MieszczaninMessengerNPCTest {

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void radomirMustBeQuestionedBeforeHisBindingsAreCut() {
		final Player player = PlayerTestHelper.createPlayer("RadomirTester");
		MieszczaninHideoutProgress.markCleared(player);
		final MieszczaninMessengerNPC radomir = new MieszczaninMessengerNPC(player);
		final Engine engine = radomir.getEngine();

		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		final String opening = getReply(radomir);
		assertTrue(opening.contains("#napastnicy"));
		assertTrue(opening.contains("po pomoc do straży"));
		assertFalse(opening.contains("Dragon Knights"));
		assertFalse(MieszczaninHideoutProgress.isMessengerFreed(player));

		engine.step(player, "napastnicy");
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(radomir).contains("#narzędzia"));
		assertFalse(MieszczaninHideoutProgress.isMessengerFreed(player));

		engine.step(player, "narzędzia");
		assertEquals(ConversationStates.INFORMATION_9, engine.getCurrentState());
		assertTrue(getReply(radomir).contains("#więzy"));
		assertFalse(MieszczaninHideoutProgress.isMessengerFreed(player));

		engine.step(player, "więzy");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(MieszczaninHideoutProgress.isMessengerFreed(player));
	}

	@Test
	public void legacyFreedomKeywordStillFreesRadomir() {
		final Player player = PlayerTestHelper.createPlayer("LegacyFreedomTester");
		MieszczaninHideoutProgress.markCleared(player);
		final MieszczaninMessengerNPC radomir = new MieszczaninMessengerNPC(player);
		final Engine engine = radomir.getEngine();

		engine.setCurrentState(ConversationStates.INFORMATION_9);
		engine.step(player, "wolność");

		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(MieszczaninHideoutProgress.isMessengerFreed(player));
	}
}
