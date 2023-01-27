/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.city.GreeterNPC;
import games.stendhal.server.maps.semos.tavern.TraderNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class HatForMonogenesTest {
	private SpeakerNPC npc;
	private Engine en;
	private SpeakerNPC npcXin;
	private Engine enXin;
	private AbstractQuest quest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		assertTrue(SingletonRepository.getNPCList().getNPCs().isEmpty());
	}

	@After
	public void tearDown() throws Exception {
		SingletonRepository.getNPCList().clear();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new GreeterNPC().configureZone(zone, null);
		npc = SingletonRepository.getNPCList().get("Monogenes");
		en = npc.getEngine();

		final ZoneConfigurator zoneConf = new TraderNPC();
		zoneConf.configureZone(new StendhalRPZone("int_semos_tavern"), null);
		npcXin = SingletonRepository.getNPCList().get("Xin Blanca");
		enXin = npcXin.getEngine();

		// configure Xin Blanca's shop
		SingletonRepository.getShopList().configureNPC("Xin Blanca", "sellstuff", true, false);

		quest = new MeetMonogenes();
		quest.addToWorld();
		quest = new HatForMonogenes();
		quest.addToWorld();

	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		final Player player = PlayerTestHelper.createPlayer("player");
		en.step(player, "hi");
		assertEquals(
				"Witaj nieznajomy! Nie bądź zbyt onieśmielony, gdy ludzie siedzą cicho lub są zajęci... strach przed Blordroughtem i jego wojskami padł na cały kraj. Jesteśmy trochę zaniepokojeni. Mogę dać Tobie trochę rad odnośnie zawierania przyjaźni. Chciałbyś je usłyszeć?",
				getReply(npc));
		en.step(player, "no");
		assertEquals(
				"I jak chcesz wiedzieć co się dzieje? Czytając Semos Tribune? Hah! Do widzenia.",
				getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj ponownie player. W czym mogę #pomóc tym razem?", getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czy mógłbyś przynieść mi #kapelusz do zakrycia mojej łysinki? Brrrrr! Dni w Semos robią się coraz chłodniejsze...",
				getReply(npc));
		en.step(player, "hat");
		assertEquals(
				"Nie wiesz co to jest kapelusz?! Wszystko co może zakryć moją świecącą głowę jak na przykład skóra. Zrobisz to dla mnie?",
				getReply(npc));
		en.step(player, "no");
		assertEquals(
				"Jestem pewien, że masz lepsze rzeczy do zrobienia. Będę stał tutaj i zamarzał na śmierć... *sniff*",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj ponownie player. W czym mogę #pomóc tym razem?", getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czy mógłbyś przynieść mi #kapelusz do zakrycia mojej łysinki? Brrrrr! Dni w Semos robią się coraz chłodniejsze...",
				getReply(npc));
		en.step(player, "hat");
		assertEquals(
				"Nie wiesz co to jest kapelusz?! Wszystko co może zakryć moją świecącą głowę jak na przykład skóra. Zrobisz to dla mnie?",
				getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję przyjacielu. Będę tutaj czekał na twój powrót!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		final Item item = ItemTestHelper.createItem("money", 25);
		player.getSlot("bag").add(item);
		enXin.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npcXin));
		enXin.step(player, "buy skórzany hełm");
		assertEquals("skórzany hełm kosztuje 25. Chcesz kupić to?", getReply(npcXin));
		enXin.step(player, "yes");
		assertEquals("Gratulacje! Oto twój skórzany hełm!", getReply(npcXin));
		enXin.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npcXin));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Hej! Czy ten skórzany hełm jest dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("Ktoś miał dzisiaj dużo szczęścia... *Apsik*.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Hej! Czy ten skórzany hełm jest dla mnie?", getReply(npc));
		npc.remove("text");
		player.drop("skórzany hełm");
		int oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP, player.getXP());
		assertEquals(null, getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals(
				"Hej, mój dobry przyjacielu, pamiętasz ten skórzany kapelusz, o który cię wcześniej pytałem? Nadal jest tu dość chłodno...",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		player.equip("bag", SingletonRepository.getEntityManager().getItem("skórzany hełm"));
		en.step(player, "hi");
		assertEquals("Hej! Czy ten skórzany hełm jest dla mnie?", getReply(npc));
		oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP + 300, player.getXP());

		assertEquals("Niech Cię pobłogosławię mój dobry przyjacielu! Teraz mojej głowie będzie wygodnie i ciepło.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		// (sorry i meant to put it on ground to test if he noticed it went
		// missing, i did, but i forgot i had one on my head too, he took that.)
	}

	/**
	 * Tests for getHistory.
	 */
	@Test
	public void testGetHistory() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		final List<String> history = new ArrayList<String>();
		assertEquals(history, quest.getHistory(player));

		player.setQuest("hat_monogenes", "");
		history.add("Spotkałem Monogenes na wiosnę w wiosce Semos");
		history.add("Muszę znaleźć jakiś skórzany kapelusz, który trzymałby ciepło.");
		assertEquals(history, quest.getHistory(player));

		player.setQuest("hat_monogenes", "start");
		player.equip("bag", ItemTestHelper.createItem("skórzany hełm"));
		history.add("Zdobyłem kapelusz.");

		assertEquals(history, quest.getHistory(player));
		player.setQuest("hat_monogenes", "done");
		history.add("Dałem kapelusz Monogenesowi i nagrodził mnie swym doświadczeniem.");

		assertEquals(history, quest.getHistory(player));
	}
}
