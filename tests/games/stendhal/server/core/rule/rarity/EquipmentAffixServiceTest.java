/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.EquipmentStatusResistanceService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StatusResistantItem;
import games.stendhal.server.entity.status.StatusType;
import utilities.RPClass.ItemTestHelper;

public class EquipmentAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void flatDefenseIsEquipmentOnlyAndMaterializesFinalDefense() {
		final Item armour = item("armor", 100);
		final Item sword = item("sword", 100);

		assertTrue(EquipmentAffixService.applyFlatDefense(armour,
				new Random(7L)));
		assertTrue(armour.getInt(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE)
				>= 5);
		assertTrue(armour.getInt(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE)
				<= 15);
		assertTrue(armour.getInt("def") > 100);
		assertFalse(EquipmentAffixService.applyFlatDefense(sword,
				new Random(7L)));
	}

	@Test
	public void accessoryFlatAttackAndDefenseUseSmallExactRolls() {
		final Item ring = item("ring", 0);

		assertTrue(EquipmentAffixService.applyFlatAttack(ring,
				new Random(11L)));
		assertTrue(ring.getInt(EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE)
				>= 1);
		assertTrue(ring.getInt(EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE)
				<= 3);
		assertTrue(ring.getInt("atk") >= 1 && ring.getInt("atk") <= 3);

		assertTrue(EquipmentAffixService.applyFlatDefense(ring,
				new Random(13L)));
		assertTrue(ring.getInt(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE)
				>= 1);
		assertTrue(ring.getInt(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE)
				<= 3);
		assertTrue(ring.getInt("def") >= 1 && ring.getInt("def") <= 3);
	}

	@Test
	public void accessoryOffensivePercentageRollsStayInsideConfiguredRanges() {
		final Item accuracy = item("necklace", 0);
		final Item criticalChance = item("ring", 0);
		final Item criticalDamage = item("ring", 0);

		assertTrue(EquipmentAffixService.applyAccessoryAccuracy(accuracy,
				new Random(17L)));
		assertBetween(accuracy.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE),
				3.0, 10.0);

		assertTrue(EquipmentAffixService.applyAccessoryCriticalChance(
				criticalChance, new Random(19L)));
		assertBetween(criticalChance.getDouble(
				CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE), 2.0, 7.0);

		assertTrue(EquipmentAffixService.applyAccessoryCriticalDamage(
				criticalDamage, new Random(23L)));
		assertBetween(criticalDamage.getDouble(
				CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE), 0.05, 0.15);
	}

	@Test
	public void allCombatStatusResistanceRollsUseTenToTwentyFivePercent() {
		final StatusType[] statuses = {
				StatusType.POISONED, StatusType.BLEEDING, StatusType.SHOCKED,
				StatusType.CONFUSED, StatusType.HEAVY };
		for (int i = 0; i < statuses.length; i++) {
			final Item item = item("helmet", 20);
			final StatusType status = statuses[i];
			assertTrue(EquipmentAffixService.applyStatusResistance(item, status,
					new Random(31L + i)));
			final String attribute = EquipmentStatusResistanceService
					.getResistanceAttribute(status);
			assertBetween(item.getDouble(attribute), 0.10, 0.25);
		}
	}

	@Test
	public void intrinsicAccessoryAttributePreventsEquivalentRolledAffix() {
		final Item ring = item("ring", 0);
		ring.put(WeaponAffixService.ACCURACY_ATTRIBUTE, 12.0);

		assertFalse(EquipmentAffixService.isAccessoryAccuracyEligible(ring));
		assertFalse(EquipmentAffixService.applyAccessoryAccuracy(ring,
				new Random(37L)));
	}

	@Test
	public void intrinsicStatusResistantItemPreventsEquivalentRolledResistance() {
		final StatusResistantItem ring = new StatusResistantItem(
				"intrinsic resistance ring", "ring", "test", null);
		final Map<StatusType, Double> resistances =
				new EnumMap<StatusType, Double>(StatusType.class);
		resistances.put(StatusType.POISONED, Double.valueOf(0.40));
		ring.initializeStatusResistancesList(resistances);

		assertFalse(EquipmentAffixService.isStatusResistanceEligible(
				ring, StatusType.POISONED));
		assertTrue(EquipmentAffixService.isStatusResistanceEligible(
				ring, StatusType.BLEEDING));
	}

	private Item item(final String itemClass, final int defense) {
		final Map<String, String> attributes = new HashMap<String, String>();
		if (defense > 0) {
			attributes.put("def", Integer.toString(defense));
		}
		return new Item("equipment affix test", itemClass, "test", attributes);
	}

	private void assertBetween(final double value, final double minimum,
			final double maximum) {
		assertTrue("Expected " + value + " >= " + minimum, value >= minimum);
		assertTrue("Expected " + value + " <= " + maximum, value <= maximum);
	}
}
