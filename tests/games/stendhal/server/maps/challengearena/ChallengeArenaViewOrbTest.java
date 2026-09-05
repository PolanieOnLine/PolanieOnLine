/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.useable.ViewChangeEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;

/** Tests the Challenge Arena observation orbs. */
public class ChallengeArenaViewOrbTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendhalRPRuleProcessor.get();
		MockStendlRPWorld.get();
	}

	@Test
	public void viewOrbsCenterOnCombatAreaWithoutScryingRequirements() {
		final StendhalRPZone zone = new StendhalRPZone(
				"int_tarnow_challenge_arena_view_orb_test", 64, 64);
		ChallengeArenaZone.configureViewOrbs(zone);

		assertViewOrb(zone, ChallengeArenaZone.LEFT_VIEW_ORB_X, "left-viewer");
		assertViewOrb(zone, ChallengeArenaZone.RIGHT_VIEW_ORB_X, "right-viewer");
	}

	private void assertViewOrb(final StendhalRPZone zone, final int orbX,
			final String playerName) {
		final ViewChangeEntity orb = findViewOrb(zone, orbX, ChallengeArenaZone.VIEW_ORB_Y);
		assertNotNull("Expected a view orb at " + orbX + ","
				+ ChallengeArenaZone.VIEW_ORB_Y, orb);

		final Player player = PlayerTestHelper.createPlayer(playerName);
		player.setPosition(orbX, ChallengeArenaZone.VIEW_ORB_Y + 1);
		zone.add(player);

		final int previousEventCount = player.events().size();
		assertTrue("Arena view orb should be usable without quest or money", orb.onUsed(player));
		assertEquals(previousEventCount + 1, player.events().size());

		final RPEvent event = player.events().get(player.events().size() - 1);
		assertEquals(Events.VIEW_CHANGE, event.getName());
		assertEquals(ChallengeArenaZone.COMBAT_X, event.getInt("x"));
		assertEquals(ChallengeArenaZone.COMBAT_Y, event.getInt("y"));
	}

	private ViewChangeEntity findViewOrb(final StendhalRPZone zone, final int x,
			final int y) {
		for (final Entity entity : zone.getEntitiesAt(x, y)) {
			if (entity instanceof ViewChangeEntity) {
				return (ViewChangeEntity) entity;
			}
		}
		return null;
	}
}
