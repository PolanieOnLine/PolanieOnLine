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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

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
import games.stendhal.server.maps.kalavan.castle.KingNPC;
import games.stendhal.server.maps.kalavan.castle.PrincessNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class ImperialPrincessTest {


	private static String questSlot = "imperial_princess";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new PrincessNPC().configureZone(zone, null);
		new KingNPC().configureZone(zone, null);

		final AbstractQuest quest = new ImperialPrincess();
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
		// try going to king cozart before favour is done for princess
		npc = SingletonRepository.getNPCList().get("King Cozart");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Zostaw mnie! Nie widzisz, że próbuję jeść?", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Księżniczka Ylflia");
		en = npc.getEngine();
		// she uses the player level for this, so lets set the player level  to what it was in this test
		player.setLevel(270);

		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "help");
		assertEquals("Uważaj na szalonych naukowców. Mój ojciec uwolnił ich, aby wykonali pewną pracę w podziemiach i obawiam się, że pewne rzeczy, wymknęły się im spod kontroli...", getReply(npc));
		en.step(player, "offer");
		assertEquals("Przepraszam, ale nie mam Ci nic do zaoferowania. Mógłbyś mi wyświadczyć pewną #przysługę, chociaż...", getReply(npc));
		en.step(player, "favour");
		assertEquals("Nie mogę uwolnić więźniów w piwnicy, ale mogę zrobić jedną rzecz. Złagodzić im ból. Potrzebuję #ziół do tego.", getReply(npc));
		en.step(player, "herbs");
		assertEquals("Potrzebuję 7 #arandula, 1 #kokuda, 1 #sclaria, 1 #kekik, 28 #eliksir i 14 #antidotum. Zdobędziesz to dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("Pozwalasz im cierpieć! Jakie to podłe.", getReply(npc));
		assertThat(player.getQuest(questSlot), is("rejected"));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));

		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie mogę uwolnić więźniów w piwnicy, ale mogę zrobić jedną rzecz. Złagodzić im ból. Potrzebuję #ziół do tego.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));

		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "herbs");
		assertEquals("Potrzebuję 7 #arandula, 1 #kokuda, 1 #sclaria, 1 #kekik, 28 #eliksir i 14 #antidotum. Zdobędziesz to dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Musimy być subtelni w tej sprawie. Nie chcę, aby naukowcy coś podejrzewali i wtrącili się. Gdy wrócisz ze składnikami to powiedz mi hasło #zioła.", getReply(npc));
		en.step(player, "herbs");
		assertEquals("Cii! Nie mów nic dopóki nie będziesz miał 7 #arandula, 1 #kokuda, 1 #sclaria, 1 #kekik, 28 #eliksir oraz 14 #antidotum. Nie chce, aby ktoś się domyślił naszego przepisu.", getReply(npc));
		en.step(player, "kokuda");
		assertEquals("Wiem, że to zioło można tylko znaleźć na wyspie Athor, sądzę, że tam dobrze strzegą sekretu.", getReply(npc));
		en.step(player, "kekik");
		assertEquals("Przyjaciel mojej pokojówki Jenny ma źródło niedaleko niego. Mogą występować na lesistych terenach nad rzeką we wschodniej części Nalwor.", getReply(npc));
		en.step(player, "sclaria");
		assertEquals("Uzdrowiciele, którzy używają sclaria zbierają je w różnych miejscach w okolicach Or'ril, w lesie Nalwor. Jestem pewna, że znajdziesz je bez problemu.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));
		// she should have stored the player level in the quest slot.
		assertTrue(player.getQuest(questSlot).equals(Integer.toString(player.getLevel())));


		Item item = ItemTestHelper.createItem("arandula", 7);
		player.getSlot("bag").add(item);
		item = ItemTestHelper.createItem("kokuda", 1);
		player.getSlot("bag").add(item);
		item = ItemTestHelper.createItem("sclaria", 1);
		player.getSlot("bag").add(item);
		item = ItemTestHelper.createItem("kekik", 1);
		player.getSlot("bag").add(item);
		item = ItemTestHelper.createItem("antidotum", 14);
		player.getSlot("bag").add(item);
		item = ItemTestHelper.createItem("eliksir", 28);
		player.getSlot("bag").add(item);

		final int xp = player.getXP();
		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Już o coś się Ciebie pytałam.", getReply(npc));
		// note the typos here, i should have said herbs but she understood herbd anyway.
		en.step(player, "herbd");
		assertEquals("Doskonale! Zarekomenduję Cie mojemu ojcu jako dobrą i pomocną osobę. On się zgodzi ze mną, że nadajesz się na obywatela Kalavan.", getReply(npc));
		// [22:21] kymara earns 110400 experience points.
		assertFalse(player.isEquipped("eliksir"));
		assertFalse(player.isEquipped("antidotum"));
		assertFalse(player.isEquipped("arandula"));
		assertFalse(player.isEquipped("sclaria"));
		assertFalse(player.isEquipped("kekik"));
		assertFalse(player.isEquipped("kokoda"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getQuest(questSlot), is("recommended"));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));

		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Porozmawiaj z moim ojcem, Królem Cozartem. Zapytałam się go o przyznanie Tobie obywatelstwa Kalavan, aby wyrazić Ci moją wdzięczność.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));

		npc = SingletonRepository.getNPCList().get("King Cozart");
		en = npc.getEngine();

		final int xp2 = player.getXP();
		en.step(player, "hi");
		// [22:22] kymara earns 500 experience points.
		assertEquals("Pozdrawiam! Moja cudowna córka poprosiła mnie o przyznanie Tobie obywatelstwa miasta Kalavan. Rozpatrywanie zostało zakończone. Teraz wybacz mi, że wrócę do mojego posiłku. Do widzenia.", getReply(npc));
		assertThat(player.getXP(), greaterThan(xp2));
		assertTrue(player.isQuestCompleted(questSlot));

		// try going after quest is done
		en.step(player, "hi");
		assertEquals("Zostaw mnie! Nie widzisz, że próbuję jeść?", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Księżniczka Ylflia");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Uwięzione potwory wyglądają dużo lepiej od ostatniego razu. Odważyłam się zaryzykować i zejść do piwnicy. Dziękuję!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i powodzenia.", getReply(npc));
	}
}
