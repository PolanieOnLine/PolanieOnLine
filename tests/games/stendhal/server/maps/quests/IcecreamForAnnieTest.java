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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
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
import games.stendhal.server.maps.kalavan.citygardens.IceCreamSellerNPC;
import games.stendhal.server.maps.kalavan.citygardens.LittleGirlNPC;
import games.stendhal.server.maps.kalavan.citygardens.MummyNPC;
import utilities.NPCTestHelper;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class IcecreamForAnnieTest {


	private static String questSlot = "icecream_for_annie";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new IceCreamSellerNPC().configureZone(zone, null);
		new LittleGirlNPC().configureZone(zone, null);
		new MummyNPC().configureZone(zone, null);

		final AbstractQuest quest = new IcecreamForAnnie();
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
		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć, nazywam się Annie i mam pięć lat.", getReply(npc));
		en.step(player, "help");
		assertEquals("Zapytaj mamusi.", getReply(npc));
		en.step(player, "job");
		assertEquals("Pomagam mojej mamusi.", getReply(npc));
		en.step(player, "offer");
		assertEquals("Jestem małą dziewczynką. Nie mam nic do zaoferowania.", getReply(npc));
		en.step(player, "task");
		assertEquals("Jestem głodna! Chciałabym porcję lodów, proszę. Waniliowe z polewą czekoladową. Zdobędziesz takie dla mnie?", getReply(npc));
		en.step(player, "ok");
		assertEquals("Dziękuję!", getReply(npc));
		assertThat(player.getQuest(questSlot), is("start"));
		en.step(player, "bye");
		assertEquals("Ta ta.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Cześć! Jestem głodna...", getReply(npc));
		en.step(player, "task");
		assertEquals("Łeeeeeeeee! Gdzie jest moja porcja lodów...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Ta ta.", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Sam");
		en = npc.getEngine();

		// configure ice cream seller
		NPCTestHelper.loadShops("Sam");

		Item item = ItemTestHelper.createItem("money", 30);
		player.getSlot("bag").add(item);

		en.step(player, "hi");
		assertEquals("Cześć. Czy mogę #zaoferować Tobie porcję lodów?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Lody kosztuje 30 monet. Chcesz to kupić?", getReply(npc));
		en.step(player, "no");

		en.step(player, "offer");
		assertEquals("Sprzedaję lody.", getReply(npc));
		assertTrue(en.step(player, "buy 0 lody"));
		assertEquals("Ile lodów chcesz kupić?!", getReply(npc));
		en.step(player, "buy lody");
		assertEquals("Lody kosztuje 30 monet. Chcesz to kupić?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Gratulacje! Oto twój lody!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia. Ciesz się dniem!", getReply(npc));
		assertTrue(player.isEquipped("lody"));

		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Mamusia powiedziała, że nie powinnam z tobą rozmawiać. Jesteś nieznajomym.", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Mrs. Jones");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Cześć, widzę, że spotkałeś moją córkę Annie. Mam nadzieję, że nie była zbyt wymagająca. Wyglądasz na miłą osobę.", getReply(npc));
		assertThat(player.getQuest(questSlot), is("mummy"));
		en.step(player, "task");
		assertEquals("Nic, dziękuję.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Annie Jones");
		en = npc.getEngine();

		final long xp = player.getXP();
		final double karma = player.getKarma();
		en.step(player, "hi");
		assertEquals("Pychotka! Czy ta porcja lodów jest dla mnie?", getReply(npc));
		en.step(player, "yes");
		// [15:06] kymara earns 500 experience points.
		assertFalse(player.isEquipped("lody"));
		assertTrue(player.isEquipped("prezent"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));
		assertTrue(player.getQuest(questSlot).startsWith("eating"));
		assertEquals("Dziękuję bardzo! Jesteś bardzo miły. Weź ten prezent.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Ta ta.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Cześć.", getReply(npc));
		en.step(player, "task");
		assertEquals("Zjadłam za dużo lodów. Niedobrze mi.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Ta ta.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------
		final double newKarma = player.getKarma();
		// [15:07] Changed the state of quest 'icecream_for_annie' from 'eating;1219676807283' to 'eating;0'
		player.setQuest(questSlot, "eating;0");
		en.step(player, "hi");
		assertEquals("Cześć.", getReply(npc));
		en.step(player, "task");
		assertEquals("Mam nadzieję, że następna porcja lodów nie będzie obżarstwem. Czy możesz zdobyć je dla mnie?", getReply(npc));
		en.step(player, "no");
		assertThat(player.getQuest(questSlot), is("rejected"));
		assertThat(player.getKarma(), lessThan(newKarma));
		assertEquals("Dobrze. Zapytam moją mamusię.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Cześć.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Ta ta.", getReply(npc));
	}
}
