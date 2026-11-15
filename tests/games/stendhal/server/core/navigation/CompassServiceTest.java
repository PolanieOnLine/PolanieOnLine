package games.stendhal.server.core.navigation;

import static org.junit.Assert.*;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.mapstuff.portal.ConditionAndActionPortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.portal.OneWayPortalDestination;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompassServiceTest {
	@Test
	public void aWalkingNpcDoesNotRestartTheSearchAcrossAllMaps() {
		CountingZone first = new CountingZone("compass_moving_first");
		first.setPosition();
		SingletonRepository.getRPWorld().addRPZone("compass_moving", first);
		StendhalRPZone last = register("compass_moving_last");
		Portal exit = new Portal();
		exit.setPosition(12, 2);
		exit.setDestination(last.getName(), "entry");
		first.add(exit);
		Player player = PlayerTestHelper.createPlayer("compass_moving");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal initial = CompassService.localTarget(player,
				new NavigationGoal(last.getName(), 7, 1, "Julius", "return", "npc:Julius"));
		assertNotNull(initial);
		first.checks = 0;
		NavigationGoal moved = CompassService.localTarget(player,
				new NavigationGoal(last.getName(), 8, 1, "Julius", "return", "npc:Julius"));
		assertEquals(initial.remainingSteps + 1, moved.remainingSteps);
		assertEquals("An NPC taking one step must not restart the world route search", 0, first.checks);
	}

	private static final class CountingZone extends StendhalRPZone {
		int checks;
		boolean lineCheck;

		CountingZone(String name) {
			super(name, 20, 20);
		}

		@Override
		public boolean collides(int x, int y) {
			if (!lineCheck) {
				checks++;
			}
			return super.collides(x, y);
		}

		@Override
		public boolean collidesOnLine(int x1, int y1, int x2, int y2) {
			lineCheck = true;
			try {
				return super.collidesOnLine(x1, y1, x2, y2);
			} finally {
				lineCheck = false;
			}
		}
	}

	@Test
	public void aShortcutWithinTheSameMapAdvancesToTheNextExit() {
		StendhalRPZone first = register("compass_self_first");
		StendhalRPZone last = register("compass_self_last");
		Teleporter shortcut = new Teleporter(new Spot(first, 18, 18));
		shortcut.setPosition(2, 3);
		first.add(shortcut);
		Portal onward = new Portal();
		onward.setPosition(18, 17);
		onward.setDestination(last.getName(), "entry");
		first.add(onward);
		Player player = PlayerTestHelper.createPlayer("compass_self");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 3, 1, "cel", "active");
		assertEquals(4, CompassService.localTarget(player, goal).remainingSteps);
		assertTrue(player.teleport(first, 18, 18, null, null));
		// The compass may be refreshed after the player has already left the landing tile.
		player.setPosition(17, 18);
		NavigationGoal local = CompassService.localTarget(player, goal);
		assertEquals(4, local.remainingSteps);
		assertTrue(local.x >= 17);
	}

	@Test
	public void realAdosCliffsLeadToTheCityWithoutLoopsOrDistanceResets() throws Exception {
		String[] names = {"mountain_n2_w2", "mountain_n2_w", "mountain_n2", "wall_n2",
				"mountain_n_w2", "mountain_nw", "outside_nw", "wall_n",
				"rock_w", "rock", "outside_w", "wall", "city_n2"};
		List<StendhalRPZone> zones = new ArrayList<>();
		for (int i = 0; i < names.length; i++) {
			String file = "data/maps/Level 0/ados/" + names[i] + ".tmx";
			LayerDefinition collision = TMXLoader.load(getClass().getClassLoader().getResource(file).toExternalForm())
					.getLayer("collision");
			StendhalRPZone zone = new StendhalRPZone("compass_ados_" + names[i], collision.getWidth(), collision.getHeight());
			int column = i == 12 ? 4 : i % 4;
			int row = i == 12 ? 0 : i / 4;
			zone.setPosition(79, 10000 + column * 128, 10000 + row * 128);
			zone.addCollisionLayer("collision", collision);
			SingletonRepository.getRPWorld().addRPZone("compass_ados", zone);
			zones.add(zone);
		}
		for (int start : new int[] {1, 2, 4, 5}) {
			followAdosRoute(zones, start);
		}
	}

	private void followAdosRoute(List<StendhalRPZone> zones, int start) {
		Player player = PlayerTestHelper.createPlayer("compass_ados_" + start);
		player.setPosition(100, 100);
		zones.get(start).add(player);
		NavigationGoal goal = new NavigationGoal(zones.get(7).getName(), 84, 109, "Julius", "active");
		Set<String> visited = new HashSet<>();
		int previousDistance = -1;
		long started = System.nanoTime();
		for (int step = 0; step < 1600; step++) {
			StendhalRPZone current = player.getZone();
			assertTrue("The route must not lead back to an earlier tile",
					visited.add(current.getName() + ":" + player.getX() + "," + player.getY()));
			NavigationGoal local = CompassService.localTarget(player, goal);
			assertNotNull("No guidance at " + current.getName() + ":" + player.getX() + "," + player.getY(), local);
			if (previousDistance >= 0) {
				assertEquals("Distance must count continuously across actual map borders", previousDistance - 1, local.remainingSteps);
			}
			previousDistance = local.remainingSteps;
			if (local.remainingSteps == 0) {
				assertEquals(goal.zone, current.getName());
				assertEquals(goal.x, player.getX());
				assertEquals(goal.y, player.getY());
				System.out.println("Ados cliff route: " + step + " steps, " + (System.nanoTime() - started) / 1000000 + " ms");
				return;
			}
			assertTrue("Do not stop at an intermediate turn", local.x != player.getX() || local.y != player.getY());
			int x = player.getX() + Integer.compare(local.x, player.getX());
			int y = player.getY() + (x == player.getX() ? Integer.compare(local.y, player.getY()) : 0);
			if (x >= 0 && y >= 0 && x < current.getWidth() && y < current.getHeight()) {
				assertFalse("Compass must not lead into the cliff", current.collides(x, y));
				player.setPosition(x, y);
			} else {
				int worldX = current.getX() + x;
				int worldY = current.getY() + y;
				StendhalRPZone next = zones.stream().filter(z -> worldX >= z.getX() && worldY >= z.getY()
						&& worldX < z.getX() + z.getWidth() && worldY < z.getY() + z.getHeight()).findFirst().get();
				current.remove(player);
				player.setPosition(worldX - next.getX(), worldY - next.getY());
				next.add(player);
			}
		}
		fail("The city was never reached");
	}

	@Test
	public void portalBehindACliffMustBeReachedFromTheOpenSide() throws Exception {
		StendhalRPZone first = register("compass_stairs_first");
		StendhalRPZone last = register("compass_stairs_last");
		LayerDefinition wall = new LayerDefinition(20, 20);
		wall.setName("collision");
		for (int y = 0; y < 11; y++) {
			wall.set(9, y, 1);
		}
		first.addCollisionLayer("collision", wall);
		Portal stairs = new Portal();
		stairs.setPosition(10, 2);
		stairs.setDestination(last.getName(), "entry");
		first.add(stairs);
		Player player = PlayerTestHelper.createPlayer("compass_stairs");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 1, 1, "schody", "active");
		Set<String> visited = new HashSet<>();
		for (int step = 0; step < 60; step++) {
			assertTrue("Do not stop beside the cliff", visited.add(player.getX() + "," + player.getY()));
			NavigationGoal local = CompassService.localTarget(player, goal);
			assertNotNull(local);
			int x = player.getX() + Integer.compare(local.x, player.getX());
			int y = player.getY() + (x == player.getX() ? Integer.compare(local.y, player.getY()) : 0);
			assertFalse("The chosen side of the stairs must be reachable", first.collides(x, y));
			player.setPosition(x, y);
			if (x == 10 && y == 2) {
				return;
			}
		}
		fail("The stairs were never reached");
	}

	@Test
	public void shorterWalkingRouteWinsEvenWhenItCrossesMoreMaps() {
		StendhalRPZone first = register("compass_short_first");
		StendhalRPZone middle = register("compass_short_middle");
		StendhalRPZone last = register("compass_short_last");
		Portal longWay = new Portal();
		longWay.setPosition(18, 18);
		longWay.setDestination(last.getName(), "entry");
		first.add(longWay);
		Portal shortcut = new Portal();
		shortcut.setPosition(2, 3);
		shortcut.setDestination(middle.getName(), "entry");
		first.add(shortcut);
		Portal onward = new Portal();
		onward.setPosition(2, 1);
		onward.setDestination(last.getName(), "entry");
		middle.add(onward);
		Player player = PlayerTestHelper.createPlayer("compass_short");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal local = CompassService.localTarget(player,
				new NavigationGoal(last.getName(), 3, 1, "cel", "active"));
		assertNotNull(local);
		assertEquals(4, local.remainingSteps);
		assertEquals(2, local.x);
		assertEquals(3, local.y);
	}

	@Test
	public void diagonalApproachDoesNotStopBeforeThePortal() {
		StendhalRPZone first = register("compass_diagonal_first");
		StendhalRPZone last = register("compass_diagonal_last");
		Portal exit = new Portal();
		exit.setPosition(10, 10);
		exit.setDestination(last.getName(), "entry");
		first.add(exit);
		Player player = PlayerTestHelper.createPlayer("compass_diagonal");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 3, 1, "cel", "active");
		for (int step = 0; step < 40; step++) {
			NavigationGoal local = CompassService.localTarget(player, goal);
			assertNotNull(local);
			assertTrue("Guidance must not end at a tile diagonally beside stairs",
					local.x != player.getX() || local.y != player.getY());
			player.setPosition(local.x, local.y);
			if (local.x == 10 && local.y == 10) {
				return;
			}
		}
		fail("Portal was never reached");
	}

	@Test
	public void leavingTheRouteRecalculatesInsteadOfReturningToAnOldTurn() throws Exception {
		StendhalRPZone zone = register("compass_deviation");
		LayerDefinition wall = new LayerDefinition(20, 20);
		wall.setName("collision");
		for (int y = 0; y < 15; y++) {
			wall.set(10, y, 1);
		}
		zone.addCollisionLayer("collision", wall);
		Player player = PlayerTestHelper.createPlayer("compass_deviation");
		player.setPosition(2, 2);
		zone.add(player);
		NavigationGoal goal = new NavigationGoal(zone.getName(), 17, 2, "cel", "active");
		assertNotNull(CompassService.localTarget(player, goal));
		player.setPosition(2, 15);
		NavigationGoal local = CompassService.localTarget(player, goal);
		assertEquals("Do not return to a turn already bypassed along the cliff", 15, local.y);
		assertEquals(28, local.remainingSteps);
	}

	@Test
	public void anotherEntranceCanReachAnExitInADisconnectedRoom() throws Exception {
		StendhalRPZone from = register("compass_rooms_from");
		StendhalRPZone middle = register("compass_rooms_middle");
		StendhalRPZone last = register("compass_rooms_last");
		LayerDefinition wall = new LayerDefinition(20, 20);
		wall.setName("collision");
		for (int x = 0; x < 20; x++) wall.set(x, 10, 1);
		middle.addCollisionLayer("collision", wall);
		for (int index = 0; index < 2; index++) {
			Portal exit = new Portal();
			exit.setPosition(index == 0 ? 8 : 2, index == 0 ? 2 : 8);
			exit.setDestination(middle.getName(), "room_" + index);
			from.add(exit);
			Portal entry = new OneWayPortalDestination();
			entry.setIdentifier("room_" + index);
			entry.setPosition(2, index == 0 ? 2 : 15);
			middle.add(entry);
		}
		Portal onward = new Portal();
		onward.setPosition(17, 15);
		onward.setDestination(last.getName(), "entry");
		middle.add(onward);
		Player player = PlayerTestHelper.createPlayer("compass_rooms");
		player.setPosition(2, 2);
		from.add(player);
		NavigationGoal local = CompassService.localTarget(player,
				new NavigationGoal(last.getName(), 7, 7, "cel", "active"));
		assertNotNull(local);
		assertEquals(2, local.x);
		assertTrue(local.y > 2);
		assertEquals(33, local.remainingSteps);
	}
	@Test
	public void distanceIncludesWalksAfterCoordinateTeleportersWithoutUsingThem() {
		StendhalRPZone first = register("compass_coordinate_first");
		StendhalRPZone last = register("compass_coordinate_last");
		Teleporter exit = new Teleporter(new Spot(last, 1, 6)) {
			@Override
			protected boolean usePortal(Player player) {
				fail("Navigation must never use a portal");
				return false;
			}
		};
		exit.setPosition(6, 6);
		first.add(exit);
		Player player = PlayerTestHelper.createPlayer("compass_coordinate");
		player.setPosition(2, 6);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 7, 6, "cel", "active");
		NavigationGoal local = CompassService.localTarget(player, goal);
		assertNotNull(local);
		assertEquals(10, local.remainingSteps);
		assertEquals(first.getName(), player.getZone().getName());
	}

	@Test
	public void totalDistanceIncludesBothPortalLegsAndTheFinalMap() {
		StendhalRPZone first = register("compass_portal_first");
		StendhalRPZone middle = register("compass_portal_middle");
		StendhalRPZone last = register("compass_portal_last");
		Portal firstExit = new Portal();
		firstExit.setPosition(10, 2);
		firstExit.setDestination(middle.getName(), "middle_entry");
		first.add(firstExit);
		Portal middleEntry = new OneWayPortalDestination();
		middleEntry.setIdentifier("middle_entry");
		middleEntry.setPosition(1, 2);
		middleEntry.put("offset", 4);
		middle.add(middleEntry);
		Portal middleExit = new Portal();
		middleExit.setPosition(15, 2);
		middleExit.setDestination(last.getName(), "last_entry");
		middle.add(middleExit);
		Portal lastEntry = new OneWayPortalDestination();
		lastEntry.setIdentifier("last_entry");
		lastEntry.setPosition(3, 2);
		last.add(lastEntry);
		Player player = PlayerTestHelper.createPlayer("compass_portals");
		player.setPosition(2, 2);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 9, 2, "cel", "active");
		assertEquals(27, CompassService.localTarget(player, goal).remainingSteps);
		first.remove(player);
		player.setPosition(2, 2);
		middle.add(player);
		assertEquals(19, CompassService.localTarget(player, goal).remainingSteps);
		middle.remove(player);
		player.setPosition(3, 2);
		last.add(player);
		assertEquals(6, CompassService.localTarget(player, goal).remainingSteps);
	}
	@Test
	public void distanceIncludesAllMapsAndDoesNotResetAtTheirBorders() {
		StendhalRPZone first = register("compass_total_first");
		StendhalRPZone middle = register("compass_total_middle");
		StendhalRPZone last = register("compass_total_last");
		first.setPosition(57, 2000, 2000);
		middle.setPosition(57, 2020, 2000);
		last.setPosition(57, 2040, 2000);
		Player player = PlayerTestHelper.createPlayer("compass_total");
		player.setPosition(2, 9);
		first.add(player);
		NavigationGoal goal = new NavigationGoal(last.getName(), 7, 9, "cel", "active");
		assertEquals(45, CompassService.localTarget(player, goal).remainingSteps);
		player.setPosition(3, 9);
		assertEquals(44, CompassService.localTarget(player, goal).remainingSteps);
		first.remove(player);
		player.setPosition(0, 9);
		middle.add(player);
		assertEquals(27, CompassService.localTarget(player, goal).remainingSteps);
		middle.remove(player);
		last.add(player);
		assertEquals(7, CompassService.localTarget(player, goal).remainingSteps);
	}

	@Test
	public void waypointStaysFixedUntilReachedAndDistanceCountsTheWholeDetour() throws Exception {
		StendhalRPZone from = register("compass_stable_from");
		StendhalRPZone to = register("compass_stable_to");
		LayerDefinition collision = new LayerDefinition(20, 20);
		collision.setName("collision");
		for (int y = 0; y < 15; y++) collision.set(10, y, 1);
		from.addCollisionLayer("collision", collision);
		Portal exit = new Portal();
		exit.setPosition(17, 2);
		exit.setDestination(to.getName(), "stable_entry");
		from.add(exit);
		Portal entry = new Portal();
		entry.setIdentifier("stable_entry");
		entry.setPosition(2, 2);
		to.add(entry);
		Player player = PlayerTestHelper.createPlayer("compass_stable");
		player.setPosition(2, 2);
		from.add(player);
		NavigationGoal goal = new NavigationGoal(to.getName(), 7, 2, "cel", "active");
		NavigationGoal initial = CompassService.localTarget(player, goal);
		assertNotNull(initial);
		assertTrue(initial.remainingSteps > 30);
		player.setPosition(initial.x != 2 ? 2 + Integer.compare(initial.x, 2) : 2,
				initial.x == 2 ? 2 + Integer.compare(initial.y, 2) : 2);
		NavigationGoal moved = CompassService.localTarget(player, goal);
		assertEquals(initial.x, moved.x);
		assertEquals(initial.y, moved.y);
		assertEquals(initial.remainingSteps - 1, moved.remainingSteps);
		assertFalse(from.collidesOnLine(player.getX(), player.getY(), moved.x, moved.y));
		player.setPosition(initial.x != 2 ? initial.x - Integer.compare(initial.x, 2) : initial.x,
				initial.x == 2 ? initial.y - Integer.compare(initial.y, 2) : initial.y);
		NavigationGoal beforeTurn = CompassService.localTarget(player, goal);
		assertEquals(initial.x, beforeTurn.x);
		assertEquals(initial.y, beforeTurn.y);
		player.setPosition(initial.x, initial.y);
		NavigationGoal afterTurn = CompassService.localTarget(player, goal);
		assertTrue(afterTurn.x != initial.x || afterTurn.y != initial.y);
		assertEquals(beforeTurn.remainingSteps - 1, afterTurn.remainingSteps);
	}

	@Test
	public void localGoalBehindWallAlsoUsesAReachableWaypoint() throws Exception {
		StendhalRPZone zone = register("compass_local_wall");
		LayerDefinition collision = new LayerDefinition(20, 20);
		collision.setName("collision");
		for (int y = 0; y < 15; y++) collision.set(10, y, 1);
		zone.addCollisionLayer("collision", collision);
		Player player = PlayerTestHelper.createPlayer("compass_local_wall_player");
		player.setPosition(2, 2);
		zone.add(player);
		NavigationGoal local = CompassService.localTarget(player,
				new NavigationGoal(zone.getName(), 17, 2, "cel", "active"));
		assertNotNull(local);
		assertFalse(zone.collidesOnLine(player.getX(), player.getY(), local.x, local.y));
		assertTrue(local.remainingSteps > 30);
	}
	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Test
	public void portalPreviewChecksEachPlayerWithoutRunningAction() {
		Player first = PlayerTestHelper.createPlayer("compass_gate1");
		Player second = PlayerTestHelper.createPlayer("compass_gate2");
		ConditionAndActionPortal portal =
				new ConditionAndActionPortal(
						new QuestInStateCondition("compass_gate", "open"),
						(player, sentence, speaker) -> {
							fail("Navigation must not run portal action");
						});
		first.setQuest("compass_gate", "open");
		assertTrue(portal.isAccessibleForNavigation(first));
		assertFalse(portal.isAccessibleForNavigation(second));
		assertFalse(second.hasQuest("compass_gate"));
	}

	@Test
	public void unknownPortalConditionDoesNotRunDuringNavigation() {
		ConditionAndActionPortal portal =
				new ConditionAndActionPortal(
						(player, sentence, speaker) -> {
							fail("Unknown condition must not execute");
							return true;
						},
						null);
		assertFalse(portal.isAccessibleForNavigation(PlayerTestHelper.createPlayer("compass_unknown")));
	}

	@Test
	public void wireLabelCannotInjectAnotherField() {
		assertEquals("a b c", CompassService.sanitize("a\tb\nc"));
	}

	@Test
	public void completionRestoresPermanentUnlockAndRemovesStaleTargets() {
		Player player = PlayerTestHelper.createPlayer("compass_unlock");
		StendhalRPZone zone = new StendhalRPZone("compass_unlock_zone", 20, 20);
		zone.add(player);
		player.put(CompassService.TARGETS, "stale", "3\t4\tmain\tDawny cel");
		CompassService.update(player);
		assertFalse(player.has(CompassService.UNLOCKED));
		assertFalse(player.has(CompassService.TARGETS, "stale"));
		player.setQuest("wanderers_compass", "done");
		CompassService.update(player);
		assertEquals(1, player.getInt(CompassService.UNLOCKED));
		assertEquals(zone.getName(), player.get(CompassService.TARGETS, "_zone"));
		assertFalse(player.isEquipped("kompas wędrowca"));
		assertEquals(1, player.getMap(CompassService.TARGETS).size());
	}

	@Test
	public void sameZoneGoalUsesLocalCoordinatesAndCountsWalkingSteps() {
		Player player = PlayerTestHelper.createPlayer("compass_local");
		StendhalRPZone zone = new StendhalRPZone("compass_test_zone", 20, 20);
		zone.add(player);
		NavigationGoal goal = new NavigationGoal(zone.getName(), 13, 12, "cel", "active");
		NavigationGoal local = CompassService.localTarget(player, goal);
		assertEquals(goal.zone, local.zone);
		assertEquals(Math.abs(player.getX() - goal.x) + Math.abs(player.getY() - goal.y), local.remainingSteps);
		assertFalse(zone.collidesOnLine(player.getX(), player.getY(), local.x, local.y));
	}

	@Test
	public void remoteRoutingRespectsEachPlayersGateAndRechecksCachedRoutes() {
		StendhalRPZone from = register("compass_route_gate_from");
		StendhalRPZone to = register("compass_route_gate_to");
		Player first = PlayerTestHelper.createPlayer("compass_route1");
		Player second = PlayerTestHelper.createPlayer("compass_route2");
		first.setPosition(2, 2);
		second.setPosition(2, 2);
		from.add(first);
		from.add(second);
		ConditionAndActionPortal gate =
				new ConditionAndActionPortal(new QuestInStateCondition("route_key", "open"), null);
		gate.setPosition(16, 2);
		gate.setDestination(to.getName(), "entry");
		from.add(gate);
		first.setQuest("route_key", "open");
		NavigationGoal goal = new NavigationGoal(to.getName(), 7, 7, "cel", "active");
		NavigationGoal local = CompassService.localTarget(first, goal);
		assertNotNull(local);
		assertTrue(
				"The remote marker should point toward the passage, not one tile away", local.x > 10);
		assertNull(CompassService.localTarget(second, goal));
		first.setQuest("route_key", "closed");
		assertNull(CompassService.localTarget(first, goal));
	}

	@Test
	public void remoteWaypointGoesAroundWall() throws Exception {
		StendhalRPZone from = register("compass_wall_from");
		StendhalRPZone to = register("compass_wall_to");
		LayerDefinition collision = new LayerDefinition(20, 20);
		collision.setName("collision");
		for (int y = 0; y < 15; y++) {
			collision.set(10, y, 1);
		}
		from.addCollisionLayer("collision", collision);
		Portal portal = new Portal();
		portal.setPosition(17, 2);
		portal.setDestination(to.getName(), "entry");
		from.add(portal);
		Player player = PlayerTestHelper.createPlayer("compass_wall");
		player.setPosition(2, 2);
		from.add(player);
		NavigationGoal local =
				CompassService.localTarget(player, new NavigationGoal(to.getName(), 7, 7, "cel", "active"));
		assertNotNull(local);
		assertFalse(from.collidesOnLine(player.getX(), player.getY(), local.x, local.y));
		assertTrue(Math.abs(player.getX() - local.x) + Math.abs(player.getY() - local.y) > 1);
	}

	@Test
	public void borderGuidancePointsAcrossTheBoundaryWhenReached() {
		StendhalRPZone from = register("compass_border_from");
		StendhalRPZone to = register("compass_border_to");
		from.setPosition(17, 1000, 1000);
		to.setPosition(17, 1020, 1000);
		Player player = PlayerTestHelper.createPlayer("compass_border");
		player.setPosition(19, 9);
		from.add(player);
		NavigationGoal local =
				CompassService.localTarget(player, new NavigationGoal(to.getName(), 7, 7, "cel", "active"));
		assertNotNull(local);
		assertEquals(20, local.x);
		assertEquals(9, local.y);
	}

	private StendhalRPZone register(String name) {
		StendhalRPZone zone = new StendhalRPZone(name, 20, 20);
		zone.setPosition();
		SingletonRepository.getRPWorld().addRPZone("compass_test", zone);
		Portal entry = new OneWayPortalDestination();
		entry.setIdentifier("entry");
		entry.setPosition(1, 1);
		zone.add(entry);
		return zone;
	}
}
