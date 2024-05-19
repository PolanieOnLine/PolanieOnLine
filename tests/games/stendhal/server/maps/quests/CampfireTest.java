/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.orril.river.CampingGirlNPC;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject.ID;
import utilities.PlayerTestHelper;

public class CampfireTest {

	private static final String ZONE_NAME = "testzone";

	private static final String CAMPFIRE = "campfire";

	private Player player;

	private SpeakerNPC npc;

	private StendhalRPZone zone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendhalRPRuleProcessor.get();
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
		zone = new StendhalRPZone("zone");
		new CampingGirlNPC().configureZone(zone, null);
		npc = NPCList.get().get("Sally");
		final AbstractQuest quest = new BuiltQuest(new Campfire().story());
		quest.addToWorld();
	}

	@After
	public void tearDown()  {
		player = null;
		NPCList.get().clear();
	}

	/**
	 * Tests for canStartQuestNow.
	 */
	@Test
	public void testCanStartQuestNow() {

		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Cześć, jak się masz?", getReply(npc));
		assertTrue(en.step(player, "bye"));

		player.setQuest(CampfireTest.CAMPFIRE, 0, "start");
		assertTrue(en.step(player, "hi"));
		assertEquals(
				"Cześć, jak się masz?",
				getReply(npc));
		assertTrue(en.step(player, "bye"));

		player.setQuest(CampfireTest.CAMPFIRE, 1, String.valueOf(System.currentTimeMillis()));
		en.step(player, "hi");
		assertEquals(
				"Cześć, jak się masz?",
				getReply(npc));
		assertTrue(en.step(player, "bye"));

		final long SIXMINUTESAGO = System.currentTimeMillis() - 6 * TimeUtil.MILLISECONDS_IN_MINUTE;
		player.setQuest(CampfireTest.CAMPFIRE, 1, String.valueOf(SIXMINUTESAGO));
		en.step(player, "hi");
		assertEquals("delay is 5 minutes, so 6 minutes should be enough", "Cześć, jak się masz?", getReply(npc));
		assertTrue(en.step(player, "bye"));
	}

	/**
	 * Tests for doQuest.
	 */
	@Test
	public void testDoQuest() {

		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Cześć, jak się masz?", getReply(npc));
		assertTrue(en.step(player, "favor"));

		assertEquals(
				"Potrzebuję więcej drewna, aby podtrzymać ogień w moim ognisku, ale nie mogę go zostawić bez opieki, żeby iść po nie! Czy mógłbyś przynieść trochę z lasu? Potrzebuję dziesięć kawałków.",
				getReply(npc));
		assertTrue(en.step(player, "yes"));
		assertEquals(
				"Dobra. Drewno znajdziesz na północ od tego miejsca. Wróć, gdy zdobędziesz dziesięć kawałków drewna!",
				getReply(npc));
		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
		final StackableItem wood = new StackableItem("polano", "", "", null);
		wood.setQuantity(10);
		wood.setID(new ID(2, ZONE_NAME));
		player.getSlot("bag").add(wood);
		assertEquals(10, player.getNumberOfEquipped("polano"));
		assertTrue(en.step(player, "hi"));
		assertEquals(
				"Hej ponownie! Masz drewno, widzę; czy masz te 10 kawałków drewna, o które wcześniej pytałam?",
				getReply(npc));
		assertTrue(en.step(player, "yes"));
		assertEquals(0, player.getNumberOfEquipped("polano"));
		String reply = getReply(npc);
		assertTrue(reply.contains("Dziękuję! Weź proszę "));
		assertTrue((10 == player.getNumberOfEquipped("mięso"))
				|| (10 == player.getNumberOfEquipped("szynka")));
		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Do widzenia.", getReply(npc));

	}

	/**
	 * Tests for jobAndOffer.
	 */
	@Test
	public void testJobAndOffer() {
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertTrue(npc.isTalking());
		assertEquals("Cześć, jak się masz?", getReply(npc));
		assertTrue(en.step(player, "job"));
		assertEquals("Praca? Jestem tylko małą dziewczynką! Skautem.",
				getReply(npc));
		assertFalse("no matching state transition", en.step(player, "offers"));
		assertEquals(null, getReply(npc));
		assertTrue(en.step(player, "help"));
		assertEquals(
				"Możesz znaleźć sporo użytecznych rzeczy w lesie na przykład drewno i grzyby. Uważaj, niektóre grzyby są trujące!",
				getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertFalse(npc.isTalking());
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for canNotRepeatYet.
	 */
	@Test
	public void testCanNotRepeatYet() {
		final String questState = Long.toString(new Date().getTime());

		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, 0, "done");
			player.setQuest(CAMPFIRE, 1, questState);

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			String reply = getReply(npc);
			assertTrue("Dzięki, ale myślę, że drewno, które przyniosłeś, wystarczy na 60 minut.".equals(reply) || "Dzięki, ale myślę, że drewno, które przyniosłeś, wystarczy na 1 godzinę.".equals(reply));
			assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
			assertEquals("quest state unchanged", questState, player.getQuest(CAMPFIRE, 1));
		}
	}

	/**
	 * Tests for repeatQuest.
	 */
	@Test
	public void testRepeatQuest() {
		final String questState = Long.toString(new Date().getTime() - 61 * 60 * 1000);
		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, 0, "done");
			player.setQuest(CAMPFIRE, 1, questState);

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			assertEquals("Moje ognisko znowu potrzebuje drewna, dziesięć kawałków #drewna będzie wystarczające. Czy mógłbyś przynieść te kawałki #drewna z lasu dla mnie? Proszę powiedz tak!", getReply(npc));
			assertEquals(ConversationStates.QUEST_OFFERED, en.getCurrentState());
			assertEquals("quest state unchanged", questState, player.getQuest(CAMPFIRE, 1));
		}
	}

	/**
	 * Tests for allowRestartAfterRejecting.
	 */
	@Test
	public void testAllowRestartAfterRejecting() {
		for (String request : ConversationPhrases.QUEST_MESSAGES) {
			final Engine en = npc.getEngine();
			player.setQuest(CAMPFIRE, 0, "rejected");

			en.setCurrentState(ConversationStates.ATTENDING);
			en.step(player, request);
			assertEquals("Potrzebuję więcej drewna, aby podtrzymać ogień w moim ognisku, ale nie mogę go zostawić bez opieki, żeby iść po nie! Czy mógłbyś przynieść trochę z lasu? Potrzebuję dziesięć kawałków.", getReply(npc));
			assertEquals(ConversationStates.QUEST_OFFERED, en.getCurrentState());
			assertEquals("quest state unchanged", "rejected", player.getQuest(CAMPFIRE, 0));
		}
	}

	/**
	 * Tests for refuseQuest.
	 */
	@Test
	public void testRefuseQuest() {
		final Engine en = npc.getEngine();
		final double karma = player.getKarma();

		en.setCurrentState(ConversationStates.QUEST_OFFERED);
		en.step(player, "no");

		assertEquals("Ojej, jak mam ugotować całe to mięso? Chyba będę musiała to po prostu podać zwierzętom..."
, getReply(npc));
		assertEquals(ConversationStates.ATTENDING, en.getCurrentState());
		assertEquals("quest state 'rejected'", "rejected", player.getQuest(CAMPFIRE, 0 ));
		assertEquals("karma penalty", karma - 5.0, player.getKarma(), 0.01);
	}
}