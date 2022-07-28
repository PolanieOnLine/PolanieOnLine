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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.nalwor.tower.PrincessNPC;
import games.stendhal.server.maps.semos.house.FlowerSellerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ElfPrincessTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;
	private SpeakerNPC npcRose = null;
	private Engine enRose = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		ZoneConfigurator zoneConf = new PrincessNPC();
		zoneConf.configureZone(new StendhalRPZone("admin_test"), null);
		npc = SingletonRepository.getNPCList().get("Tywysoga");
		en = npc.getEngine();

		final StendhalRPZone zone = new StendhalRPZone("int_semos_house");
		MockStendlRPWorld.get().addRPZone(zone);
		zoneConf = new FlowerSellerNPC();
		zoneConf.configureZone(zone, null);
		npcRose = SingletonRepository.getNPCList().get("Róża Kwiaciarka");
		enRose = npcRose.getEngine();

		final AbstractQuest quest = new ElfPrincess();
		quest.addToWorld();
		en = npc.getEngine();

		player = PlayerTestHelper.createPlayer("player");
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		en.step(player, "hi");
		assertEquals("Witaj, człowieku!", getReply(npc));
		en.step(player, "help");
		assertEquals("Stanowcza osoba mogłaby zrobić dla mnie #zadanie.", getReply(npc));
		en.step(player, "task");
		assertEquals("Znajdziesz wędrowną sprzedawczynię kwiatów Różę Kwiaciarkę i odbierzesz dla mnie orchideę, mój ulubiony kwiatek?", getReply(npc));
		en.step(player, "no");
		assertEquals("Och nieważne. Żegnaj.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj, człowieku!", getReply(npc));
		en.step(player, "task");
		assertEquals("Znajdziesz wędrowną sprzedawczynię kwiatów Różę Kwiaciarkę i odbierzesz dla mnie orchideę, mój ulubiony kwiatek?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Gdy będziesz miał to powiedz #kwiatek, a wtedy będę wiedziała, że go masz. W zamian dam Ci nagrodę.", getReply(npc));
		en.step(player, "flower");
		assertEquals("Widzę, że nie masz przy sobie orchidei. Róża Kwiaciarka przemierza całą wyspę i jestem pewna, że pewnego dnia ją spotkasz!", getReply(npc));
		en.step(player, "task");
		assertEquals("Słodkie są te kwiatki od Róży Kwiaciarki...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia nieznajomy.", getReply(npc));

		// -----------------------------------------------
		// Find Rose Leigh and get the flower from her

		String roseSays = getReply(npcRose);
		assertTrue("Kwiatki! Świeże kwiatki!".equals(roseSays) || (roseSays == null));
		enRose.step(player, "hi");
		assertEquals("Witaj skarbie. Mój daleki wzrok powiedział mi, że potrzebujesz kwiatek dla pięknej dziewczyny. Oto on i do zobaczenia.", getReply(npcRose));
		assertEquals(ConversationStates.IDLE, enRose.getCurrentState());


		// -----------------------------------------------
		// return to Tywysoga

		en.step(player, "hi");
		assertEquals("Witaj, człowieku!", getReply(npc));
		en.step(player, "task");
		assertEquals("Słodkie są te kwiatki od Róży Kwiaciarki...", getReply(npc));
		en.step(player, "flower");
		assertTrue(getReply(npc).startsWith("Dziękuję! Weź te "));
		assertTrue(player.isEquipped("sztabka złota"));
		// [00:09] superkym earns 5000 experience points.
		en.step(player, "bye");
		assertEquals("Do widzenia nieznajomy.", getReply(npc));

		// -----------------------------------------------
		// talk to Rose Leigh without having an active task to fetch flowser

		enRose.step(player, "hi");
		assertEquals("Dziś nie mam nic dla Ciebie, przykro mi. Wyruszam teraz w dalszą drogę. Do widzenia.", getReply(npcRose));

		// -----------------------------------------------
		// do the quest again

		en.step(player, "hi");
		assertEquals("Witaj, człowieku!", getReply(npc));
		en.step(player, "task");
		assertEquals("Ostatnią orchideę, którą mi przyniosłeś była taka piękna. Przyniesiesz mi następną od Róży Kwiaciarki?", getReply(npc));

		en.step(player, "yes");
		assertEquals("Dziękuję! Gdy będziesz miał to powiedz #kwiatek, a wtedy będę wiedziała, że go masz. W zamian dam Ci nagrodę.", getReply(npc));
		en.step(player, "flower");
		assertEquals("Widzę, że nie masz przy sobie orchidei. Róża Kwiaciarka przemierza całą wyspę i jestem pewna, że pewnego dnia ją spotkasz!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia nieznajomy.", getReply(npc));

		// -----------------------------------------------

		enRose.step(player, "hi");
		assertEquals("Dałam tobie kwiat pięć minut temu! Jej Królewska Wysokość może się nimi cieszyć przez jakiś czas.", getReply(npcRose));
		assertEquals(ConversationStates.IDLE, enRose.getCurrentState());

		// -----------------------------------------------

		// Allow get flower a bit later;
		player.setQuest("elf_princess", 1, Long.toString(System.currentTimeMillis() - 5 * 60 * 1000 - 10));

		enRose.step(player, "hi");
		assertEquals("Witaj skarbie. Mój daleki wzrok powiedział mi, że potrzebujesz kwiatek dla pięknej dziewczyny. Oto on i do zobaczenia.", getReply(npcRose));
		assertEquals(ConversationStates.IDLE, enRose.getCurrentState());

		en.step(player, "hi");
		assertEquals("Witaj, człowieku!", getReply(npc));
		en.step(player, "flower");
		assertTrue(getReply(npc).startsWith("Dziękuję! Weź te "));
		// [00:10] superkym earns 5000 experience points.
		en.step(player, "bye");
		assertEquals("Do widzenia nieznajomy.", getReply(npc));
	}
}
