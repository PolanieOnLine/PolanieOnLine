/* $Id$ */
/***************************************************************************
 *                     (C) Copyright 2011 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.kalavan.cottage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.fsm.Engine;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

/**
 * Tests for the Granny Graham NPC.
 *
 * @author Martin Fuchs
 */
public class HouseKeeperNPCTest extends ZonePlayerAndNPCTestImpl {
	private static final String ZONE_NAME = "0_kalavan_city_gardens";
	private static final String QUEST_SLOT = "granny_brew_tea";
	
	private static String greetings = "Cześć. W czymś mogłabym #pomóc?";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		setupZone(ZONE_NAME);
	}

	public HouseKeeperNPCTest() {
		setNpcNames("babcia Graham");
		setZoneForPlayer(ZONE_NAME);
		addZoneConfigurator(new HouseKeeperNPC(), ZONE_NAME);
	}

	/**
	 * Tests for hiAndBye.
	 */
	@Test
	public void testHiAndBye() {
		final SpeakerNPC npc = getNPC("babcia Graham");
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mleko", 1);
		requiredResources.put("miód", 1);

		SingletonRepository.getProducerRegister().configureNPC(
			npc.getName(), new ProducerBehaviour(QUEST_SLOT, Arrays.asList("brew"), "filiżanka herbaty", requiredResources, 3*60), greetings);

		assertNotNull(npc);
		final Engine en = npc.getEngine();

		assertTrue(en.step(player, "hello"));
		assertEquals(greetings, getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for MakeTea.
	 */
	@Test
	public void testMakeTea() {
		final SpeakerNPC npc = getNPC("babcia Graham");
		final Engine en = npc.getEngine();

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mleko", 1);
		requiredResources.put("miód", 1);

		SingletonRepository.getProducerRegister().configureNPC(
			npc.getName(), new ProducerBehaviour(QUEST_SLOT, Arrays.asList("brew"), "filiżanka herbaty", requiredResources, 0), greetings);

		assertTrue(en.step(player, "hi"));
		assertEquals(greetings, getReply(npc));

		assertTrue(en.step(player, "job"));
		assertEquals("Jestem gospodynią domową. Mogę zaparzyć filiżankę świeżej #herbaty o ile chcesz. Powiedz tylko #zaparz.", getReply(npc));

		assertTrue(en.step(player, "offer"));
		assertEquals("Zaparzę Tobie filiżankę #herbaty o ile chcesz. Powiedz tylko #zaparz.", getReply(npc));

		assertTrue(en.step(player, "quest"));
		assertEquals("Mam ból głowy i małą Annie, która za każdym razem jak schodzi to hałasuje. Może mógłbyś dać jej jakieś zajęcie? ... tak, aby się uciszyła ...", getReply(npc));

		assertTrue(en.step(player, "herbaty"));
		assertEquals("To najlepszy napój. Słodzę ją miodem. Powiedz #'zaparz filiżanka herbaty' o ile będziesz chciał.", getReply(npc));

		assertTrue(en.step(player, "brew"));
		assertEquals("Mogę zrobić filiżanka herbaty jeżeli przyniesiesz mi 1 #miód oraz 1 #mleko.", getReply(npc));

		assertTrue(en.step(player, "mleko"));
		assertEquals("Cóż spodziewam się, że zdobędziesz mleko z farmy.", getReply(npc));

		assertTrue(en.step(player, "miód"));
		assertEquals("Nie znasz pszczelarza z lasu Fado?", getReply(npc));

		assertFalse(player.isEquipped("filiżanka herbaty"));

		PlayerTestHelper.equipWithItem(player, "mleko");
		PlayerTestHelper.equipWithItem(player, "miód");

		assertTrue(en.step(player, "brew"));
		assertEquals("Potrzebuję, abyś przyniósł mi 1 #miód oraz 1 #mleko do tej pracy, która zajmie 3 minuty. Posiadasz to przy sobie?", getReply(npc));

		assertTrue(en.step(player, "no"));
		assertEquals("Dobrze, nie ma problemu.", getReply(npc));

		assertTrue(en.step(player, "brew"));
		assertEquals("Potrzebuję, abyś przyniósł mi 1 #miód oraz 1 #mleko do tej pracy, która zajmie 3 minuty. Posiadasz to przy sobie?", getReply(npc));

		assertTrue(en.step(player, "yes"));
		assertEquals("Dobrze zrobię dla Ciebie filiżanka herbaty, ale zajmie mi to trochę czasu. Wróć za 3 minuty.", getReply(npc));
		assertFalse(player.isEquipped("filiżanka herbaty"));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));

		// wait one minute
		setPastTime(player, QUEST_SLOT, 2, 1*60);

		assertTrue(en.step(player, "hi"));
		assertEquals("Witaj z powrotem! Wciąż zajmuje się twoim zleceniem filiżanka herbaty. Wróć za 2 minuty, aby odebrać.", getReply(npc));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));

		// wait three minutes
		setPastTime(player, QUEST_SLOT, 2, 3*60);

		assertTrue(en.step(player, "hi"));
		assertEquals("Witaj z powrotem! Skończyłam twoje zlecenie. Trzymaj, oto filiżanka herbaty.", getReply(npc));

		assertTrue(player.isEquipped("filiżanka herbaty", 1));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for buying.
	 */
	@Test
	public void testBuy() {
		final SpeakerNPC npc = getNPC("babcia Graham");
		final Engine en = npc.getEngine();

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mleko", 1);
		requiredResources.put("miód", 1);

		SingletonRepository.getProducerRegister().configureNPC(
			npc.getName(), new ProducerBehaviour(QUEST_SLOT, Arrays.asList("brew"), "filiżanka herbaty", requiredResources, 0), greetings);

		assertTrue(en.step(player, "hi babcia Graham"));
		assertEquals(greetings, getReply(npc));

		// Currently there are no response to buy sentences for Granny Graham.
		assertFalse(en.step(player, "buy"));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

	/**
	 * Tests for selling.
	 */
	@Test
	public void testSell() {
		final SpeakerNPC npc = getNPC("babcia Graham");
		final Engine en = npc.getEngine();

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mleko", 1);
		requiredResources.put("miód", 1);

		SingletonRepository.getProducerRegister().configureNPC(
			npc.getName(), new ProducerBehaviour(QUEST_SLOT, Arrays.asList("brew"), "filiżanka herbaty", requiredResources, 0), greetings);

		assertTrue(en.step(player, "hi babcia Graham"));
		assertEquals(greetings, getReply(npc));

		// Currently there are no response to sell sentences for Granny Graham.
		assertFalse(en.step(player, "sell"));

		assertTrue(en.step(player, "bye"));
		assertEquals("Do widzenia.", getReply(npc));
	}

}
