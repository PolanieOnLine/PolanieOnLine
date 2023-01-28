/***************************************************************************
 *                    Copyright © 2020-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import utilities.AchievementTestHelper;

public class ExperienceAchievementFactoryTest extends AchievementTestHelper {
	private final Player player;

	public ExperienceAchievementFactoryTest() {
		player = createPlayer("player");
	}

	@Before
	public void setUp() throws Exception {
		assertNotNull(player);
		init(player);
	}

	@Test
	public void initTests() {
		doCycle(ExperienceAchievementFactory.ID_PAROBEK, 10);
		doCycle(ExperienceAchievementFactory.ID_CHLOP, 50);
		doCycle(ExperienceAchievementFactory.ID_KMIEC, 100);
		doCycle(ExperienceAchievementFactory.ID_MIESZCZANIN, 150);
		doCycle(ExperienceAchievementFactory.ID_SZLACHCIC, 200);
		doCycle(ExperienceAchievementFactory.ID_RYCERZ, 250);
		doCycle(ExperienceAchievementFactory.ID_BARONET, 300);
		doCycle(ExperienceAchievementFactory.ID_BARON, 350);
		doCycle(ExperienceAchievementFactory.ID_WICEHRABIA, 400);
		doCycle(ExperienceAchievementFactory.ID_HRABIA, 450);
		doCycle(ExperienceAchievementFactory.ID_MAGNAT, 500);
		doCycle(ExperienceAchievementFactory.ID_KSIAZE, 550);
		doCycle(ExperienceAchievementFactory.ID_KROL, 597);
	}

	private void doCycle(final String id, final int reqLevel) {
		while(player.getLevel() < reqLevel) {
			assertFalse(achievementReached(player, id));
			player.setLevel(player.getLevel() + 1);
		}
		assertTrue(achievementReached(player, id));
	}
}
