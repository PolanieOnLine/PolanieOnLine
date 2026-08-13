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
package games.stendhal.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects.GameObjectListener;
import games.stendhal.client.entity.IEntity;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;

/** Tests cleanup behavior used when changing the active character. */
public class GameObjectsCharacterSessionTest {
	private GameObjects gameObjects;
	private TrackingListener listener;

	@BeforeClass
	public static void setUpWorld() {
		Log4J.init();
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		gameObjects = GameObjects.createInstance(null);
		gameObjects.clear();
		listener = new TrackingListener();
		gameObjects.addGameObjectListener(listener);
	}

	@After
	public void tearDown() {
		gameObjects.removeGameObjectListener(listener);
		gameObjects.clear();
	}

	@Test
	public void testClearAndNotifyListenersRemovesEntityViews() {
		gameObjects.onAdded(createGroundItem());

		assertEquals(1, listener.added);
		assertTrue(gameObjects.iterator().hasNext());

		gameObjects.clearAndNotifyListeners();

		assertEquals(1, listener.removed);
		assertFalse(gameObjects.iterator().hasNext());
	}

	@Test
	public void testPlainClearDoesNotDuplicateRemovalNotifications() {
		gameObjects.onAdded(createGroundItem());

		gameObjects.clear();

		assertEquals(0, listener.removed);
		assertFalse(gameObjects.iterator().hasNext());
	}

	private RPObject createGroundItem() {
		RPObject object = new RPObject();
		object.setID(new RPObject.ID(12345, "zone"));
		object.setRPClass("item");
		object.put("class", "misc");
		object.put("subclass", "seed");
		object.put("x", 1);
		object.put("y", 1);
		return object;
	}

	private static final class TrackingListener implements GameObjectListener {
		private int added;
		private int removed;

		@Override
		public void addEntity(final IEntity entity) {
			added++;
		}

		@Override
		public void removeEntity(final IEntity entity) {
			removed++;
		}
	}
}
