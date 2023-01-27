/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.experience;

import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_PAROBEK;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_CHLOP;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_KMIEC;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_MIESZCZANIN;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_SZLACHCIC;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_RYCERZ;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_BARONET;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_BARON;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_WICEHRABIA;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_HRABIA;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_MAGNAT;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_KSIAZE;
import static games.stendhal.server.core.rp.achievement.factory.ExperienceAchievementFactory.ID_KROL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.server.game.db.DatabaseFactory;
import utilities.AchievementTestHelper;
import utilities.PlayerTestHelper;
import utilities.ZoneAndPlayerTestImpl;

public class ExperienceAchievementTest extends ZoneAndPlayerTestImpl {
	private Player player;

	private final List<String> idList = Arrays.asList(ID_PAROBEK, ID_CHLOP,
			ID_KMIEC, ID_MIESZCZANIN, ID_SZLACHCIC, ID_RYCERZ, ID_BARONET,
			ID_BARON, ID_WICEHRABIA, ID_HRABIA, ID_MAGNAT, ID_KSIAZE,
			ID_KROL);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DatabaseFactory().initializeDatabase();
		// initialize world
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		PlayerTestHelper.removeAllPlayers();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		zone = setupZone("testzone");

		super.setUp();
	}

	@Test
	public void init() {
		resetPlayer();

		doCycle(ID_PAROBEK, 10);
		doCycle(ID_CHLOP, 50);
		doCycle(ID_KMIEC, 100);
		doCycle(ID_MIESZCZANIN, 150);
		doCycle(ID_SZLACHCIC, 200);
		doCycle(ID_RYCERZ, 250);
		doCycle(ID_BARONET, 300);
		doCycle(ID_BARON, 350);
		doCycle(ID_WICEHRABIA, 400);
		doCycle(ID_HRABIA, 450);
		doCycle(ID_MAGNAT, 500);
		doCycle(ID_KSIAZE, 550);
		doCycle(ID_KROL, 597);
	}

	/**
	 * Resets player achievements.
	 */
	private void resetPlayer() {
		player = null;
		assertNull(player);
		player = PlayerTestHelper.createPlayer("player");
		assertNotNull(player);

		AchievementTestHelper.init(player);
		for (final String ID: idList) {
			assertFalse(achievementReached(ID));
		}
	}

	private void doCycle(final String id, final int reqLevel) {
		while(player.getLevel() < reqLevel) {
			assertFalse(achievementReached(id));
			player.setLevel(player.getLevel() + 1);
		}
		assertTrue(achievementReached(id));
	}

	/**
	 * Checks if the player has reached the achievement.
	 *
	 * @return
	 * 		<code>true</player> if the player has the achievement.
	 */
	private boolean achievementReached(final String ID) {
		return AchievementTestHelper.achievementReached(player, ID);
	}
}
