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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class CrownForTheWannaBeKingTest {
	private static final String QUEST_SLOT = "crown_for_the_wannabe_king";
	private static Engine npcEngine;
	private static SpeakerNPC npc;

	@BeforeClass
	public static void setUpBeforeClass() {
		PlayerTestHelper.generateNPCRPClasses();

		npc = new SpeakerNPC("Ivan Abe");
		SingletonRepository.getNPCList().add(npc);

		final SpeakerNPC rewardnpc = new SpeakerNPC("Kendra Mattori");
		SingletonRepository.getNPCList().add(rewardnpc);

		final CrownForTheWannaBeKing cotbk = new CrownForTheWannaBeKing();
		cotbk.addToWorld();

		npcEngine = npc.getEngine();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		SingletonRepository.getNPCList().clear();
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Tests for idleToQuestion1.
	 */
	@Test
	public void testIdleToQuestion1() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, "");
			npcEngine.setCurrentState(ConversationStates.IDLE);
			npcEngine.step(bob, playerSays);
			assertThat("Used greeting: " + playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Och to znowu ty. Czy przyniosłeś jakieś #przedmioty do mojej korony?", getReply(npc));
		}
	}

	/**
	 * Tests for idleToIdleQuestCompleted.
	 */
	@Test
	public void testIdleToIdleQuestCompleted() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {

			final Player bob = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(ConversationStates.IDLE);
			bob.setQuest(QUEST_SLOT, "done");
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(true));
			npcEngine.step(bob, playerSays);
			assertThat(npcEngine.getCurrentState(), is(ConversationStates.IDLE));
			assertEquals("Moja nowa korona będzie wkrótce gotowa i zdetronizuję króla! Uhahaha!", getReply(npc));
		}
	}

	/**
	 * Tests for idleToIdleQuestinStatereward.
	 */
	@Test
	public void testIdleToIdleQuestinStatereward() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {

			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, "reward");
			npcEngine.setCurrentState(ConversationStates.IDLE);

			npcEngine.step(bob, playerSays);
			assertThat(npcEngine.getCurrentState(), is(ConversationStates.IDLE));
			assertEquals("Moja nowa korona będzie wkrótce gotowa i zdetronizuję króla! Uhahaha!", getReply(npc));
		}
	}

	/**
	 * Tests for idleToAttending.
	 */
	@Test
	public void testIdleToAttending() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {

			final Player bob = PlayerTestHelper.createPlayer("bob");
			assertThat(bob.hasQuest(QUEST_SLOT), is(false));
			npcEngine.setCurrentState(ConversationStates.IDLE);

			npcEngine.step(bob, playerSays);
			assertThat(npcEngine.getCurrentState(), is(ConversationStates.ATTENDING));
			assertEquals(
					"Pozdrawiam. Szybko, jaką masz do mnie sprawę, bo mam dużo pracy do zrobienia., a następnym razem wyczyść swoje buty. Masz szczęście, że nie jestem królem...jeszcze!",
					getReply(npc));
		}
	}

	/**
	 * Tests for attendingToQuestOffered.
	 */
	@Test
	public void testAttendingToQuestOffered() {
		final Player bob = PlayerTestHelper.createPlayer("bob");
		npcEngine.setCurrentState(ConversationStates.ATTENDING);
		assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
		npcEngine.step(bob, "crown");
		assertThat(npcEngine.getCurrentState(), is(ConversationStates.QUEST_OFFERED));
		assertEquals("Tak, potrzebuję klejnotów i złota na moją nową koronę. Pomożesz mi?", getReply(npc));
	}

	/**
	 * Tests for attendingToIdle.
	 */
	@Test
	public void testAttendingToIdle() {
		final Player bob = PlayerTestHelper.createPlayer("bob");
		npcEngine.setCurrentState(ConversationStates.ATTENDING);
		npcEngine.step(bob, "reward");
		assertThat(npcEngine.getCurrentState(), is(ConversationStates.IDLE));
		assertEquals(
				"Tak jak powiedziałem znajdź kapłankę Kendra Mattori w świątyni w mieście czarodziejów. Ona da Ci nagrodę. Teraz idź już jestem zajęty!",
				getReply(npc));
	}

	/**
	 * Tests for attendingToIdleQuestNotCompleted.
	 */
	@Test
	public void testAttendingToIdleQuestNotCompleted() {
		final String[] triggers = { "no", "nothing" };
		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(ConversationStates.ATTENDING);
			bob.setQuest(QUEST_SLOT, "");
			assertThat(bob.hasQuest(QUEST_SLOT), is(true));
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			npcEngine.step(bob, playerSays);
			assertThat(npcEngine.getCurrentState(), is(ConversationStates.IDLE));
			assertEquals("Cóż, nie wracaj dopóki nie znajdziesz czegoś dla mnie!", getReply(npc));
		}
	}

	/**
	 * Tests for questOfferedToQuestion1.
	 */
	@Test
	public void testQuestOfferedToQuestion1() {
		for (final String playerSays : ConversationPhrases.YES_MESSAGES) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(ConversationStates.QUEST_OFFERED);
			assertTrue(new QuestNotStartedCondition(QUEST_SLOT).fire(bob, null, npc));

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertThat(playerSays, bob.hasQuest(QUEST_SLOT), is(true));
			assertEquals(
					"Chcę, aby moja korona była piękna i lśniąca. Potrzebuję 2 #diamenty, 1 #obsydian, 2 #rubiny, 3 #szafiry, 4 #szmaragdy, oraz 2 #'sztabka złota'."
					+ " Masz coś z tego przy sobie?",
					getReply(npc));
		}
	}

	/**
	 * Tests for questOfferedToIdle.
	 */
	@Test
	public void testQuestOfferedToIdle() {
		final String[] triggers = { "no", "nothing" };
		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			final double oldkarma = bob.getKarma();
			npcEngine.setCurrentState(ConversationStates.QUEST_OFFERED);
			assertThat(playerSays, bob.isQuestCompleted(QUEST_SLOT), is(false));
			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.IDLE));
			assertThat(playerSays, bob.getKarma(), lessThan(oldkarma));
			assertThat(playerSays, bob.hasQuest(QUEST_SLOT), is(true));
			assertThat(playerSays, bob.getQuest(QUEST_SLOT), is("rejected"));
			assertEquals("Och. Nie chcesz mi pomóc?! Wynoś się stąd marnujesz mój cenny czas!", getReply(npc));
		}
	}

	/**
	 * Tests for attendingToAttending.
	 */
	@Test
	public void testAttendingToAttending() {
		final String[] triggers = { "plan", "favor", "favour", "quest", "task", "work", "job", "trade", "deal", "offer" };
		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");

			npcEngine.setCurrentState(ConversationStates.ATTENDING);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.ATTENDING));
		}
	}

	/**
	 * Tests for question1toQuestion1.
	 */
	@Test
	public void testQuestion1toQuestion1() {
		for (final String playerSays : ConversationPhrases.YES_MESSAGES) {
			final Player bob = PlayerTestHelper.createPlayer("bob");

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
		}
	}

	/**
	 * Tests for question1toQuestion1PosactionList.
	 */
	@Test
	public void testQuestion1toQuestion1PosactionList() {
		npc.remove("text");
		final Player bob = PlayerTestHelper.createPlayer("bob");
		bob.setQuest(QUEST_SLOT, CrownForTheWannaBeKing.NEEDED_ITEMS);
		npcEngine.setCurrentState(ConversationStates.QUESTION_1);

		npcEngine.step(bob, "items");
		assertThat("items", npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
		assertThat(
				"items",
				getReply(npc),
				is("Potrzebuję 2 #diamenty, 1 #obsydian, 2 #rubiny, 3 #szafiry, 4 #szmaragdy, oraz 2 #'sztabka złota'. Przyniosłeś coś z tego?"));
	}

	/**
	 * Tests for question1ToIdle.
	 */
	@Test
	public void testQuestion1ToIdle() {
		final String[] triggers = { "no", "nothing" };
		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, "");
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.IDLE));
		}
	}

	/**
	 * Tests for question1ToQuestion1Itembrought.
	 */
	@Test
	public void testQuestion1ToQuestion1Itembrought() {
		final String[] triggers = { "obsydian", "diament", "rubin", "szafir", "szmaragd", "sztabka złota" };

		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, "");
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Już to przynisłeś!", getReply(npc));
		}

		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, CrownForTheWannaBeKing.NEEDED_ITEMS);
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Nie masz przy sobie " + playerSays + "!", getReply(npc));
		}

		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, CrownForTheWannaBeKing.NEEDED_ITEMS);

			PlayerTestHelper.equipWithItem(bob, playerSays);
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Dobra, masz coś jeszcze?", getReply(npc));
			assertThat(bob.getQuest(QUEST_SLOT), not((is(CrownForTheWannaBeKing.NEEDED_ITEMS))));
		}

		for (final String playerSays : triggers) {
			final Player bob = PlayerTestHelper.createPlayer("bob");
			bob.setQuest(QUEST_SLOT, CrownForTheWannaBeKing.NEEDED_ITEMS);

			PlayerTestHelper.equipWithItem(bob, playerSays);
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.setCurrentState(ConversationStates.QUESTION_1);

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Dobra, masz coś jeszcze?", getReply(npc));
			assertThat(bob.getQuest(QUEST_SLOT), not((is(CrownForTheWannaBeKing.NEEDED_ITEMS))));
		}
	}

	/**
	 * Tests for bringItems.
	 */
	@Test
	public void testBringItems() {
		final Player bob = PlayerTestHelper.createPlayer("bob");
		bob.setQuest(QUEST_SLOT, CrownForTheWannaBeKing.NEEDED_ITEMS);

		// is("I need 2 #gold bar, 4 #emerald, 3 #sapphire, 2 #carbuncle,
		// 2 #diamond, and 1 #obsidian. Did you bring something?"));
		final String[] triggers = { "obsydian", "diament", "diament", "rubin", "rubin", "szafir", "szafir",
				"szafir", "szmaragd", "szmaragd", "szmaragd", "szmaragd", "sztabka złota" };
		npcEngine.setCurrentState(ConversationStates.QUESTION_1);
		for (final String playerSays : triggers) {
			PlayerTestHelper.equipWithItem(bob, playerSays);
			assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
			assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

			npcEngine.step(bob, playerSays);
			assertThat(playerSays, npcEngine.getCurrentState(), is(ConversationStates.QUESTION_1));
			assertEquals("Dobra, masz coś jeszcze?", getReply(npc));
			assertThat(bob.getQuest(QUEST_SLOT), not((is(CrownForTheWannaBeKing.NEEDED_ITEMS))));
		}

		PlayerTestHelper.equipWithItem(bob, "sztabka złota");
		assertThat(bob.isQuestCompleted(QUEST_SLOT), is(false));
		assertThat(bob.getQuest(QUEST_SLOT), not((is("reward"))));

		npcEngine.step(bob, "sztabka złota");
		assertEquals(
				"Służyłeś mi dobrze. Moja korona będzie najwspanialsza spośród wszystkich! Spotkaj się z Kendra Mattori w mieście magów, aby odebrać swoją #nagrodę.",
				getReply(npc));
        assertThat("last thing brought", npcEngine.getCurrentState(), is(ConversationStates.ATTENDING));
        assertEquals("reward", bob.getQuest(QUEST_SLOT));

		final double oldkarma = bob.getKarma();
		final int oldatk = bob.getAtkXP();

		final SpeakerNPC rewardnpc = SingletonRepository.getNPCList().get("Kendra Mattori");
		final Engine rewardEngine = rewardnpc.getEngine();
		rewardEngine.setCurrentState(ConversationStates.ATTENDING);
		rewardEngine.step(bob, "reward");

		assertThat(bob.isQuestCompleted(QUEST_SLOT), is(true));
		assertThat(bob.getKarma(), greaterThan(oldkarma));
		assertThat(bob.getAtkXP(), greaterThan(oldatk));
	}

}
