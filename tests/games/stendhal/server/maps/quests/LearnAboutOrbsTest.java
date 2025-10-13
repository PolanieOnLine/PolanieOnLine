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
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.semos.temple.HealerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class LearnAboutOrbsTest {

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
		new HealerNPC().configureZone(zone, null);


		AbstractQuest quest = new LearnAboutOrbs();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@Test
	public void testQuestAppropriateLevel() {
		long before = player.getXP();
		player.setLevel(11);
		npc = SingletonRepository.getNPCList().get("Ilisa");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "quest");
		assertEquals("Pewne kule mają specjalne właściwości. Mogłabym Cię nauczyć jak #używać kuli jak ta co leży na stole.", getReply(npc));
		en.step(player, "no");
		en.step(player, "use");
		assertEquals("Naciśnij prawy przycisk i wybierz Użyj. Dostałeś jakąś wiadomość?", getReply(npc));
		en.step(player, "no");
		assertEquals("Cóż, musisz stanąć obok tej kuli. Zbliż się, czy dostałeś teraz wiadomość?", getReply(npc));
		en.step(player, "no");
		assertEquals("Cóż, musisz stanąć obok tej kuli. Zbliż się, czy dostałeś teraz wiadomość?", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "quest");
		assertEquals("Pewne kule mają specjalne właściwości. Mogłabym Cię nauczyć jak #używać kuli jak ta co leży na stole.", getReply(npc));
		en.step(player, "use");
		assertEquals("Naciśnij prawy przycisk i wybierz Użyj. Dostałeś jakąś wiadomość?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Jesteś naturalny! Teraz jak nauczyłeś się korzystać z kuli to możesz się przenieść do miejsca pełnego magi. Nie używaj go dopóki nie będziesz mógł znaleźć drogi powrotnej!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "task");
		assertEquals("Mogę Ci przypomnieć jak #używać kuli.", getReply(npc));
		en.step(player, "use");
		assertEquals("Naciśnij prawy przycisk na kuli i wybierz Użyj.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
		assertEquals(player.getQuest(new LearnAboutOrbs().getSlotName()),"done");
		long xpAfterQuest = before + 50;
		assertEquals(player.getXP(), xpAfterQuest);
	}

	@Test
	public void testQuestTooLowLevel() {
		player.setLevel(1);
		npc = SingletonRepository.getNPCList().get("Ilisa");
		en = npc.getEngine();
		en.step(player, "hi");
		assertEquals("Pozdrawiam! W czym mogę pomóc?", getReply(npc));
		en.step(player, "quest");
		assertEquals("Pewne kule mają specjalne właściwości. Mogłabym Cię nauczyć jak #używać kuli jak ta co leży na stole.", getReply(npc));
		en.step(player, "no");
		en.step(player, "use");
		assertEquals("Aha... Dostałam wiadomość, że wciąż jesteś tutaj nowy. Może wróć później, gdy będziesz miał więcej doświadczenia. Na razie jeżeli potrzebujesz #pomocy to pytaj!", getReply(npc));
	en.step(player, "bye");
	assertEquals("Do widzenia.", getReply(npc));
	}
}
