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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.tunnel.WishmanNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class DragonLairTest {


	private static String questSlot = "dragon_lair";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		// must add the zone here as the wishman teleports player into dragon lair
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("-1_ados_outside_w"));
		new WishmanNPC().configureZone(zone, null);


		final AbstractQuest quest = new DragonLair();
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

		npc = SingletonRepository.getNPCList().get("Wishman");
		en = npc.getEngine();

		// see if level 0 player can enter (they could)
		en.step(player, "hi");
		assertEquals("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy chciałbyś odwiedzić legowisko smoków?", getReply(npc));
		en.step(player, "no");
		assertEquals("Dobrze, ale nasze smoki będą zawiedzione jeżeli nie wpadniesz do nich.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy chciałbyś odwiedzić legowisko smoków?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Wspaniale! Ciesz się wizytą. Wiem, że oni będą. Aha uważaj. Mamy parę jeźdźców smoków chaosu dowodzących naszymi smokami. Nie wchodź im w drogę!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.", getReply(npc));
		// [21:59] green dragon has been killed by kymara
		// [21:59] kymara earns 1750 experience points.
		// [21:59] red dragon has been killed by kymara
		// [21:59] kymara earns 20700 experience points.

		// quest slot should now start with "start"
		assertTrue(player.getQuest(questSlot) + " starts with start", player.isQuestInState(questSlot, "start"));

		// the portal sets the quest slot to  done
		player.setQuest(questSlot, "done;" + Long.toString(System.currentTimeMillis()));

		en.step(player, "hi");
		assertEquals("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?", getReply(npc));
		en.step(player, "task");
		assertThat(getReply(npc), is(oneOf("Sądzę, że mają już dosyć wrażeń na jakiś czas. Wróć za 7 dni.",
										  "Sądzę, że mają już dosyć wrażeń na jakiś czas. Wróć za 1 tydzień.")));
		en.step(player, "bye");
		assertEquals("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.", getReply(npc));

		// [22:00] Admin kymara changed your state of the quest 'dragon_lair' from 'done;1219874335035' to 'done;0'
		// [22:00] Changed the state of quest 'dragon_lair' from 'done;1219874335035' to 'done;0'

		player.setQuest(questSlot, "done;0");
		en.step(player, "hi");
		assertEquals("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?", getReply(npc));
		en.step(player, "task");
		assertEquals("Uważaj smoki zaczęły ziać ogniem! Czy chciałbyś ponownie odwiedzić nasze smoki?", getReply(npc));
		en.step(player, "no");
		assertEquals("Dobrze, ale nasze smoki będą zawiedzione jeżeli nie wpadniesz do nich.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Pozdrawiam podróżniku. Co mogę dla Ciebie zrobić?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy chciałbyś odwiedzić legowisko smoków?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Wspaniale! Ciesz się wizytą. Wiem, że oni będą. Aha uważaj. Mamy parę jeźdźców smoków chaosu dowodzących naszymi smokami. Nie wchodź im w drogę!", getReply(npc));
		en.step(player, "bye");
		// [22:01] chaos green dragonrider has been killed by kymara
		// [22:01] kymara earns 31900 experience points.
		// [22:01] bone dragon has been killed by kymara
		// [22:01] kymara earns 2210 experience points.
		assertEquals("Na razie. Niech twoje serce zawsze będzie wolne, a życie długie.", getReply(npc));

	}
}
