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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.amazon.hut.PrincessNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class AmazonPrincessTest {
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;
	private AbstractQuest quest = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final ZoneConfigurator zoneConf = new PrincessNPC();
		zoneConf.configureZone(new StendhalRPZone("admin_test"), null);
		npc = SingletonRepository.getNPCList().get("Księżniczka Esclara");
		en = npc.getEngine();

		quest = new BuiltQuest(new AmazonPrincess().story());
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("player");
	}
	@After
	public void tearDown() {
		SingletonRepository.getNPCList().clear();
	}

	/**
	 * Tests for getSlotname.
	 */
	@Test
	public void testGetSlotname() {
		assertEquals("amazon_princess", quest.getSlotName());
	}

	/**
	 * Tests for hasRecovered.
	 */
	@Test
	public void testhasRecovered() {
		en.setCurrentState(ConversationStates.ATTENDING);
		player.setQuest(quest.getSlotName(), "done;0");
		en.step(player, "task");
		assertEquals("Ostatni koktajl, który mi przyniosłeś, był taki cudowny. Przyniesiesz mi jeszcze jeden?", getReply(npc));
		assertEquals(ConversationStates.QUEST_OFFERED, en.getCurrentState());
		en.step(player, "yes");
		assertEquals("start", player.getQuest(quest.getSlotName(), 0));
	}


	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "task");
		assertEquals("Szukam napoju, powinien być egzotyczny. Możesz mi przynieść?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Jeśli będziesz w posiadaniu takiego napoju, na pewno dam ci miłą nagrodę.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Lubię te egzotyczne napoje, tylko zapomniałam nazwy mojego ulubionego.", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "favor");
		assertEquals("Lubię te egzotyczne napoje, tylko zapomniałam nazwy mojego ulubionego.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "quest");
		assertEquals("Lubię te egzotyczne napoje, tylko zapomniałam nazwy mojego ulubionego.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------
		final Item item = ItemTestHelper.createItem("napój z oliwką");
		player.getSlot("bag").add(item);

		en.step(player, "hi");
		assertEquals("Ach, rozumiem, masz §'napój z oliwką'. Czy to dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertTrue(getReply(npc).startsWith("Dziękuję! Przyjmij proszę "));
		assertTrue(player.isEquipped("tarta z rybnym nadzieniem"));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertTrue(getReply(npc).startsWith("Jestem pewna, że nie będę teraz w stanie wypić kolejnego takiego napoju."));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));
	}
}
