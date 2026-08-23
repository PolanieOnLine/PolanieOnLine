/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.area.OnePlayerArea;
import games.stendhal.server.util.Area;

/** Configures the dedicated Challenge Arena on the Praslavic map near Krakow. */
public final class ChallengeArenaZone implements ZoneConfigurator {
	public static final String ZONE_NAME = "int_krakow_challenge_arena";
	public static final String KRAKOW_EXIT_ZONE = "0_krakow_n";
	public static final int KRAKOW_EXIT_X = 72;
	public static final int KRAKOW_EXIT_Y = 36;

	private static final int COMBAT_AREA_X = 6;
	private static final int COMBAT_AREA_Y = 6;
	private static final int COMBAT_AREA_WIDTH = 52;
	private static final int COMBAT_AREA_HEIGHT = 50;
	private static final int LOBBY_X = 30;
	private static final int LOBBY_Y = 60;
	private static final int COMBAT_X = 32;
	private static final int COMBAT_Y = 30;

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		final Rectangle2D shape = new Rectangle2D.Double(COMBAT_AREA_X,
				COMBAT_AREA_Y, COMBAT_AREA_WIDTH, COMBAT_AREA_HEIGHT);
		final Area combatArea = new Area(zone, shape);
		final ChallengeArenaInfo info = new ChallengeArenaInfo(combatArea, zone,
				zone.getName(), LOBBY_X, LOBBY_Y, COMBAT_X, COMBAT_Y);
		ChallengeArenaManager.configureArena(info);

		final OnePlayerArea playerGate = new OnePlayerArea(
				COMBAT_AREA_WIDTH, COMBAT_AREA_HEIGHT);
		playerGate.setPosition(COMBAT_AREA_X, COMBAT_AREA_Y);
		zone.add(playerGate);

		final ChallengeArenaRankingSign rankingSign = new ChallengeArenaRankingSign();
		rankingSign.setPosition(28, 59);
		zone.add(rankingSign);
		ChallengeArenaManager.configureRankingSign(rankingSign);

		ChallengeArenaNPC.create(zone, 32, 59);
	}
}
