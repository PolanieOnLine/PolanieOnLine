/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static games.stendhal.common.constants.Actions.MOVE_CONTINUOUS;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.OneWayPortalDestination;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.mapstuff.useable.ViewChangeEntity;
import games.stendhal.server.util.Area;

/** Configures the dedicated Challenge Arena in Tarnow. */
public final class ChallengeArenaZone implements ZoneConfigurator {
	private static final Logger logger = Logger.getLogger(ChallengeArenaZone.class);

	public static final String ZONE_NAME = "int_tarnow_challenge_arena";
	public static final String TARNOW_CITY_ZONE = "0_tarnow_city";

	private static final String ARENA_LOBBY_DESTINATION = "challenge_arena_lobby_entry";
	private static final String TARNOW_RETURN_DESTINATION = "challenge_arena_tarnow_return";

	private static final int COMBAT_AREA_X = 1;
	private static final int COMBAT_AREA_Y = 1;
	private static final int COMBAT_AREA_WIDTH = 62;
	private static final int COMBAT_AREA_HEIGHT = 39;

	static final int LOBBY_RETURN_X = 32;
	// One tile directly in front of the NPC at 32,46.
	static final int LOBBY_RETURN_Y = 47;
	static final int COMBAT_X = 32;
	static final int COMBAT_Y = 19;

	static final int LEFT_VIEW_ORB_X = 27;
	static final int RIGHT_VIEW_ORB_X = 37;
	static final int VIEW_ORB_Y = 43;

	static final int NPC_X = 32;
	static final int NPC_Y = 46;
	private static final int RANKING_X = 41;
	private static final int RANKING_Y = 39;

	private static final int ARENA_ENTRY_X = 32;
	private static final int ARENA_ENTRY_Y = 62;
	private static final int ARENA_EXIT_Y = 63;

	private static final int TARNOW_ENTRANCE_Y = 63;
	private static final int TARNOW_RETURN_X = 44;
	private static final int TARNOW_RETURN_Y = 64;

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		final Rectangle2D shape = new Rectangle2D.Double(COMBAT_AREA_X,
				COMBAT_AREA_Y, COMBAT_AREA_WIDTH, COMBAT_AREA_HEIGHT);
		final Area combatArea = new Area(zone, shape);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(combatArea, zone,
				zone.getName(), LOBBY_RETURN_X, LOBBY_RETURN_Y, COMBAT_X, COMBAT_Y);
		ChallengeArenaManager.configureArena(info);

		final ChallengeArenaPlayerGate playerGate = new ChallengeArenaPlayerGate(
				COMBAT_AREA_WIDTH, COMBAT_AREA_HEIGHT);
		playerGate.setPosition(COMBAT_AREA_X, COMBAT_AREA_Y);
		zone.add(playerGate);

		configureViewOrbs(zone);
		configureArenaPortals(zone);
		configureTarnowPortals();

		final ChallengeArenaRankingSign rankingSign = new ChallengeArenaRankingSign();
		rankingSign.setPosition(RANKING_X, RANKING_Y);
		zone.add(rankingSign);
		ChallengeArenaManager.configureRankingSign(rankingSign);

		ChallengeArenaNPC.create(zone, NPC_X, NPC_Y);
	}

	static void configureViewOrbs(final StendhalRPZone zone) {
		addViewOrb(zone, LEFT_VIEW_ORB_X);
		addViewOrb(zone, RIGHT_VIEW_ORB_X);
	}

	private static void addViewOrb(final StendhalRPZone zone, final int x) {
		final ViewChangeEntity orb = ViewChangeEntity.unrestricted(COMBAT_X, COMBAT_Y);
		orb.setPosition(x, VIEW_ORB_Y);
		zone.add(orb);
	}

	private void configureArenaPortals(final StendhalRPZone zone) {
		final OneWayPortalDestination lobbyDestination = new OneWayPortalDestination();
		lobbyDestination.setIdentifier(ARENA_LOBBY_DESTINATION);
		lobbyDestination.setPosition(ARENA_ENTRY_X, ARENA_ENTRY_Y);
		lobbyDestination.setFaceDirection(Direction.UP);
		zone.add(lobbyDestination);

		for (int x = 31; x <= 33; x++) {
			final Portal exit = new Portal();
			exit.setIdentifier("challenge_arena_exit_" + x);
			exit.setPosition(x, ARENA_EXIT_Y);
			exit.setDestination(TARNOW_CITY_ZONE, TARNOW_RETURN_DESTINATION);
			exit.put(MOVE_CONTINUOUS, "");
			zone.add(exit);
		}
	}

	private void configureTarnowPortals() {
		final StendhalRPZone city = SingletonRepository.getRPWorld().getZone(TARNOW_CITY_ZONE);
		if (city == null) {
			logger.error("Could not configure Challenge Arena entrance because Tarnow city is not loaded");
			return;
		}

		if (city.getPortal(TARNOW_RETURN_X, TARNOW_RETURN_Y) == null) {
			final OneWayPortalDestination returnDestination = new OneWayPortalDestination();
			returnDestination.setIdentifier(TARNOW_RETURN_DESTINATION);
			returnDestination.setPosition(TARNOW_RETURN_X, TARNOW_RETURN_Y);
			returnDestination.setFaceDirection(Direction.DOWN);
			city.add(returnDestination);
		}

		for (int x = 43; x <= 46; x++) {
			if (city.getPortal(x, TARNOW_ENTRANCE_Y) != null) {
				logger.warn("Challenge Arena entrance tile already contains a portal at "
						+ x + "," + TARNOW_ENTRANCE_Y);
				continue;
			}
			final Portal entrance = new Portal();
			entrance.setIdentifier("challenge_arena_tarnow_entrance_" + x);
			entrance.setPosition(x, TARNOW_ENTRANCE_Y);
			entrance.setDestination(ZONE_NAME, ARENA_LOBBY_DESTINATION);
			entrance.put(MOVE_CONTINUOUS, "");
			city.add(entrance);
		}
	}
}
