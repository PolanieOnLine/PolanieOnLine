/***************************************************************************
 *                     Copyright © 2023 - Arianne                          *
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import utilities.AchievementTestHelper;

public class FightingAchievementFactoryTest extends AchievementTestHelper {
	private Player player;
	private static Collection<Creature> creatures;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		AchievementTestHelper.setUpBeforeClass();
		SingletonRepository.getEntityManager().populateCreatureList();
		creatures = SingletonRepository.getEntityManager().getCreatures();
		assertNotNull(creatures);
		assertFalse(creatures.isEmpty());
	}

	@Before
	public void setUpPlayer() {
		player = createPlayer("player");
		assertNotNull(player);
		init(player);
	}

	@After
	public void tearDownPlayer() {
		player = null;
	}

	private void resetPlayer() {
		tearDownPlayer();
		setUpPlayer();
	}

	private String[] getNormalCreatureNames() {
		final List<String> names = new ArrayList<>();
		for (final Creature cr: creatures) {
			if (!cr.isRare() && !cr.isAbnormal()) {
				names.add(cr.getName());
			}
		}
		assertFalse(names.isEmpty());
		return names.stream().toArray(String[]::new);
	}

	private String[] getRareCreatureNames() {
		final List<String> names = new ArrayList<>();
		for (final Creature cr: creatures) {
			if (cr.isRare()) {
				names.add(cr.getName());
			}
		}
		assertFalse(names.isEmpty());
		return names.stream().toArray(String[]::new);
	}

	/**
	 * Handles kills for achievements requiring a quantity of each enemy
	 * solo or shared.
	 */
	private void checkKillAnyEach(final String id, final int amount, final String... names) {
		assertTrue(achievementEnabled(id));
		for (final String name: names) {
			assertEquals(0, player.getAllKillCount(name));
			boolean alternate = false;
			while (player.getAllKillCount(name) < amount) {
				assertFalse(achievementReached(player, id));
				if (!alternate) {
					player.incSoloKillCount(name);
					alternate = true;
				} else {
					player.incSharedKillCount(name);
					alternate = false;
				}
				an.onKill(player);
			}
		}
		assertTrue(achievementReached(player, id));
	}

	/**
	 * Handles kills for achievement requiring a unique quantity of each
	 * enemy solo or shared.
	 */
	private void checkKillAnyEach(final String id, final Map<String, Integer> enemies) {
		assertTrue(achievementEnabled(id));
		for (final Map.Entry<String, Integer> enemy: enemies.entrySet()) {
			final String name = enemy.getKey();
			final int amount = enemy.getValue();
			assertEquals(0, player.getAllKillCount(name));
			boolean alternate = false;
			while (player.getAllKillCount(name) < amount) {
				assertFalse(achievementReached(player, id));
				if (!alternate) {
					player.incSoloKillCount(name);
					alternate = true;
				} else {
					player.incSharedKillCount(name);
					alternate = false;
				}
				an.onKill(player);
			}
		}
		assertTrue(achievementReached(player, id));
	}

	/**
	 * Handles kills for achievement requiring a quantity of each enemy
	 * solo.
	 */
	private void checkKillSoloEach(final String id, final int amount, final String... names) {
		assertTrue(achievementEnabled(id));
		for (final String name: names) {
			assertEquals(0, player.getAllKillCount(name));
			while (player.getSoloKillCount(name) < amount) {
				assertFalse(achievementReached(player, id));
				player.incSoloKillCount(name);
				an.onKill(player);
			}
		}
		assertTrue(achievementReached(player, id));
	}

	/**
	 * Handles kills for achievement requiring a quanity of each enemy
	 * shared.
	 */
	private void checkKillSharedEach(final String id, final int amount, final String... names) {
		assertTrue(achievementEnabled(id));
		for (final String name: names) {
			assertEquals(0, player.getAllKillCount(name));
			while (player.getSharedKillCount(name) < amount) {
				assertFalse(achievementReached(player, id));
				player.incSharedKillCount(name);
				an.onKill(player);
			}
		}
		assertTrue(achievementReached(player, id));
	}

	/**
	 * Handles kills for achievement requiring a combined quantity from
	 * from any of the enemies solo or shared.
	 */
	private void checkKillAnyCombined(final String id, int amount, final String... names) {
		assertTrue(achievementEnabled(id));
		final int amounteach = amount / names.length;
		int rem = amount % amounteach;
		for (final String name: names) {
			assertEquals(0, player.getAllKillCount(name));
			boolean alternate = false;
			amount = amounteach + rem;
			while (player.getAllKillCount(name) < amount) {
				assertFalse(achievementReached(player, id));
				if (!alternate) {
					player.incSoloKillCount(name);
					alternate = true;
				} else {
					player.incSharedKillCount(name);
					alternate = false;
				}
				an.onKill(player);
			}
			// don't multiply remainder
			rem = 0;
		}
		assertTrue(achievementReached(player, id));
	}

	@Test
	public void testRatHunter() {
		checkKillAnyEach(FightingAchievementFactory.ID_RATS, 15, "szczur");
	}

	@Test
	public void testExterminator() {
		checkKillAnyEach(FightingAchievementFactory.ID_EXTERMINATOR, 10,
				FightingAchievementFactory.ENEMIES_EXTERMINATOR);
	}

	@Test
	public void testDeerHunter() {
		checkKillAnyEach(FightingAchievementFactory.ID_DEER, 25, "jeleń");
	}

	@Test
	public void testBoarHunter() {
		checkKillAnyEach(FightingAchievementFactory.ID_BOARS, 20, "dzik");
	}

	@Test
	public void testBearHunter() {
		checkKillAnyEach(FightingAchievementFactory.ID_BEARS, 10,
				FightingAchievementFactory.ENEMIES_BEARS);
	}

	@Test
	public void testFoxHunter() {
		checkKillAnyEach(FightingAchievementFactory.ID_FOXES, 20, "lisica");
	}

	@Test
	public void testSafari() {
		final Map<String, Integer> enemies = new HashMap<String, Integer>() {{
			put("tygrys", 30);
			put("lew", 30);
			put("słoń", 50);
		}};
		checkKillAnyEach(FightingAchievementFactory.ID_SAFARI, enemies);
	}

	@Test
	public void testWoodCutter() {
		checkKillAnyEach(FightingAchievementFactory.ID_ENTS, 10, "drzewiec", "drzewcowa", "uschły drzewiec");
	}

	@Test
	public void testPoacher() {
		final String id = FightingAchievementFactory.ID_POACHER;
		for (final String name: getRareCreatureNames()) {
			checkKillAnyEach(id, 1, name);
			resetPlayer();
		}
		// check that a normal creature does not invoke achievement
		assertFalse(achievementReached(player, id));
		player.incSoloKillCount("lisica");
		an.onKill(player);
		assertFalse(achievementReached(player, id));
	}

	@Test
	public void testLegend() {
		checkKillSoloEach(FightingAchievementFactory.ID_LEGEND, 1, getNormalCreatureNames());
	}

	@Test
	public void testTeamPlayer() {
		checkKillSharedEach(FightingAchievementFactory.ID_TEAM_PLAYER, 1,
				getNormalCreatureNames());
	}

	@Test
	public void testDavidVsGoliath() {
		checkKillSoloEach(FightingAchievementFactory.ID_GIANTS, 20,
				FightingAchievementFactory.ENEMIES_GIANTS);
	}

	@Test
	public void testHeavenlyWrath() {
		checkKillAnyEach(FightingAchievementFactory.ID_ANGELS, 100,
				FightingAchievementFactory.ENEMIES_ANGELS);
	}

	@Test
	public void testSilverBullet() {
		checkKillAnyEach(FightingAchievementFactory.ID_WEREWOLF, 500, "wilkołak");
	}

	@Test
	public void testSerenadeTheSiren() {
		checkKillAnyCombined(FightingAchievementFactory.ID_MERMAIDS, 5000,
				FightingAchievementFactory.ENEMIES_MERMAIDS);
	}

	@Test
	public void testDeepSeaFisherman() {
		checkKillAnyEach(FightingAchievementFactory.ID_DEEPSEA, 500,
				FightingAchievementFactory.ENEMIES_DEEPSEA);
	}

	@Test
	public void testZombieApocalypse() {
		checkKillAnyCombined(FightingAchievementFactory.ID_ZOMBIES, 500,
				FightingAchievementFactory.ENEMIES_ZOMBIES);
	}

	@Test
	public void testChickenNuggets() {
		checkKillAnyEach(FightingAchievementFactory.ID_FOWL, 100,
				FightingAchievementFactory.ENEMIES_FOWL);
	}

	@Test
	public void testPachydermMayhem() {
		checkKillAnyEach(FightingAchievementFactory.ID_PACHYDERM, 100,
				FightingAchievementFactory.ENEMIES_PACHYDERM);
	}
}
