package games.stendhal.server.entity;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.status.HeavyStatus;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class MovementSpeedTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void fasterMovementConsumesAllWholeStepsAndKeepsFractionalProgress() {
		final Player player = player("speed-fast", new StendhalRPZone("speed_fast", 40, 20));
		player.setBaseSpeed(1.2);
		player.setSpeed(1.2);
		for (int turn = 0; turn < 5; turn++) {
			player.applyMovement();
		}
		assertEquals(11, player.getX());
		assertEquals(6, player.getStepsTaken());
	}

	@Test
	public void slowMovementAndBaseSpeedChangesApplyImmediatelyWithoutRestarting() {
		final Player player = player("speed-change", new StendhalRPZone("speed_change", 40, 20));
		player.setBaseSpeed(0.5);
		assertEquals(0.5, player.getSpeed(), 0.00001);
		player.applyMovement();
		assertEquals(5, player.getX());
		player.applyMovement();
		assertEquals(6, player.getX());
		player.setBaseSpeed(2.0);
		assertEquals(2.0, player.getSpeed(), 0.00001);
		player.applyMovement();
		assertEquals(8, player.getX());
		player.stop();
		player.setBaseSpeed(1.0);
		assertTrue(player.stopped());
	}

	@Test
	public void fastMovementChecksEveryIntermediateCollisionTile() {
		final StendhalRPZone zone = new StendhalRPZone("speed_wall", 40, 20) {
			@Override
			public boolean simpleCollides(final Entity entity, final double x, final double y,
					final double width, final double height) {
				return x == 7 || super.simpleCollides(entity, x, y, width, height);
			}
		};
		final Player player = player("speed-wall", zone);
		player.setSpeed(3.0);
		player.applyMovement();
		assertEquals(6, player.getX());
	}

	@Test
	public void fastGhostMovementStillCrossesSolidTilesAtTheRequestedSpeed() {
		final StendhalRPZone zone = new StendhalRPZone("speed_ghost_wall", 40, 20) {
			@Override
			public boolean simpleCollides(final Entity entity, final double x, final double y,
					final double width, final double height) {
				return true;
			}
		};
		final Player player = player("speed-ghost", zone);
		player.setGhost(true);
		player.setSpeed(3.0);
		player.applyMovement();
		assertEquals(8, player.getX());
	}

	@Test
	public void usingAPortalEndsTheOldMovementBudgetEvenWhenMovementContinues() {
		final StendhalRPZone originZone = new StendhalRPZone("speed_portal_origin", 40, 20);
		final StendhalRPZone destination = new StendhalRPZone("speed_portal_destination", 40, 20);
		final Portal portal = new Portal() {
			@Override
			public boolean onUsed(final RPEntity user) {
				originZone.remove(user);
				destination.add(user);
				user.setPosition(15, 5);
				return true;
			}
		};
		portal.setPosition(6, 5);
		originZone.add(portal);
		final Player player = player("speed-portal", originZone);
		player.setSpeed(3.0);
		player.applyMovement();
		assertSame(destination, player.getZone());
		assertEquals(15, player.getX());
		assertEquals(3.0, player.getSpeed(), 0.00001);
	}

	@Test
	public void fasterPathFollowingTurnsAtCornersAndStopsAtItsDestination() {
		final Player player = player("speed-path", new StendhalRPZone("speed_path", 40, 20));
		player.setBaseSpeed(3.0);
		player.setPath(new FixedPath(Arrays.asList(new Node(5, 5), new Node(6, 5),
				new Node(6, 6)), false));
		player.applyMovement();
		assertEquals(6, player.getX());
		assertEquals(6, player.getY());
		assertTrue(player.stopped());
	}

	@Test
	public void fastLoopingRoutesTurnBackTowardTheFirstNode() {
		final Player player = player("speed-loop", new StendhalRPZone("speed_loop", 40, 20));
		player.setBaseSpeed(4.0);
		player.setPath(new FixedPath(Arrays.asList(new Node(5, 5), new Node(6, 5),
				new Node(6, 6), new Node(5, 6)), true));
		player.applyMovement();
		assertEquals(5, player.getX());
		assertEquals(5, player.getY());
		assertFalse(player.stopped());
	}

	@Test
	public void heavyHalvesTheActualSpeedAndRestoresMovementWhenRemoved() {
		final Player player = player("speed-heavy", new StendhalRPZone("speed_heavy", 40, 20));
		player.setBaseSpeed(1.2);
		player.setSpeed(1.2);
		final HeavyStatus status = new HeavyStatus(10);
		player.getStatusList().inflictStatus(status, null);
		assertEquals(0.6, player.getBaseSpeed(), 0.00001);
		assertEquals(0.6, player.getSpeed(), 0.00001);
		player.getStatusList().remove(status);
		assertEquals(1.2, player.getBaseSpeed(), 0.00001);
		assertEquals(1.2, player.getSpeed(), 0.00001);
	}

	@Test
	public void anAdminSpeedOverrideCannotRestoreAStaleValueDuringAStatusUpdate() {
		final Player player = player("speed-admin", new StendhalRPZone("speed_admin", 40, 20));
		player.put("base_speed", 1.2);
		player.update();
		final HeavyStatus status = new HeavyStatus(10);
		player.getStatusList().inflictStatus(status, null);
		player.update();
		assertEquals(0.6, player.getBaseSpeed(), 0.00001);
		assertEquals(0.6, player.getSpeed(), 0.00001);
		player.getStatusList().remove(status);
		player.update();
		assertEquals(1.2, player.getBaseSpeed(), 0.00001);
	}

	@Test
	public void simultaneousSlowsRestoreEachCharactersOwnSpeed() {
		final StendhalRPZone zone = new StendhalRPZone("speed_independent", 40, 20);
		final Player first = player("speed-first", zone);
		final Player second = player("speed-second", zone);
		first.setBaseSpeed(1.2);
		second.setBaseSpeed(0.8);
		final HeavyStatus firstStatus = new HeavyStatus(10);
		final HeavyStatus secondStatus = new HeavyStatus(20);
		first.getStatusList().inflictStatus(firstStatus, null);
		second.getStatusList().inflictStatus(secondStatus, null);
		first.getStatusList().remove(firstStatus);
		assertEquals(1.2, first.getSpeed(), 0.00001);
		assertEquals(0.4, second.getSpeed(), 0.00001);
		second.getStatusList().remove(secondStatus);
		assertEquals(0.8, second.getSpeed(), 0.00001);
	}

	@Test
	public void anOldExpirationDoesNotRemoveANewerSlow() {
		final Player player = player("speed-expiry", new StendhalRPZone("speed_expiry", 40, 20));
		final HeavyStatus old = new HeavyStatus(10);
		player.getStatusList().inflictStatus(old, null);
		player.getStatusList().remove(old);
		final HeavyStatus current = new HeavyStatus(20);
		player.getStatusList().inflictStatus(current, null);
		player.getStatusList().remove(old);
		assertEquals(0.5, player.getSpeed(), 0.00001);
		assertTrue(player.getStatusList().getStatuses().contains(current));
		player.getStatusList().remove(current);
	}

	@Test
	public void separateSessionsWithTheSameNameKeepTheirOwnSlowState() {
		final Player oldSession = player("speed-relogin", new StendhalRPZone("speed_old_session", 40, 20));
		final Player newSession = player("speed-relogin", new StendhalRPZone("speed_new_session", 40, 20));
		oldSession.setBaseSpeed(1.2);
		newSession.setBaseSpeed(0.8);
		final HeavyStatus oldStatus = new HeavyStatus(10);
		final HeavyStatus newStatus = new HeavyStatus(20);
		oldSession.getStatusList().inflictStatus(oldStatus, null);
		newSession.getStatusList().inflictStatus(newStatus, null);
		oldSession.getStatusList().remove(oldStatus);
		assertEquals(1.2, oldSession.getSpeed(), 0.00001);
		newSession.getStatusList().remove(newStatus);
		assertEquals(0.8, newSession.getSpeed(), 0.00001);
	}

	private Player player(final String name, final StendhalRPZone zone) {
		final Player player = PlayerTestHelper.createPlayer(name);
		zone.add(player);
		player.setPosition(5, 5);
		player.setDirection(Direction.RIGHT);
		player.setSpeed(player.getBaseSpeed());
		return player;
	}
}
