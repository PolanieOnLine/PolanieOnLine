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
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.fado.hut.SellerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class LearnAboutKarmaTest {

	private static final String KARMA_ANSWER = "Gdy robisz dobre rzeczy dla innych takie jak #zadania dostajesz dobrą karmę. Dobra karma oznacza, że będzie ci się powodzić w bitwach, w łowieniu ryb, poszukiwaniu złota i drogocennych kamieni. Chcesz wiedzieć jaką masz teraz karmę?";
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		StendhalRPZone zone = new StendhalRPZone("admin_test");
		new SellerNPC().configureZone(zone, null);
		npc = SingletonRepository.getNPCList().get("Sarzina");

		// configure Sarzina's shop
		SingletonRepository.getShopsList().configureNPC("Sarzina", "superhealing", true);

		final AbstractQuest quest = new LearnAboutKarma();
		quest.addToWorld();
		en = npc.getEngine();

		player = PlayerTestHelper.createPlayer("player");
		player.addKarma(-1 * player.getKarma());
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "help");
		assertEquals("Możesz wziąć przygotowane przeze mnie lekarstwo na podróż. Zapytaj mnie o #ofertę.", getReply(npc));
		en.step(player, "offer");
		assertEquals("Sprzedaję antidotum, mocne antidotum, eliksir, duży eliksir, oraz wielki eliksir.", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy jesteś tym, który lubi pomagać innym?", getReply(npc));
		en.step(player, "no");
		assertEquals("Wiedziałam... pewnie otacza cię zła #karma.", getReply(npc));
		en.step(player, "karma");
		assertEquals(KARMA_ANSWER, getReply(npc));
		en.step(player, "yes");
		assertEquals("Twoja karma to -10, nie jest zbyt dobra. Teraz zobaczymy jakiego koloru jest twoja karma. Postaraj się, aby była na granicy niebiesko.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Jeśli chcesz by towarzyszyła ci dobra #karma jedyne co musisz robić, to pomagać innym. Znam dziewczę o imieniu Sally, która potrzebuje drewna, i znam inne dziewczę co zwie się Annie, która uwielbia lody. Cóż, znam wielu mieszkańców tej krainy, którzy stale potrzebować będą pomocy. Jestem pewna, że jeśli im pomożesz czeka cię sowita zapłata.", getReply(npc));
		en.step(player, "karma");
		assertEquals(KARMA_ANSWER, getReply(npc));
		en.step(player, "yes");
		assertEquals("Twoja karma to -10, nie jest zbyt dobra. Teraz zobaczymy jakiego koloru jest twoja karma. Postaraj się, aby była na granicy niebiesko.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------
		// start quest again (clean)
		player.setQuest("learn_karma", null);

		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "job");
		assertEquals("Wytwarzam mikstury i antidota, aby #zaoferować je wojownikom.", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy jesteś tym, który lubi pomagać innym?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Wyśmienicie! Musi cię otaczać dobra #karma.", getReply(npc));
		en.step(player, "karma");
		assertEquals(KARMA_ANSWER, getReply(npc));
		en.step(player, "yes");
		assertEquals("Twoja karma to -10, nie jest zbyt dobra. Teraz zobaczymy jakiego koloru jest twoja karma. Postaraj się, aby była na granicy niebiesko.", getReply(npc));
		// add a bit of karma to test different values
		player.addKarma(15.0);
		en.step(player, "karma");
		assertEquals(KARMA_ANSWER, getReply(npc));
		en.step(player, "yes");
		assertEquals("Twoja karma wynosi 5. Teraz zobaczymy jakiego koloru jest twoja karma. Znajduje się w połowie skali.", getReply(npc));
		// add a bit more karma to test more values
		player.addKarma(10.0);
		en.step(player, "karma");
		assertEquals(KARMA_ANSWER, getReply(npc));
		en.step(player, "yes");
		assertEquals("Twoja karma jest dobra i wynosi 15. Teraz zobaczymy jakiego koloru jest twoja karma. Musisz uważać, zbliża się do granicy czerwonego.",  getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
