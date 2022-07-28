/* $Id$ */
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
import games.stendhal.server.maps.nalwor.city.GuessKillsNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;
import utilities.RPClass.CreatureTestHelper;

public class GuessKillsTest extends ZonePlayerAndNPCTestImpl {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;
	private static final String ZONE_NAME = "0_nalwor_city";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public GuessKillsTest() {
		super(ZONE_NAME, "Crearid");
	}

	@Override
	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone(ZONE_NAME);
		new GuessKillsNPC().configureZone(zone, null);

		// Add creature
		SingletonRepository.getEntityManager().getCreature("jeleń");
		quest = new GuessKills();
		quest.addToWorld();

		questSlot = quest.getSlotName();

		player = PlayerTestHelper.createPlayer("player");
	}

	@Test
	public void testQuest() {
		npc = SingletonRepository.getNPCList().get("Crearid");
		CreatureTestHelper.generateRPClasses();
		en = npc.getEngine();

		//Test default responses and if player does not meet requirement
		en.step(player, "hi");
		assertEquals("Pozdrawiam.", getReply(npc));
		en.step(player, "play");
		assertEquals("Lubię się rozerwać, ale nie wyglądasz na takiego co jest gotowy. Wróć, gdy zdobędziesz trochę doświadczenia w walce z potworami.", getReply(npc));
		en.step(player, "job");
		assertEquals("Jestem tylko starą kobietą obserwującą wszystkich podczas spaceru.", getReply(npc));
		en.step(player, "help");
		assertEquals("Nie wiem jak tobie pomóc. Od paru dni lubię #grać w #gry.", getReply(npc));
		en.step(player, "play gamEs");
		assertEquals("Lubię się rozerwać, ale nie wyglądasz na takiego co jest gotowy. Wróć, gdy zdobędziesz trochę doświadczenia w walce z potworami.", getReply(npc));
		en.step(player, "Play");
		assertEquals("Lubię się rozerwać, ale nie wyglądasz na takiego co jest gotowy. Wróć, gdy zdobędziesz trochę doświadczenia w walce z potworami.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia słonko.", getReply(npc));

		// Give player enough kills
		player.setKeyedSlot("!kills", "solo.jeleń", "1001");

		// Test quest offer if meets requirements
		en.step(player, "hi");
		assertEquals("Pozdrawiam.", getReply(npc));
		en.step(player, "play");
		assertEquals("Teraz trochę się nudzę. Czy chciałbyś zagrać ze mną?", getReply(npc));
		en.step(player, "yes");
		// Deer was added in setup(), so we know that's what we get
		assertEquals("Liczyłam ile potworów zabiłeś, a teraz powiedz mi ile jelenie myślisz, że zabiłeś? Masz trzy próby, a ja zaakceptuję próbę, która jest blisko poprawnej odpowiedzi.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia. Wróć, gdy będziesz chciał kontynuować.", getReply(npc));

		// Add other creature to test if NPC remembers old creature
		SingletonRepository.getEntityManager().getCreature("szczur");
		player.setKeyedSlot("!kills", "solo.szczur", "10");

		// Leave quest early and come back, get guess close
		en.step(player, "hi");
		assertEquals("Pozdrawiam.", getReply(npc));
		en.step(player, "play");
		assertEquals("Nie skończyliśmy ostatniej gry czy chcesz ją kontynuować?", getReply(npc));
		en.step(player, "no");
		assertEquals("Cóż, no to przegrałeś. W czym jeszcze mogłabym Ci pomóc?", getReply(npc));
		en.step(player, "play");
		assertEquals("Nie skończyliśmy ostatniej gry czy chcesz ją kontynuować?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Zobaczmy... zostało Tobie 3 próby... i jeśli dobrze pamiętam to zapytałam się ciebie... ile jelenie myślisz, że zabiłeś?", getReply(npc));
		en.step(player, "8");
		assertEquals("Nie to nie jest prawidłowa odpowiedź. Spróbuj ponownie.", getReply(npc));
		en.step(player, "5");
		assertEquals("Znów źle. Masz jeszcze jedną próbę.", getReply(npc));
		en.step(player, "981");
		assertEquals("Łał było blikso. Dobra robota!", getReply(npc));

		// Reset quest because of timestamp
		player.setQuest(questSlot, "done;0;");

		// Test bogus answers and exact answer
		en.step(player, "play");
		assertEquals("Teraz trochę się nudzę. Czy chciałbyś zagrać ze mną?", getReply(npc));
		en.step(player, "yes");

		String reply = getReply(npc);
		assertEquals(reply.startsWith("Liczyłam ile potworów zabiłeś, a teraz powiedz mi ile "), true);
		assertEquals(reply.endsWith(" myślisz, że zabiłeś? Masz trzy próby, a ja zaakceptuję próbę, która jest blisko poprawnej odpowiedzi."), true);

		en.step(player, "sdf");
		assertEquals("Jak to możliwe, że to może być odpowiedź? Podaj mi liczbę.", getReply(npc));

		if (reply.contains("szczur")) {
			en.step(player, "10");
		} else {
			en.step(player, "1001");
		}

		assertEquals("Zdumiewające! To dokładna liczba! Jesteś szczęściarzem lub naprawdę zwracasz na to uwagę.", getReply(npc));

		// Reset quest because of timestamp
		player.setQuest(questSlot, "done;0;");

		// Test other bogus answers
		en.step(player, "play");
		assertEquals("Teraz trochę się nudzę. Czy chciałbyś zagrać ze mną?", getReply(npc));
		en.step(player, "yes");

		reply = getReply(npc);
		assertEquals(reply.startsWith("Liczyłam ile potworów zabiłeś, a teraz powiedz mi ile "), true);
		assertEquals(reply.endsWith(" myślisz, że zabiłeś? Masz trzy próby, a ja zaakceptuję próbę, która jest blisko poprawnej odpowiedzi."), true);

		en.step(player, "98");
		assertEquals("Nie to nie jest prawidłowa odpowiedź. Spróbuj ponownie.", getReply(npc));
		en.step(player, "o");
		assertEquals("Czy to możliwe? Podaj prawidłową odpowiedź.", getReply(npc));
		en.step(player, "023");
		assertEquals("Znów źle. Masz jeszcze jedną próbę.", getReply(npc));
		en.step(player, "88");

		reply = getReply(npc);
		assertEquals(reply.startsWith("Niestety jest to nieprawidłowa liczba. Poprawna odpowiedź jest w tym regionie "), true);
		assertEquals(reply.endsWith("Wysiliłeś się."), true);

		en.step(player, "play");
		assertEquals(getReply(npc).startsWith("Nieźle się bawiłam. Dziękuję! Wróć powiedzmy za "), true);
	}
}
