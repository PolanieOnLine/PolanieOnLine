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
import games.stendhal.server.entity.npc.quest.BuiltQuest;
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
		quest = new BuiltQuest(new ArmorForDagobert().story());
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
		assertEquals("Tak bardzo boję się, że zostanę okradziony podczas jakiegoś napadu. Nie mam żadnej ochrony. Myślisz, że jesteś w stanie mi pomóc?",
				getReply(npc));
		en.step(player, "no");
		java.util.List<String> questHistory = new LinkedList<String>();
		questHistory.add("W banku napotkany został Dagobert. Jest konsultantem w banku w Semos.");
		questHistory.add("Poprosił mnie o znalezienie skórzanego kirysu, ale nie zamierzam pomagać z jego prośbą.");
		assertEquals(questHistory, quest.getHistory(player));

		assertEquals("Cóż, w takim razie chyba po prostu schylę się i ukryję.", getReply(npc));
		en.step(player, "bye");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Witam w banku w Semos! Jeśli potrzebujesz #pomocy w sprawie skrzyń powiedz mi o tym.", getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "task");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Tak bardzo boję się, że zostanę okradziony podczas jakiegoś napadu. Nie mam żadnej ochrony. Myślisz, że jesteś w stanie mi pomóc?",
				getReply(npc));
		en.step(player, "yes");
		questHistory = new LinkedList<String>();
		questHistory.add("W banku napotkany został Dagobert. Jest konsultantem w banku w Semos.");
		questHistory.add("Znajdę dla niego skórzany kirys, ponieważ został okradziony.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals(
				"Kiedyś miałem fajny #'skórzany kirys', ale uległ zniszczeniu podczas ostatniego napadu. Jeśli znajdziesz nowy, dam ci nagrodę.",
				getReply(npc));
		en.step(player, "leather");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals(
				"Skórzany kirys to tradycyjna zbroja cyklopa. Niektórzy cyklopi żyją w lochach głęboko pod miastem.",
				getReply(npc));
		en.step(player, "bye");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));

		// -----------------------------------------------
		final Item item = ItemTestHelper.createItem("skórzany kirys");
		player.getSlot("bag").add(item);
		questHistory.add("Znalazł się skórzany kirys i niosę już go Dagobertowi.");
		assertEquals(questHistory, quest.getHistory(player));

		en.step(player, "hi");
		assertEquals("Przepraszam! Zauważyłem skórzany kirys, który nosisz. Czy to dla mnie?",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "no");
		assertEquals("No cóż, mam nadzieję, że znajdziesz inny i możesz mi go dać, zanim znowu zostanę obrabowany.",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		en.step(player, "bye");
		assertEquals("Przyjemnie mi się z tobą pracowało!", getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Przepraszam! Zauważyłem skórzany kirys, który nosisz. Czy to dla mnie?",
				getReply(npc));
		assertEquals(questHistory, quest.getHistory(player));
		// put it out of bag onto ground, then say yes.
		player.drop("skórzany kirys");
		assertFalse(player.isEquipped("skórzany kirys"));
		questHistory.remove("Znalazł się skórzany kirys i niosę już go Dagobertowi.");
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
		questHistory.add("Znalazł się skórzany kirys i niosę już go Dagobertowi.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Przepraszam! Zauważyłem skórzany kirys, który nosisz. Czy to dla mnie?",
				getReply(npc));
		final long xpBeforeReward = player.getXP();
		en.step(player, "yes");
		questHistory.add("Skórzany kirys został zwrócony Dagobertowi. W ramach drobnej wdzięczności pozwoli mi skorzystać z prywatnego skarbca.");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Och, jestem bardzo wdzięczny! Oto trochę złota, które znalazłem… ehm… gdzieś. Masz moją dozgodną wdzięczność oraz zaufanie, w nagrodę teraz możesz mieć dostęp do własnego prywatnego skarbca, kiedy tylko chcesz.", getReply(npc));
		assertEquals(xpBeforeReward + 50, player.getXP());
		en.step(player, "task");
		assertEquals(questHistory, quest.getHistory(player));
		assertEquals("Dziękuję bardzo za zbroję, ale nie mam dla ciebie innego zadania.", getReply(npc));
	}


}
