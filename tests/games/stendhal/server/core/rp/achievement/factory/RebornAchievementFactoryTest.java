/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;
import games.stendhal.server.events.ReachedAchievementEvent;
import marauroa.common.game.RPEvent;
import utilities.AchievementTestHelper;

public class RebornAchievementFactoryTest extends AchievementTestHelper {
	private Player player;

	@Before
	public void setUp() throws Exception {
		player = createPlayer("reborn-achievements");
		init(player);

		// These tests focus on achievement awarding, not special item rewards.
		player.put(RebornSystem.ATTR_REWARDS, 7);
	}

	@Test
	public void testAchievementsAreAwardedImmediatelyAfterReborn() {
		final String[] achievementIds = {
			RebornAchievementFactory.ID_NEWBORN,
			RebornAchievementFactory.ID_NEW_ADVENTURE,
			RebornAchievementFactory.ID_COMING,
			RebornAchievementFactory.ID_REPLAY,
			RebornAchievementFactory.ID_NEW_HISTORY
		};
		final int[] expectedHealthBonuses = {1000, 2000, 3000, 4000, 6000};
		final int levelHealth = Level.maxLevel() * 10;

		for (int reborn = 1; reborn <= achievementIds.length; reborn++) {
			assertFalse(achievementReached(player, achievementIds[reborn - 1]));

			player.setLevel(Level.maxLevel());
			player.setBaseHP(100 + levelHealth
					+ (reborn == 1 ? 0 : expectedHealthBonuses[reborn - 2]));
			player.setHP(player.getBaseHP());
			player.clearEvents();

			assertTrue(RebornSystem.performReborn(player) == reborn);
			assertTrue(achievementReached(player, achievementIds[reborn - 1]));
			assertTrue(hasAchievementPopupEvent(player));
			assertTrue(player.getBaseHP() == 100 + expectedHealthBonuses[reborn - 1]);
		}
	}

	private boolean hasAchievementPopupEvent(final Player player) {
		final Iterator<RPEvent> events = player.eventsIterator();
		while (events.hasNext()) {
			if (events.next() instanceof ReachedAchievementEvent) {
				return true;
			}
		}
		return false;
	}
}
