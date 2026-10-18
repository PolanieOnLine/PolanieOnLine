/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class RebornSystemTest {

	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void testProgressionBonusesAreCappedAtFiveReborns() {
		final Player player = PlayerTestHelper.createPlayer("reborn-bonuses");

		assertBonuses(player, 0, 0, 0, 0);
		assertBonuses(player, 1, 2, 0, 1000);
		assertBonuses(player, 2, 4, 0, 2000);
		assertBonuses(player, 3, 6, 0, 3000);
		assertBonuses(player, 4, 8, 0, 4000);
		assertBonuses(player, 5, 10, 0, 6000);
		assertBonuses(player, 6, 10, -5, 6000);
		assertBonuses(player, 7, 10, -10, 6000);
		assertBonuses(player, 8, 10, -15, 6000);
		assertBonuses(player, 9, 10, -20, 6000);
		assertBonuses(player, 10, 10, -25, 6000);
		assertBonuses(player, 100, 10, -25, 6000);
		assertBonuses(player, Integer.MAX_VALUE, 10, -25, 6000);
	}

	@Test
	public void testAttackAndCombatExperienceScaling() {
		final Player player = PlayerTestHelper.createPlayer("reborn-scaling");

		player.put(RebornSystem.ATTR_REBORNS, 1);
		assertEquals(102, RebornSystem.applyAttackBonus(player, 100));
		assertEquals(100, RebornSystem.applyCombatExperienceBonus(player, 100));

		player.put(RebornSystem.ATTR_REBORNS, 5);
		assertEquals(110, RebornSystem.applyAttackBonus(player, 100));
		assertEquals(100, RebornSystem.applyCombatExperienceBonus(player, 100));

		final int[] rewards = { 95, 90, 85, 80, 75 };
		for (int reborns = 6; reborns <= 10; reborns++) {
			player.put(RebornSystem.ATTR_REBORNS, reborns);
			assertEquals(rewards[reborns - 6], RebornSystem.applyCombatExperienceBonus(player, 100));
		}

		player.put(RebornSystem.ATTR_REBORNS, 50);
		assertEquals(110, RebornSystem.applyAttackBonus(player, 100));
		assertEquals(75, RebornSystem.applyCombatExperienceBonus(player, 100));
	}

	@Test
	public void testCombatReductionKeepsRoundingAndNonPositiveRewardsSafe() {
		final Player player = PlayerTestHelper.createPlayer("reborn-small-rewards");
		player.put(RebornSystem.ATTR_REBORNS, 10);
		assertEquals(0, RebornSystem.applyCombatExperienceBonus(player, 0));
		assertEquals(-1, RebornSystem.applyCombatExperienceBonus(player, -1));
		assertEquals(1, RebornSystem.applyCombatExperienceBonus(player, 1));
		assertEquals(3, RebornSystem.applyCombatExperienceBonus(player, 4));
		assertEquals(1610612735, RebornSystem.applyCombatExperienceBonus(player, Integer.MAX_VALUE));
		assertEquals(100, RebornSystem.applyCombatExperienceBonus(null, 100));
	}

	@Test
	public void testLegacyRebornStates() {
		assertEquals(0, RebornSystem.getLegacyRebornCount(null));
		assertEquals(0, RebornSystem.getLegacyRebornCount("start"));
		assertEquals(1, RebornSystem.getLegacyRebornCount("start;2"));
		assertEquals(4, RebornSystem.getLegacyRebornCount("start;5"));
		assertEquals(1, RebornSystem.getLegacyRebornCount("done"));
		assertEquals(2, RebornSystem.getLegacyRebornCount("done;2"));
		assertEquals(5, RebornSystem.getLegacyRebornCount("done;reborn_5"));
	}

	@Test
	public void testLegacyMigrationMovesProgressAndRewardsOutOfQuests() {
		final Player player = PlayerTestHelper.createPlayer("reborn-migration");
		player.remove(RebornSystem.ATTR_MIGRATION_VERSION);
		player.remove(RebornSystem.ATTR_REBORNS);
		player.remove(RebornSystem.ATTR_REWARDS);

		player.setQuest("reset_level", "done;reborn_5");
		player.setQuest("reborn_extra_reward3", "done");
		player.setQuest("reborn_extra_reward4", "done");
		player.setQuest("reborn_extra_reward5", "done");

		RebornSystem.migrateLegacyData(player);

		assertEquals(5, RebornSystem.getRebornCount(player));
		assertEquals(3, RebornSystem.getClaimedRewardCount(player));
		assertFalse(player.hasQuest("reset_level"));
		assertFalse(player.hasQuest("reborn_extra_reward3"));
		assertFalse(player.hasQuest("reborn_extra_reward4"));
		assertFalse(player.hasQuest("reborn_extra_reward5"));
		assertTrue(player.has(RebornSystem.ATTR_MIGRATION_VERSION));

		RebornSystem.migrateLegacyData(player);
		assertEquals(5, RebornSystem.getRebornCount(player));
		assertEquals(3, RebornSystem.getClaimedRewardCount(player));
	}

	@Test
	public void testRebornRequiresCurrentMaximumLevel() {
		final Player player = PlayerTestHelper.createPlayer("reborn-level");
		player.setLevel(Level.maxLevel() - 1);
		assertFalse(RebornSystem.canReborn(player));

		player.setLevel(Level.maxLevel());
		assertTrue(RebornSystem.canReborn(player));
	}

	@Test
	public void testRebornRemovesOnlyMaximumLevelHealth() {
		final Player player = PlayerTestHelper.createPlayer("reborn-health");
		final int levelHealth = Level.maxLevel() * 10;
		final int permanentHealth = 750;
		final int missingHealth = 125;

		player.setLevel(Level.maxLevel());
		player.setBaseHP(100 + levelHealth + permanentHealth);
		player.setHP(player.getBaseHP() - missingHealth);

		assertEquals(1, RebornSystem.performReborn(player));
		assertEquals(0, player.getLevel());
		assertEquals(0, player.getXP());
		assertEquals(100 + permanentHealth + 1000, player.getBaseHP());
		assertEquals(player.getBaseHP() - missingHealth, player.getHP());
	}

	@Test
	public void testRebornHealthRewardsStopAfterFifthReborn() {
		final Player player = PlayerTestHelper.createPlayer("reborn-health-cap");
		final int levelHealth = Level.maxLevel() * 10;
		int expectedBaseHP = 100;

		// Mark special item rewards as already received so this test only checks HP.
		player.put(RebornSystem.ATTR_REWARDS, 7);

		for (int reborn = 1; reborn <= 6; reborn++) {
			player.setLevel(Level.maxLevel());
			player.setBaseHP(expectedBaseHP + levelHealth);
			player.setHP(player.getBaseHP());

			assertEquals(reborn, RebornSystem.performReborn(player));

			if (reborn <= 4) {
				expectedBaseHP += 1000;
			} else if (reborn == 5) {
				expectedBaseHP += 2000;
			}
			assertEquals(expectedBaseHP, player.getBaseHP());
			assertEquals(expectedBaseHP, player.getHP());
		}
	}

	private void assertBonuses(final Player player, final int reborns,
			final int attackPercent, final int combatExperiencePercent,
			final int healthBonus) {
		player.put(RebornSystem.ATTR_REBORNS, reborns);
		assertEquals(attackPercent, RebornSystem.getAttackBonusPercent(player));
		assertEquals(combatExperiencePercent,
				RebornSystem.getCombatExperienceBonusPercent(player));
		assertEquals(healthBonus, RebornSystem.getHealthBonus(player));
	}
}
