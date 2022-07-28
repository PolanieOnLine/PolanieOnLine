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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;

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
import games.stendhal.server.maps.semos.plains.LittleBoyNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;
import utilities.RPClass.PassiveEntityRespawnPointTestHelper;

public class PlinksToyTest {
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		PassiveEntityRespawnPointTestHelper.generateRPClasses();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("0_semos_plains_n");
		MockStendlRPWorld.get().addRPZone(zone);
		new LittleBoyNPC().configureZone(zone, null);

		final PlinksToy quest = new PlinksToy();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("player");
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Plink");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("*płacz* Wilki są w #parku! *płacz* Uciekłem, ale upuściłem mojego #misia! Proszę przyniesiesz go dla mnie? *siąknięcie* Proszę?", getReply(npc));
		en.step(player, "park!");
		assertEquals("Moi rodzice mówili mi żebym sam nie chodził do parku, ale się zgubiłem podczas zabawy... Proszę nie mów moim rodzicom! Czy możesz mi przynieść misia #teddy z powrotem?", getReply(npc));
		en.step(player, "yes");
		assertEquals("*siąknięcie* Dziękuję bardzo! *uśmiech*", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("*płacz* Wilki są w #parku! *płacz* Uciekłem, ale upuściłem mojego #misia! Proszę przyniesiesz go dla mnie? *siąknięcie* Proszę?", getReply(npc));
		en.step(player, "pluszowy miś");
		assertEquals("Miś jest moją ulubioną zabawką! Przyniesiesz mi ją?", getReply(npc));
		en.step(player, "no");
		assertEquals("*pociągnięcie nosem* Ale... ale... PROSZĘ! *płacz*", getReply(npc));

		en.step(player, "teddy bear");
		assertEquals("Miś jest moją ulubioną zabawką! Przyniesiesz mi ją?", getReply(npc));
		en.step(player, "yes");
		assertEquals("*siąknięcie* Dziękuję bardzo! *uśmiech*", getReply(npc));

		// -----------------------------------------------

		final Item teddy = ItemTestHelper.createItem("pluszowy miś");
		teddy.setEquipableSlots(Arrays.asList("bag"));
		player.equipToInventoryOnly(teddy);
		assertTrue(player.isEquipped("pluszowy miś"));

		System.out.println(player.getSlot("!quests"));
		System.out.println(player.getSlot("lhand"));
		System.out.println(player.getSlot("rhand"));

		en.step(player, "hi");
		// [21:25] player earns 10 experience points.
		assertEquals("Znalazłeś go! *przytula misia* Dziękuję, dziękuję! *uśmiech*", getReply(npc));

		assertFalse(player.isEquipped("pluszowy miś"));

		en.step(player, "help");
		assertEquals("Bądź ostrożny idąc na wschód. Tam są wilki!", getReply(npc));
		en.step(player, "job");
		assertEquals("Bawię się cały dzień.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
