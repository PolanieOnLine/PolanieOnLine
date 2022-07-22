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

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import games.stendhal.server.maps.ados.forest.FarmerNPC;
import games.stendhal.server.maps.semos.tavern.BowAndArrowSellerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class BowsForOuchitTest {

	// better: use the one from quest and make it visible
	private static final String QUEST_SLOT = "bows_ouchit";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {

		final StendhalRPZone zone = new StendhalRPZone("admin_test");

		// this is Ouchit
		new BowAndArrowSellerNPC().configureZone(zone, null);
		// this is Karl
		new FarmerNPC().configureZone(zone, null);

		AbstractQuest quest = new BowsForOuchit();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@Test
	public void testGetWood() {
		npc = SingletonRepository.getNPCList().get("Ouchit");
		en = npc.getEngine();

		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czy jesteś tu, aby mi pomóc?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dobrze! Sprzedaję łuki i strzały. Byłoby super gdybyś mógł przynieść 10 sztuk #polan. Możesz mi przynieść drewno?", getReply(npc));
		en.step(player, "polano");
		assertEquals("Drzewo jest potrzebne do różnych rzeczy, znajdziesz go... Najlepiej spytaj Drwala koło Zakopanego. Przyniesiesz mi 10 sztuk?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Wspaniale! Jak wrócisz z nim powiedz #polano.", getReply(npc));
		en.step(player, "polano");
		assertEquals("Drzewo jest surowcem potrzebnym do wielu rzeczy. Jestem pewien, że znajdziesz w lesie. Lepiej porozmawiaj z drwalem koło Zakopanego. I nie zapomnij powiedzieć #'polano', gdy wrócisz z nim.", getReply(npc));
		en.step(player, "polano");
		assertEquals("Drzewo jest surowcem potrzebnym do wielu rzeczy. Jestem pewien, że znajdziesz w lesie. Lepiej porozmawiaj z drwalem koło Zakopanego. I nie zapomnij powiedzieć #'polano', gdy wrócisz z nim.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"wood");

		// test without wood
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czekam na ciebie, aż mi przyniesiesz 10 sztuk #drewna.", getReply(npc));
		en.step(player, "polano");
		assertEquals("Drzewo jest surowcem potrzebnym do wielu rzeczy. Jestem pewien, że znajdziesz w lesie. Lepiej porozmawiaj z drwalem koło Zakopanego. I nie zapomnij powiedzieć #'polano', gdy wrócisz z nim.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// test with wood
		PlayerTestHelper.equipWithStackableItem(player, "wood", 10);

		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "polano");
		assertEquals("Bardzo dobrze, teraz mogę zrobić nowe strzały. Ale do łuku, który robię potrzebuję cięciwę. Proszę idź do #Karl. Wiem, że posiada konie powiedz moje imię, a da ci końskiego włosa. Z niego zrobię cięciwę.", getReply(npc));

		// check quest slot
		assertEquals(player.getQuest(QUEST_SLOT),"hair");

		en.step(player, "horse hairs");
		assertEquals("Włos koński stosowany może być do cięciwy, a dostaniesz go od #Karl.", getReply(npc));
		en.step(player, "Karl");
		assertEquals("Karl is a farmer, east of Semos. He has many pets on his farm.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// test without hairs or going to Karl
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czekam tu na ciebie, abyś przyniósł mi #'końskie włosie'.", getReply(npc));

		// notice a typo here done by the actual player
		en.step(player, "hore hairs");
		assertEquals("Włos koński stosowany może być do cięciwy, a dostaniesz go od #Karl.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		assertEquals(player.getQuest(QUEST_SLOT),"hair");
	}

	@Test
	public void testGetHairs() {
		npc = SingletonRepository.getNPCList().get("Karl");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "hair");

		en.step(player, "hi");
		assertEquals("Heja! Miło Cię widzieć w naszym gospodarstwie.", getReply(npc));
		en.step(player, "help");
		assertEquals("Potrzebujesz pomocy? Mogę coś ci opowiedzieć o #sąsiedztwie.", getReply(npc));
		en.step(player, "neighborhood");

		assertEquals("Na północy znajduje się jaskinia z niedźwiedziami i innymi potworami. Jeżeli pójdziesz na północny-wschód to po pewnym czasie dojdziesz do dużego miasta Ados. Na wschodzie jest duuuuża skała. Balduin wciąż tam mieszka? Chcesz wyruszyć na południowy-wschód? Cóż.. możesz tamtędy dojść do Ados, ale droga jest trochę trudniejsza.", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie mam teraz czasu na takie rzeczy. Praca.. praca.. praca..", getReply(npc));

		// he doesn't seem to reply to horse hairs
		en.step(player, "horse hairs");
		assertNull(getReply(npc));

		en.step(player, "Ouchit");
		assertEquals("Witam, witam! Ouchit potrzebuje więcej włosia z moich koni? Nie ma problemu. Weź to i przekaż Ouchitowi serdeczne pozdrowienia ode mnie.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia, do widzenia. Bądź ostrożny.", getReply(npc));

		// check quest slot and item
		assertTrue(player.isEquipped("koński włos"));
		assertEquals(player.getQuest(QUEST_SLOT),"hair");
	}

	@Test
	public void testBringHairs() {
		npc = SingletonRepository.getNPCList().get("Ouchit");
		en = npc.getEngine();

		// the state wasn't remembered across the new test method so we need to set it to what it was when we ended the last
		player.setQuest(QUEST_SLOT, "hair");
		// nor was what the player was equipped with
		PlayerTestHelper.equipWithItem(player, "koński włos");

		// remember the xp and karma, did it go up?
		final int xp = player.getXP();
		final double karma = player.getKarma();

		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Czekam tu na ciebie, abyś przyniósł mi #'końskie włosie'.", getReply(npc));
		en.step(player, "horse hairs");
		assertEquals("Wspaniale, masz włos koński. Dziękuję bardzo. Karl jest bardzo miły. Proszę, zostawił tutaj ten... Mi nie jest to potrzebne, a tobie może się przydać.", getReply(npc));

		// [19:57] kymara earns 100 experience points.
		// check quest slot and rewards
		assertTrue(player.getQuest(QUEST_SLOT).equals("done"));
		assertTrue(player.isEquipped("zbroja łuskowa"));
		assertTrue(player.isEquipped("spodnie nabijane ćwiekami"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));

		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// check how he replies when the quest is finished
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Dziękuję za twoją pomoc. Jeżeli mogę zaoferować coś po prostu zapytaj o #ofertę.", getReply(npc));
		en.step(player, "offer");
		assertEquals("I sell wooden bow and wooden arrow.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
