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
package games.stendhal.server.maps.kirdneh.city;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test buying roses.
 * @author Martin Fuchs
 */
public class FlowerSellerNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "int_ados_felinas_house";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public FlowerSellerNPCTest() {
		setNpcNames("Fleur");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new FlowerSellerNPC(), ZONE_NAME);
	}

	/**
	 * Tests for hiAndBye.
	 */
	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("Fleur");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Fleur"));
		assertEquals("Cześć! Przyszedłeś tutaj #pohandlować?", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia i zapraszam ponownie!", getReply(npc));
	}

	/**
	 * Tests for buyFlower.
	 */
	@Test
	public void testBuyFlower() {
		final SpeakerNPC npc = getNPC("Fleur");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi"));
		assertEquals("Cześć! Przyszedłeś tutaj #pohandlować?", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("Sprzedaję tutaj róże.", getReply(npc));

		assertTrue(en.step(player, "trade"));
		assertEquals("Sprzedaję róża.", getReply(npc));

		// There is currently no quest response defined for Fleur.
		assertFalse(en.step(player, "quest"));

		assertTrue(en.step(player, "buy"));
		assertEquals("róża kosztuje 50. Chcesz kupić to?", getReply(npc));
		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy dog"));
		assertEquals("Nie sprzedaję dogi.", getReply(npc));

		assertTrue(en.step(player, "buy candle"));
		assertEquals("Nie sprzedaję candla.", getReply(npc));

		assertTrue(en.step(player, "buy a glass of wine"));
		assertEquals("Nie sprzedaję wina.", getReply(npc));

		assertTrue(en.step(player, "buy róża"));
		assertEquals("róża kosztuje 50. Chcesz kupić to?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy róża"));
		assertEquals("róża kosztuje 50. Chcesz kupić to?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		assertTrue(en.step(player, "buy dwa róża"));
		assertEquals("2 róże kosztuje 100. Chcesz kupić je?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		// equip with enough money to buy one rose
		assertTrue(equipWithMoney(player, 50));
		assertTrue(en.step(player, "buy róża"));
		assertEquals("róża kosztuje 50. Chcesz kupić to?", getReply(npc));

		assertFalse(player.isEquipped("róża"));

		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój róża!", getReply(npc));

		assertTrue(player.isEquipped("róża"));

		// equip with enough money to buy five roses
		assertTrue(equipWithMoney(player, 250));
		assertTrue(en.step(player, "buy 5 róża"));
		assertEquals("5 róże kosztuje 250. Chcesz kupić je?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój róża!", getReply(npc));

		assertTrue(player.isEquipped("róża", 6));
		assertFalse(player.isEquipped("róża", 7));
	}

}
