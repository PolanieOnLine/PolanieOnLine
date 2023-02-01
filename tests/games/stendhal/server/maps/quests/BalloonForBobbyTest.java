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
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.fado.city.SmallBoyNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class BalloonForBobbyTest {
	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		new SmallBoyNPC().configureZone(zone, null);

		AbstractQuest quest = new BalloonForBobby();
		QuestHelper.loadQuests(quest);

		questSlot = quest.getSlotName();

		player = PlayerTestHelper.createPlayerWithOutFit("bob");
	}

	@Test
	public void testQuest() {
		// Quest not started
		player.setQuest(questSlot, null);
		runQuestDialogue();
		// Quest started
		player.setQuest(questSlot, 0, "start");
		runQuestDialogue();
		// Quest rejected
		player.setQuest(questSlot,  0, "rejected");
		runQuestDialogue();
		// Quest done
		player.setQuest(questSlot, 0, "done");
		runQuestDialogue();

		// Quest not started (during Mine Town Weeks)
		player.setQuest(questSlot, null);
		runMinetownQuestDialogue();
		// Quest started (during Mine Town Weeks)
		player.setQuest(questSlot, 0, "start");
		runMinetownQuestDialogue();
		// Quest rejected (during Mine Town Weeks)
		player.setQuest(questSlot,  0, "rejected");
		runMinetownQuestDialogue();
		// Quest done (during Mine Town Weeks)
		player.setQuest(questSlot, 0, "done");
		runMinetownQuestDialogue();
	}

	public void runQuestDialogue() {
		System.getProperties().remove("stendhal.minetown");

		npc = SingletonRepository.getNPCList().get("Bobby");
		en = npc.getEngine();
		Outfit outfitWithBalloon = new Outfit(0, 8, 1, 6, 5, 4, 3, 2, 1);

		// -----------------------------------------------

		// Player HAS balloon; NO Mine Town Weeks
		// Player says "Yes" straight out
		player.setOutfit(outfitWithBalloon);


		en.step(player, "hi");
		assertEquals("Cześć! Czy ten balonik jest dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Hurra! Leć baloniku! Leć!", getReply(npc));
		en.step(player, "balloon");
		assertEquals("Nie masz dla mnie balonika :(", getReply(npc));
		en.step(player, "help");
		assertEquals("Zastanawiam się czy dzięki #balonikowi będę mógł się wznieść wystarczająco wysoko, aby dotknąć chmur...", getReply(npc));
		en.step(player, "job");
		assertEquals("Praca? Czy to coś takiego co mogę zjeść?", getReply(npc));
		en.step(player, "quest");
		assertEquals("Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));


		// Player HAS balloon; NO Mine Town Weeks
		// Player says "No" first
		player.setOutfit(outfitWithBalloon);

		en.step(player, "hi");
		assertEquals("Cześć! Czy ten balonik jest dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("!me dąsa się.", getReply(npc));
		en.step(player, "help");
		assertEquals("Zastanawiam się czy dzięki #balonikowi będę mógł się wznieść wystarczająco wysoko, aby dotknąć chmur...", getReply(npc));
		en.step(player, "job");
		assertEquals("Praca? Czy to coś takiego co mogę zjeść?", getReply(npc));
		en.step(player, "quest");
		assertEquals("Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		en.step(player, "balloon");
		assertEquals("Czy ten balonik jest dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("!me dąsa się.", getReply(npc));
		en.step(player, "balloon");
		assertEquals("Czy ten balonik jest dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Hurra! Leć baloniku! Leć!", getReply(npc));
		en.step(player, "balloon");
		assertEquals("Nie masz dla mnie balonika :(", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		assertTrue(player.isQuestCompleted(questSlot));
	}

	public void runMinetownQuestDialogue() {

		npc = SingletonRepository.getNPCList().get("Bobby");
		en = npc.getEngine();
		Outfit outfitNoBalloon = new Outfit(3, 3, 3, null, 4, null, 7, 1, 0);
		Outfit outfitWithBalloon = new Outfit(3, 3, 3, null, 4, null, 7, 1, 1);

		// Mine Town weeks are on: it should not matter if player has a balloon or not
		System.setProperty("stendhal.minetown", "true");

		// Player HAS balloon; Mine Town Weeks ARE ON
		player.setOutfit(outfitWithBalloon);

		en.step(player, "hi");
		assertEquals("Hm?", getReply(npc));
		en.step(player, "help");
		assertEquals("Zastanawiam się czy dzięki #balonikowi będę mógł się wznieść wystarczająco wysoko, aby dotknąć chmur...", getReply(npc));
		en.step(player, "quest");
		if (player.getQuest(questSlot) == null || player.getQuest(questSlot, 0).equals("rejected")) {
			assertEquals("Czy mógłbyś zdobyć dla mnie #'balonik'? Nim zaczną się dni miasta, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		} else {
			assertEquals("Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		}
		en.step(player, "balloon");
		if (player.getQuest(questSlot) == null || player.getQuest(questSlot, 0).equals("rejected")) {
			assertEquals("Pewnego dnia będę miał wystarczająco dużo baloników, aby odlecieć!", getReply(npc));
		} else {
			assertEquals("Chmury podpowiedziały mi, że dni mine town wciąż trwają - Mogę sam zdobyć balonik. Wróć, gdy skończy się święto :)", getReply(npc));
		}
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		System.getProperties().remove("stendhal.minetown");


		// Player HAS NO balloon; Mine Town Weeks ARE ON
		player.setOutfit(outfitNoBalloon);
		System.setProperty("stendhal.minetown", "true");

		en.step(player, "hi");
		assertEquals("Hm?", getReply(npc));
		en.step(player, "help");
		assertEquals("Zastanawiam się czy dzięki #balonikowi będę mógł się wznieść wystarczająco wysoko, aby dotknąć chmur...", getReply(npc));
		en.step(player, "quest");
		if (player.getQuest(questSlot) == null || player.getQuest(questSlot, 0).equals("rejected")) {
			assertEquals("Czy mógłbyś zdobyć dla mnie #'balonik'? Nim zaczną się dni miasta, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		} else {
			assertEquals("Mam nadzieję, że wkrótce zdobędziesz dla mnie #'balonik'. Chyba, że już jest święto zwane Mine Town Weeks, bo wtedy sam będę mógł zdobyć :)", getReply(npc));
		}
		en.step(player, "balloon");
		if (player.getQuest(questSlot) == null || player.getQuest(questSlot, 0).equals("rejected")) {
			assertEquals("Pewnego dnia będę miał wystarczająco dużo baloników, aby odlecieć!", getReply(npc));
		} else {
			assertEquals("Chmury podpowiedziały mi, że dni mine town wciąż trwają - Mogę sam zdobyć balonik. Wróć, gdy skończy się święto :)", getReply(npc));
		}
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		System.getProperties().remove("stendhal.minetown");
	}
}
