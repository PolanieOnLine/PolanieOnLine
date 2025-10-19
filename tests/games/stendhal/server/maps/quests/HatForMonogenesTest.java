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
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.city.GreeterNPC;
import games.stendhal.server.maps.semos.tavern.TraderNPC;
import utilities.NPCTestHelper;
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
		NPCTestHelper.loadShops("Xin Blanca");

		quest = new MeetMonogenes();
		quest.addToWorld();
		quest = new BuiltQuest(new HatForMonogenes().story());
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
				"Czy mógłbyś przynieść mi #czapkę, żeby przykryć moją łysą głowę? Brrrrr! Dni tutaj w Semos robią się naprawdę zimne...",
				getReply(npc));
		en.step(player, "czapka");
		assertEquals(
				"Nie wiesz, co to jest czapka?! Cokolwiek lekkiego, co może przykryć moją głowę; na przykład skóra. No, zrobisz to?",
				getReply(npc));
		en.step(player, "no");
		assertEquals(
				"Z pewnością masz ważniejsze rzeczy do zrobienia, a mało czasu, by je zrobić. Chyba zostanę tu i zamarznę... *pociągnięcie nosem*",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj ponownie player. W czym mogę #pomóc tym razem?", getReply(npc));
		en.step(player, "task");
		assertEquals(
				"Czy mógłbyś przynieść mi #czapkę, żeby przykryć moją łysą głowę? Brrrrr! Dni tutaj w Semos robią się naprawdę zimne...",
				getReply(npc));
		en.step(player, "czapka");
		assertEquals(
				"Nie wiesz, co to jest czapka?! Cokolwiek lekkiego, co może przykryć moją głowę; na przykład skóra. No, zrobisz to?",
				getReply(npc));
		en.step(player, "yes");
		assertEquals("Dzięki, mój dobry przyjacielu. Będę tu czekać na twój powrót!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		final Item item = ItemTestHelper.createItem("money", 25);
		player.getSlot("bag").add(item);
		enXin.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npcXin));
		enXin.step(player, "buy skórzany hełm");
		assertEquals("Skórzany hełm kosztuje 25 monet. Chcesz to kupić?", getReply(npcXin));
		enXin.step(player, "yes");
		assertEquals("Gratulacje! Oto twój skórzany hełm!", getReply(npcXin));
		enXin.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npcXin));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Hej! Czy ta skórzana czapka jest dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("Chyba ktoś bardziej szczęśliwy dziś dostanie swoją czapkę... *kichnięcie*", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Hej! Czy ta skórzana czapka jest dla mnie?", getReply(npc));
		npc.remove("text");
		player.drop("skórzany hełm");
		long oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP, player.getXP());
		assertEquals(null, getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals(
				"Witaj ponownie player. W czym mogę #pomóc tym razem?",
				getReply(npc));
		en.step(player, "quest");
		assertEquals(
				"Hej, mój dobry przyjacielu, pamiętasz tę skórzaną czapkę, o której ci mówiłem wcześniej? Wciąż tu dosyć zimno...",
				getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		player.equip("bag", SingletonRepository.getEntityManager().getItem("skórzany hełm"));
		en.step(player, "hi");
		assertEquals("Hej! Czy ta skórzana czapka jest dla mnie?", getReply(npc));
		oldXP = player.getXP();
		en.step(player, "yes");
		assertEquals(oldXP + 50, player.getXP());

		assertEquals("Błogosławię cię, mój dobry przyjacielu! Teraz moja głowa będzie ładnie ciepła.", getReply(npc));
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
		history.add("Spotkałem Monogenesa przy źródle w wiosce Semos.");
		history.add("Muszę znaleźć mu czapkę, coś skórzanego, żeby ogrzać mu głowę.");
		assertEquals(history, quest.getHistory(player));

		player.setQuest("hat_monogenes", "start");
		player.equip("bag", ItemTestHelper.createItem("skórzany hełm"));
		history.add("Znalazłem czapkę.");

		assertEquals(history, quest.getHistory(player));
		player.setQuest("hat_monogenes", "done");
		history.add("Oddałem Monogenesowi czapkę, żeby ogrzała mu łysą głowę.");

		assertEquals(history, quest.getHistory(player));

	}
}