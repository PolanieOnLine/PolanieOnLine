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
package games.stendhal.server.maps.kalavan.citygardens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Test buying lody.
 *
 * @author Martin Fuchs
 */
public class IceCreamSellerNPCTest extends ZonePlayerAndNPCTestImpl {

	private static final String ZONE_NAME = "0_kalavan_city_gardens";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();

		setupZone(ZONE_NAME);
	}

	public IceCreamSellerNPCTest() {
		setNpcNames("Sam");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new IceCreamSellerNPC(), ZONE_NAME);
	}

	/**
	 * Tests for hiAndBye.
	 */
	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("Sam");
		assertNotNull(npc);
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hello"));
		assertEquals("Cześć. Czy mogę #zaoferować Tobie porcję lodów?", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia. Ciesz się dniem!", getReply(npc));
	}

	/**
	 * Tests for buyIceCream.
	 */
	@Test
	public void testBuyIceCream() {
		final SpeakerNPC npc = getNPC("Sam");
		final Engine en = npc.getEngine();

		SingletonRepository.getShopList().configureNPC("Sam", "icecreamseller", true, true);

		assertTrue(en.step(player, "hi"));
		assertEquals("Cześć. Czy mogę #zaoferować Tobie porcję lodów?", getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("Sprzedaje pyszne lody.", getReply(npc));

		assertTrue(en.step(player, "offer"));
		assertEquals("Sprzedaję lody.", getReply(npc));

		assertTrue(en.step(player, "quest"));
		assertEquals("Prowadzę proste życie. Nie potrzebuję wiele do szczęścia.", getReply(npc));

		assertTrue(en.step(player, "buy"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));
		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy dog"));
		assertEquals("Nie sprzedaję dogi.", getReply(npc));

		assertTrue(en.step(player, "buy house"));
		assertEquals("Nie sprzedaję housa.", getReply(npc));

		assertTrue(en.step(player, "buy someunknownthing"));
		assertEquals("Nie sprzedaję someunknownthingi.", getReply(npc));

		assertTrue(en.step(player, "buy a bunch of socks"));
		assertEquals("Nie sprzedaję bunchy of socks.", getReply(npc));

		assertTrue(en.step(player, "buy 0 lody"));
		assertEquals("Ile lodów chcesz kupić?!", getReply(npc));

		assertTrue(en.step(player, "buy lody"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze w czym jeszcze mogę pomóc?", getReply(npc));

		assertTrue(en.step(player, "buy lody"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		// equip with enough money to buy two lodys
		assertTrue(equipWithMoney(player, 60));

		assertTrue(en.step(player, "buy trzy lody"));
		assertEquals("3 lody kosztuje 90. Chcesz kupić je?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!", getReply(npc));

		assertTrue(en.step(player, "buy lody"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));

		assertFalse(player.isEquipped("lody"));

		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój lody!", getReply(npc));
		assertTrue(player.isEquipped("lody", 1));

		assertTrue(en.step(player, "buy lody"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój lody!", getReply(npc));
		assertTrue(player.isEquipped("lody", 2));

		assertTrue(en.step(player, "buy 0 lody"));
		assertEquals("Ile lodów chcesz kupić?!", getReply(npc));

		// buying one lody by answering "yes" to npc's greeting
		assertTrue(equipWithMoney(player, 30));
		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia. Ciesz się dniem!", getReply(npc));
		assertTrue(en.step(player, "hi"));
		assertEquals("Cześć. Czy mogę #zaoferować Tobie porcję lodów?", getReply(npc));
		assertTrue(en.step(player, "yes"));
		assertEquals("lody kosztuje 30. Chcesz kupić to?", getReply(npc));
		assertTrue(en.step(player, "yes"));
		assertEquals("Gratulacje! Oto twój lody!", getReply(npc));
		assertTrue(player.isEquipped("lody", 3));
	}

	/**
	 * Tests for sellIceCream.
	 */
	@Test
	public void testSellIceCream() {
		final SpeakerNPC npc = getNPC("Sam");
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hi Sam"));
		assertEquals("Cześć. Czy mogę #zaoferować Tobie porcję lodów?", getReply(npc));

		// Currently there are no response to sell sentences for Sam.
		assertFalse(en.step(player, "sell"));
	}

}
