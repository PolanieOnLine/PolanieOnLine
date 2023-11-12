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
package games.stendhal.server.maps.magic.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ShopHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test buying scrolls.
 *
 * @author Martin Fuchs
 */
public class GreeterNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "-1_fado_great_cave_e3";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		setupZone(ZONE_NAME);
	}

	public GreeterNPCTest() {
		setNpcNames("Erodel Bmud");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new GreeterNPC(), ZONE_NAME);
	}

	/**
	 * Tests for hi and bye.
	 */
	@Test
	public void testHiAndByeSimple() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		assertNotNull(npc);
		final Engine en = npc.getEngine();

		// FIXME: this should be set up before test is run
		ShopHelper.initSeller("allscrolls");

		assertTrue(en.step(player, "hi"));
		String reply = getReply(npc);
		assertNotNull(reply);
		assertEquals("Witaj wędrowcze.", reply);

		assertTrue(en.step(player, "bye"));
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for hi and bye with NPC sure name.
	 */
	@Test
	public void testHiAndByeSureName() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		assertNotNull(npc);
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Erodel"));
		String reply = getReply(npc);
		assertNotNull(reply);
		assertEquals("Witaj wędrowcze.", reply);

		assertTrue(en.step(player, "bye"));
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for hiAndBye with full name.
	 */
	@Test
	public void testHiAndByeFullName() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		assertNotNull(npc);
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Erodel Bmud"));
		String reply = getReply(npc);
		assertNotNull(reply);
		assertEquals("Witaj wędrowcze.", reply);

		assertTrue(en.step(player, "bye"));
		assertEquals("Żegnaj.", getReply(npc));
	}

	/**
	 * Tests for buyScroll.
	 */
	@Test
	public void testBuyScroll() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Witaj wędrowcze.", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("Jestem czarodziejem jak każdy, który mieszka w tym podziemnym magicznym mieście. Praktykujemy tutaj #magię.", getReply(npc));

		assertTrue(en.step(player, "magic"));
		assertEquals("W rzeczywistości czary takie jak Sunlight Spell służą tutaj do utrzymania trawy i kwiatków. Wygląda na to, że zastanawiasz się dlaczego tradycyjni wrogowie tacy jak mroczne i zielone elfy żyją tutaj razem. Pozwól mi #wyjaśnić.", getReply(npc));

		assertTrue(en.step(player, "explain"));
		assertEquals("Jako miasto tylko dla czarodziei mamy dużo do nauczenia się od innych. Dlatego stare zwady są zapominane i dzięki temu żyjemy tutaj w pokoju.", getReply(npc));

		assertTrue(en.step(player, "quest"));
		assertEquals("Nikt nie może żyć, gdy inny przetrwał! Lord ciemności musi zginąć... nie... czekaj... to innym razem. Wybacz mi za zmylenie Ciebie. Niczego nie potrzebuję.", getReply(npc));

		assertTrue(en.step(player, "buy"));
		assertEquals("Powiedz mi co chcesz zrobić.", getReply(npc));

		assertTrue(en.step(player, "buy cat"));
		assertEquals("Nie sprzedaję caty.", getReply(npc));

		assertTrue(en.step(player, "buy someunknownthing"));
		assertEquals("Nie sprzedaję someunknownthingi.", getReply(npc));

		assertTrue(en.step(player, "buy a bottle of wine"));
		assertEquals("Nie sprzedaję wina.", getReply(npc));

		assertTrue(en.step(player, "buy zwój"));
		assertEquals("Jest więcej niż jeden zwój. Powiedz mi jaki rodzaj zwój chcesz użyć.", getReply(npc));

		assertTrue(en.step(player, "buy zwój przywołania"));
		assertEquals("Zwój przywołania kosztuje 300 monet. Chcesz to kupić?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy zwój przywołania"));
		assertEquals("Zwój przywołania kosztuje 300 monet. Chcesz to kupić?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		assertTrue(en.step(player, "buy dwa zwój przywołania"));
		assertEquals("2 zwój przywołania kosztuje 600 monet. Chcesz je kupić?", getReply(npc));

		// equip with enough money to buy the two scrolls
		assertTrue(equipWithMoney(player, 600));

		assertFalse(player.isEquipped("zwój przywołania"));
		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój zwój przywołania!", getReply(npc));
		assertTrue(player.isEquipped("zwój przywołania"));

		assertTrue(en.step(player, "buy zwój semos"));
		assertEquals("Zwój semos kosztuje 375 monet. Chcesz to kupić?", getReply(npc));

		assertTrue(equipWithMoney(player, 300));
		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		assertTrue(en.step(player, "buy zwój semos"));
		assertEquals("Zwój semos kosztuje 375 monet. Chcesz to kupić?", getReply(npc));

		// add another 75 coins to be able to buy the scroll
		assertTrue(equipWithMoney(player, 75));

		assertFalse(player.isEquipped("zwój semos"));
		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój zwój semos!", getReply(npc));
		assertTrue(player.isEquipped("zwój semos"));
	}

	/**
	 * Tests for sellScroll.
	 */
	@Test
	public void testSellScroll() {
		final SpeakerNPC npc = getNPC("Erodel Bmud");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Witaj wędrowcze.", getReply(npc));

		// There is not yet a trigger for selling things to Erodel
		assertFalse(en.step(player, "sell summon scroll"));
	}

}
