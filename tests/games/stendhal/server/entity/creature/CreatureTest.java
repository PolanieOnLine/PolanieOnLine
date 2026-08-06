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
package games.stendhal.server.entity.creature;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.CreatureTestHelper;

public class CreatureTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	/**
	 * Tests for getNearestEnemy.
	 */
	@Test
	public void testGetNearestEnemy() {

		final Player onebyone = PlayerTestHelper.createPlayer("bob");
		onebyone.setPosition(6, 0);
		final MockCreature sevenbyseven = new MockCreature();

		final StendhalRPZone zone = new StendhalRPZone("test", 20 , 20);
		zone.add(sevenbyseven);
		zone.add(onebyone);
		enemies.add(onebyone);
		assertSame(onebyone, sevenbyseven.getNearestEnemy(6));
		assertSame(onebyone, sevenbyseven.getNearestEnemy(5));
		assertNull(sevenbyseven.getNearestEnemy(4));

		sevenbyseven.setSize(7, 7);
		onebyone.setPosition(10, 10);
		assertSame(onebyone, sevenbyseven.getNearestEnemy(7));
		assertSame(onebyone, sevenbyseven.getNearestEnemy(6));
		assertSame(onebyone, sevenbyseven.getNearestEnemy(5));
		assertNull(sevenbyseven.getNearestEnemy(4));
	}


	private static List<RPEntity> enemies  = new LinkedList<RPEntity>();
	private static class MockCreature extends Creature {

		@Override
		public List<RPEntity> getEnemyList() {

			return enemies;
		}
	}

	/**
	 * Tests for hasTargetMoved.
	 */
	@Test
	public void testhasTargetMoved() {
		final StendhalRPZone zone = new StendhalRPZone("testzone");
		final Creature attacker = new Creature();

		final Creature attackTarget = new Creature();
		zone.add(attacker);
		zone.add(attackTarget);
		attacker.setTarget(attackTarget);
		assertFalse(attacker.hasTargetMoved());
		assertFalse(attacker.hasTargetMoved());
		attackTarget.setPosition(1, 0);
		assertTrue(attacker.hasTargetMoved());
		assertFalse(attacker.hasTargetMoved());
	}

	/**
	 * Tests for isAttackTurn.
	 */
	@Test
	public void testIsAttackTurn() {
		final Creature creature = new Creature();
		int counter = 0;
		for (int i = 0; i < 10; i++) {
			if (creature.isAttackTurn(i)) {
				counter++;
			}
		}
		assertThat(counter, is(2));
	}

	@Test
	public void testEachNonStackableDropIsCreatedIndependently() {
		final Creature creature = new Creature();
		creature.clearDropItemList();
		creature.addDropItem("test reward", 100.0, 3);
		final List<ItemCreationContext> contexts =
				new ArrayList<ItemCreationContext>();
		final EntityManager manager = (EntityManager) Proxy.newProxyInstance(
				EntityManager.class.getClassLoader(),
				new Class<?>[] {EntityManager.class}, (proxy, method, args) -> {
					if ("getItem".equals(method.getName()) && args.length == 2
							&& args[1] instanceof ItemCreationContext) {
						contexts.add((ItemCreationContext) args[1]);
						return new Item((String) args[0], "armor", "test",
								Collections.<String, String>emptyMap());
					}
					throw new UnsupportedOperationException(method.getName());
				});

		final List<Item> drops = creature.createDroppedItems(manager);
		assertEquals(3, drops.size());
		assertEquals(3, contexts.size());
		for (final ItemCreationContext context : contexts) {
			assertSame(ItemCreationContext.Source.DROP, context.getSource());
		}
		assertNotSame(drops.get(0), drops.get(1));
		assertNotSame(drops.get(1), drops.get(2));
	}

}
