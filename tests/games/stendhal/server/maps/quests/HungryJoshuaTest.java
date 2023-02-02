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
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.goldsmith.GoldsmithNPC;
import games.stendhal.server.maps.semos.blacksmith.BlacksmithNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class HungryJoshuaTest {


	private static String questSlot = "hungry_joshua";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new GoldsmithNPC().configureZone(zone, null);
		new BlacksmithNPC().configureZone(zone, null);

		final AbstractQuest quest = new HungryJoshua();
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

		npc = SingletonRepository.getNPCList().get("Xoderos");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Witaj! Przykro mi to mówić, ale z powodu trwającej wojny nie wolno mi sprzedawać broni nikomu spoza grona oficjalnych wojskowych. Mogę odlać dla Ciebie żelazo, a może interesuję Cię moja #oferta specjalna? Powiedz tylko #odlej.", getReply(npc));
		en.step(player, "task");
		assertEquals("Martwię się o mojego brata, który mieszka w Ados. Potrzebuję kogoś kto przekaże mu #jedzenie.", getReply(npc));
		en.step(player, "food");
		assertEquals("Sądzę, że pięć kanapek mu wystarczy. Mój brat nazywa się #Joshua. Czy możesz pomóc?", getReply(npc));
		en.step(player, "joshua");
		assertEquals("Jest złotnikiem w Ados. Nie mają tam zapasów jedzenia. Pomożesz mu?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję. Powiedz mu #jedzenie lub #kanapka to będzie wiedział, że nie jesteś tylko klientem.", getReply(npc));
		assertThat(player.getQuest(questSlot), is("start"));
		en.step(player, "food");
		assertEquals("#Joshua będzie coraz głodniejszy! Proszę pospiesz się!", getReply(npc));
		en.step(player, "joshua");
		assertEquals("Mój brat jest złotnikiem w Ados.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj! Przykro mi to mówić, ale z powodu trwającej wojny nie wolno mi sprzedawać broni nikomu spoza grona oficjalnych wojskowych. Mogę odlać dla Ciebie żelazo, a może interesuję Cię moja #oferta specjalna? Powiedz tylko #odlej.", getReply(npc));
		en.step(player, "task");
		assertEquals("Proszę nie zapomnij o pięciu #kanapkach dla #Joshua!", getReply(npc));
		en.step(player, "sandwiches");
		assertEquals("#Joshua będzie coraz głodniejszy! Proszę pospiesz się!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------


		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Joshua");
		en = npc.getEngine();

		String greeting = "Cześć! Jestem tutejszym złotnikiem. Jeżeli będziesz chciał, abym odlał dla Ciebie #sztabkę #złota to daj znać! Wystarczy, że powiesz #odlej.";

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("ser", 1);
		final ProducerBehaviour behaviour = new ProducerBehaviour("joshua_cast_gold", Arrays.asList("cast"),
				"sztabka złota", requiredResources, 0);
		SingletonRepository.getProducerRegister().configureNPC(npc.getName(), behaviour, greeting);

		Item item = ItemTestHelper.createItem("kanapka", 5);
		player.getSlot("bag").add(item);
		final int xp = player.getXP();

		en.step(player, "hi");
		assertEquals(greeting, getReply(npc));
		en.step(player, "food");
		assertEquals("Och wspaniale! Czy mój brat Xoderos przysłał Ciebie z tymi kanapkami?", getReply(npc));
		en.step(player, "yes");
		// [07:28] kymara earns 150 experience points.
		assertEquals("Dziękuję! Proszę daj znać Xoderosowi, że ze mną jest wszystko w porządku. Powiedz moje imię Joshua, a będzie wiedział, że ja Ciebie przysłałem. Prawdopodobnie da ci coś w zamian.", getReply(npc));
		assertFalse(player.isEquipped("kanapka"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getQuest(questSlot), is("joshua"));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Xoderos");
		en = npc.getEngine();
		final int xp2 = player.getXP();

		en.step(player, "hi");
		assertEquals("Witaj! Przykro mi to mówić, ale z powodu trwającej wojny nie wolno mi sprzedawać broni nikomu spoza grona oficjalnych wojskowych. Mogę odlać dla Ciebie żelazo, a może interesuję Cię moja #oferta specjalna? Powiedz tylko #odlej.", getReply(npc));
		en.step(player, "task");
		assertEquals("Mam nadzieję, że #Joshua ma się dobrze...", getReply(npc));
		en.step(player, "food");
		assertEquals("Chciałbym, abyś mi przekazał, że #Joshua ma się dobrze...", getReply(npc));
		en.step(player, "joshua");
		// [07:29] kymara earns 50 experience points.
		assertEquals("Jestem wdzięczny, że Joshua ma się dobrze. Co mogę dla Ciebie zrobić? Wiem. Naprawię to uszkodzone kółko od kluczy, które nosisz ... proszę, powinno już działać!", getReply(npc));
		assertThat(player.getXP(), greaterThan(xp2));
		assertTrue(player.isQuestCompleted(questSlot));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
