/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class PierscienRycerzaRestartTest {
	private static SpeakerNPC zakonnik;

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		SingletonRepository.getNPCList().add(new SpeakerNPC("Edgard"));
		zakonnik = new SpeakerNPC("Zakonnik");
		SingletonRepository.getNPCList().add(zakonnik);
		new PierscienRycerza().addToWorld();
	}

	@AfterClass
	public static void afterClass() {
		SingletonRepository.getNPCList().clear();
	}

	private static void recordFullWatchAsSoloKills(final Player player) {
		player.setSoloKill("pokutnik z bagien");
		player.setSoloKill("pokutnik z bagien");
		player.setSoloKill("pokutnik z łąk");
		player.setSoloKill("pokutnik z gór");
		player.setSoloKill("superczłowiek olbrzym");
		player.setSoloKill("superczłowiek olbrzym");
		player.setSoloKill("orzeł gigant");
		player.setSoloKill("pegaz brązowy");
	}

	private static void recordFullWatchAsSharedKills(final Player player) {
		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("pokutnik z łąk");
		player.setSharedKill("pokutnik z gór");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("orzeł gigant");
		player.setSharedKill("pegaz brązowy");
	}

	@Test
	public void missingTrialSubstateReconstructsFullWatchWithoutCountingOldKills() {
		final Player player = PlayerTestHelper.createPlayer("RestartedWatch");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_TRIAL);
		recordFullWatchAsSoloKills(player);
		assertFalse(player.hasQuest(PierscienRycerza.TRIAL_SLOT));

		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");

		assertEquals(PierscienRycerza.TRIAL_WATCH,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));
		final String requirements = player.getQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_KILLS_INDEX);
		assertTrue(requirements.contains("pokutnik z bagien"));
		assertTrue(requirements.contains("pokutnik z łąk"));
		assertTrue(requirements.contains("pokutnik z gór"));
		assertTrue(requirements.contains("superczłowiek olbrzym"));
		assertTrue(requirements.contains("orzeł gigant"));
		assertTrue(requirements.contains("pegaz brązowy"));
		assertTrue(player.getQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_STARTED_INDEX).length() > 0);

		final long started = System.currentTimeMillis()
				- (PierscienRycerza.WATCH_MINUTES + 1L) * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_STARTED_INDEX, Long.toString(started));
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertTrue(getReply(zakonnik).contains("Warta trwa"));

		recordFullWatchAsSharedKills(player);
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.INFORMATION_7, witnessEngine.getCurrentState());
		assertTrue(getReply(zakonnik).contains("#świadectwo"));
	}
}
