/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.maps.ados.tavern.BarMaidNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class FruitsForCoraliaTest extends ZonePlayerAndNPCTestImpl {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;
	private static final String ZONE_NAME = "int_ados_tavern_0";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public FruitsForCoraliaTest() {
		super(ZONE_NAME, "Coralia");
	}

	@Override
	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone(ZONE_NAME);
		new BarMaidNPC().configureZone(zone, null);

		quest = new FruitsForCoralia();
		quest.addToWorld();

		questSlot = quest.getSlotName();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Coralia");
		en = npc.getEngine();


		// -----------------------------------------------


		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Och witam! Czy nie przeszkodziłam czasem w podziwianiu mojego pięknego #kapelusza?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hat");

		// -----------------------------------------------

		assertEquals("Co za szkoda, że wszystkie uschły. Potrzebuję trochę świeżych #owoców...", getReply(npc));

		// -----------------------------------------------

		en.step(player, "fruit");

		// -----------------------------------------------

		assertEquals("Czy byłbyś na tyle miły i przyniósłbyś dla mnie trochę świeżych owoców do mojego kapelusza? Byłabym wdzięczna!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "no");

		// -----------------------------------------------

		assertEquals("Ten egzotyczny kapelusz już się nie trzyma. Wiesz...", getReply(npc));

		// -----------------------------------------------

		en.step(player, "bye");

		// -----------------------------------------------

		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------

		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Och witam! Czy nie przeszkodziłam czasem w podziwianiu mojego pięknego #kapelusza?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "quest");

		// -----------------------------------------------

		assertEquals("Czy chcesz znaleźć dla mnie trochę świeżych owoców do mojego kapelusza?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "yes");

		// -----------------------------------------------

		assertEquals("To wspaniale! Chciałabym te świeże owoce: 1 #arbuz, 5 #banany, 2 #granaty, 4 #gruszki, 4 #jabłka, 2 #winogrony, oraz 9 #wisienki.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "apples");

		// -----------------------------------------------

		assertEquals("Lśniące, błszczące jabłka! Te, które mam pochodzą gdzieś ze wschodniego Semos.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "bye");

		// -----------------------------------------------

		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		PlayerTestHelper.equipWithStackableItem(player, "jabłko", 4);

		// -----------------------------------------------

		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Witaj ponownie. Jeżeli przyniosłeś świeże owoce na mój kapelusz to z radością je wezmę!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hat");

		// -----------------------------------------------

		assertEquals("Wciąż potrzebowałabym 1 #arbuz, 5 #banany, 2 #granaty, 4 #gruszki, 4 #jabłka, 2 #winogrony, oraz 9 #wisienki. Przyniosłeś coś?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "jabłko");

		// -----------------------------------------------

		assertEquals("Wspaniale! Czy przyniosłeś coś jeszcze?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "no");

		// -----------------------------------------------

		assertEquals("Och co za szkoda, powiedz mi, gdy znajdziesz kilka. Wciąż potrzebuję 1 #arbuz, 5 #banany, 2 #granaty, 4 #gruszki, 2 #winogrony, oraz 9 #wisienki.", getReply(npc));

		// -----------------------------------------------

		en.step(player, "bye");

		// -----------------------------------------------

		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------

		PlayerTestHelper.equipWithStackableItem(player, "wisienka", 9);

		// -----------------------------------------------

		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Witaj ponownie. Jeżeli przyniosłeś świeże owoce na mój kapelusz to z radością je wezmę!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "quest");

		// -----------------------------------------------

		assertEquals("Wciąż potrzebowałabym 1 #arbuz, 5 #banany, 2 #granaty, 4 #gruszki, 2 #winogrony, oraz 9 #wisienki. Przyniosłeś coś?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "yes");

		// -----------------------------------------------

		assertEquals("Cudownie jakie jeszcze świeże przysmaki mi przyniosłeś?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "wisienka");

		// -----------------------------------------------

		PlayerTestHelper.equipWithStackableItem(player, "banan", 5);
		PlayerTestHelper.equipWithStackableItem(player, "winogrona", 2);
		PlayerTestHelper.equipWithStackableItem(player, "gruszka", 4);
		PlayerTestHelper.equipWithStackableItem(player, "granat", 2);
		PlayerTestHelper.equipWithStackableItem(player, "arbuz", 1);

		final long xp = player.getXP();
		final double karma = player.getKarma();

		en.step(player, "banan");
		en.step(player, "winogrona");
		en.step(player, "gruszka");
		en.step(player, "granat");
		en.step(player, "arbuz");

		// -----------------------------------------------

		assertEquals("Mój kapelusz jeszcze nigdy nie wyglądał tak wybornie! Bardzo ci dziękuję! Przyjmij tą nagrodę.", getReply(npc));

		// -----------------------------------------------

		// [19:05] pinch earns 50 experience points.
		assertTrue(player.isEquipped("naleśniki z polewą czekoladową"));
		assertTrue(player.isEquipped("mały eliksir"));
		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));

		// -----------------------------------------------

		en.step(player, "bye");

		// -----------------------------------------------

		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------

		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Och witam! Czy nie przeszkodziłam czasem w podziwianiu mojego pięknego #kapelusza?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hat");

		// -----------------------------------------------

		assertEquals("Czy mój kapelusz nie wygląda świeżo? Teraz nie potrzebuję świeżych owoców, ale dziękuję za troskę!", getReply(npc));

		// -----------------------------------------------

		en.step(player, "bye");

		// -----------------------------------------------

		// [19:05] Removed contained minor potion item with ID 6 from bag

		// -----------------------------------------------

		assertEquals("Do widzenia.", getReply(npc));

		// -----------------------------------------------


		// -----------------------------------------------

		en.step(player, "hi");

		// -----------------------------------------------

		assertEquals("Och witam! Czy nie przeszkodziłam czasem w podziwianiu mojego pięknego #kapelusza?", getReply(npc));

		// -----------------------------------------------

		en.step(player, "hat");

		// -----------------------------------------------

		player.setQuest(questSlot, "done;0");
		//player.setQuest(questSlot, 1, "0");	This doesn't seem to work either.


		//TODO: correct test (or fix bug in quest ^^)
		/*
		assertEquals("I'm sorry to say that the fruits you brought for my hat aren't very fresh anymore.. Would you be kind enough to find me some more?", getReply(npc));

		en.step(player, "yes");

		assertEquals("That's wonderful! I'd like these fresh fruits: 4 #apples, 5 #bananas, 9 #cherries, 2 #'bunches of grapes', 4 #pears, 2 #pomegranates, and a #watermelon?", getReply(npc));

		en.step(player, "bye");

		assertEquals("Do widzenia.", getReply(npc));
		*/
	}
}
