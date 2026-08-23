/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;

/** Client prediction must not make invisible private scenes block spectators. */
public class PrivateQuestCollisionTest {

	private static final String TEST_RPCLASS = "private_quest_collision_test";

	@BeforeClass
	public static void beforeClass() {
		if (!RPClass.hasRPClass(TEST_RPCLASS)) {
			new RPClass(TEST_RPCLASS);
		}
	}

	private RPObject object(final int resistance) {
		final RPObject object = new RPObject();
		object.put("x", 1);
		object.put("y", 1);
		object.put("width", 1);
		object.put("height", 1);
		object.put("resistance", resistance);
		object.setRPClass(TEST_RPCLASS);
		return object;
	}

	private Entity movingEntity(final boolean user) {
		final Entity entity = new Entity() {
			@Override
			public boolean isUser() {
				return user;
			}
		};
		entity.initialize(object(100));
		return entity;
	}

	@Test
	public void privateQuestBlockOnlyBlocksLocalUser() {
		final RPObject object = object(100);
		object.put("class", "questprop");
		final Block block = new Block();
		block.initialize(object);

		assertTrue(block.isObstacle(movingEntity(true)));
		assertFalse(block.isObstacle(movingEntity(false)));
	}

	@Test
	public void privateNpcOnlyBlocksLocalUserInPrediction() {
		final RPObject object = object(100);
		object.put("idea", "");
		object.put("owner_collision_only", "");
		final NPC npc = new NPC();
		npc.initialize(object);

		assertTrue(npc.isObstacle(movingEntity(true)));
		assertFalse(npc.isObstacle(movingEntity(false)));
	}

	@Test
	public void ordinaryNpcKeepsNormalCollision() {
		final RPObject object = object(100);
		object.put("idea", "");
		final NPC npc = new NPC();
		npc.initialize(object);

		assertTrue(npc.isObstacle(movingEntity(false)));
	}
}
