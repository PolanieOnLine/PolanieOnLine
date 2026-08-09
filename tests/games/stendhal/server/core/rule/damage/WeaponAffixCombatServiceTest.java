/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingAttacker;
import games.stendhal.server.entity.status.StatusType;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class WeaponAffixCombatServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void weightedFractionIncludesWeaponsWithoutAffix() {
		final Item first = weapon("sword", 20);
		final Item second = weapon("sword", 20);
		first.put(WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE, 0.20);

		assertEquals(0.10, WeaponAffixCombatService.getWeightedFraction(
				Arrays.asList(first, second),
				WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE), 0.0000001);
	}

	@Test
	public void weightedLegendaryBonusIncludesWeaponWithoutSignature() {
		final Item first = weapon("ranged", 30);
		final Item second = weapon("ranged", 10);
		first.put(WeaponAffixCombatService.LEGENDARY_LONGSHOT_ATTRIBUTE, 1.0);

		assertEquals(0.1875, WeaponAffixCombatService.getWeightedFixedBonus(
				Arrays.asList(first, second),
				WeaponAffixCombatService.LEGENDARY_LONGSHOT_ATTRIBUTE, 0.25),
				0.0000001);
	}

	@Test
	public void executeOnlyActivatesBelowQuarterHealth() {
		final Player target = target(100, 24);
		assertTrue(WeaponAffixCombatService.isExecuteActive(target));
		target.setHP(25);
		assertFalse(WeaponAffixCombatService.isExecuteActive(target));
	}

	@Test
	public void legendaryExecutionerOnlyActivatesBelowTwentyPercentHealth() {
		final Player target = target(100, 19);
		assertTrue(WeaponAffixCombatService.isLegendaryExecutionActive(target));
		target.setHP(20);
		assertFalse(WeaponAffixCombatService.isLegendaryExecutionActive(target));
	}

	@Test
	public void executeAndDistanceBonusesStackOnActualRangedHit() {
		final Item weapon = weapon("dagger", 20);
		weapon.put(WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE, 0.25);
		weapon.put(WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE, 0.20);
		final Player target = target(100, 20);

		assertEquals(150, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, true));
		assertEquals(125, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, false));
	}

	@Test
	public void legendaryLongshotRequiresActualRangedAttack() {
		final Item weapon = weapon("ranged", 20);
		weapon.put(WeaponAffixCombatService.LEGENDARY_LONGSHOT_ATTRIBUTE, 1.0);
		final Player target = target(100, 100);

		assertEquals(125, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, true));
		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, false));
	}

	@Test
	public void legendaryExecutionerAddsThirtyFivePercentBelowThreshold() {
		final Item weapon = weapon("axe", 20);
		weapon.put(WeaponAffixCombatService.LEGENDARY_EXECUTIONER_ATTRIBUTE, 1.0);
		final Player target = target(100, 19);

		assertEquals(135, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, false));
		target.setHP(20);
		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, false));
	}

	@Test
	public void distanceBonusRequiresActualRangedAttack() {
		final Item weapon = weapon("ranged", 20);
		weapon.put(WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE, 0.20);
		final Player target = target(100, 100);

		assertEquals(120, WeaponAffixCombatService.applyConditionalDamageBonuses(
				100, Arrays.asList(weapon), target, true));
		assertEquals(100, WeaponAffixCombatService.applyConditionalDamageBonuses(
					100, Arrays.asList(weapon), target, false));
	}

	@Test
	public void procChancesCombineIndependentlyAndAreCapped() {
		final Item first = weapon("sword", 20);
		final Item second = weapon("sword", 20);
		first.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.10);
		second.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.10);
		assertEquals(0.19, WeaponAffixCombatService.combinedProcChance(
				Arrays.asList(first, second),
				WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE), 0.0000001);

		first.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.15);
		second.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.15);
		assertEquals(0.25, WeaponAffixCombatService.combinedProcChance(
				Arrays.asList(first, second),
				WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE), 0.0000001);
	}

	@Test
	public void bleedProcDelegatesCombinedChanceToBleedingTwoPointZero() {
		final Item first = weapon("sword", 20);
		final Item second = weapon("sword", 20);
		first.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.10);
		second.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.10);

		final BleedingAttacker bleeding =
				WeaponAffixCombatService.createBleedingAttacker(
						Arrays.asList(first, second));

		assertEquals(19.0, bleeding.getProbability(), 0.0000001);
		assertEquals(0.25, bleeding.getDamageFactor(), 0.0);
	}

	@Test
	public void legendaryDeepWoundsWorksWithoutNormalBleedAffix() {
		final Item weapon = weapon("sword", 20);
		weapon.put(WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 1.0);

		final BleedingAttacker bleeding =
				WeaponAffixCombatService.createBleedingAttacker(
						Arrays.asList(weapon));

		assertEquals(15.0, bleeding.getProbability(), 0.0000001);
		assertEquals(0.35, bleeding.getDamageFactor(), 0.0);
	}

	@Test
	public void legendaryAndNormalBleedShareOneCappedProc() {
		final Item weapon = weapon("sword", 20);
		weapon.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.15);
		weapon.put(WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 1.0);

		final BleedingAttacker bleeding =
				WeaponAffixCombatService.createBleedingAttacker(
						Arrays.asList(weapon));

		assertEquals(25.0, bleeding.getProbability(), 0.0000001);
		assertEquals(0.35, bleeding.getDamageFactor(), 0.0);
	}

	@Test
	public void twoDeepWoundsSourcesRespectGlobalProcCap() {
		final Item first = weapon("sword", 20);
		final Item second = weapon("axe", 20);
		first.put(WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 1.0);
		second.put(WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 1.0);

		assertEquals(0.25, WeaponAffixCombatService.combinedFixedProcChance(
				Arrays.asList(first, second),
				WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, 0.15),
				0.0000001);
	}

	@Test
	public void bleedProcCapIsConvertedToPercentExactlyOnce() {
		final Item first = weapon("sword", 20);
		final Item second = weapon("sword", 20);
		first.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.15);
		second.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.15);

		final BleedingAttacker bleeding =
				WeaponAffixCombatService.createBleedingAttacker(
						Arrays.asList(first, second));

		assertEquals(25.0, bleeding.getProbability(), 0.0000001);
	}

	@Test
	public void noBleedAffixDoesNotCreateBleedingAttacker() {
		assertNull(WeaponAffixCombatService.createBleedingAttacker(
				Arrays.asList(weapon("sword", 20))));
	}

	@Test
	public void equipmentResistanceResolverReducesPoisonProcChance() {
		final Player target = target(100, 100);
		target.put("resist_poisoned", 0.50);

		assertEquals(0.10, WeaponAffixCombatService.effectiveStatusChance(
				target, StatusType.POISONED, 0.20), 0.0000001);
	}

	@Test
	public void deterministicProcRollUsesExactFractionBoundary() {
		assertTrue(WeaponAffixCombatService.rollChance(0.10, 1000));
		assertFalse(WeaponAffixCombatService.rollChance(0.10, 1001));
	}

	private Player target(final int baseHp, final int hp) {
		final Player target = PlayerTestHelper.createPlayer("affix target");
		target.setBaseHP(baseHp);
		target.setHP(hp);
		return target;
	}

	private Item weapon(final String itemClass, final int attack) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		return new Item("affix combat weapon", itemClass, "test", attributes);
	}
}
