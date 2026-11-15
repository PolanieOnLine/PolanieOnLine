/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.maps.zakopane.city.GawedziarzNPC;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Companion continuity is tested using the actual outdoor maps' world offsets. */
public class CompassEscortTest {
	private final List<Player> players = new ArrayList<>();
	private final StendhalRPZone[] zones = new StendhalRPZone[4];
	private Player player;

	@BeforeClass
	public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }

	@Before
	public void setup() {
		int[][] offsets = { { 513056, 18560 }, { 513184, 18560 }, { 513312, 18560 }, { 513312, 18688 } };
		for (int i = 0; i < zones.length; i++) {
			zones[i] = new StendhalRPZone(CompassEscort.ZONES[i], 128, 128);
			zones[i].setPosition(0, offsets[i][0], offsets[i][1]);
			MockStendlRPWorld.get().addRPZone(zones[i]);
			TrailSceneRegistry.prop(zones[i], "escort-test-registration", p -> false, () -> new AreaEntity(1, 1));
		}
		player = player("escort-owner");
	}

	private Player player(String name) {
		Player p = PlayerTestHelper.createPlayer(name);
		p.setPosition(68, 87);
		p.setQuest(CompassProgress.SLOT, "ESCORT;0;0");
		zones[0].add(p);
		players.add(p);
		MockStendhalRPRuleProcessor.get().addPlayer(p);
		CompassEscort.refresh(p);
		return p;
	}

	@After
	public void cleanup() {
		for (Player p : players) CompassEscort.cleanup(p);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		for (StendhalRPZone zone : zones) MockStendlRPWorld.get().removeZone(zone);
	}

	private void move(Player p, int destination, int x, int y) {
		p.getZone().remove(p);
		p.setPosition(x, y);
		zones[destination].add(p);
		int start = Math.max(0, TurnNotifier.get().getCurrentTurnForDebugging());
		for (int turn = start + 1; turn < start + 4; turn++) TurnNotifier.get().logic(turn);
	}

	private Entity stop(Player p, int step) {
		AreaEntity stop = new AreaEntity(1, 1);
		PlayerPrivateScene.assign(stop, p);
		stop.setPosition(CompassEscort.STOPS[step][0], CompassEscort.STOPS[step][1]);
		zones[step].add(stop);
		return stop;
	}

	private void arrive(int step) {
		Entity stop = stop(player, step);
		player.setPosition(stop.getX(), stop.getY() + 1);
		CompassEscort.find(player).setPosition(stop.getX(), stop.getY());
		assertTrue(CompassEscort.arrive(player, stop, step));
	}

	@Test
	public void followsAcrossFourAdjacentMapsOnlyAfterEveryRest() {
		CompassEscort escort = CompassEscort.find(player);
		assertNotNull(escort);
		arrive(0);
		escort.setPosition(126, 23);
		player.setPosition(127, 23);
		move(player, 1, 0, 23);
		assertSame(escort, CompassEscort.find(player));
		assertSame(zones[1], escort.getZone());
		assertEquals(0, escort.getX());
		arrive(1);
		escort.setPosition(126, 118);
		player.setPosition(127, 118);
		move(player, 2, 0, 118);
		assertSame(zones[2], escort.getZone());
		arrive(2);
		escort.setPosition(26, 126);
		player.setPosition(26, 127);
		move(player, 3, 26, 0);
		assertSame(zones[3], escort.getZone());
		arrive(3);
		assertEquals(Stage.REMAINS, CompassProgress.stage(player));
		assertNull(CompassEscort.find(player));
		assertFalse(zones[3].has(escort.getID()));
	}

	@Test
	public void aloneTeleportAndSkippedStopsNeverBringCompanionOrProgress() {
		CompassEscort escort = CompassEscort.find(player);
		escort.setPosition(126, 23);
		player.setPosition(127, 23);
		move(player, 1, 0, 23);
		assertSame("first stop still required", zones[0], escort.getZone());
		Entity stop = stop(player, 1);
		player.setPosition(stop.getX(), stop.getY() + 1);
		assertFalse(CompassEscort.arrive(player, stop, 1));
		assertEquals(0, CompassProgress.detail(player));
		assertEquals(zones[0].getName(), CompassEscort.target(player).zone);
		CompassEscort.cleanup(player);
		CompassEscort.refresh(player);
		assertNull("relogin ahead cannot summon him", CompassEscort.find(player));
		move(player, 0, 125, 23);
		escort = CompassEscort.find(player);
		assertNotNull(escort);
		arrive(0);
		player.setPosition(127, 23);
		escort.setPosition(126, 23);
		move(player, 1, 105, 118);
		assertSame("teleport discontinuity", zones[0], escort.getZone());
		assertFalse(CompassEscort.arrive(player, stop, 1));
		assertEquals(1, CompassProgress.detail(player));
	}

	@Test
	public void walkingWaitsDuringCombatOrWhenGuideRunsAway() {
		CompassEscort escort = CompassEscort.find(player);
		player.setPosition(74, 86);
		for (int i = 0; i < 50; i++) escort.logic();
		assertTrue("native path movement follows owner", escort.nextTo(player, 3));
		assertFalse(escort.isAttackable());
		player.setPosition(90, 86);
		int x = escort.getX(), y = escort.getY();
		for (int i = 0; i < 30; i++) escort.logic();
		assertEquals(x, escort.getX());
		assertEquals(y, escort.getY());
		assertFalse(escort.hasPath());
		player.setPosition(x + 5, y);
		Player attacker = player("escort-attacker");
		player.setTarget(attacker);
		escort.logic();
		assertFalse(escort.hasPath());
		assertEquals(x, escort.getX());
		assertEquals(y, escort.getY());
	}

	@Test
	public void savesMidMapPositionAndReconstructsForNewPlayerObject() {
		CompassEscort old = CompassEscort.find(player);
		old.setPosition(92, 103);
		CompassEscort.cleanup(player);
		String saved = player.getQuest(CompassProgress.SLOT);
		assertTrue(saved.endsWith(";escort-v1;0_koscielisko_e;92;103"));
		player.getZone().remove(player);
		Player replacement = player("escort-reconnected");
		CompassEscort.cleanup(replacement);
		replacement.setQuest(CompassProgress.SLOT, saved);
		CompassEscort.refresh(replacement);
		CompassEscort rebuilt = CompassEscort.find(replacement);
		assertNotSame(old, rebuilt);
		assertEquals(92, rebuilt.getX());
		assertEquals(103, rebuilt.getY());
		assertTrue(PlayerPrivateScene.isOwnedBy(rebuilt, replacement));
		replacement.setHP(0);
		rebuilt.logic();
		assertNull(CompassEscort.find(replacement));
	}

	@Test
	public void ownersCannotUseEachOthersCompanionOrStops() {
		Player other = player("escort-second-owner");
		CompassEscort ours = CompassEscort.find(player), theirs = CompassEscort.find(other);
		assertNotSame(ours, theirs);
		Entity stop = stop(player, 0);
		player.setPosition(122, 24);
		other.setPosition(122, 24);
		theirs.setPosition(122, 23);
		assertFalse(CompassEscort.arrive(player, stop, 0));
		assertFalse(CompassEscort.arrive(other, stop, 0));
		assertEquals(0, CompassProgress.detail(other));
		ours.setPosition(122, 23);
		assertTrue(CompassEscort.arrive(player, stop, 0));
		assertEquals(0, CompassProgress.detail(other));
		assertTrue(zones[0].has(theirs.getID()));
	}

	@Test
	public void oldFirstCheckpointResumesAtFirstRestAndArrivalRequiresOwner() {
		CompassEscort.cleanup(player);
		player.setQuest(CompassProgress.SLOT, "ESCORT;1;0");
		CompassEscort.refresh(player);
		CompassEscort escort = CompassEscort.find(player);
		assertEquals(122, escort.getX());
		assertEquals(23, escort.getY());
		Entity stop = stop(player, 1);
		escort.setPosition(104, 117);
		assertFalse(CompassEscort.arrive(player, stop, 1));
		assertEquals(1, CompassProgress.detail(player));
	}

	@Test
	public void invalidSavedCoordinatesFallBackToLastSafeRest() throws Exception {
		StendhalMapStructure map = TMXLoader.load("data/maps/Level 0/koscielisko/e.tmx");
		map.build();
		zones[0].addCollisionLayer(zones[0].getName() + ".collision", map.getLayer("collision"));
		int blockedX = -1, blockedY = -1;
		for (int x = 0; x < 128 && blockedX < 0; x++) {
			for (int y = 0; y < 128; y++) {
				if (zones[0].collides(x, y)) {
					blockedX = x;
					blockedY = y;
					break;
				}
			}
		}
		assertTrue("native forest has blocked tiles", blockedX >= 0);
		for (String coordinates : new String[] { "128;23", "122;128", "-1;23", blockedX + ";" + blockedY }) {
			CompassEscort.cleanup(player);
			player.setQuest(CompassProgress.SLOT, "ESCORT;1;9;escort-v1;0_koscielisko_e;" + coordinates);
			CompassEscort.refresh(player);
			CompassEscort rebuilt = CompassEscort.find(player);
			assertNotNull(coordinates, rebuilt);
			assertEquals(122, rebuilt.getX());
			assertEquals(23, rebuilt.getY());
			assertEquals(1, CompassProgress.detail(player));
			assertEquals(9, CompassProgress.time(player));
			assertTrue(player.getQuest(CompassProgress.SLOT).endsWith(";122;23"));
		}
	}

	@Test
	public void unrelatedZoneDepartureCannotAuthorizeCrossingButOwnerCanReturn() {
		arrive(0);
		CompassEscort escort = CompassEscort.find(player);
		escort.setPosition(126, 23);
		player.setPosition(127, 23);
		move(player, 2, 20, 20);
		assertSame(zones[0], escort.getZone());
		move(player, 1, 0, 23);
		assertSame("an unrelated detour cannot reuse the old departure", zones[0], escort.getZone());
		move(player, 0, 127, 23);
		assertSame("returning retrieves the same companion", escort, CompassEscort.find(player));
		assertSame(zones[0], escort.getZone());
		move(player, 1, 0, 23);
		assertSame("fresh continuous crossing works after returning", zones[1], escort.getZone());
		assertEquals(1, CompassProgress.detail(player));
	}

	@Test
	public void finalRestDoesNotOverlapSabala() {
		SpeakerNPC previous = NPCList.get().remove("Sabała");
		try {
			new GawedziarzNPC().configureZone(zones[3], null);
			SpeakerNPC sabala = NPCList.get().get("Sabała");
			assertNotNull(sabala);
			Entity rest = stop(player, 3);
			assertFalse("private quest prop must not hide Sabała", rest.getArea().intersects(sabala.getArea()));
			assertFalse("rest leaves room around Sabała", rest.nextTo(sabala, 1));
		} finally {
			NPCList.get().remove("Sabała");
			if (previous != null) NPCList.get().add(previous);
		}
	}

	@Test
	public void journalAndOnePrivateWarningHelpRetrieveCompanionBeforeCompassUnlock() {
		CompassEscort escort = CompassEscort.find(player);
		String normal = CompassProgress.objective(player);
		assertTrue(normal.contains("wschodnim wyjściu"));
		player.clearEvents();
		move(player, 1, 105, 118);
		String waiting = CompassProgress.objective(player);
		assertTrue(waiting.contains("Dobromir czeka we wschodnim lesie Kościeliska"));
		assertTrue(waiting.contains("Wróć po niego"));
		assertFalse(waiting.contains("0_koscielisko"));
		assertEquals(waiting, PlayerTestHelper.getPrivateReply(player));
		for (int i = 0; i < 100; i++) escort.logic();
		CompassEscort.refresh(player);
		assertNull("one message for this separation episode", PlayerTestHelper.getPrivateReply(player));
		CompassEscort.cleanup(player);
		assertEquals("journal also explains where a saved companion waits", waiting, CompassProgress.objective(player));
		move(player, 0, 68, 87);
		escort = CompassEscort.find(player);
		assertNotNull(escort);
		escort.logic();
		assertEquals("reunion restores the current route objective", normal, CompassProgress.objective(player));
		player.clearEvents();
		player.setPosition(88, 86);
		escort.logic();
		assertEquals("walking too far starts a new warning episode", waiting, PlayerTestHelper.getPrivateReply(player));
		for (int i = 0; i < 50; i++) escort.logic();
		assertNull(PlayerTestHelper.getPrivateReply(player));
		player.setPosition(68, 87);
		escort.logic();
		player.setPosition(88, 86);
		escort.logic();
		assertEquals("same actor warns again after a genuine reunion", waiting, PlayerTestHelper.getPrivateReply(player));
	}

	@Test
	public void sameCompanionActuallyWalksTheEntireNativeFourMapJourney() throws Exception {
		String[] files = { "koscielisko/e", "zakopane/w", "zakopane/c", "zakopane/s" };
		for (int i = 0; i < 4; i++) {
			StendhalMapStructure map = TMXLoader.load("data/maps/Level 0/" + files[i] + ".tmx");
			map.build();
			zones[i].addCollisionLayer(zones[i].getName() + ".collision", map.getLayer("collision"));
		}
		CompassEscort companion = CompassEscort.find(player);
		int[][] departures = { { 127, 23 }, { 127, 118 }, { 26, 127 } };
		int[][] entries = { { 0, 23 }, { 0, 118 }, { 26, 0 } };
		int walked = 0;
		for (int step = 0; step < 4; step++) {
			Entity rest = stop(player, step);
			walked += walkWithCompanion(companion, rest.getX(), rest.getY());
			assertSame("the original Dobromir reaches every rest", companion, CompassEscort.find(player));
			assertTrue("native rest " + step, CompassEscort.arrive(player, rest, step));
			if (step < 3) {
				walked += walkWithCompanion(companion, departures[step][0], departures[step][1]);
				move(player, step + 1, entries[step][0], entries[step][1]);
				walked++;
				assertSame("map entry preserves the same companion", companion, CompassEscort.find(player));
				assertSame("companion crosses the natural map edge", zones[step + 1], companion.getZone());
				assertFalse("edge projection lands on native walkable terrain",
						companion.getZone().collides(companion.getX(), companion.getY()));
			}
		}
		assertTrue("the complete native journey must cover over 500 tiles; walked " + walked, walked > 500);
		assertEquals(Stage.REMAINS, CompassProgress.stage(player));
		assertNull(CompassEscort.find(player));
	}

	/** Advance the owner one real, walkable tile and give the injured NPC bounded time to follow. */
	private int walkWithCompanion(CompassEscort companion, int targetX, int targetY) {
		StendhalRPZone zone = player.getZone();
		List<int[]> path = walkingPath(zone, player.getX(), player.getY(), targetX, targetY);
		for (int[] tile : path) {
			assertEquals("owner walks rather than teleports", 1,
					Math.abs(player.getX() - tile[0]) + Math.abs(player.getY() - tile[1]));
			assertFalse(zone.collides(tile[0], tile[1]));
			player.setPosition(tile[0], tile[1]);
			int ticks = 0;
			while (!companion.nextTo(player, 2) && ticks++ < 60) companion.logic();
			assertTrue("Dobromir stuck in " + zone.getName() + " at " + companion.getX() + ","
					+ companion.getY() + " following owner at " + player.getX() + "," + player.getY(),
					companion.nextTo(player, 2));
			assertFalse("native NPC walking respects terrain", zone.collides(companion.getX(), companion.getY()));
		}
		return path.size();
	}

	private List<int[]> walkingPath(StendhalRPZone zone, int startX, int startY, int targetX, int targetY) {
		int width = zone.getWidth(), start = startY * width + startX, target = targetY * width + targetX;
		int[] previous = new int[width * zone.getHeight()];
		java.util.Arrays.fill(previous, -1);
		previous[start] = start;
		ArrayDeque<Integer> pending = new ArrayDeque<>();
		pending.add(start);
		int[][] moves = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		while (!pending.isEmpty() && previous[target] < 0) {
			int current = pending.remove();
			for (int[] direction : moves) {
				int x = current % width + direction[0], y = current / width + direction[1];
				if (x < 0 || y < 0 || x >= width || y >= zone.getHeight() || zone.collides(x, y)) continue;
				int next = y * width + x;
				if (previous[next] < 0) {
					previous[next] = current;
					pending.add(next);
				}
			}
		}
		assertTrue("native route to " + targetX + "," + targetY + " in " + zone.getName(), previous[target] >= 0);
		List<int[]> path = new ArrayList<>();
		for (int tile = target; tile != start; tile = previous[tile]) path.add(new int[] { tile % width, tile / width });
		Collections.reverse(path);
		return path;
	}

	@Test
	public void nativeRoadsConnectEveryStopAndAllThreeSharedBoundaries() throws Exception {
		String[] files = { "koscielisko/e", "zakopane/w", "zakopane/c", "zakopane/s" };
		boolean[][][] reached = new boolean[4][][];
		for (int i = 0; i < 4; i++) {
			StendhalMapStructure map = TMXLoader.load("data/maps/Level 0/" + files[i] + ".tmx");
			map.build();
			zones[i].addCollisionLayer(zones[i].getName() + ".collision", map.getLayer("collision"));
			int[] rest = CompassEscort.STOPS[i];
			assertFalse(files[i] + " rest must be walkable", zones[i].collides(rest[0], rest[1]));
			for (LayerDefinition layer : map.getLayers()) {
				if (layer.getName().contains("roof"))
					assertEquals(files[i] + " rest must remain visible above scenery in " + layer.getName(),
							0, layer.getTileAt(rest[0], rest[1]));
			}
			reached[i] = component(zones[i], rest[0], rest[1]);
		}
		assertTrue("wounded clearing connects to first rest", reached[0][68][86]);
		boolean south = false, east1 = false, east2 = false;
		for (int tile = 0; tile < 128; tile++) {
			east1 |= reached[0][127][tile] && reached[1][0][tile];
			east2 |= reached[1][127][tile] && reached[2][0][tile];
			south |= reached[2][tile][127] && reached[3][tile][0];
		}
		assertTrue("Kościelisko E to Zakopane W has connected shared edge", east1);
		assertTrue("Zakopane W to C has connected shared edge", east2);
		assertTrue("Zakopane C to S has connected shared edge", south);
	}

	private boolean[][] component(StendhalRPZone zone, int startX, int startY) {
		boolean[][] reached = new boolean[zone.getWidth()][zone.getHeight()];
		ArrayDeque<int[]> pending = new ArrayDeque<>();
		pending.add(new int[] { startX, startY });
		reached[startX][startY] = true;
		int[][] moves = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		while (!pending.isEmpty()) {
			int[] current = pending.remove();
			for (int[] direction : moves) {
				int x = current[0] + direction[0], y = current[1] + direction[1];
				if (x >= 0 && y >= 0 && x < zone.getWidth() && y < zone.getHeight()
						&& !reached[x][y] && !zone.collides(x, y)) {
					reached[x][y] = true;
					pending.add(new int[] { x, y });
				}
			}
		}
		return reached;
	}
}
