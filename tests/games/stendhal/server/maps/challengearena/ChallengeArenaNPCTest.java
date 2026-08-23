/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ChallengeArenaNPCTest {
	@BeforeClass
	public static void beforeClass() {
		QuestHelper.setUpBeforeClass();
	}

	@After
	public void after() {
		SingletonRepository.getNPCList().clear();
	}

	@Test
	public void tierSelectionWorksAcrossArenaConversationStates() {
		final StendhalRPZone zone = new StendhalRPZone(
				"int_tarnow_challenge_arena_test", 64, 64);
		ChallengeArenaNPC.create(zone, 32, 46);

		SpeakerNPC npc = null;
		for (final SpeakerNPC candidate : zone.getEntitiesOfClass(SpeakerNPC.class)) {
			if ("Mistrz Wyzwań".equals(candidate.getName())) {
				npc = candidate;
				break;
			}
		}
		assertTrue(npc != null);

		final Player player = PlayerTestHelper.createPlayer("ArenaDialogTester");
		final Engine engine = npc.getEngine();

		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());

		engine.step(player, "arena");
		assertEquals(ConversationStates.QUESTION_1, engine.getCurrentState());
		assertTrue(getReply(npc).contains("#próba"));

		engine.step(player, "próba");
		assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
		assertEquals(ChallengeArenaTier.TRIAL.name(),
				player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT));
		assertTrue(getReply(npc).contains("100000"));

		engine.step(player, "arena");
		assertEquals(ConversationStates.QUESTION_1, engine.getCurrentState());
		engine.step(player, "potyczka");
		assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
		assertEquals(ChallengeArenaTier.SKIRMISH.name(),
				player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT));

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "legenda");
		assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
		assertEquals(ChallengeArenaTier.LEGEND.name(),
				player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT));
	}
}
