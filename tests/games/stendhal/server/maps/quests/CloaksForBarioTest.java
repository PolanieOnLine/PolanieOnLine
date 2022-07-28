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
import static utilities.SpeakerNPCTestHelper.getReply;

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
import games.stendhal.server.maps.ados.outside.CloaksCollectorNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class CloaksForBarioTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final ZoneConfigurator zoneConf = new CloaksCollectorNPC();
		zoneConf.configureZone(new StendhalRPZone("admin_test"), null);
		npc = SingletonRepository.getNPCList().get("Bario");

		final AbstractQuest quest = new CloaksForBario();
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
		assertEquals("Hej! Jak się tutaj dostałeś? Co zrobiłeś? Ha. Cóż jestem Bario. Przypuszczam, że nie zrobiłbyś dla mnie drobnego #'zadania'.", getReply(npc));
		en.step(player, "task");
		assertEquals("Nigdy nie zamierzam wyjść na górę, ponieważ ukradłem beczkę piwa należącą do krasnali. Tutaj jest strasznie zimno... Pomożesz mi?", getReply(npc));
		en.step(player, "no");
		assertEquals("Och nie... Będę miał kłopoty...", getReply(npc));
		en.step(player, "task");
		assertEquals("Nigdy nie zamierzam wyjść na górę, ponieważ ukradłem beczkę piwa należącą do krasnali. Tutaj jest strasznie zimno... Pomożesz mi?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Potrzebuję lazurowych płaszczy elfickich jeżeli chcę przeżyć zimę. Przynieś mi dziesięć, a dam Ci nagrodę.", getReply(npc));
		en.step(player, "ok");
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------
		equipCloaks(4);

		en.step(player, "hi");
		assertEquals("Witaj znów! Wciąż potrzebuję 10 lazurowych płaszczy elfickich. Masz jakiś dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 9 płaszczy.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 8 płaszczy.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 7 płaszczy.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 6 płaszczy.", getReply(npc));
		en.step(player, "no");
		assertEquals("Niedobrze.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Witaj znów! Wciąż potrzebuję 6 lazurowych płaszczy elfickich. Masz jakiś dla mnie?", getReply(npc));
		en.step(player, "yes");
		// was lying
		assertEquals("Naprawdę? Nie widzę żadnego...", getReply(npc));
		en.step(player, "no");
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------
		equipCloaks(6);

		en.step(player, "hi");
		assertEquals("Witaj znów! Wciąż potrzebuję 6 lazurowych płaszczy elfickich. Masz jakiś dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 5 płaszczy.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 4 płaszcze.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 3 płaszcze.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję 2 płaszcze.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Bardzo dziękuję! Masz więcej? Wciąż potrzebuję płaszcz.", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję bardzo! Mam teraz odpowiednio dużo płaszczy, aby przetrwać zimę. Weź tę złotą tarcze jako nagrodę.", getReply(npc));
		// [23:48] superkym earns 1500 experience points.
		en.step(player, "task");
		assertEquals("Nie mam żadnego zadania dla Ciebie.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witaj! Jeszcze raz dziękuję za te płaszcze.", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie mam żadnego zadania dla Ciebie.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}

	private void equipCloaks(final int quantity) {
		for (int i = 0; i < quantity; i++) {
			final Item item = ItemTestHelper.createItem("lazurowy płaszcz elficki");
			player.getSlot("bag").add(item);
		}
	}
}
