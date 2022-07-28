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

import java.util.LinkedList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.bank.CustomerAdvisorNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class ArmorForDagobertTest {
	private Player player;
	private SpeakerNPC npc;
	private Engine en;
	private AbstractQuest quest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		StendhalRPZone zone = new StendhalRPZone("admin_test");
		new CustomerAdvisorNPC().configureZone(zone, null);

		npc = SingletonRepository.getNPCList().get("Dagobert");
		quest = new ArmorForDagobert();
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
		assertEquals("Witam w banku w Semos! Jeśli potrzebujesz #pomocy w sprawie skrzyń powiedz mi o tym.", getReply(npc));
		assertTrue(quest.getHistory(player).isEmpty());

		en.step(player, "no");

		assertTrue(quest.getHistory(player).isEmpty());
		en.step(player, "task");
		assertTrue(quest.getHistory(player).isEmpty());
		assertEquals("Obawiam się, że zostałem okradziony. Nie mam żadnej ochrony. Czy mógłbyś mi pomóc?",
				getReply(npc));
		en.step(player, "no");
		java.util.List<String> questHistory = new LinkedList<String>();
		questHistory.add("Spotkałem Dagobert. Jest konsultantem w banku w Semos.");
		questHistory.add("Poprosił mnie o znalezienie skórzanego kirysu, ale odrzuciłem jego prośbę.");
		assertEquals(questHistory, quest.getHistory(player));

		assertEquals("Cóż, myślę, że po prostu się ukryję.", getReply(npc));
		en.step(player, "bye");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witam w banku w Semos! Jeśli potrzebujesz #pomocy w sprawie skrzyń powiedz mi o tym.", getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "task");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Obawiam się, że zostałem okradziony. Nie mam żadnej ochrony. Czy mógłbyś mi pomóc?",
				getReply(npc));
		en.step(player, "yes");
		questHistory = new LinkedList<String>();
		questHistory.add("Spotkałem Dagobert. Jest konsultantem w banku w Semos.");
		questHistory.add("Przyrzekłem, że znajdę dla niego skórzany kirys ponieważ został okradziony.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals(
				"Raz miałem #'skórzany kirys', ale został zniszczony podczas ostatniej kradzieży. Jeżeli znajdziesz nowy to dam Tobie nagrodę.",
				getReply(npc));
		en.step(player, "leather");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals(
				"Skórzany kirys jest tradycyjną zbroją cyklopów. Kilka cyklopów mieszka w podziemiach głęboko pod miastem.",
				getReply(npc));
		en.step(player, "bye");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));

		// -----------------------------------------------
		final Item item = ItemTestHelper.createItem("skórzany kirys");
		player.getSlot("bag").add(item);
		questHistory.add("Znalazłem skórzany kirys i zabiorę go do Dagoberta.");
		assertEquals(questHistory, quest.getHistory(player));

		en.step(player, "hi");
		assertEquals("Przepraszam! Zauważyłem, że masz przy sobie skórzany kirys. Jest dla mnie?",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "no");
		assertEquals("Cóż mam nadzieję, że znajdziesz i dasz mi inny nim zostanę ponownie obrabowany.",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "bye");
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Przepraszam! Zauważyłem, że masz przy sobie skórzany kirys. Jest dla mnie?",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		// put it out of bag onto ground, then say yes.
		player.drop("skórzany kirys");
		assertFalse(player.isEquipped("skórzany kirys"));
		questHistory.remove("Znalazłem skórzany kirys i zabiorę go do Dagoberta.");
		assertEquals(questHistory, quest.getHistory(player));
		npc.remove("text");
		en.step(player, "yes");
		// he doesn't do anything.
		assertEquals(questHistory, quest.getHistory(player));
		assertFalse(npc.has("text"));
		en.step(player, "bye");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));

		// -----------------------------------------------

		player.getSlot("bag").add(item);
		en.step(player, "hi");
		questHistory.add("Znalazłem skórzany kirys i zabiorę go do Dagoberta.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przepraszam! Zauważyłem, że masz przy sobie skórzany kirys. Jest dla mnie?",
				getReply(npc));
		final int xpBeforeReward = player.getXP();
		en.step(player, "yes");
		questHistory.add("Wziąłem skórzany kirys do Dagoberta. Podziękował i dał mi nagrodę.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Oh, jestem Ci tak wdzięczny, proszę weź to złoto, które znalazłem...ehm..gdzieś.", getReply(npc));
		assertEquals(xpBeforeReward + 500, player.getXP());
		en.step(player, "task");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Dziękuję za zbroję, ale nie mam więcej zadań dla Ciebie.", getReply(npc));
	}


}
