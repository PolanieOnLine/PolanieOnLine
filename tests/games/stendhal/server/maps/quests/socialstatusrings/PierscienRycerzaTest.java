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

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class PierscienRycerzaTest {
	private static SpeakerNPC edgard;
	private static SpeakerNPC zakonnik;

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		edgard = new SpeakerNPC("Edgard");
		zakonnik = new SpeakerNPC("Zakonnik");
		SingletonRepository.getNPCList().add(edgard);
		SingletonRepository.getNPCList().add(zakonnik);
		new PierscienRycerza().addToWorld();
	}

	@AfterClass
	public static void afterClass() {
		SingletonRepository.getNPCList().clear();
	}

	private static Player eligiblePlayer(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.setLevel(250);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT, PierscienMieszczanina.STATE_DONE);
		player.setQuest(PierscienRycerza.MITHRILSHIELD_QUEST_SLOT, "done");
		PlayerTestHelper.equipWithItem(player, "pierścień mieszczanina");
		return player;
	}

	private static void assertDialogueStyle(final SpeakerNPC speaker) {
		for (final RPEvent event : speaker.events()) {
			if (Events.PUBLIC_TEXT.equals(event.getName())) {
				final String reply = event.get("text");
				assertFalse(reply.contains(";"));
				assertFalse(reply.contains(":"));
				assertFalse(reply.contains("-"));
				assertFalse(reply.contains("—"));
				assertFalse(reply.contains("–"));
			}
		}
	}

	private static void askQuest(final Player player) {
		final Engine engine = edgard.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertDialogueStyle(edgard);
	}

	private static void startWatch(final Player player) {
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_TRIAL);
		player.setQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_STATE_INDEX, PierscienRycerza.TRIAL_AWAITING);
		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertDialogueStyle(zakonnik);
	}

	private static void stepWitness(final Player player, final String text) {
		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, text);
		assertDialogueStyle(zakonnik);
	}

	private static void killFullWatch(final Player player) {
		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("pokutnik z łąk");
		player.setSharedKill("pokutnik z gór");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("orzeł gigant");
		player.setSharedKill("pegaz brązowy");
	}

	private static void makeWatchOldEnough(final Player player) {
		final long started = System.currentTimeMillis()
				- (PierscienRycerza.WATCH_MINUTES + 1L) * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_STARTED_INDEX, Long.toString(started));
	}

	@Test
	public void trialRequiresPreviousStatusAndMithrilShield() {
		final Player player = PlayerTestHelper.createPlayer("Gate");
		player.setLevel(250);
		final Engine engine = edgard.getEngine();

		askQuest(player);
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(getReply(edgard).contains("próby mieszczanina"));
		assertFalse(player.hasQuest(PierscienRycerza.QUEST_SLOT));

		player.setQuest(PierscienMieszczanina.QUEST_SLOT, PierscienMieszczanina.STATE_DONE);
		PlayerTestHelper.equipWithItem(player, "pierścień mieszczanina");
		askQuest(player);
		assertTrue(getReply(edgard).contains("tarczy z mithrilu"));
		assertFalse(player.hasQuest(PierscienRycerza.QUEST_SLOT));
	}

	@Test
	public void oathStartsWitnessedFieldTrialBeforeMaterials() {
		final Player player = eligiblePlayer("Oath");
		final Engine engine = edgard.getEngine();

		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		assertTrue(getReply(edgard).contains("#przysięga"));
		engine.step(player, "przysięga");
		assertDialogueStyle(edgard);
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(edgard).contains("#obowiązek"));
		engine.step(player, "obowiązek");
		assertDialogueStyle(edgard);
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertDialogueStyle(edgard);

		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_TRIAL));
		assertEquals(PierscienRycerza.TRIAL_AWAITING,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));
		assertTrue(getReply(edgard).contains("#warcie"));
		stepWitness(player, "warcie");
		assertEquals(PierscienRycerza.TRIAL_WATCH,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));
		assertTrue(player.isEquipped("pierścień mieszczanina"));
		assertFalse(player.isEquipped("pierścień rycerza"));
	}

	@Test
	public void watchRecordsRegionalThreatsAndIgnoresKillsFromBeforeDuty() {
		final Player player = eligiblePlayer("Baseline");
		player.setSoloKill("pokutnik z bagien");
		player.setSoloKill("pokutnik z bagien");
		player.setSoloKill("pokutnik z łąk");
		player.setSoloKill("pokutnik z gór");
		player.setSoloKill("superczłowiek olbrzym");
		player.setSoloKill("superczłowiek olbrzym");
		player.setSoloKill("orzeł gigant");
		player.setSoloKill("pegaz brązowy");

		startWatch(player);
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

		makeWatchOldEnough(player);
		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("pokutnik z łąk");
		player.setSharedKill("pokutnik z gór");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("orzeł gigant");

		stepWitness(player, "warta");
		assertEquals(ConversationStates.ATTENDING, zakonnik.getEngine().getCurrentState());
		assertTrue(getReply(zakonnik).contains("Warta trwa"));

		player.setSharedKill("pokutnik z bagien");
		player.setSharedKill("superczłowiek olbrzym");
		player.setSharedKill("pegaz brązowy");
		stepWitness(player, "warta");
		assertEquals(ConversationStates.INFORMATION_7, zakonnik.getEngine().getCurrentState());
		assertTrue(getReply(zakonnik).contains("#świadectwo"));
	}

	@Test
	public void activeWatchHasOptionalEscalatingReports() {
		final Player player = eligiblePlayer("Reports");
		startWatch(player);

		stepWitness(player, "bagna");
		assertTrue(getReply(zakonnik).contains("Dwa pokutniki z bagien"));
		stepWitness(player, "łąki");
		assertTrue(getReply(zakonnik).contains("pokutnika z łąk"));
		stepWitness(player, "góry");
		assertTrue(getReply(zakonnik).contains("pokutnika z gór"));
		stepWitness(player, "olbrzymy");
		assertTrue(getReply(zakonnik).contains("Dwóch superczłowieków olbrzymów"));
		stepWitness(player, "niebo");
		final String skyReply = getReply(zakonnik);
		assertTrue(skyReply.contains("orła giganta"));
		assertTrue(skyReply.contains("pegaza brązowego"));
	}

	@Test
	public void fullWatchCannotFinishImmediatelyAfterCombat() {
		final Player player = eligiblePlayer("DutyTime");
		startWatch(player);
		killFullWatch(player);

		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.ATTENDING, witnessEngine.getCurrentState());
		assertTrue(getReply(zakonnik).contains("warta nie kończy się ostatnim ciosem"));
		assertEquals(PierscienRycerza.TRIAL_WATCH,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));

		makeWatchOldEnough(player);
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.INFORMATION_7, witnessEngine.getCurrentState());
		assertTrue(getReply(zakonnik).contains("#świadectwo"));
	}

	@Test
	public void partialTrialWithoutAuxiliarySlotReconstructsWatchWithoutOldKills() {
		final Player player = eligiblePlayer("RecoveredWatch");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_TRIAL);
		killFullWatch(player);

		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");

		assertEquals(PierscienRycerza.TRIAL_WATCH,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));
		final String started = player.getQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_STARTED_INDEX);
		assertTrue(started.length() > 0);

		makeWatchOldEnough(player);
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.ATTENDING, witnessEngine.getCurrentState());
		assertTrue(getReply(zakonnik).contains("Warta trwa"));

		player.setSharedKill("pokutnik z gór");
		final String progress = player.getQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_KILLS_INDEX);
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(progress, player.getQuest(PierscienRycerza.TRIAL_SLOT,
				PierscienRycerza.TRIAL_KILLS_INDEX));
	}

	@Test
	public void legacyWatchWithoutTimestampIsNotTrappedByNewDuration() {
		final Player player = eligiblePlayer("LegacyWatch");
		startWatch(player);
		killFullWatch(player);
		player.setQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STARTED_INDEX, "");

		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.INFORMATION_7, witnessEngine.getCurrentState());
		assertTrue(getReply(zakonnik).contains("#świadectwo"));
	}

	@Test
	public void witnessMustConfirmDutyBeforeEdgardAcceptsForgingMaterials() {
		final Player player = eligiblePlayer("Witness");
		startWatch(player);
		killFullWatch(player);
		makeWatchOldEnough(player);

		final Engine witnessEngine = zakonnik.getEngine();
		witnessEngine.setCurrentState(ConversationStates.ATTENDING);
		witnessEngine.step(player, "warta");
		assertEquals(ConversationStates.INFORMATION_7, witnessEngine.getCurrentState());
		witnessEngine.step(player, "świadectwo");
		assertEquals(PierscienRycerza.TRIAL_WITNESSED,
				player.getQuest(PierscienRycerza.TRIAL_SLOT, PierscienRycerza.TRIAL_STATE_INDEX));

		askQuest(player);
		assertTrue(getReply(edgard).contains("#świadectwo"));
		edgard.getEngine().step(player, "świadectwo");
		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_MATERIALS));
		assertTrue(getReply(edgard).contains("#materiałów"));
		edgard.getEngine().step(player, "materiałów");
		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, edgard.getEngine().getCurrentState());
	}

	@Test
	public void missingMaterialsDoNotConsumePreviousRing() {
		final Player player = eligiblePlayer("Missing");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka żelaza", 30);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 10);
		PlayerTestHelper.equipWithStackableItem(player, "bryłka mithrilu", 4);
		final Engine engine = edgard.getEngine();

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, engine.getCurrentState());
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_MATERIALS));
		assertTrue(player.isEquipped("pierścień mieszczanina"));
		assertTrue(player.isEquipped("sztabka żelaza", 30));
		assertTrue(player.isEquipped("sztabka złota", 10));
		assertTrue(player.isEquipped("bryłka mithrilu", 4));
		assertTrue(getReply(edgard).contains("niepełnym"));
	}

	@Test
	public void completeMaterialsStartTimedForgingAndConsumeInputs() {
		final Player player = eligiblePlayer("Forge");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka żelaza", 30);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 10);
		PlayerTestHelper.equipWithStackableItem(player, "bryłka mithrilu", 5);
		final Engine engine = edgard.getEngine();

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.getQuest(PierscienRycerza.QUEST_SLOT)
				.startsWith(PierscienRycerza.FORGING_PREFIX));
		assertFalse(player.isEquipped("pierścień mieszczanina"));
		assertFalse(player.isEquipped("sztabka żelaza"));
		assertFalse(player.isEquipped("sztabka złota"));
		assertFalse(player.isEquipped("bryłka mithrilu"));
		assertFalse(player.isEquipped("pierścień rycerza"));
	}

	@Test
	public void ringCannotBeCollectedBeforeForgingFinishes() {
		final Player player = PlayerTestHelper.createPlayer("Waiting");
		player.setQuest(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.FORGING_PREFIX + System.currentTimeMillis());
		askQuest(player);
		assertTrue(getReply(edgard).contains("Jeszcze nie"));
		assertFalse(player.isEquipped("pierścień rycerza"));
	}

	@Test
	public void ringIsCollectedAfterNinetyMinutesAndKeepsReward() {
		final Player player = PlayerTestHelper.createPlayer("Ready");
		final int initialXp = player.getXP();
		final long started = System.currentTimeMillis() - 91L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.FORGING_PREFIX + started);

		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, edgard.getEngine().getCurrentState());
		edgard.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_DONE));
		assertTrue(player.isEquipped("pierścień rycerza"));
		assertEquals(initialXp + 100000, player.getXP());
	}

	@Test
	public void legacyItemsStateContinuesWithNewForgingMaterialsWithoutNewWatch() {
		final Player player = eligiblePlayer("LegacyItems");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_LEGACY_ITEMS);
		final Engine engine = edgard.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "przypomnij");
		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, engine.getCurrentState());
		final String materialsReply = getReply(edgard);
		assertTrue(materialsReply.contains("30 sztabek żelaza"));
		assertTrue(materialsReply.contains("10 sztabek złota"));
		assertTrue(materialsReply.contains("5 bryłek mithrilu"));
		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_LEGACY_ITEMS));
	}

	@Test
	public void currentMaterialsStateDoesNotForceExistingPlayersBackToWatch() {
		final Player player = eligiblePlayer("CurrentMaterials");
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_MATERIALS);
		askQuest(player);
		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_MATERIALS));
	}

	@Test
	public void malformedForgingTimestampDoesNotTrapPlayer() {
		final Player player = PlayerTestHelper.createPlayer("BrokenTime");
		player.setQuest(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.FORGING_PREFIX + "broken");
		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, edgard.getEngine().getCurrentState());
		edgard.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienRycerza.QUEST_SLOT,
				PierscienRycerza.STATE_DONE));
		assertTrue(player.isEquipped("pierścień rycerza"));
	}
}
