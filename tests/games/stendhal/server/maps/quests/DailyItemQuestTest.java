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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.townhall.MayorNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class DailyItemQuestTest {


	private static String questSlot = "daily_item";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new MayorNPC().configureZone(zone, null);

		final AbstractQuest quest = new DailyItemQuest();
		quest.addToWorld();

	}
	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("player");
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {

		npc = SingletonRepository.getNPCList().get("Mayor Chalmers");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Pozdrawiam Cię w imieniu mieszkańców Ados.", getReply(npc));
		en.step(player, "task");
		assertTrue(getReply(npc).startsWith("Ados potrzebuje zapasów. Zdobądź "));
		en.step(player, "complete");
		assertTrue(getReply(npc).startsWith("Jeszcze nie przyniosłeś "));
		en.step(player, "bye");
		assertEquals("Życzę miłego dnia.", getReply(npc));

		player.setQuest(questSlot, "napój z oliwką;100");
		Item item = ItemTestHelper.createItem("napój z oliwką");
		player.getSlot("bag").add(item);
		final int xp = player.getXP();

		en.step(player, "hi");
		assertEquals("Pozdrawiam Cię w imieniu mieszkańców Ados.", getReply(npc));
		en.step(player, "complete");
		assertFalse(player.isEquipped("napój z oliwką"));
		assertThat(player.getXP(), greaterThan(xp));
		assertTrue(player.isQuestCompleted(questSlot));
		// [10:50] kymara earns 455960 experience points.
		assertEquals("Dobra robota! Pozwól sobie podziękować w imieniu obywateli Ados!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Życzę miłego dnia.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Pozdrawiam Cię w imieniu mieszkańców Ados.", getReply(npc));
		en.step(player, "task");
		assertThat(getReply(npc),
				is(oneOf("Możesz dostać tylko jedno zadanie dziennie. Proszę wróć za 24 godziny.",
						"Możesz dostać tylko jedno zadanie dziennie. Proszę wróć za 1 dzień.")));
		en.step(player, "bye");
		assertEquals("Życzę miłego dnia.", getReply(npc));

		// -----------------------------------------------
		player.setQuest(questSlot, "done;0");
		// [10:51] Changed the state of quest 'daily_item' from 'done;1219834233092;1' to 'done;0'
		en.step(player, "hi");
		assertEquals("Pozdrawiam Cię w imieniu mieszkańców Ados.", getReply(npc));
		en.step(player, "task");
		assertTrue(getReply(npc).startsWith("Ados potrzebuje zapasów. Zdobądź "));
		en.step(player, "bye");
		assertEquals("Życzę miłego dnia.", getReply(npc));

		// -----------------------------------------------

		// [10:53] Changed the state of quest 'daily_item' from 'dwarf cloak;1219834342834;0' to 'dwarf cloak;0'
		player.setQuest(questSlot, "płaszcz krasnoludzki;0");
		en.step(player, "hi");
		assertEquals("Pozdrawiam Cię w imieniu mieszkańców Ados.", getReply(npc));
		en.step(player, "task");
		assertEquals("Jesteś na etapie poszukiwania płaszcz krasnoludzki, powiedz #załatwione, jak przyniesiesz. Być może nie ma tych przedmiotów! Możesz przynieść #inny przedmiot jeżeli chcesz lub wróć z tym, o który cię na początku prosiłem.", getReply(npc));
		en.step(player, "another");
		assertTrue(getReply(npc).startsWith("Ados potrzebuje zapasów. Zdobądź "));
		en.step(player, "bye");
		assertEquals("Życzę miłego dnia.", getReply(npc));

	}
}
