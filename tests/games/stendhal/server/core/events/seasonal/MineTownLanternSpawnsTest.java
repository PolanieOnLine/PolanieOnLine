/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class MineTownLanternSpawnsTest {
	private String previousProperty;
	private final List<StendhalRPZone> zones = new ArrayList<StendhalRPZone>();
	private final Map<StendhalRPZone, Boolean> accessible = new IdentityHashMap<StendhalRPZone, Boolean>();
	private MineTownLanternSpawns spawns;

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		previousProperty = System.getProperty("stendhal.minetown");
		System.setProperty("stendhal.minetown", "true");
		for (final IRPZone raw : SingletonRepository.getRPWorld()) {
			final StendhalRPZone zone = (StendhalRPZone) raw;
			accessible.put(zone, zone.isPublicAccessible());
			zone.setPublicAccessible(false);
		}
		spawns = new MineTownLanternSpawns();
	}

	@After
	public void cleanup() throws Exception {
		spawns.stop();
		for (final StendhalRPZone zone : zones) {
			SingletonRepository.getRPWorld().removeRPZone(zone.getID());
		}
		for (final Map.Entry<StendhalRPZone, Boolean> entry : accessible.entrySet()) {
			entry.getKey().setPublicAccessible(entry.getValue());
		}
		if (previousProperty == null) { System.clearProperty("stendhal.minetown"); }
		else { System.setProperty("stendhal.minetown", previousProperty); }
		PlayerTestHelper.removeAllPlayers();
	}

	private StendhalRPZone zone(final String name) {
		final StendhalRPZone zone = new StendhalRPZone(name, 32, 32);
		zone.secretMap.init(32, 32);
		zone.setPosition(0, 0, 0);
		zone.setPublicAccessible(true);
		SingletonRepository.getRPWorld().addRPZone(zone);
		zones.add(zone);
		return zone;
	}

	private int count(final List<StendhalRPZone> list) {
		int count = 0;
		for (final StendhalRPZone zone : list) {
			for (final RPObject object : zone) {
				if (object instanceof Item && "latarenka".equals(((Item) object).getName())) {
					assertEquals(MineTownLanternSpawns.itemData(), ((Item) object).getItemData());
					count++;
				}
			}
		}
		return count;
	}

	@Test
	public void oneOrTwoLanternsPerThreeMapsWithoutAnyPlayerQuest() {
		for (int i = 0; i < 12; i++) { zone("lantern_test_" + String.format("%02d", i)); }
		spawns.start();
		for (int i = 0; i < 12; i += 3) {
			final int count = count(zones.subList(i, i + 3));
			assertTrue("Count per three maps: " + count, count >= 1 && count <= 2);
		}
		spawns.start(); // Restarting replaces the population; it does not accumulate.
		assertTrue(count(zones) >= 4 && count(zones) <= 8);
		spawns.stop();
		assertEquals(0, count(zones));
		for (final StendhalRPZone zone : zones) { assertTrue(zone.getPlantGrowers().isEmpty()); }
	}

	@Test
	public void pickupSchedulesExactlyThirtyMinutesAndStopKeepsCollectedLantern() {
		final StendhalRPZone zone = zone("lantern_pickup_test");
		spawns.start();
		final PassiveEntityRespawnPoint point = zone.getPlantGrowers().get(0);
		Item collected = null;
		for (final RPObject object : zone) {
			if (object instanceof Item) { collected = (Item) object; }
		}
		assertNotNull(collected);
		point.onFruitPicked(collected);
		zone.remove(collected);
		final Player player = PlayerTestHelper.createPlayer("lantern_collector");
		assertTrue(player.equipToInventoryOnly(collected));
		assertEquals(1800, TurnNotifier.get().getRemainingSeconds(point));
		assertEquals(0, count(zones));
		TurnNotifier.get().dontNotify(point);
		point.onTurnReached(1);
		assertEquals(1, count(zones));
		assertEquals(1, player.getNumberOfEquipped("latarenka"));
		spawns.stop();
		assertEquals(0, count(zones));
		assertEquals(1, player.getNumberOfEquipped("latarenka"));
		assertNull(collected.getPlantGrower());
		// A stale callback after unloading cannot repopulate the world.
		point.onTurnReached(2);
		assertEquals(0, count(zones));
	}

	@Test
	public void inactiveEventDoesNotCreateLanternsOrRegrowThem() {
		final StendhalRPZone zone = zone("lantern_event_off_test");
		System.clearProperty("stendhal.minetown");
		spawns.start();
		assertEquals(0, count(zones));
		System.setProperty("stendhal.minetown", "true");
		spawns.start();
		final PassiveEntityRespawnPoint point = zone.getPlantGrowers().get(0);
		Item item = null;
		for (final RPObject object : zone) { if (object instanceof Item) { item = (Item) object; } }
		point.onFruitPicked(item);
		zone.remove(item);
		System.clearProperty("stendhal.minetown");
		point.onTurnReached(1);
		assertEquals(0, count(zones));
	}

	@Test
	public void skipsPrivateAdministrativeSmallAndArenaMaps() {
		final StendhalRPZone privateZone = zone("lantern_private_test");
		privateZone.setPublicAccessible(false);
		assertFalse(MineTownLanternSpawns.isEligible(privateZone));
		assertFalse(MineTownLanternSpawns.isEligible(zone("int_admin_lantern_test")));
		assertFalse(MineTownLanternSpawns.isEligible(zone("lantern_arena_test")));
		assertFalse(MineTownLanternSpawns.isEligible(new StendhalRPZone("small", 8, 8)));
		spawns.start();
		assertEquals(0, count(zones));
	}

	@Test
	public void rejectsSecretAndEnclosedTilesButAllowsConnectedGround() {
		final StendhalRPZone zone = zone("lantern_reachability_test");
		assertTrue(MineTownLanternSpawns.isReachable(zone, new Point(20, 20)));
		for (int i = 7; i <= 13; i++) {
			zone.collisionMap.setCollide(7, i);
			zone.collisionMap.setCollide(13, i);
			zone.collisionMap.setCollide(i, 7);
			zone.collisionMap.setCollide(i, 13);
		}
		assertFalse(MineTownLanternSpawns.isReachable(zone, new Point(10, 10)));
		zone.secretMap.setCollide(20, 20);
		assertFalse(MineTownLanternSpawns.isReachable(zone, new Point(20, 20)));
	}

	@Test
	public void mapsWithoutOptionalSecretLayerCanSpawnLanterns() {
		final StendhalRPZone zone = zone("lantern_no_secret_layer_test");
		zone.secretMap = new games.stendhal.common.CollisionDetection();
		spawns.start();
		assertEquals(1, count(zones));
	}

	@Test
	public void interiorNeedsReachablePortalNotJustAnEmptyTile() {
		final StendhalRPZone zone = zone("lantern_interior_test");
		zone.setPosition();
		assertFalse(MineTownLanternSpawns.isReachable(zone, new Point(10, 10)));
		final Portal portal = new Portal();
		portal.setPosition(5, 5);
		zone.add(portal);
		assertTrue(MineTownLanternSpawns.isReachable(zone, new Point(10, 10)));
	}
}
