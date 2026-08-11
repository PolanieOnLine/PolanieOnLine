/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.net.OutputSerializer;
import utilities.RPClass.CreatureTestHelper;

public class CreatureRespawnPointCombatStatTest {
	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void randomizedCombatStatsAreClampedToShortAttributeRange() {
		assertEquals(0, CreatureRespawnPoint.clampRespawnCombatStat(-1));
		assertEquals(Short.MAX_VALUE,
				CreatureRespawnPoint.clampRespawnCombatStat(Integer.MAX_VALUE));
	}

	@Test
	public void creatureAtShortMaxRespawnsAndRemainsSerializable() throws IOException {
		final StendhalRPZone zone = new StendhalRPZone("short_max_respawn", 20, 20);
		final Creature prototype = createPrototype();
		final OverflowingCombatStatRespawnPoint point =
				new OverflowingCombatStatRespawnPoint(zone, prototype);

		point.respawnForTest();

		assertEquals(1, point.size());
		final Creature spawned = point.getLastSpawnedCreature();
		assertEquals(Short.MAX_VALUE, spawned.getAtk());
		assertEquals(Short.MAX_VALUE, spawned.getDef());
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		spawned.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
	}

	private Creature createPrototype() {
		final Creature creature = new Creature();
		creature.setEntityClass("animal");
		creature.setEntitySubclass("short_max_test");
		creature.setName("testowy potwór graniczny");
		creature.setDescription("Potwór używany przez test odradzania.");
		creature.setSize(1, 1);
		creature.setBaseHP(100);
		creature.setHP(100);
		creature.setAtk(Short.MAX_VALUE);
		creature.setDef(Short.MAX_VALUE);
		creature.setLevel(1);
		creature.setXP(1);
		return creature;
	}

	private static final class OverflowingCombatStatRespawnPoint
			extends CreatureRespawnPoint {
		private OverflowingCombatStatRespawnPoint(final StendhalRPZone zone,
				final Creature prototype) {
			super(zone, 5, 5, prototype, 1);
		}

		@Override
		int rollRespawnCombatStat(final int baseValue) {
			return Integer.MAX_VALUE;
		}

		private void respawnForTest() {
			respawn();
		}

		private Creature getLastSpawnedCreature() {
			return creatures.get(creatures.size() - 1);
		}
	}
}
