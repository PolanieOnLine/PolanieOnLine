/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

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

public class PierscienMagnataNarrativeTest {
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

	private static void startCouncil(final Player player) {
		final Engine engine = zdzichu.getEngine();
		stepAttending(zdzichu, player, ConversationPhrases.QUEST_MESSAGES.get(0));
		engine.step(player, "dostatek");
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

	@Test
	public void councilWitnessesRecallTheThreeEarlierStatuses() {
		final Player player = eligiblePlayer("NarrativeCouncil");
		startCouncil(player);

		stepAttending(dobrawa, player, "wspólnota");
		final String communityReply = getReply(dobrawa);
		assertTrue(communityReply.contains("próby mieszczanina"));
		assertTrue(communityReply.contains("#zaufania"));

		stepAttending(edgard, player, "słowo");
		final String dutyReply = getReply(edgard);
		assertTrue(dutyReply.contains("rycerską wartę"));
		assertTrue(dutyReply.contains("#obowiązek"));

		stepAttending(edragon, player, "prawo");
		final String lawReply = getReply(edragon);
		assertTrue(lawReply.contains("baroniej próbie"));
		assertTrue(lawReply.contains("#miary"));
	}

	@Test
	public void threeVotesAreFramedAsOneCombinedMagnateDuty() {
		final Player player = eligiblePlayer("NarrativeGate");
		completeCouncil(player);
		stepAttending(zdzichu, player, ConversationPhrases.QUEST_MESSAGES.get(0));

		final String reply = getReply(zdzichu);
		assertTrue(reply.contains("zaufanie mieszczanina"));
		assertTrue(reply.contains("służbę i słowo rycerza"));
		assertTrue(reply.contains("odpowiedzialność barona za prawo"));
		assertTrue(reply.contains("#pieczę"));
	}

	@Test
	public void finalRingDialogueClosesTheWholeStatusPath() {
		final Player player = PlayerTestHelper.createPlayer("NarrativeFinish");
		final long started = System.currentTimeMillis() - 181L * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.FORGING_PREFIX + started);
		stepAttending(zdzichu, player, ConversationPhrases.QUEST_MESSAGES.get(0));
		zdzichu.getEngine().step(player, "pierścień");

		final String reply = getReply(zdzichu);
		assertTrue(reply.contains("Mieszczanin"));
		assertTrue(reply.contains("Rycerz"));
		assertTrue(reply.contains("Baron"));
		assertTrue(reply.contains("Magnat"));
		assertTrue(player.isQuestInState(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_DONE));
	}

	@Test
	public void resumedCouncilHistoryUsesNaturalBaronWording() {
		final Player player = eligiblePlayer("NarrativeHistory");
		player.setQuest(PierscienMagnata.QUEST_SLOT, PierscienMagnata.STATE_COUNCIL);
		player.setQuest(PierscienMagnata.COUNCIL_SLOT,
				PierscienMagnata.COUNCIL_LAW, PierscienMagnata.COUNCIL_DONE);

		final String history = String.join(" ", new PierscienMagnata().getHistory(player));
		assertTrue(history.contains("baronowska miara prawa"));
		assertFalse(history.contains("baronia miara"));
	}
}
