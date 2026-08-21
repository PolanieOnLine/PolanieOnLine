/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.BlockTestHelper;

public class MieszczaninRoadSceneTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
		MockStendlRPWorld.get();
	}

	@Test
	public void witomirPathKeepsDesignedWaypoints() {
		final List<Node> path = MieszczaninRoadScene.createWitomirPath();

		assertEquals(40, path.size());
		assertEquals(MieszczaninRoadScene.WITOMIR_START_X, path.get(0).getX());
		assertEquals(MieszczaninRoadScene.WITOMIR_START_Y, path.get(0).getY());
		assertEquals(119, path.get(21).getX());
		assertEquals(57, path.get(21).getY());
		assertEquals(MieszczaninRoadScene.WITOMIR_END_X, path.get(path.size() - 1).getX());
		assertEquals(MieszczaninRoadScene.WITOMIR_END_Y, path.get(path.size() - 1).getY());
		assertEquals(Direction.DOWN, MieszczaninRoadScene.finalDirection());
	}

	@Test
	public void trackPropsUseQuestTrailWithDescriptionsAndClearEntrance() {
		final StendhalRPZone zone = new StendhalRPZone(MieszczaninRoadScene.ZONE_NAME, 128, 128);
		final Player owner = PlayerTestHelper.createPlayer("Tracker");

		MieszczaninRoadScene.ensureTrackProps(zone, owner);
		MieszczaninRoadScene.ensureTrackProps(zone, owner);

		int count = 0;
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			assertEquals("Tracker", prop.getOwnerName());
			assertEquals(MieszczaninRoadScene.TRACK_TILESET,
					prop.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE));
			assertEquals(MieszczaninRoadScene.TRACK_TILESET_COLUMNS,
					prop.getInt(PlayerPrivateQuestProp.TILESET_COLUMNS_ATTRIBUTE));
			assertEquals(MieszczaninRoadScene.TRACK_Z_ORDER, prop.getInt("z"));
			assertTrue(prop.hasDescription());
			assertFalse(prop.isObstacle(owner));

			if (prop.getX() == 44 && prop.getY() == 54) {
				assertEquals(0, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
			} else if (prop.getX() == 35 && prop.getY() == 72) {
				assertEquals(1, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
			} else if (prop.getX() == 31 && prop.getY() == 92) {
				assertEquals(2, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
			} else if (prop.getX() == 27 && prop.getY() == 94) {
				assertEquals(3, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
			} else if (prop.getX() == MieszczaninRoadScene.TRACK_ENTRANCE_X
					&& prop.getY() == MieszczaninRoadScene.TRACK_ENTRANCE_Y) {
				assertEquals(4, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
				assertTrue(prop instanceof MieszczaninHideoutEntrance);
				assertEquals("Wejdź|use", prop.get("menu"));
				assertTrue(prop.getDescription().contains("wejście do kryjówki"));
			}
			count++;
		}
		assertEquals(15, count);

		MieszczaninRoadScene.removeTrackProps(zone, owner);
		assertEquals(0, zone.getEntitiesOfClass(PlayerPrivateQuestProp.class).size());
	}

	@Test
	public void wreckPropsHaveIndividualDescriptions() {
		final StendhalRPZone zone = new StendhalRPZone(MieszczaninRoadScene.ZONE_NAME, 128, 128);
		final Player owner = PlayerTestHelper.createPlayer("Witness");
		owner.setQuest(PierscienMieszczanina.QUEST_SLOT, PierscienMieszczanina.STATE_ROAD);

		MieszczaninRoadScene.ensureWreckProps(zone, owner);

		int count = 0;
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (prop.isOwnedBy(owner)) {
				assertTrue(prop.hasDescription());
				assertFalse(prop.getDescription().contains("prywatnej sceny zadania"));
				count++;
			}
		}
		assertEquals(7, count);
	}
}
