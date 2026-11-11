package games.stendhal.server.core.navigation;

import static org.junit.Assert.*;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.ConditionAndActionPortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class CompassServiceTest {
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
  public void sameZoneGoalUsesRealTile() {
    Player player = PlayerTestHelper.createPlayer("compass_local");
    StendhalRPZone zone = new StendhalRPZone("compass_test_zone", 20, 20);
    zone.add(player);
    NavigationGoal goal = new NavigationGoal(zone.getName(), 13, 12, "cel", "active");
    NavigationGoal local = CompassService.localTarget(player, goal);
    assertSame(goal, local);
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
    return zone;
  }
}
