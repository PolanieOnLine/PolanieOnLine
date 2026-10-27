/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.common.Rand;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class RebornSystemTest {

	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void testCombatSkillCapsGrowThroughTenReborns() {
		final Player player = PlayerTestHelper.createPlayer("reborn-skill-caps");
		player.setLevel(597);
		player.setAtk(300);
		player.setRatk(300);
		player.setDef(300);
		final int[] reborns = { -1, 0, 1, 5, 9, 10, 11, Integer.MAX_VALUE };
		final int[] attackCaps = { 123, 123, 131, 163, 195, 203, 203, 203 };
		final int[] defenceCaps = { 144, 144, 152, 184, 216, 224, 224, 224 };
		for (int i = 0; i < reborns.length; i++) {
			player.put(RebornSystem.ATTR_REBORNS, reborns[i]);
			assertEquals("ATK at reborn " + reborns[i], attackCaps[i], player.getCappedAtk());
			assertEquals("RATK at reborn " + reborns[i], attackCaps[i], player.getCappedRatk());
			assertEquals("DEF at reborn " + reborns[i], defenceCaps[i], player.getCappedDef());
		}
	}

	@Test
	public void testCombatSkillCapsFollowLevelWithoutGrantingTrainedSkills() {
		final Player player = PlayerTestHelper.createPlayer("reborn-trained-skills");
		player.put(RebornSystem.ATTR_REBORNS, 10);
		player.setAtk(193);
		player.setRatk(190);
		player.setDef(210);
		player.setMining(300);
		assertEquals(95, player.getCappedAtk());
		assertEquals(95, player.getCappedRatk());
		assertEquals(95, player.getCappedDef());
		assertEquals(60, player.getCappedMining());
		player.setLevel(597);
		assertEquals(193, player.getCappedAtk());
		assertEquals(190, player.getCappedRatk());
		assertEquals(210, player.getCappedDef());
		assertEquals(193, player.getAtk());
		assertEquals(190, player.getRatk());
		assertEquals(210, player.getDef());
		player.setAtk(20);
		player.setRatk(20);
		player.setDef(20);
		assertEquals(20, player.getCappedAtk());
		assertEquals(20, player.getCappedRatk());
		assertEquals(20, player.getCappedDef());
	}

	@Test
	public void testRebornDoesNotMultiplyDamageBelowTheSkillCap() throws Exception {
		final Player attacker = PlayerTestHelper.createPlayer("reborn-damage-attacker");
		final Player defender = PlayerTestHelper.createPlayer("reborn-damage-defender");
		attacker.setLevel(597);
		attacker.setAtk(100);
		attacker.addKarma(-attacker.getKarma());
		defender.setLevel(597);
		defender.setDef(100);
		defender.addKarma(-defender.getKarma());
		final Field randomField = Rand.class.getDeclaredField("rand");
		randomField.setAccessible(true);
		final Random previousRandom = (Random) randomField.get(null);
		try {
			final int[] baseline = new int[32];
			randomField.set(null, new Random(1433));
			for (int i = 0; i < baseline.length; i++) {
				baseline[i] = attacker.damageDone(defender, 114.5, Nature.LIGHT);
			}
			for (final int reborns : new int[] { 1, 5, 10, 11 }) {
				attacker.put(RebornSystem.ATTR_REBORNS, reborns);
				randomField.set(null, new Random(1433));
				for (int i = 0; i < baseline.length; i++) {
					assertEquals("damage at reborn " + reborns, baseline[i],
							attacker.damageDone(defender, 114.5, Nature.LIGHT));
				}
			}
		} finally {
			randomField.set(null, previousRandom);
		}
	}

	@Test
	public void testSkillCapsGrowThroughTenWhileHealthRewardsStopAtFive() {
		final Player player = PlayerTestHelper.createPlayer("reborn-bonuses");

		assertBonuses(player, 0, 0, 0, 0);
		assertBonuses(player, 1, 8, 0, 1000);
		assertBonuses(player, 2, 16, 0, 2000);
		assertBonuses(player, 3, 24, 0, 3000);
		assertBonuses(player, 4, 32, 0, 4000);
		assertBonuses(player, 5, 40, 0, 6000);
		assertBonuses(player, 6, 48, -5, 6000);
		assertBonuses(player, 7, 56, -10, 6000);
		assertBonuses(player, 8, 64, -15, 6000);
		assertBonuses(player, 9, 72, -20, 6000);
		assertBonuses(player, 10, 80, -25, 6000);
		assertBonuses(player, 100, 80, -25, 6000);
		assertBonuses(player, Integer.MAX_VALUE, 80, -25, 6000);
	}

	@Test
	public void testCombatExperienceScaling() {
		final Player player = PlayerTestHelper.createPlayer("reborn-scaling");

		player.put(RebornSystem.ATTR_REBORNS, 1);
		assertEquals(100, RebornSystem.applyCombatExperienceBonus(player, 100));

		player.put(RebornSystem.ATTR_REBORNS, 5);
		assertEquals(100, RebornSystem.applyCombatExperienceBonus(player, 100));

		final int[] rewards = { 95, 90, 85, 80, 75 };
		for (int reborns = 6; reborns <= 10; reborns++) {
			player.put(RebornSystem.ATTR_REBORNS, reborns);
			assertEquals(rewards[reborns - 6], RebornSystem.applyCombatExperienceBonus(player, 100));
		}

		player.put(RebornSystem.ATTR_REBORNS, 50);
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

		final int cappedBaseHP = Short.MAX_VALUE + levelHealth;
		player.setLevel(Level.maxLevel());
		player.setBaseHP(cappedBaseHP);
		player.setHP(player.getBaseHP() - missingHealth);
		assertEquals(2, RebornSystem.performReborn(player));
		assertEquals(Short.MAX_VALUE + 1000, player.getUncappedBaseHP());
		assertEquals(Short.MAX_VALUE, player.getBaseHP());
		assertEquals(Short.MAX_VALUE - missingHealth, player.getHP());
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
			final int skillCapBonus, final int combatExperiencePercent,
			final int healthBonus) {
		player.put(RebornSystem.ATTR_REBORNS, reborns);
		assertEquals(skillCapBonus, RebornSystem.getSkillCapBonus(player));
		assertEquals(combatExperiencePercent,
				RebornSystem.getCombatExperienceBonusPercent(player));
		assertEquals(healthBonus, RebornSystem.getHealthBonus(player));
	}
}
