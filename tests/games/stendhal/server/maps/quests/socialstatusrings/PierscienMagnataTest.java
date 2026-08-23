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

public class PierscienMagnataTest {
	private static SpeakerNPC zdzichu;
	private static SpeakerNPC dobrawa;
	private static SpeakerNPC edgard;
	private static SpeakerNPC edragon;

	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		zdzichu = addNpc("Jubiler Zdzichu");
		dobrawa = addNpc("Dobrawa");
		edgard = addNpc("Edgard");
		edragon = addNpc("eDragon");
		new PierscienMagnata().addToWorld();
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
		player.setLevel(500);
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_DONE);
		player.setQuest(PierscienMagnata.CLUB_THORNS_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.KILL_DRAGONS_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.VAMPIRE_SWORD_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.IMMORTAL_SWORD_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.FIND_RAT_KIDS_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.FIND_GHOSTS_QUEST_SLOT, "done");
		player.setQuest(PierscienMagnata.SAD_SCIENTIST_QUEST_SLOT, "done");
		PlayerTestHelper.equipWithItem(player, "pierścień barona");
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
		stepAttending(zdzichu, player, ConversationPhrases.QUEST_MESSAGES.get(0));
	}

	private static void startCouncil(final Player player) {
		final Engine engine = zdzichu.getEngine();
		askQuest(player);
		engine.step(player, "dostatkiem");
		assertDialogueStyle(zdzichu);
		engine.step(player, "piecza");
		assertDialogueStyle(zdzichu);
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertDialogueStyle(zdzichu);
	}

	private static void completeCouncil(final Player player) {
		startCouncil(player);
		stepAttending(dobrawa, player, "wspólnota");
		stepAttending(dobrawa, player, "zaufania");
		stepAttending(edgard, player, "słowo");
		stepAttending(edgard, player, "obowiązek");
		stepAttending(edragon, player, "prawo");
		stepAttending(edragon, player, "miary");
	}

	private static void startStewardship(final Player player) {
		completeCouncil(player);
		askQuest(player);
		final String reply = getReply(zdzichu);
		assertTrue(reply.contains("#pieczę"));
		zdzichu.getEngine().step(player, "pieczę");
		assertDialogueStyle(zdzichu);
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT,
				PierscienMagnata.STATE_STEWARDSHIP));
	}

	private static void createFund(final Player player) {
		PlayerTestHelper.equipWithStackableItem(player, "money", PierscienMagnata.ENDOWMENT_MONEY);
		stepAttending(dobrawa, player, "fundusz");
		stepAttending(dobrawa, player, "zapas");
	}

	private static void completePledge(final Player player) {
		stepAttending(edgard, player, "poręczenie");
		stepAttending(edgard, player, "słowo");
	}

	private static void completeAccount(final Player player) {
		stepAttending(edragon, player, "rachunek");
		stepAttending(edragon, player, "miarę");
	}

	private static void makeStewardshipOldEnough(final Player player) {
		final long started = System.currentTimeMillis()
				- (PierscienMagnata.STEWARDSHIP_MINUTES + 1L) * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienMagnata.STEWARD_SLOT,
				PierscienMagnata.STEWARD_STARTED, Long.toString(started));
	}

	@Test
	public void newTrialRequiresBaronStatusAndOldResponsibilities() {
		final Player player = PlayerTestHelper.createPlayer("GateMagnat");
		player.setLevel(500);
		askQuest(player);
		assertTrue(getReply(zdzichu).contains("baroniego znaku"));
		assertFalse(player.hasQuest(PierscienMagnata.QUEST_SLOT));
		player.setQuest(PierscienBarona.QUEST_SLOT, PierscienBarona.STATE_DONE);
		PlayerTestHelper.equipWithItem(player, "pierścień barona");
		askQuest(player);
		assertTrue(getReply(zdzichu).contains("dawne powinności"));
		assertFalse(player.hasQuest(PierscienMagnata.QUEST_SLOT));
	}

	@Test
	public void stewardshipDialogueStartsCouncilInsteadOfGivingMaterials() {
		final Player player = eligiblePlayer("StewardMagnat");
		final Engine engine = zdzichu.getEngine();
		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_7, engine.getCurrentState());
		assertTrue(getReply(zdzichu).contains("#dostatkiem"));
		engine.step(player, "dostatkiem");
		assertEquals(ConversationStates.INFORMATION_8, engine.getCurrentState());
		assertTrue(getReply(zdzichu).contains("#piecza"));
		engine.step(player, "piecza");
		assertEquals(ConversationStates.QUEST_OFFERED, engine.getCurrentState());
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_COUNCIL));
		assertTrue(player.isEquipped("pierścień barona"));
		final String councilReply = getReply(zdzichu);
		assertTrue(councilReply.contains("Dobrawa"));
		assertTrue(councilReply.contains("Edgard"));
		assertTrue(councilReply.contains("eDragon"));
		assertFalse(player.hasQuest(PierscienMagnata.COUNCIL_SLOT));
	}

	@Test
	public void eachCouncilWitnessRequiresItsOwnFollowUpWord() {
		final Player player = eligiblePlayer("CouncilWords");
		startCouncil(player);
		stepAttending(dobrawa, player, "wspólnota");
		assertEquals(PierscienMagnata.COUNCIL_ASKED, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY));
		assertTrue(getReply(dobrawa).contains("#zaufania"));
		stepAttending(dobrawa, player, "zaufania");
		assertEquals(PierscienMagnata.COUNCIL_DONE, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY));
		stepAttending(edgard, player, "słowo");
		assertEquals(PierscienMagnata.COUNCIL_ASKED, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_DUTY));
		assertTrue(getReply(edgard).contains("#obowiązek"));
		stepAttending(edgard, player, "obowiązek");
		assertEquals(PierscienMagnata.COUNCIL_DONE, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_DUTY));
		stepAttending(edragon, player, "prawo");
		assertEquals(PierscienMagnata.COUNCIL_ASKED, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_LAW));
		assertTrue(getReply(edragon).contains("#miary"));
		stepAttending(edragon, player, "miary");
		assertEquals(PierscienMagnata.COUNCIL_DONE, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_LAW));
	}

	@Test
	public void councilAskedStateSurvivesRestartAndCompletedWitnessCannotReset() {
		final Player player = eligiblePlayer("CouncilAskedResume");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_COUNCIL);
		player.setQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY, PierscienMagnata.COUNCIL_ASKED);
		stepAttending(dobrawa, player, "zaufanie");
		assertEquals(PierscienMagnata.COUNCIL_DONE, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY));
		stepAttending(dobrawa, player, "wspólnota");
		assertEquals(PierscienMagnata.COUNCIL_DONE, player.getQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY));
		assertTrue(getReply(dobrawa).contains("już masz"));
	}

	@Test
	public void threeCouncilVotesOnlyOpenTheRealStewardshipTrial() {
		final Player player = eligiblePlayer("CouncilGate");
		completeCouncil(player);
		askQuest(player);
		final String councilCompleteReply = getReply(zdzichu);
		assertTrue(councilCompleteReply.contains("#pieczę"));
		assertFalse(councilCompleteReply.contains("#oprawy"));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_COUNCIL));
		zdzichu.getEngine().step(player, "pieczę");
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_STEWARDSHIP));
		assertTrue(getReply(zdzichu).contains("#fundusz"));
	}

	@Test
	public void communityFundMustBeCompleteAndIsNotARefundableFee() {
		final Player player = eligiblePlayer("FundMagnat");
		startStewardship(player);
		PlayerTestHelper.equipWithStackableItem(player, "money", PierscienMagnata.ENDOWMENT_MONEY - 1);
		stepAttending(dobrawa, player, "fundusz");
		assertEquals(PierscienMagnata.STEWARD_ASKED, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND));
		stepAttending(dobrawa, player, "zapas");
		assertTrue(player.isEquipped("money", PierscienMagnata.ENDOWMENT_MONEY - 1));
		assertEquals(PierscienMagnata.STEWARD_ASKED, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND));
		PlayerTestHelper.equipWithStackableItem(player, "money", 1);
		stepAttending(dobrawa, player, "zapas");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND));
		assertFalse(player.isEquipped("money"));
		assertTrue(player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_STARTED).length() > 0);
	}

	@Test
	public void pledgeAndPublicAccountRequireFundAndHaveFollowUpWords() {
		final Player player = eligiblePlayer("PledgeMagnat");
		startStewardship(player);
		stepAttending(edgard, player, "poręczenie");
		assertFalse(player.hasQuest(PierscienMagnata.STEWARD_SLOT));
		stepAttending(edragon, player, "rachunek");
		assertFalse(player.hasQuest(PierscienMagnata.STEWARD_SLOT));
		createFund(player);
		stepAttending(edgard, player, "poręczenie");
		assertEquals(PierscienMagnata.STEWARD_ASKED, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_PLEDGE));
		assertTrue(getReply(edgard).contains("#słowo"));
		stepAttending(edgard, player, "słowo");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_PLEDGE));
		stepAttending(edragon, player, "rachunek");
		assertEquals(PierscienMagnata.STEWARD_ASKED, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT));
		assertTrue(getReply(edragon).contains("#miarę"));
		stepAttending(edragon, player, "miara");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT));
	}

	@Test
	public void completedStewardshipEntriesAreIdempotent() {
		final Player player = eligiblePlayer("IdempotentMagnat");
		startStewardship(player);
		createFund(player);
		completePledge(player);
		completeAccount(player);
		stepAttending(dobrawa, player, "fundusz");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND));
		stepAttending(edgard, player, "poręczenie");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_PLEDGE));
		stepAttending(edragon, player, "rachunek");
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT));
	}

	@Test
	public void completedObligationsStillRequireNinetyMinutesOfStewardship() {
		final Player player = eligiblePlayer("StewardTimeMagnat");
		startStewardship(player);
		createFund(player);
		completePledge(player);
		completeAccount(player);
		askQuest(player);
		final String waitingReply = getReply(zdzichu);
		assertTrue(waitingReply.contains("trwałej pieczy"));
		assertFalse(waitingReply.contains("#oprawa"));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_STEWARDSHIP));
		makeStewardshipOldEnough(player);
		askQuest(player);
		assertTrue(getReply(zdzichu).contains("#oprawę"));
		zdzichu.getEngine().step(player, "oprawę");
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS));
		assertTrue(getReply(zdzichu).contains("#materiałów"));
	}

	@Test
	public void stewardshipProgressSurvivesRestartAndListsOnlyMissingDuties() {
		final Player player = eligiblePlayer("StewardResumeMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_STEWARDSHIP);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND, PierscienMagnata.STEWARD_DONE);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_PLEDGE, PierscienMagnata.STEWARD_DONE);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT, PierscienMagnata.STEWARD_ASKED);
		final long started = System.currentTimeMillis() - 5L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_STARTED, Long.toString(started));
		askQuest(player);
		final String reply = getReply(zdzichu);
		assertFalse(reply.contains("Dobrawy"));
		assertFalse(reply.contains("Edgarda"));
		assertTrue(reply.contains("eDragona"));
		assertTrue(reply.contains("trwałej pieczy"));
		assertEquals(PierscienMagnata.STEWARD_DONE, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND));
		assertEquals(PierscienMagnata.STEWARD_ASKED, player.getQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT));
	}

	@Test
	public void malformedStewardshipTimestampDoesNotTrapFinishedObligations() {
		final Player player = eligiblePlayer("BrokenStewardMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_STEWARDSHIP);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_FUND, PierscienMagnata.STEWARD_DONE);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_PLEDGE, PierscienMagnata.STEWARD_DONE);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_ACCOUNT, PierscienMagnata.STEWARD_DONE);
		player.setQuest(PierscienMagnata.STEWARD_SLOT, PierscienMagnata.STEWARD_STARTED, "broken");
		askQuest(player);
		assertTrue(getReply(zdzichu).contains("#oprawę"));
	}

	@Test
	public void councilProgressSurvivesConversationRestart() {
		final Player player = eligiblePlayer("CouncilResume");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_COUNCIL);
		player.setQuest(PierscienMagnata.COUNCIL_SLOT, PierscienMagnata.COUNCIL_COMMUNITY, PierscienMagnata.COUNCIL_DONE);
		askQuest(player);
		final String reply = getReply(zdzichu);
		assertFalse(reply.contains("Dobrawa"));
		assertTrue(reply.contains("Edgard"));
		assertTrue(reply.contains("eDragon"));
	}

	@Test
	public void incompleteMaterialSetDoesNotConsumeBaronRing() {
		final Player player = eligiblePlayer("MissingMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka srebra", 80);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 50);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka mithrilu", 24);
		final Engine engine = zdzichu.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS));
		assertTrue(player.isEquipped("pierścień barona"));
		assertTrue(player.isEquipped("sztabka srebra", 80));
		assertTrue(player.isEquipped("sztabka złota", 50));
		assertTrue(player.isEquipped("sztabka mithrilu", 24));
		assertTrue(getReply(zdzichu).contains("Brakuje"));
	}

	@Test
	public void completeMaterialSetStartsThreeHourForging() {
		final Player player = eligiblePlayer("ForgeMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka srebra", 80);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka złota", 50);
		PlayerTestHelper.equipWithStackableItem(player, "sztabka mithrilu", 25);
		final Engine engine = zdzichu.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		engine.step(player, "materiały");
		engine.step(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertTrue(player.getQuest(PierscienMagnata.QUEST_SLOT).startsWith(PierscienMagnata.FORGING_PREFIX));
		assertFalse(player.isEquipped("pierścień barona"));
		assertFalse(player.isEquipped("sztabka srebra"));
		assertFalse(player.isEquipped("sztabka złota"));
		assertFalse(player.isEquipped("sztabka mithrilu"));
		assertFalse(player.isEquipped("pierścień magnata"));
	}

	@Test
	public void magnateRingCannotBeCollectedBeforeForgingFinishes() {
		final Player player = PlayerTestHelper.createPlayer("WaitingMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.FORGING_PREFIX + System.currentTimeMillis());
		askQuest(player);
		assertTrue(getReply(zdzichu).contains("Jeszcze nie"));
		assertFalse(player.isEquipped("pierścień magnata"));
	}

	@Test
	public void magnateRingIsCollectedAfterThreeHoursAndAwardsXP() {
		final Player player = PlayerTestHelper.createPlayer("ReadyMagnat");
		final int initialXp = player.getXP();
		final long started = System.currentTimeMillis() - 181L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.FORGING_PREFIX + started);
		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, zdzichu.getEngine().getCurrentState());
		assertTrue(getReply(zdzichu).contains("#pierścień"));
		zdzichu.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_DONE));
		assertTrue(player.isEquipped("pierścień magnata"));
		assertEquals(initialXp + 500000, player.getXP());
	}

	@Test
	public void legacyStartStateUsesNewMaterialSetWithoutCouncilOrStewardship() {
		final Player player = eligiblePlayer("LegacyMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_LEGACY_START);
		stepAttending(zdzichu, player, "przypomnij");
		final String reply = getReply(zdzichu);
		assertEquals(ConversationStates.QUEST_ITEM_QUESTION, zdzichu.getEngine().getCurrentState());
		assertTrue(reply.contains("80 sztabek srebra"));
		assertTrue(reply.contains("50 sztabek złota"));
		assertTrue(reply.contains("25 sztabek mithrilu"));
		assertFalse(reply.contains("150000"));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_LEGACY_START));
		assertFalse(player.hasQuest(PierscienMagnata.COUNCIL_SLOT));
		assertFalse(player.hasQuest(PierscienMagnata.STEWARD_SLOT));
	}

	@Test
	public void currentMaterialsStateDoesNotForcePlayerBackToCouncilOrStewardship() {
		final Player player = eligiblePlayer("CurrentMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS);
		askQuest(player);
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_MATERIALS));
		assertFalse(player.hasQuest(PierscienMagnata.COUNCIL_SLOT));
		assertFalse(player.hasQuest(PierscienMagnata.STEWARD_SLOT));
	}

	@Test
	public void malformedForgingTimestampDoesNotTrapPlayer() {
		final Player player = PlayerTestHelper.createPlayer("BrokenMagnat");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.FORGING_PREFIX + "broken");
		askQuest(player);
		assertEquals(ConversationStates.INFORMATION_9, zdzichu.getEngine().getCurrentState());
		zdzichu.getEngine().step(player, "pierścień");
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_DONE));
		assertTrue(player.isEquipped("pierścień magnata"));
	}
}
