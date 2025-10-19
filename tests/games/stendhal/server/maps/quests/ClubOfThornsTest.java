/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ClubOfThornsTest {
	private static final String NPC = "Szaman Orków";
	private static final String KEY_NAME = "klucz do więzienia Kotoch";
	private static final String QUEST_NAME = "club_thorns";
	private static final String VICTIM = "szef górskich orków";

	@BeforeClass
	public static void setupBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().remove(NPC);
	}

	@Test
	public final void rejectQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final ClubOfThorns quest = new ClubOfThorns();
		quest.addToWorld();
		final SpeakerNPC npc = quest.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		final double karma = player.getKarma();

		// Greetings missing. Jump straight to attending
		en.setCurrentState(ConversationStates.ATTENDING);

		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Zemścij się! Zabij szefa górskich orków i jego towarzyszy! Zrozumiałeś?", getReply(npc));

		en.stepTest(player, "no");
		assertEquals("Answer to refusal", "Ugg! Chcę człowieka, który wykona wyrok na szefie górskich orków a nie mazgaja.", getReply(npc));
		assertEquals("Karma penalty", karma - 6.0, player.getKarma(), 0.01);
	}

	@Test
	public final void doQuest() {
		SingletonRepository.getNPCList().add(new SpeakerNPC(NPC));
		final ClubOfThorns quest = new ClubOfThorns();
		quest.addToWorld();
		final SpeakerNPC npc = quest.npcs.get(NPC);
		final Engine en = npc.getEngine();
		final Player player = PlayerTestHelper.createPlayer("player");
		double karma = player.getKarma();

		// Kill a szef górskich orków to to allow checking the slot gets cleaned
		player.setSoloKill(VICTIM);
		// Greetings missing. Jump straight to attending
		en.setCurrentState(ConversationStates.ATTENDING);

		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Zemścij się! Zabij szefa górskich orków i jego towarzyszy! Zrozumiałeś?", getReply(npc));

		// test the stuff that should be done at the quest start
		en.stepTest(player, ConversationPhrases.YES_MESSAGES.get(0));
		assertEquals("Weź ten klucz. On jest w więzieniu. Zabij go! Potem, wróć ze słowami: #zabity!", getReply(npc));
		assertTrue(player.isEquipped(KEY_NAME));
		assertEquals("player", player.getFirstEquipped(KEY_NAME).getBoundTo());
		assertEquals("Karma bonus for accepting the quest",
			karma + 10.0, player.getKarma(), 0.01);
		assertEquals("start", player.getQuest(QUEST_NAME, 0));
		//assertFalse("Cleaning kill slot", player.hasKilled(VICTIM));
		final String[] questTokens = player.getQuest(QUEST_NAME, 1).split(",");
		assertEquals(questTokens[0],"szef górskich orków");
		assertEquals(questTokens[1],"0");
		assertEquals(questTokens[2],"1");
		assertEquals(Arrays.asList(questTokens).size(), 5);

		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Zemścij się! #Zabij szefa górskich orków!", getReply(npc));

		en.stepTest(player, "kill");
		assertEquals("Zabij Szefa górskich orków! Orki z Kotoch chcą zemsty!", getReply(npc));

		// Kill a szef górskich orków
		player.setSoloKill("szef górskich orków");
		// Try restarting the task in the middle
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Zemścij się! #Zabij szefa górskich orków!", getReply(npc));
		assertTrue("Keeping the kill slot, while the quest is active", player.hasKilled(VICTIM));

		// completion and rewards
		karma = player.getKarma();
		en.stepTest(player, "kill");
		assertEquals("Zemsta dokonana! Dobrze! Weź tą potężną maczugę cierniową w nagrodę.", getReply(npc));
		assertTrue(player.isEquipped("maczuga cierniowa"));
		assertEquals("The club is bound", "player", player.getFirstEquipped("maczuga cierniowa").getBoundTo());
		assertEquals("Final karma bonus", karma + 20.0, player.getKarma(), 0.01);
		assertEquals("XP", 10000L, player.getXP());
		assertEquals("done", player.getQuest(QUEST_NAME));

		// don't allow restarting
		en.stepTest(player, ConversationPhrases.QUEST_MESSAGES.get(0));
		assertEquals("Szaman zemścił się! Dobrze!", getReply(npc));
		assertEquals("done", player.getQuest(QUEST_NAME));
	}
}
