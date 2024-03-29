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
package games.stendhal.server.entity.item;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class WeddingRingTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();

		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("int_semos_guard_house", 100, 100));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		PlayerTestHelper.removeAllPlayers();
	}

	/**
	 * Tests for describe.
	 */
	@Test
	public void testDescribe() {
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		assertThat(ring.describe(), is("Oto §'obrączka ślubna'."));
		ring.setItemData("juliet");
		assertThat(ring.describe(), is("Oto §'obrączka ślubna'. Wygrawerowano na nim: \"W imię wiecznej miłości dla juliet\"."));
	}

	/**
	 * Test when player attempts to use ring without equipping it to slot.
	 */
	@Test
	public void testOnUsedOnGround() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		assertFalse(ring.onUsed(romeo));
		assertEquals("Powinieneś podnieść swoją obrączkę ślubną, by go użyć.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for onUsedNotMarried.
	 */
	@Test
	public void testOnUsedNotMarried() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		romeo.equip("finger", ring);

		assertFalse(ring.onUsed(romeo));
		assertEquals("Oto obrączka ślubna, która jeszcze nie została wygrawerowana imieniem ukochanej osoby.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for onUsedNotOnline.
	 */
	@Test
	public void testOnUsedNotOnline() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		ring.setItemData("juliet");
		romeo.equip("finger", ring);
		assertFalse(ring.onUsed(romeo));
		assertEquals("juliet nie ma w grze.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for onUsedOnlineButNotWearingTheRing.
	 */
	@Test
	public void testOnUsedOnlineButNotWearingTheRing() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		PlayerTestHelper.registerPlayer(juliet);

		ring.setItemData("juliet");
		romeo.equip("finger", ring);
		assertFalse(ring.onUsed(romeo));
		assertEquals("juliet nie nosi pierścionka ślubnego.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for onUsedOnlineButEngaged.
	 */
	@Test
	public void testOnUsedOnlineButEngaged() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(juliet);

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		juliet.equipToInventoryOnly(ring2);

		assertFalse(ring.onUsed(romeo));

		assertEquals("Przepraszam, ale juliet rozwiódł się z tobą i jest teraz zaręczony z kimś innym.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for onUsedOnlineButRemarried.
	 */
	@Test
	public void testOnUsedOnlineButRemarried() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(juliet);

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("paris");
		juliet.equipToInventoryOnly(ring2);

		assertFalse(ring.onUsed(romeo));

		assertEquals("Przepraszam, ale juliet rozwiódł się z tobą i jest teraz zaręczony z kimś innym.", romeo.events().get(0).get("text"));
	}

	/**
	 * Tests for noTeleportOut.
	 */
	@Test
	public void testNoTeleportOut() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "int_semos_guard_house");

		final StendhalRPZone zone = (StendhalRPZone) MockStendlRPWorld.get().getRPZone("int_semos_guard_house");
		zone.disallowOut();

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		assertFalse(ring.onUsed(romeo));
		assertEquals(romeo.events().get(0).get("text"), "Silna antymagiczna aura w tym obszarze blokuje działanie pierścionka ślubnego!");
		// no such thing as removing teleport restrictions
		MockStendlRPWorld.get().removeZone(zone);
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("int_semos_guard_house", 100, 100));
	}

	/**
	 * Tests for noTeleportIn.
	 */
	@Test
	public void testNoTeleportIn() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "int_semos_guard_house");

		final StendhalRPZone zone = (StendhalRPZone) MockStendlRPWorld.get().getRPZone("int_semos_guard_house");
		zone.disallowIn();

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		assertFalse(ring.onUsed(romeo));
		assertEquals(romeo.events().get(0).get("text"), "Silna antymagiczna aura w docelowym obszarze blokuje działanie pierścionka ślubnego!");
		// no such thing as removing teleport restrictions
		MockStendlRPWorld.get().removeZone(zone);
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("int_semos_guard_house", 100, 100));
	}

	/**
	 * Tests for notVisited.
	 */
	@Test
	public void testNotVisited() {
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("moon", 10, 10));
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "moon");

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		assertFalse(ring.onUsed(romeo));
		assertEquals(romeo.events().get(0).get("text"), "Słyszałeś wiele plotek o miejscu docelowym. Nie możesz dołączyć do juliet ponieważ znajduje się w nieznany dla Ciebie miejscu.");
	}

	/**
	 * Tests for onUsedSuccesfull.
	 */
	@Test
	public void testOnUsedSuccesfull() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "int_semos_guard_house");

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		assertTrue(ring.onUsed(romeo));
	}

	/**
	 * Tests for coolingTime.
	 */
	@Test
	public void testCoolingTime() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "int_semos_guard_house");

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		assertTrue(ring.onUsed(romeo));
		assertFalse(ring.onUsed(romeo));
		assertTrue(romeo.events().get(0).get("text").startsWith("Pierścień jeszcze nie odzyskał w pełni swojej mocy."));
	}

	/**
	 * Tests for coolingTimePassed.
	 */
	@Test
	public void testCoolingTimePassed() {
		final Player romeo = PlayerTestHelper.createPlayer("romeo");
		final Player juliet = PlayerTestHelper.createPlayer("juliet");
		PlayerTestHelper.registerPlayer(romeo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(juliet, "int_semos_guard_house");

		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring.setItemData("juliet");
		romeo.equip("finger", ring);

		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		ring2.setItemData("romeo");
		juliet.equipToInventoryOnly(ring2);

		ring.onUsed(romeo);
		// a time well in the past
		ring.put("amount", 0);
		assertTrue(ring.onUsed(romeo));
	}

	/**
	 * Tests for addToSlotUnmarked.
	 */
	@Test
	public void testAddToSlotUnmarked() {
		final Player frodo = PlayerTestHelper.createPlayer("frodo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		frodo.equip("bag", ring);
		frodo.equip("bag", ring2);

		assertNotNull(frodo.getAllEquipped("obrączka ślubna"));
		assertEquals(frodo.getAllEquipped("obrączka ślubna").size(), 2);
	}

	/**
	 * Tests for addToSlotOneMarked.
	 */
	@Test
	public void testAddToSlotOneMarked() {
		final Player frodo = PlayerTestHelper.createPlayer("frodo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		ring.setBoundTo("frodo");
		frodo.equip("bag", ring);
		frodo.equip("bag", ring2);

		assertNotNull(frodo.getAllEquipped("obrączka ślubna"));
		assertEquals(frodo.getAllEquipped("obrączka ślubna").size(), 2);
	}

	/**
	 * Tests for addToSlotTwoMarkedSame.
	 */
	@Test
	public void testAddToSlotTwoMarkedSame() {
		final Player frodo = PlayerTestHelper.createPlayer("frodo");
		final Player galadriel = PlayerTestHelper.createPlayer("galadriel");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		final WeddingRing ring3 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		PlayerTestHelper.registerPlayer(frodo, "int_semos_guard_house");
		PlayerTestHelper.registerPlayer(galadriel, "int_semos_guard_house");

		ring.setBoundTo("frodo");
		ring2.setBoundTo("frodo");
		ring2.setItemData("galadriel");
		frodo.equip("bag", ring);
		frodo.equip("bag", ring2);

		assertNotNull(frodo.getAllEquipped("obrączka ślubna"));
		assertEquals("one should be destroyed", frodo.getAllEquipped("obrączka ślubna").size(), 1);

		ring3.setItemData("frodo");
		galadriel.equipToInventoryOnly(ring3);

		assertFalse(((WeddingRing) frodo.getFirstEquipped("obrączka ślubna")).onUsed(frodo));
		assertTrue("Should use up the energy at destruction", frodo.events().get(0).get("text").startsWith("Pierścień jeszcze nie odzyskał w pełni swojej mocy."));
	}

	/**
	 * Tests for addToSlotTwoMarkedDifferent.
	 */
	@Test
	public void testAddToSlotTwoMarkedDifferent() {
		final Player frodo = PlayerTestHelper.createPlayer("frodo");
		final WeddingRing ring = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");
		final WeddingRing ring2 = (WeddingRing) SingletonRepository.getEntityManager().getItem("obrączka ślubna");

		ring.setBoundTo("frodo");
		ring2.setBoundTo("gollum");
		frodo.equip("bag", ring);
		frodo.equip("bag", ring2);

		assertNotNull(frodo.getAllEquipped("obrączka ślubna"));
		assertEquals(frodo.getAllEquipped("obrączka ślubna").size(), 2);
	}
}
