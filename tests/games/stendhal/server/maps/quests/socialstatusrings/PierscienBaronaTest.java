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

public class PierscienBaronaTest {
	private static SpeakerNPC edragon;
	private static SpeakerNPC edgard;
	private static SpeakerNPC dobrawa;
	private static SpeakerNPC zakonnik;

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		edragon = addNpc("eDragon");
		edgard = addNpc("Edgard");
		dobrawa = addNpc("Dobrawa");
		zakonnik = addNpc("Zakonnik");
		new PierscienBarona().addToWorld();
	}

	@AfterClass
	public static void afterClass() {
		SingletonRepository.getNPCList().clear();
	}

	private static SpeakerNPC addNpc(final String name) {
		final SpeakerNPC npc = new SpeakerNPC(name);
		SingletonRepository.getNPCList().add(npc);
		return npc;
	}

	private static Player eligiblePlayer(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.setLevel(350);
		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_DONE);
		player.setQuest(PierscienBarona.HUNGRY_JOSHUA_QUEST_SLOT, "done");
		player.setQuest(PierscienBarona.FISHERMANS_LICENSE2_QUEST_SLOT, "done");
		player.setQuest(PierscienBarona.OBSIDIAN_KNIFE_QUEST_SLOT, "done");
		player.setQuest(PierscienBarona.MITHRIL_CLOAK_QUEST_SLOT, "done");
		player.setQuest(PierscienBarona.CIUPAGA_DWA_WASY_QUEST_SLOT, "done");
		PlayerTestHelper.equipWithItem(player, "pierścień rycerza");
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

	private static void stepAttending(final SpeakerNPC npc, final Player player, final String text) {
		npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
		npc.getEngine().step(player, text);
		assertDialogueStyle(npc);
	}

	private static void askQuest(final Player player) {
		stepAttending(edragon, player, ConversationPhrases.QUEST_MESSAGES.get(0));
	}

	private static void startJudgmentTrial(final Player player) {
		final Engine engine = edragon.getEngine();
		askQuest(player);
		engine.step(player, "władza");
		assertDialogueStyle(edragon);
		engine.step(player, "odpowiedzialność");
		assertDialogueStyle(edragon);
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertDialogueStyle(edragon);
	}

	private static void reachStewardship(final Player player) {
		startJudgmentTrial(player);
		final Engine engine = edragon.getEngine();
		engine.step(player, "granicy");
		assertDialogueStyle(edragon);
		engine.step(player, "świadkom");
		assertDialogueStyle(edragon);
		engine.step(player, "spichlerzu");
		assertDialogueStyle(edragon);
		engine.step(player, "wspólnoty");
		assertDialogueStyle(edragon);
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_JUDGMENT));
		engine.step(player, "wyroku");
		assertDialogueStyle(edragon);
		engine.step(player, "dowodu");
		assertDialogueStyle(edragon);
		engine.step(player, "prawo");
		assertDialogueStyle(edragon);
	}

	private static void completeBoundaryDuty(final Player player) {
		stepAttending(edgard, player, "granica");
		stepAttending(edgard, player, "równość");
	}

	private static void completeGranaryDuty(final Player player) {
		PlayerTestHelper.equipWithStackableItem(player, "chleb", PierscienBarona.REQUIRED_BREAD);
		stepAttending(dobrawa, player, "spichlerz");
		stepAttending(dobrawa, player, "zapas");
	}

	private static void startJudgmentDelay(final Player player) {
		stepAttending(zakonnik, player, "wyrok");
	}

	private static void makeJudgmentDelayOldEnough(final Player player) {
		final long started = System.currentTimeMillis()
				- (PierscienBarona.JUDGMENT_MINUTES + 1L) * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_STARTED_INDEX, Long.toString(started));
	}

	private static void completeJudgmentDuty(final Player player) {
		startJudgmentDelay(player);
		makeJudgmentDelayOldEnough(player);
		stepAttending(zakonnik, player, "dowód");
	}

	@Test
	public void newTrialRequiresKnightStatusAndPreviousTrials() {
		final Player player = PlayerTestHelper.createPlayer("GateBaron");
		player.setLevel(350);

		askQuest(player);
		assertEquals(ConversationStates.ATTENDING, edragon.getEngine().getCurrentState());
		assertTrue(getReply(edragon).contains("rycerskiego słowa"));
		assertFalse(player.hasQuest(PierscienBarona.QUEST_SLOT));

		player.setQuest(PierscienRycerza.QUEST_SLOT, PierscienRycerza.STATE_DONE);
		PlayerTestHelper.equipWithItem(player, "pierścień rycerza");
		askQuest(player);
		assertTrue(getReply(edragon).contains("dawne próby"));
		assertFalse(player.hasQuest(PierscienBarona.QUEST_SLOT));
	}

	@Test
	public void responsibilityDialogueOpensJudgmentTrialBeforeMaterials() {
		final Player player = eligiblePlayer("DutyBaron");
		final Engine engine = edragon.getEngine();

		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		assertTrue(getReply(edragon).contains("#władza"));
		engine.step(player, "władza");
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(edragon).contains("#odpowiedzialność"));
		engine.step(player, "odpowiedzialność");
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_BOUNDARY));
		assertTrue(player.isEquipped("pierścień rycerza"));
		assertTrue(getReply(edragon).contains("#granicy"));
	}

	@Test
	public void eachJudgmentPresentsARealArgumentBeforeTheChoice() {
		final Player player = eligiblePlayer("ContextBaron");
		startJudgmentTrial(player);
		final Engine engine = edragon.getEngine();

		engine.step(player, "granicy");
		assertTrue(getReply(edragon).contains("#rody"));
		engine.step(player, "rody");
		final String boundary = getReply(edragon);
		assertTrue(boundary.contains("oczyścił zawalony trakt"));
		assertTrue(boundary.contains("stary kamień"));
		assertTrue(boundary.contains("#świadkom"));
		assertTrue(boundary.contains("#rodowi"));
		engine.step(player, "świadkom");

		engine.step(player, "spichlerzu");
		assertTrue(getReply(edragon).contains("#kupca"));
		engine.step(player, "kupca");
		final String granary = getReply(edragon);
		assertTrue(granary.contains("naprawić most"));
		assertTrue(granary.contains("pustymi garnkami"));
		assertTrue(granary.contains("#wspólnoty"));
		assertTrue(granary.contains("#zysk"));
		engine.step(player, "wspólnotę");

		engine.step(player, "wyroku");
		assertTrue(getReply(edragon).contains("#tłum"));
		engine.step(player, "tłum");
		final String judgment = getReply(edragon);
		assertTrue(judgment.contains("stracił zapas na zimę"));
		assertTrue(judgment.contains("groził mu przy świadkach"));
		assertTrue(judgment.contains("#dowodu"));
		assertTrue(judgment.contains("#gniew"));
	}

	@Test
	public void selfishJudgmentsDoNotAdvanceTrialAndExplainWhy() {
		final Player player = eligiblePlayer("WrongBaron");
		startJudgmentTrial(player);
		final Engine engine = edragon.getEngine();

		engine.step(player, "granicy");
		engine.step(player, "rody");
		assertTrue(getReply(edragon).contains("#świadkom"));
		engine.step(player, "rodowi");
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_BOUNDARY));
		final String wrongBoundary = getReply(edragon);
		assertTrue(wrongBoundary.contains("naprawdę wykonał pracę"));
		assertTrue(wrongBoundary.contains("#świadkowie"));

		engine.step(player, "świadkom");
		engine.step(player, "spichlerzu");
		engine.step(player, "kupiec");
		engine.step(player, "zysk");
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_GRANARY));
		final String wrongGranary = getReply(edragon);
		assertTrue(wrongGranary.contains("Most naprawdę wymaga naprawy"));
		assertTrue(wrongGranary.contains("#wspólnotę"));

		engine.step(player, "wspólnotę");
		engine.step(player, "wyroku");
		engine.step(player, "tłum");
		engine.step(player, "gniew");
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_JUDGMENT));
		final String wrongJudgment = getReply(edragon);
		assertTrue(wrongJudgment.contains("Strata jest prawdziwa"));
		assertTrue(wrongJudgment.contains("#dowodu"));
	}

	@Test
	public void correctJudgmentsLeadToConsequencesNotDirectlyToMaterials() {
		final Player player = eligiblePlayer("JudgeBaron");
		reachStewardship(player);

		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_STEWARDSHIP));
		assertTrue(player.isEquipped("pierścień rycerza"));
		final String stewardshipReply = getReply(edragon);
		assertTrue(stewardshipReply.contains("Edgard"));
		assertTrue(stewardshipReply.contains("Dobrawa"));
		assertTrue(stewardshipReply.contains("Zakonnik"));
		assertTrue(stewardshipReply.contains("#granicy"));
		assertTrue(stewardshipReply.contains("#zapas"));
		assertTrue(stewardshipReply.contains("#wyrok"));
		assertFalse(stewardshipReply.contains("#spichlerzowy"));
	}

	@Test
	public void boundaryRulingMustBePubliclyConfirmed() {
		final Player player = eligiblePlayer("BoundaryBaron");
		reachStewardship(player);

		stepAttending(edgard, player, "granica");
		assertEquals(PierscienBarona.DUTY_ASKED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_BOUNDARY_INDEX));
		assertTrue(getReply(edgard).contains("#równość"));
		stepAttending(edgard, player, "równość");

		assertEquals(PierscienBarona.DUTY_DONE,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_BOUNDARY_INDEX));
		assertTrue(getReply(edgard).contains("obu rodom"));
	}

	@Test
	public void granaryReserveIsOneLogicalCostAndIncompleteSetIsNotConsumed() {
		final Player player = eligiblePlayer("GranaryBaron");
		reachStewardship(player);
		PlayerTestHelper.equipWithStackableItem(player, "chleb", PierscienBarona.REQUIRED_BREAD - 1);

		stepAttending(dobrawa, player, "spichlerz");
		assertEquals(PierscienBarona.DUTY_ASKED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_GRANARY_INDEX));
		stepAttending(dobrawa, player, "zapas");
		assertTrue(player.isEquipped("chleb", PierscienBarona.REQUIRED_BREAD - 1));
		assertEquals(PierscienBarona.DUTY_ASKED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_GRANARY_INDEX));

		PlayerTestHelper.equipWithStackableItem(player, "chleb", 1);
		stepAttending(dobrawa, player, "zapas");
		assertEquals(PierscienBarona.DUTY_DONE,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_GRANARY_INDEX));
		assertFalse(player.isEquipped("chleb"));
	}

	@Test
	public void judgmentDelayCannotBeSkippedAndSurvivesConversationRestart() {
		final Player player = eligiblePlayer("JudgmentBaron");
		reachStewardship(player);
		startJudgmentDelay(player);

		assertEquals(PierscienBarona.DUTY_STARTED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_JUDGMENT_INDEX));
		assertTrue(player.getQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_STARTED_INDEX).length() > 0);
		stepAttending(zakonnik, player, "dowód");
		assertEquals(PierscienBarona.DUTY_STARTED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_JUDGMENT_INDEX));
		assertTrue(getReply(zakonnik).contains("Jeszcze nie"));

		makeJudgmentDelayOldEnough(player);
		stepAttending(zakonnik, player, "wyrok");
		assertTrue(getReply(zakonnik).contains("#dowodzie"));
		stepAttending(zakonnik, player, "dowodzie");
		assertEquals(PierscienBarona.DUTY_DONE,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_JUDGMENT_INDEX));
	}

	@Test
	public void allThreeConsequencesAreRequiredBeforeMaterialsAndCloseTheStory() {
		final Player player = eligiblePlayer("ExecutionBaron");
		reachStewardship(player);
		completeBoundaryDuty(player);
		completeGranaryDuty(player);

		askQuest(player);
		assertTrue(getReply(edragon).contains("Zakonnika"));
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_STEWARDSHIP));

		completeJudgmentDuty(player);
		askQuest(player);
		assertTrue(getReply(edragon).contains("#prawo"));
		edragon.getEngine().step(player, "prawo");
		final String closing = getReply(edragon);
		assertTrue(closing.contains("Granicy nie przesunąłeś na korzyść silniejszego rodu"));
		assertTrue(closing.contains("Zboża nie sprzedałeś kupcowi"));
		assertTrue(closing.contains("Oskarżonego nie skazałeś pod naciskiem rozgniewanego tłumu"));
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_MATERIALS));
		assertTrue(closing.contains("#materiałów"));
	}

	@Test
	public void stewardshipProgressSurvivesRestartWithoutResettingDoneDuties() {
		final Player player = eligiblePlayer("ResumeBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_STEWARDSHIP);
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_BOUNDARY_INDEX, PierscienBarona.DUTY_DONE);
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_GRANARY_INDEX, PierscienBarona.DUTY_ASKED);
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_INDEX, PierscienBarona.DUTY_STARTED);
		final long started = System.currentTimeMillis() - 5L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_STARTED_INDEX, Long.toString(started));

		askQuest(player);
		final String reply = getReply(edragon);
		assertFalse(reply.contains("Edgarda"));
		assertTrue(reply.contains("Dobrawy"));
		assertTrue(reply.contains("Zakonnika"));
		assertEquals(PierscienBarona.DUTY_DONE,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_BOUNDARY_INDEX));
		assertEquals(PierscienBarona.DUTY_STARTED,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_JUDGMENT_INDEX));
	}

	@Test
	public void malformedJudgmentTimestampDoesNotTrapActiveDuty() {
		final Player player = eligiblePlayer("BrokenJudgmentBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_STEWARDSHIP);
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_INDEX, PierscienBarona.DUTY_STARTED);
		player.setQuest(PierscienBarona.DUTY_SLOT,
				PierscienBarona.DUTY_JUDGMENT_STARTED_INDEX, "broken");

		stepAttending(zakonnik, player, "dowód");
		assertEquals(PierscienBarona.DUTY_DONE,
				player.getQuest(PierscienBarona.DUTY_SLOT, PierscienBarona.DUTY_JUDGMENT_INDEX));
	}

	@Test
	public void oldJudgmentStateStillReconstructsItsOwnCase() {
		final Player player = eligiblePlayer("OldTrialBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_TRIAL_GRANARY);

		askQuest(player);
		assertTrue(getReply(edragon).contains("#spichlerza"));
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_GRANARY));
	}

	@Test
	public void oldActiveTrialCanStillUseDecisionKeywordsWithoutNewContextStep() {
		final Player boundary = eligiblePlayer("LegacyBoundaryContext");
		boundary.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_TRIAL_BOUNDARY);
		stepAttending(edragon, boundary, "świadkom");
		assertTrue(boundary.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_GRANARY));

		final Player granary = eligiblePlayer("LegacyGranaryContext");
		granary.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_TRIAL_GRANARY);
		stepAttending(edragon, granary, "wspólnota");
		assertTrue(granary.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_JUDGMENT));

		final Player judgment = eligiblePlayer("LegacyJudgmentContext");
		judgment.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_TRIAL_JUDGMENT);
		stepAttending(edragon, judgment, "dowód");
		assertTrue(judgment.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_TRIAL_APPROVED));
	}

	@Test
	public void incompleteMaterialSetDoesNotConsumeKnightRing() {
		final Player player = eligiblePlayer("MissingBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 40);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka mithrilu", 20);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka platyny", 9);
		final Engine engine = edragon.getEngine();

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_MATERIALS));
		assertTrue(player.isEquipped("pierścień rycerza"));
		assertTrue(player.isEquipped("sztabka złota", 40));
		assertTrue(player.isEquipped("sztabka mithrilu", 20));
		assertTrue(player.isEquipped("sztabka platyny", 9));
		assertTrue(getReply(edragon).contains("Brakuje"));
	}

	@Test
	public void completeMaterialSetStartsTwoHourForging() {
		final Player player = eligiblePlayer("ForgeBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 40);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka mithrilu", 20);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka platyny", 10);
		final Engine engine = edragon.getEngine();

		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));

		assertTrue(player.getQuest(PierscienBarona.QUEST_SLOT)
				.startsWith(PierscienBarona.FORGING_PREFIX));
		assertFalse(player.isEquipped("pierścień rycerza"));
		assertFalse(player.isEquipped("sztabka złota"));
		assertFalse(player.isEquipped("sztabka mithrilu"));
		assertFalse(player.isEquipped("sztabka platyny"));
		assertFalse(player.isEquipped("pierścień barona"));
	}

	@Test
	public void baronRingCannotBeCollectedBeforeCoolingFinishes() {
		final Player player = PlayerTestHelper.createPlayer("WaitingBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT,
				PierscienBarona.FORGING_PREFIX + System.currentTimeMillis());

		askQuest(player);
		assertTrue(getReply(edragon).contains("Jeszcze nie"));
		assertFalse(player.isEquipped("pierścień barona"));
	}

	@Test
	public void baronRingIsCollectedAfterTwoHoursAndAwardsXP() {
		final Player player = PlayerTestHelper.createPlayer("ReadyBaron");
		final int initialXp = player.getXP();
		final long started = System.currentTimeMillis() - 121L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienBarona.QUEST_SLOT,
				PierscienBarona.FORGING_PREFIX + started);

		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, edragon.getEngine().getCurrentState());
		assertTrue(getReply(edragon).contains("#pierścień"));
		edragon.getEngine().step(player, "pierścień");

		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_DONE));
		assertTrue(player.isEquipped("pierścień barona"));
		assertEquals(initialXp + 100000, player.getXP());
	}

	@Test
	public void legacyListStateContinuesWithoutNewStewardship() {
		final Player player = eligiblePlayer("LegacyBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_LEGACY_LIST);
		stepAttending(edragon, player, "przypomnij");

		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, edragon.getEngine().getCurrentState());
		final String materialsReply = getReply(edragon);
		assertTrue(materialsReply.contains("40 sztabek złota"));
		assertTrue(materialsReply.contains("20 sztabek mithrilu"));
		assertTrue(materialsReply.contains("10 sztabek platyny"));
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_LEGACY_LIST));
		assertFalse(player.hasQuest(PierscienBarona.DUTY_SLOT));
	}

	@Test
	public void currentMaterialsStateDoesNotForcePlayerBackIntoStewardship() {
		final Player player = eligiblePlayer("CurrentBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_MATERIALS);

		askQuest(player);
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_MATERIALS));
		assertFalse(player.hasQuest(PierscienBarona.DUTY_SLOT));
	}

	@Test
	public void malformedForgingTimestampDoesNotTrapPlayer() {
		final Player player = PlayerTestHelper.createPlayer("BrokenBaron");
		player.setQuest(PierscienBarona.QUEST_SLOT,
				PierscienBarona.FORGING_PREFIX + "broken");

		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, edragon.getEngine().getCurrentState());
		edragon.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienBarona.QUEST_SLOT,
				PierscienBarona.STATE_DONE));
		assertTrue(player.isEquipped("pierścień barona"));
	}
}
