package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;

public class ChallengeArenaCreatureSpawnerTest {
	@Test
	public void spawnSearchNeverCrossesSolidCollisionWall() {
		final StendhalRPZone zone = new StendhalRPZone("test_arena", 20, 20);
		for (int y = 1; y <= 18; y++) {
			zone.collisionMap.setCollide(10, y);
		}

		final List<Point> points = ChallengeArenaCreatureSpawner
				.findReachableSpawnTiles(zone,
						new Rectangle2D.Double(1, 1, 18, 18), 5, 10);

		assertFalse(points.isEmpty());
		for (final Point point : points) {
			assertTrue("spawn crossed collision wall at " + point,
					point.x < 10);
		}
	}

	@Test
	public void spawnSearchKeepsDistanceFromPlayerAndStopsAtRadius() {
		final StendhalRPZone zone = new StendhalRPZone("test_arena", 40, 40);
		final List<Point> points = ChallengeArenaCreatureSpawner
				.findReachableSpawnTiles(zone,
						new Rectangle2D.Double(1, 1, 38, 38), 20, 20);

		assertFalse(points.isEmpty());
		for (final Point point : points) {
			final int distance = Math.abs(point.x - 20) + Math.abs(point.y - 20);
			assertTrue(distance >= 4);
			assertTrue(distance <= 14);
		}
	}
}
