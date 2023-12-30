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
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.semos.guardhouse.RetiredAdventurerNPC;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject.ID;
import utilities.PlayerTestHelper;

public class BeerForHayunnTest {

	private SpeakerNPC hayunn;

	private AbstractQuest bfh;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();

		MockStendhalRPRuleProcessor.get();

		MockStendlRPWorld.reset();
		MockStendlRPWorld.get();
	}

	@Before
	public void setup() {
		PlayerTestHelper.removeAllPlayers();
		StendhalRPZone zone = new StendhalRPZone("admin_test");
		new RetiredAdventurerNPC().configureZone(zone, null);
		hayunn = SingletonRepository.getNPCList().get("Hayunn Naratha") ;

		bfh = new BuiltQuest(new BeerForHayunn().story());

		bfh.addToWorld();
	}

	@Test
	public void quest() {

		final Player player = PlayerTestHelper.createPlayer("player");

		final Engine en = hayunn.getEngine();
		en.step(player, "hi");
		// we assume the player has already completed the meet hayunn quest
		// so that we know which of the greetings he will use
		player.setQuest("meet_hayunn", "done");
		assertTrue(player.isQuestCompleted("meet_hayunn"));
		assertTrue(hayunn.isTalking());
		assertEquals(
				"Witaj! Założę się, że przysłano Cię tutaj byś dowiedział się nieco o przygodach jakie Cię tu spotkają. Najpierw zobaczmy z jakiej gliny jesteś ulepiony. Idź i zabij szczura wałęsającego się gdzieś na zewnątrz. Czy chcesz się nauczyć jak atakować?",
				getReply(hayunn));
		en.step(player, "quest");
		assertEquals(
				"Zaschło mi w gardle, ale nie mogę opuścić mojego posterunku! Czy mógłbyś mi przynieść #sok z chmielu z #oberży?",
				getReply(hayunn));
		en.step(player, "yes");
		assertTrue(player.hasQuest("beer_hayunn"));
		en.step(player, "bye");
		assertFalse(hayunn.isTalking());
		assertEquals("start", player.getQuest("beer_hayunn"));
		final StackableItem beer = new StackableItem("sok z chmielu", "", "", null);
		beer.setQuantity(1);
		beer.setID(new ID(2, "testzone"));
		player.getSlot("bag").add(beer);
		assertEquals(1, player.getNumberOfEquipped("sok z chmielu"));
		en.step(player, "hi");
		en.step(player, "yes");
		assertEquals("done", player.getQuest("beer_hayunn", 0));
		en.step(player, "bye");
		// reject
		final Player player2 = PlayerTestHelper.createPlayer("player");

		en.step(player2, "hi");
		player2.setQuest("meet_hayunn", "done");
		assertTrue(player2.isQuestCompleted("meet_hayunn"));
		assertTrue(hayunn.isTalking());
		assertEquals(
				"Witaj! Założę się, że przysłano Cię tutaj byś dowiedział się nieco o przygodach jakie Cię tu spotkają. Najpierw zobaczmy z jakiej gliny jesteś ulepiony. Idź i zabij szczura wałęsającego się gdzieś na zewnątrz. Czy chcesz się nauczyć jak atakować?",
				getReply(hayunn));
		en.step(player2, "quest");
		assertEquals(
				"Zaschło mi w gardle, ale nie mogę opuścić mojego posterunku! Czy mógłbyś mi przynieść #sok z chmielu z #oberży?",
				getReply(hayunn));
		en.step(player2, "no");
		assertTrue(player2.hasQuest("beer_hayunn"));
		assertEquals("rejected", player2.getQuest("beer_hayunn"));
		en.step(player2, "bye");
	}

	/**
	 * Tests for getHistory.
	 */
	@Test
	public void testgetHistory() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		assertTrue(bfh.getHistory(player).isEmpty());
		player.setQuest("beer_hayunn", null);
		final List<String> history = new LinkedList<String>();
		history.add("Rozmawiałem z Hayunn.");

		player.setQuest("beer_hayunn", "rejected");
		history.add("Nie chcę kupować soku z chmielu dla Hayunn.");
		assertEquals(history, bfh.getHistory(player));

		player.setQuest("beer_hayunn", "start");
		history.remove("Nie chcę kupować soku z chmielu dla Hayunn.");
		history.add("Obiecałem, że kupię mu sok z chmielu od Margaret w tawernie Semos.");
		assertEquals(history, bfh.getHistory(player));

		player.equipToInventoryOnly(SingletonRepository.getEntityManager().getItem("sok z chmielu"));
		history.add("Mam już sok z chmielu.");
		assertEquals(history, bfh.getHistory(player));
		player.setQuest("beer_hayunn", "done");
		history.add("Dałem sok z chmielu Hayunn. Otrzymałem 20 złotych monet i kilka lekcji z jego doświadczenia.");
		assertEquals(history, bfh.getHistory(player));

	}
}
