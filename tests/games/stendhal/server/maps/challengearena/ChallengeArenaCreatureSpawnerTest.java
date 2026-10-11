package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Collections;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import utilities.RPClass.CreatureTestHelper;

public class ChallengeArenaCreatureSpawnerTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void spawnedArenaMonsterRetainsTemplateLoot() {
		final Creature template = SingletonRepository.getEntityManager().getCreature("kamienny golem");
		template.clearDropItemList();
		template.addDropItem("kolczuga", 100.0, 1);
		final ChallengeArenaCreatureSpawner spawner = new ChallengeArenaCreatureSpawner(Collections.singletonList(template));
		final StendhalRPZone zone = new StendhalRPZone("arena-loot-spawn", 40, 40);
		final Player player = PlayerTestHelper.createPlayer("ArenaLoot");
		zone.add(player);
		player.setPosition(20, 20);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(new Area(zone,
				new Rectangle2D.Double(1, 1, 38, 38)), zone, zone.getName(), 1, 1, 20, 20);
		final Creature spawned = spawner.spawn(player, info, template.getLevel(), false,
				false, ChallengeArenaTier.TRIAL, Collections.<ChallengeArenaModifier>emptyList());
		assertTrue(spawned instanceof ArenaCreature);
		spawned.onDead(player, true);
		final Corpse corpse = ((ArenaCreature) spawned).getLootCorpse();
		assertTrue(corpse.getSlot("content").size() > 0);
		assertTrue("kolczuga".equals(((Item) corpse.getSlot("content").iterator().next()).getName()));
		assertTrue(zone.has(corpse.getID()));
		final Corpse unrelatedCorpse = new Corpse("player", 2, 2);
		zone.add(unrelatedCorpse);
		spawner.removeAllCreatures();
		assertFalse(zone.has(corpse.getID()));
		assertTrue(zone.has(unrelatedCorpse.getID()));
	}

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
	@Test
	public void removedCreatureDoesNotBlockFollowingWave() {
		final StendhalRPZone zone = new StendhalRPZone("test_arena_wave", 20, 20);
		final Creature creature = new Creature();
		creature.setBaseHP(100);
		creature.setHP(100);
		zone.add(creature);
		creature.setPosition(5, 5);

		assertTrue(ChallengeArenaCreatureSpawner.isActiveArenaCreature(creature));

		zone.remove(creature);

		assertFalse(ChallengeArenaCreatureSpawner.isActiveArenaCreature(creature));
	}

}
