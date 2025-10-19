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

import static games.stendhal.server.entity.npc.ConversationStates.ATTENDING;
import static games.stendhal.server.entity.npc.ConversationStates.IDLE;
import static games.stendhal.server.entity.npc.ConversationStates.QUESTION_1;
import static games.stendhal.server.entity.npc.ConversationStates.QUEST_OFFERED;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class ElvishArmorTest {
	private static Engine npcEngine;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		MockStendlRPWorld.get();
		PlayerTestHelper.generateNPCRPClasses();
		npc = new SpeakerNPC("Lupos");
		npcEngine = npc.getEngine();
		SingletonRepository.getNPCList().add(npc);
		new ElvishArmor().addToWorld();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		SingletonRepository.getNPCList().clear();
	}

	private static final String QUEST_SLOT = "elvish_armor";

	private static final List<String> NEEDEDITEMS = Arrays.asList(
			"zbroja elficka", "spodnie elfickie", "buty elfickie", "miecz elficki",
			"płaszcz elficki", "tarcza elficka", "kapelusz elficki", "rękawice elfickie",
			"hełm elficki");

	private static SpeakerNPC npc;

	/**
	 * Tests for idleTOAttending.
	 */
	@Test
	public void testIdleTOAttending() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(IDLE);
			assertThat(player.hasQuest(QUEST_SLOT), is(false));

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));
			assertThat(playerSays, getReply(npc), is("Witaj wędrowcze, widzę że przyszedłeś z bardzo daleka. Interesuję się każdym kto widział naszych pobratymców, zielonych elfów z Nalwor. Pilnują swoich #sekretów."));
		}
	}


	/**
	 * Tests for questOfferedToQuestOffered.
	 */
	@Test
	public void testQuestOfferedToQuestOffered() {
		for (final String playerSays : Arrays.asList("secrets")) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(QUEST_OFFERED));
			assertThat(playerSays, getReply(npc), is("Nie chcą ujawnić wiedzy jak stworzyć zieloną zbroję, tarcze i inne. Wolą je nazywać elfiszami. Zastanawiam się czy przybysz taki jak ty mógłby mi przynieść dowolny elfisz?"));
		}
	}


	/**
	 * Tests for questOfferedToIdle.
	 */
	@Test
	public void testQuestOfferedToIdle() {
		for (final String playerSays : ConversationPhrases.YES_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(QUEST_OFFERED);

			assertFalse(player.hasQuest(QUEST_SLOT));

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(IDLE));
			assertThat(playerSays, getReply(npc), is("Sekrety zielonych elfów w końcu byłyby nareszcie nasze! Przynieś mi wszystkie elfisze jakie znajdziesz. Dobrze Cię wynagrodzę!"));
			assertTrue(playerSays, player.hasQuest(QUEST_SLOT));
		}
	}

	/**
     * Player is not willing to help.
     */
	@Test
	public void testQuestOfferedToQuestOfferes() {
		for (final String playerSays : Arrays.asList("no", "nothing")) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(QUEST_OFFERED);

			assertFalse(player.hasQuest(QUEST_SLOT));
			final double oldkarma = player.getKarma();

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(IDLE));
			assertThat(playerSays, getReply(npc), is("Widzę, że jesteś niechętny do pomocy."));
			assertFalse(playerSays, player.hasQuest(QUEST_SLOT));
			assertThat(playerSays, player.getKarma(), lessThan(oldkarma));
		}
	}


	/**
	 *  Player returns while quest is still active.
	 */
	@Test
	public void testIdleToQuestion1() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(IDLE);
			player.setQuest(QUEST_SLOT, "");
			assertTrue(player.hasQuest(QUEST_SLOT));
			assertFalse(player.isQuestCompleted(QUEST_SLOT));


			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));
			assertThat(playerSays, getReply(npc), is("Witam! Mam nadzieję, że dobrze Ci idzie poszukiwanie elfiszowego #rynsztunku?"));
		}
	}



	/**
	 * Player says he the name of a required item he has not got.
	 */
	@Test
	public void testQuestion1ToQuestion1NeededITems() {
		for (final String playerSays : NEEDEDITEMS) {

			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(QUESTION_1);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(QUESTION_1));
			assertThat(playerSays, getReply(npc), is("Kłamca! Nie masz " + playerSays + " ze sobą."));
		}
	}

	/**
	 * Player says the name of a required item he has got.
	 * and repeats it (brings it twice).
	 */
	@Test
	public void testQuestion1ToQuestion1NeededITemsGot() {
		for (final String playerSays : NEEDEDITEMS) {
			final Player player = PlayerTestHelper.createPlayer("bob");

			PlayerTestHelper.equipWithItem(player, playerSays);
			npcEngine.setCurrentState(QUESTION_1);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(QUESTION_1));
			assertThat(playerSays, getReply(npc), is("Doskonała robota. Zrabowałeś coś jeszcze?"));

			PlayerTestHelper.equipWithItem(player, playerSays);
			npcEngine.setCurrentState(QUESTION_1);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Już mi przyniosłeś ten elficki rynsztunek."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(QUESTION_1));
		}
	}

	/**
	 * Player brings all items.
	 */
	@Test
	public void testQuestion1ToAttending() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		final double oldKarma = player.getKarma();
		final long oldXp = player.getXP();
		npcEngine.setCurrentState(QUESTION_1);
		for (final String playerSays : NEEDEDITEMS) {
			PlayerTestHelper.equipWithItem(player, playerSays);
			npcEngine.step(player, playerSays);
		}
		assertThat(npcEngine.getCurrentState(), is(ATTENDING));
		assertThat(getReply(npc), is("Zbadam to! Albino elfy mają dług wdzięczności wobec Ciebie."));
		assertThat(player.getKarma(), greaterThan(oldKarma));
		assertThat(player.getXP(), is(oldXp + 20000));
	}

	/**
	 * Player brings an item not in list.
	 */
	@Test
	public void testQuestion1ToQuestion1NotInList() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		npcEngine.setCurrentState(QUESTION_1);
		npcEngine.step(player, "NotanItem");

		assertThat(npcEngine.getCurrentState(), is(QUESTION_1));
		assertThat(getReply(npc), is("Nie sądzę, aby to był fragment elfickiego wyposażenia..."));
	}

	/**
	 * Tests for question1toIdle.
	 */
    @Ignore
    @Test
    // ignored because removed from quest logic now.
    public void testQuestion1toIdle() {
	    for (final String playerSays : ConversationPhrases.GOODBYE_MESSAGES) {
		    final Player player = PlayerTestHelper.createPlayer("bob");
		    npcEngine.setCurrentState(QUESTION_1);

		    npcEngine.step(player, playerSays);

		    assertThat(playerSays, npcEngine.getCurrentState(), is(IDLE));
		    assertThat(playerSays, getReply(npc), is("Do widzenia."));
	    }
    }

	/**
	 * Tests for attendingToAttending.
	 */
	@Test
	public void testAttendingToAttending() {
		for (final String playerSays : Arrays.asList("no", "nothing")) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, npcEngine.getCurrentState(), is(IDLE));
			assertThat(playerSays, getReply(npc), is("Rozumiem, że zielone elfy dobrze się chronią. Jeżeli mógłbym coś innego dla Ciebie zrobić to powiedz."));
		}
	}


	/**
	 * Player says no to different question.
	 */
	@Test
	public void testQuestion1ToAttendingNoToAny() {
		for (final String playerSays : Arrays.asList("no")) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "");
			npcEngine.setCurrentState(QUESTION_1);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Rozumiem, że zielone elfy dobrze się chronią. Jeżeli mógłbym coś innego dla Ciebie zrobić to powiedz."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(IDLE));

		}
	}

	/**
	 * Tests for idleToAttendingQuestCompleted.
	 */
	@Test
	public void testIdleToAttendingQuestCompleted() {
		for (final String playerSays : ConversationPhrases.GREETING_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "done");
			npcEngine.setCurrentState(IDLE);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Pozdrawiam cię stary przyjacielu."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));

		}
	}

    /**
     * Player with finished quest says offer.
     */
	@Test
	public void testAttendingtoAttendingOffer() {
		for (final String playerSays : ConversationPhrases.OFFER_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "done");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Jeżeli znajdziesz więcej elfiszów to będę wdzięczny gdybyś mógł mi je #sprzedać. Kupię elficką: zbroję, spodnie, buty, płaszcz, rękawice, miecz, hełm oraz kapelusz. Kupiłbym także miecz elfów ciemności jeżeli będziesz miał."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));
		}
	}

	/**
	 * player returns after finishing the quest and says quest.
	 * *This is no longer in quest as the general logic doesn't have it in
	 */
	@Ignore
    @Test
	public void testAttendingtoAttendingDoneQuestmessage() {
		for (final String playerSays : ConversationPhrases.QUEST_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "done");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Jestem teraz zajęty badaniem właściwości elfickich części zbroi, które mi przyniosłeś. To intrygujące. Dopóki nie będę ich produkował to będę je skupywał od Ciebie. Liczę na twoje zaangażowanie."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));

		}
	}

	/**
	 * player returns after finishing the quest and says quest.
	 * *This is no longer in quest as the general logic doesn't have it in
	 */
	@Ignore
    @Test
	public void testAttendingtoQuestion1NotDoneQuestmessage() {
		for (final String playerSays : ConversationPhrases.QUEST_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("As you already know, I seek elvish #equipment."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(QUESTION_1));
		}
	}

	/**
	 * Tests for attendingtoAttendingOfferquestnotdone.
	 */
	@Test
	public void testAttendingtoAttendingOfferquestnotdone() {
		for (final String playerSays : ConversationPhrases.OFFER_MESSAGES) {
			final Player player = PlayerTestHelper.createPlayer("bob");
			player.setQuest(QUEST_SLOT, "");
			npcEngine.setCurrentState(ATTENDING);

			npcEngine.step(player, playerSays);

			assertThat(playerSays, getReply(npc), is("Nie sądzę, abym mógł Tobie zaufać..."));
			assertThat(playerSays, npcEngine.getCurrentState(), is(ATTENDING));
		}
	}

}
