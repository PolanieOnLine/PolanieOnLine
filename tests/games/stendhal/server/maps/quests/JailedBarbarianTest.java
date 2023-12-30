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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.amazon.hut.JailedBarbNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class JailedBarbarianTest {


	//private static String questSlot = "jailedbarb";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		MockStendlRPWorld.get();

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		new JailedBarbNPC().configureZone(zone, null);
		new games.stendhal.server.maps.amazon.hut.PrincessNPC().configureZone(zone, null);
		new games.stendhal.server.maps.kalavan.castle.PrincessNPC().configureZone(zone, null);

		final AbstractQuest quest = new JailedBarbarian();
		// Księżniczka Esclara's greeting response is defined in her quest
		final AbstractQuest quest2 = new BuiltQuest(new AmazonPrincess().story());
		quest.addToWorld();
		quest2.addToWorld();

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

		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Potrzebuję pomocy przy ucieczce z tego więzienia. Te wstrętne Amazonki! Czy możesz mi pomóc?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Po pierwsze potrzebuję #kosy do wycięcia tych okropnych kwiatków. Tylko nie przynoś mi starej! Daj znać, gdy będziesz ją miał!", getReply(npc));
		en.step(player, "scythe");
		assertEquals("Nie masz jeszcze kosy! Idź i zdobądź jakąś dla mnie. Pospiesz się!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Już się Ciebie pytałem o przyniesienie mi #kosy do ścięcia tych kwiatków!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------
		Item item = ItemTestHelper.createItem("kosa", 1);
		player.getSlot("bag").add(item);

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Już się Ciebie pytałem o przyniesienie mi #kosy do ścięcia tych kwiatków!", getReply(npc));
		en.step(player, "kosa");
		// [15:41] lula earns 1000 experience points.
		assertEquals("Dziękuję!! Pierwszą część mamy za sobą! Teraz mogę ściąć wszystkie te kwiatki! Zapytaj Księżniczki Esclarii dlaczego tutaj jestem! Sądzę, że coś powie, gdy podasz jej moje imię...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Proszę zapytaj Księżniczki Esclarii dlaczego tutaj jestem! Sądzę, że wypowiadając moje imię sprowokujesz ją do wyjawienia powodu.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Księżniczka Esclara");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "task");
		assertEquals("Napiłabym się drinka, powinien być egzotyczny. Czy możesz mi go przynieść?", getReply(npc));
		en.step(player, "no");
		assertEquals("Och, nieważne. Do widzenia.", getReply(npc));
		en.step(player, "bye");

		en.step(player, "hi");
		assertEquals("Huh, co ty tutaj robisz?!", getReply(npc));
		en.step(player, "lorenz");
		assertEquals("Chcesz wiedzieć dlaczego on tutaj jest? On i jego wstrętni przyjaciele kopali #tunel do naszej cudownej wyspy! Dlatego został uwięziony!", getReply(npc));
		en.step(player, "tunnel");
		assertEquals("Jestem teraz wściekła i nie chce już o tym rozmawiać! Jeżeli chcesz się dowiedzieć więcej to musisz go zapytać o #tunel!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i strzeż się barbarzyńców.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------


		// -----------------------------------------------
		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Założę się, że Księżniczka Esclara powiedziała, że zostałem uwięziony za #tunel...", getReply(npc));
		en.step(player, "tunnel");
		assertEquals("Co chce mnie doprowadzić do szaleństwa jak wszystkie te kwiatki! Robię się głodny. Przynieś #jajo dla mnie! Daj znać, gdy zdobędziesz.", getReply(npc));
		en.step(player, "egg");
		assertEquals("Nie widzę jaja!!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------


		item = ItemTestHelper.createItem("jajo", 1);
		player.getSlot("bag").add(item);
		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Prosiłem Ciebie o przyniesienie #jaja!", getReply(npc));
		en.step(player, "egg");
		// [15:43] lula earns 1000 experience points.
		assertEquals("Dziękuję przyjacielu. Teraz musisz powiedzieć Księżniczce Ylflii w Zamku Kalavan, że jestem tutaj #uwięziony. Pospiesz się!", getReply(npc));
		en.step(player, "jailed");
		assertEquals("Wiem to, że *jestem* uwięziony! Potrzebuję Ciebie, abyś powiedział Księżniczce Ylflii, że jestem tutaj!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Potrzebuję Ciebie, abyś poszedł do Księżniczki Ylflii i powiedział, że jestem tutaj #uwięziony.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		npc = SingletonRepository.getNPCList().get("Księżniczka Ylflia");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("He, co tutaj robisz?!", getReply(npc));
		en.step(player, "lorenz");
		assertEquals("Och. Mój ojciec nie powinien wiedzieć o tym. Mam nadzieje, że Lorenz ma się dobrze! Dziękuję za wiadomość! Wyślij mu #pozdrowienia! Lepiej wróć do niego. Może potrzebować pomocy.", getReply(npc));
		en.step(player, "greetings");
		assertEquals("Idź i przekaż Lorenz moje #pozdrowienia.", getReply(npc));


		npc = SingletonRepository.getNPCList().get("Lorenz");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "greetings");
		assertEquals("Dziękuję przyjacielu. Teraz ostatnie zadanie! Przynieś mi zbroję barbarzyńcy. Bez tego nie ucieknę stąd! Idź! Idź! I daj znać, gdy zdobędziesz #zbroję!", getReply(npc));
		en.step(player, "armor");
		assertEquals("Nie posiadasz zbroi barbarzyńcy przy sobie! Idź i zdobądź!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------


		// -----------------------------------------------
		item = ItemTestHelper.createItem("zbroja barbarzyńcy", 1);
		player.getSlot("bag").add(item);
		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Czekam na #zbroję barbarzyńcy od Ciebie. Teraz jestem dość silny, aby uciec.", getReply(npc));
		en.step(player, "armor");
		// [15:43] lula earns 50000 experience points.
		assertEquals("To wszystko! Teraz jestem gotowy do mojej ucieczki! Oto coś dla Ciebie. Ukradłem Księżniczce Esclari! Żeby tylko się nie dowiedziała. Teraz zostaw mnie!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));

		en.step(player, "hi");
		assertEquals("Kwiatki, kwiatki. Wszędzie te wstrętne kwiatki!", getReply(npc));
		en.step(player, "quest");
		assertEquals("Dziękuję za pomoc! Teraz mogę uciec!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia i zetnij trochę tych wstrętnych kwiatków!", getReply(npc));
	}
}
