package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public void endgameWavesRotateThroughFamiliesAndKeepEachWaveTogether() {
		final ChallengeArenaCreatureSpawner spawner = new ChallengeArenaCreatureSpawner();
		final Map<String, Set<String>> members = new HashMap<>();
		String previous = null;
		for (int cycle = 0; cycle < 3; cycle++) {
			final Set<String> seen = new HashSet<>();
			for (int wave = 0; wave < 3; wave++) {
				spawner.beginWave(633);
				String family = null;
				for (int enemy = 0; enemy < 30; enemy++) {
					final Creature creature = spawner.calculateCreature(633);
					final String selected = familyOf(creature.getName());
					members.computeIfAbsent(selected, key -> new HashSet<>()).add(creature.getName());
					assertFalse(creature.isBoss());
					if (family == null) family = selected;
					assertEquals(family, selected);
				}
				assertFalse(family.equals(previous));
				assertTrue(seen.add(family));
				previous = family;
			}
			assertEquals(3, seen.size());
		}
		for (final Set<String> family : members.values()) {
			assertTrue("A family must offer more than a few endgame templates", family.size() >= 5);
		}
	}

	@Test
	public void themedCopiesShareABoundedCombatBudgetAndRetainTheirNativeRewards() {
		final ChallengeArenaCreatureSpawner spawner = new ChallengeArenaCreatureSpawner();
		final StendhalRPZone zone = new StendhalRPZone("arena-themed-budgets", 40, 40);
		final Player player = PlayerTestHelper.createPlayer("ArenaThemedBudget");
		zone.add(player);
		player.setPosition(20, 20);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(new Area(zone,
				new Rectangle2D.Double(1, 1, 38, 38)), zone, zone.getName(), 1, 1, 20, 20);
		final Creature prince = SingletonRepository.getEntityManager().getCreature("książę szkieletów");
		final Creature giant = SingletonRepository.getEntityManager().getCreature("szarak gigant");
		final double hp = (prince.getBaseHP() + prince.getBaseArmorHP()
				+ giant.getBaseHP() + giant.getBaseArmorHP()) / 2.0 * 633 / 550;
		final double attack = (prince.getAtk() + giant.getAtk()) / 2.0 * 633 / 550;
		final double defense = (prince.getDef() + giant.getDef()) / 2.0 * 633 / 550;
		final Set<String> appeared = new HashSet<>();
		for (int wave = 0; wave < 3; wave++) {
			spawner.beginWave(633);
			for (int enemy = 0; enemy < 30; enemy++) {
				final Creature spawned = spawner.spawn(player, info, 633, false, false,
						ChallengeArenaTier.TRIAL, Collections.<ChallengeArenaModifier>emptyList());
				assertNotNull(spawned);
				assertEquals(633, spawned.getLevel());
				assertTrue(Math.abs((spawned.getBaseHP() + spawned.getBaseArmorHP()) / hp - 1) <= 0.151);
				assertTrue(Math.abs(spawned.getAtk() / attack - 1) <= 0.101);
				assertTrue(Math.abs(spawned.getDef() / defense - 1) <= 0.061);
				final Creature source = SingletonRepository.getEntityManager().getCreature(spawned.getName());
				assertEquals(source.getXP(), spawned.getXP());
				assertEquals(source.getArmorType(), spawned.getArmorType());
				appeared.add(spawned.getName());
				zone.remove(spawned);
			}
		}
		assertTrue(appeared.containsAll(Arrays.asList("zielony smok", "czerwony smok",
				"dwugłowy lodowy smok", "zielone smoczysko", "mumia", "duch",
				"zombi", "licho", "szarak", "szarak kapitan wojowników")));
	}

	private String familyOf(final String name) {
		if (name.contains("smok") || name.contains("smoczysko")) return "dragons";
		if (name.startsWith("szarak")) return "aliens";
		return "undead";
	}

	@Test
	public void highLevelVariantScalesBeyondTheLastTemplateWithoutChangingWorldCreature() {
		final Creature template = SingletonRepository.getEntityManager().getCreature("szarak gigant");
		final int originalHp = template.getBaseHP();
		final int originalAttack = template.getAtk();
		final int originalDefense = template.getDef();
		final int originalLevel = template.getLevel();
		final int originalXp = template.getXP();
		final ChallengeArenaCreatureSpawner spawner = new ChallengeArenaCreatureSpawner(Collections.singletonList(template));
		final StendhalRPZone zone = new StendhalRPZone("arena-high-level", 40, 40);
		final Player player = PlayerTestHelper.createPlayer("ArenaHighLevel");
		zone.add(player);
		player.setPosition(20, 20);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(new Area(zone,
				new Rectangle2D.Double(1, 1, 38, 38)), zone, zone.getName(), 1, 1, 20, 20);
		final Creature spawned = spawner.spawn(player, info, 633, false, false,
				ChallengeArenaTier.TRIAL, Collections.<ChallengeArenaModifier>emptyList());
		assertNotNull(spawned);
		assertEquals(633, spawned.getLevel());
		assertTrue(spawned.getBaseHP() > originalHp);
		assertTrue(spawned.getAtk() > originalAttack);
		assertTrue(spawned.getDef() > originalDefense);
		assertEquals(originalXp, spawned.getXP());
		assertEquals(originalHp, template.getBaseHP());
		assertEquals(originalAttack, template.getAtk());
		assertEquals(originalDefense, template.getDef());
		assertEquals(originalLevel, template.getLevel());
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
		assertEquals(template.getLevel(), spawned.getLevel());
		assertEquals(template.getBaseHP(), spawned.getBaseHP());
		assertEquals(template.getAtk(), spawned.getAtk());
		assertEquals(template.getDef(), spawned.getDef());
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
