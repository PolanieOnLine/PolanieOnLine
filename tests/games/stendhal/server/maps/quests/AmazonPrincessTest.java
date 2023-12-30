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
		assertEquals("Ostatni napój, który mi kupiłeś był wspaniały. Przyniesiesz mi następny?", getReply(npc));
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
		assertEquals("Napiłabym się drinka, powinien być egzotyczny. Czy możesz mi go przynieść?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Jeżeli go znajdziesz to powiedz #napój a będę wiedziała, że go masz. W zamian dam Ci nagrodę.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Kocham te egzotyczne napoje ale zapomniałam nazwę mojego ulubionego.", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "napój z oliwką");
		assertEquals("Nie masz napoju z oliwką. Idź i lepiej dostarcz mi go!", getReply(npc));
		en.step(player, "exotic drink");
		en.step(player, "napójzoliwką");
		assertEquals("Nie masz napoju z oliwką. Idź i lepiej dostarcz mi go!", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "favor");
		assertEquals("Kocham te egzotyczne napoje ale zapomniałam nazwę mojego ulubionego.", getReply(npc));
		en.step(player, "napój z oliwką");
		assertEquals("Nie masz napoju z oliwką. Idź i lepiej dostarcz mi go!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "help");
		assertEquals("Strzeż się moich sióstr na wyspie. One nie lubią obcych.", getReply(npc));
		en.step(player, "quest");
		assertEquals("Kocham te egzotyczne napoje ale zapomniałam nazwę mojego ulubionego.", getReply(npc));
		en.step(player, "done");
		en.step(player, "drink");
		assertEquals("Nie masz napoju z oliwką. Idź i lepiej dostarcz mi go!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------
		final Item item = ItemTestHelper.createItem("napój z oliwką");
		player.getSlot("bag").add(item);

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "napój z oliwką");
		assertTrue(getReply(npc).startsWith("Dziękuję!! Proszę, weź "));
		assertTrue(player.isEquipped("tarta z rybnym nadzieniem"));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertTrue(getReply(npc).startsWith("Jestem pełna, aby wypić następny napój przez co najmniej "));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "napój z oliwką");
		assertEquals("Czasami mógłbyś mi wyświadczyć #przysługę...", getReply(npc));
		en.step(player, "favour");
		assertTrue(getReply(npc).startsWith("Jestem pełna, aby wypić następny napój przez co najmniej "));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));
	}
}
