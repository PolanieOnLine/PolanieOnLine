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
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.area.Allotment;
import games.stendhal.server.entity.mapstuff.spawner.FlowerGrower;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.GrowingPassiveEntityRespawnPointTestHelper;

public class SeedTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		GrowingPassiveEntityRespawnPointTestHelper.generateRPClasses();
	}

	/**
	 * Tests for execute.
	 */
	@Test
	public void testExecute() {
		final Seed seed = (Seed) SingletonRepository.getEntityManager().getItem("nasiona");
		final Player player = PlayerTestHelper.createPlayer("bob");
		assertNotNull(player);
		final StendhalRPZone zone = new StendhalRPZone("zone");
		SingletonRepository.getRPWorld().addRPZone(zone);
		final Allotment all = new Allotment();
		all.setPosition(1, 0);
		all.setSize(20, 20);
		zone.add(all);
		zone.add(player);

		assertNotNull(seed);
		zone.add(seed);
		seed.setPosition(1, 0);

		assertTrue(seed.onUsed(player));

		FlowerGrower grower = null;
		for (final Entity ent: player.getZone().getEntitiesAt(1, 0)) {
			if (ent instanceof FlowerGrower) {
				grower = (FlowerGrower) ent;
				break;
			}
		}
		assertNotNull(grower);
	}


	/**
	 * Tests for executeSeedInBag.
	 */
	@Test
	public void testExecuteSeedInBag() {
		final Seed seed = (Seed) SingletonRepository.getEntityManager().getItem("nasiona");
		final Player player = PlayerTestHelper.createPlayer("bob");
		assertNotNull(player);
		final StendhalRPZone zone = new StendhalRPZone("zone");
		SingletonRepository.getRPWorld().addRPZone(zone);
		final Allotment all = new Allotment();
		all.setPosition(0, 0);
		all.setSize(20, 20);
		zone.add(all);
		zone.add(player);

		assertNotNull(seed);
		player.equip("bag", seed);

		assertFalse(seed.onUsed(player));
	}

	/**
	 * Tests for executeNonameSeed.
	 */
	@Test
	public void testExecuteNonameSeed() {
		final Seed seed = (Seed) SingletonRepository.getEntityManager().getItem("nasiona");
		final Player player = PlayerTestHelper.createPlayer("bob");
		assertNotNull(player);
		final StendhalRPZone zone = new StendhalRPZone("zone");
		SingletonRepository.getRPWorld().addRPZone(zone);
		final Allotment all = new Allotment();
		all.setPosition(0, 0);
		all.setSize(20, 20);
		zone.add(all);
		zone.add(player);

		assertNotNull(seed);
		zone.add(seed);
		seed.setPosition(1, 0);

		assertTrue(seed.onUsed(player));

		FlowerGrower flg = null;
		for (final Entity ent: player.getZone().getEntitiesAt(1, 0)) {
			if (ent instanceof FlowerGrower) {
				flg = (FlowerGrower) ent;
				break;
			}
		}

		assertNotNull(flg);
		flg.setToFullGrowth();
		flg.onUsed(player);
		assertFalse(player.getZone().getEntitiesAt(1, 0).contains(flg));
		assertTrue(player.isEquipped("lilia"));
	}

	/**
	 * Tests for executeDaisiesSeed.
	 */
	@Test
	public void testExecuteDaisiesSeed() {
		final Player player = PlayerTestHelper.createPlayer("bob");
		assertNotNull(player);
		final StendhalRPZone zone = new StendhalRPZone("zone");
		SingletonRepository.getRPWorld().addRPZone(zone);
		final Allotment all = new Allotment();
		all.setPosition(0, 0);
		all.setSize(20, 20);
		zone.add(all);
		zone.add(player);

		final Seed seed = (Seed) SingletonRepository.getEntityManager().getItem("nasiona");
		assertNotNull(seed);
		seed.setItemData("stokrotki");
		zone.add(seed);
		seed.setPosition(1, 0);

		assertTrue(seed.onUsed(player));

		FlowerGrower flg = null;
		for (final Entity ent: player.getZone().getEntitiesAt(1, 0)) {
			if (ent instanceof FlowerGrower) {
				flg = (FlowerGrower) ent;
				break;
			}
		}

		assertNotNull(flg);
		flg.setToFullGrowth();
		flg.onUsed(player);
		assertFalse(player.getZone().getEntitiesAt(1, 0).contains(flg));
		assertTrue("player has stokrotki", player.isEquipped("stokrotki"));
	}

	@Test
	public void testSeedInfo() {
		// seeds
		final Seed base_seed = (Seed) SingletonRepository.getEntityManager().getItem("nasiona");
		assertNotNull(base_seed);
		assertEquals("Oto nasiona. Można sadzić w żyznej glebie, gdzie będzie mogło rosnąć.",
				base_seed.describe());
		assertEquals("seed", base_seed.get("subclass"));

		for (final String flower_name: Arrays.asList("stokrotek", "lilii", "bratków")) {
			final Seed seed = new Seed(base_seed);
			seed.setItemData(flower_name);
			final String seed_name = "nasiona " + flower_name;
			assertEquals("Oto " + seed_name
					+ ". Można sadzić w żyznej glebie, gdzie będzie mogło rosnąć.", seed.describe());
			if ("stokrotki".equals(flower_name)) {
				assertEquals("seed", seed.get("subclass"));
			} else {
				assertEquals("seed_" + flower_name.replaceFirst("stokrotki", "daisies"), seed.get("subclass"));
			}
		}

		// bulbs
		final Seed base_bulb = (Seed) SingletonRepository.getEntityManager().getItem("bulwa");
		assertNotNull(base_bulb);
		assertEquals("Oto bulwa. Można sadzić w żyznej glebie, gdzie będzie mogło rosnąć.",
				base_bulb.describe());
		assertEquals("bulwa", base_bulb.get("subclass"));

		for (final String flower_name: Arrays.asList("bielikrasy")) {
			final Seed bulb = new Seed(base_bulb);
			bulb.setItemData(flower_name);
			final String bulb_name = "bulwa " + flower_name;
			assertEquals("Oto " + bulb_name
					+ ". Można sadzić w żyznej glebie, gdzie będzie mogło rosnąć.", bulb.describe());
			if ("bielikrasa".equals(flower_name)) {
				assertEquals("bulb", bulb.get("subclass"));
			} else {
				assertEquals("bulb_" + flower_name.replaceFirst("bielikrasa", "zantedeschia"), bulb.get("subclass"));
			}
		}
	}
}
