/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Tests for the SevenCherubs quests
 *
 * @author hendrik
 */
public class SevenCherubsTest extends ZonePlayerAndNPCTestImpl {

	private SpeakerNPC npc;
	private Engine en;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone("0_semos_village_w");
		setupZone("0_nalwor_city");
		setupZone("0_orril_river_s");
		setupZone("0_orril_river_s_w2");
		setupZone("0_orril_mountain_w2");
		setupZone("0_semos_mountain_n2_w2");
		setupZone("0_ados_rock");
	}

	/**
	 * creates a test for the seven cherubs quest
	 */
	public SevenCherubsTest() {
		super("0_semos_village_w", "Cherubiel", "Gabriel", "Ophaniel", "Raphael", "Uriel", "Zophiel", "Azazel");
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		quest = new SevenCherubs();
		quest.addToWorld();
	}

	/**
	 * tests the quest
	 */
	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Cherubiel");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 6 aniołków. Żegnaj!", getReply(npc));
		assertEquals(player.getXP(), 20L);
		// [14:53] bluelads heals 1 health point.

		npc = SingletonRepository.getNPCList().get("Gabriel");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 5 aniołków. Żegnaj!", getReply(npc));
		assertEquals(player.getXP(), 620L);
		// [14:53] bluelads earns 400 experience points.


		npc = SingletonRepository.getNPCList().get("Ophaniel");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 4 aniołków. Żegnaj!", getReply(npc));
		// [14:54] bluelads earns 800 experience points.
		assertEquals(player.getXP(), 1420L);
		en.step(player, "hi");
		assertEquals("Poszukaj innych aniołków, aby dostać nagrodę!", getReply(npc));
		assertEquals(player.getXP(), 1420L);


		npc = SingletonRepository.getNPCList().get("Raphael");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 3 aniołków. Żegnaj!", getReply(npc));
		// [14:54] bluelads earns 1000 experience points.
		assertEquals(player.getXP(), 2420L);


		npc = SingletonRepository.getNPCList().get("Uriel");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 2 aniołków. Żegnaj!", getReply(npc));
		// [14:54] bluelads earns 1200 experience points.
		assertEquals(player.getXP(), 3620L);
		en.step(player, "hi");
		assertEquals("Poszukaj innych aniołków, aby dostać nagrodę!", getReply(npc));
		assertEquals(player.getXP(), 3620L);


		npc = SingletonRepository.getNPCList().get("Zophiel");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Bardzo dobrze! Potrzebujesz znaleźć jeszcze 1 aniołków. Żegnaj!", getReply(npc));
		// [14:54] bluelads earns 1400 experience points.
		assertEquals(player.getXP(), 5020L);
		en.step(player, "hi");
		assertEquals("Poszukaj innych aniołków, aby dostać nagrodę!", getReply(npc));
		assertEquals(player.getXP(), 5020L);


		npc = SingletonRepository.getNPCList().get("Azazel");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Udowodniłeś, że jesteś w stanie nosić ten potężny artefakt!", getReply(npc));
		// [14:55] bluelads earns 2000 experience points.
		assertEquals(player.getXP(), 25020L);

		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Szukałeś i znalazłeś wszystkie aniołki! W nagrodę dostałeś potężny artefakt.", getReply(npc));
		// [14:55] You see a pair of golden boots. They will be heavy to wear but well worth their weight. It is a special quest reward for bluelads, and cannot be used by others. Stats are (DEF: 8).
		assertEquals(player.getXP(), 25020L);
	}
}
