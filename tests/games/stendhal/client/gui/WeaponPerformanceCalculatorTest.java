/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.gui.WeaponPerformanceCalculator.WeaponPerformance;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class WeaponPerformanceCalculatorTest {
	@Test
	public void testMeleeWeaponPerformance() {
		final RPObject object = new RPObject();
		object.put("atk", 30);
		object.put("rate", 5);

		final WeaponPerformance performance =
				WeaponPerformanceCalculator.calculate(object);

		assertEquals(30, performance.getAttackPoints());
		assertEquals(5, performance.getAttackRate());
		assertEquals(1.5, performance.getAttackIntervalSeconds(), 0.0001);
		assertEquals(2.0 / 3.0, performance.getAttacksPerSecond(), 0.0001);
		assertEquals(20.0, performance.getBaseDps(), 0.0001);
		assertFalse(performance.isRanged());
	}

	@Test
	public void testPerformanceReadsVisibleTooltipMap() {
		/* A typed item is required because RPObject maps must be declared in its
		 * RPClass before values can be inserted. */
		final Item object = ItemTestHelper.createItem("tooltip weapon");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.ATTACK, "32");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.ATTACK_RATE, "2");

		final WeaponPerformance performance =
				WeaponPerformanceCalculator.calculate(object);

		assertEquals(32, performance.getAttackPoints());
		assertEquals(2, performance.getAttackRate());
		assertEquals(0.6, performance.getAttackIntervalSeconds(), 0.0001);
		assertEquals(53.3333, performance.getBaseDps(), 0.001);
	}

	@Test
	public void testRangedWeaponPerformance() {
		final RPObject object = new RPObject();
		object.put("ratk", 24);
		object.put("rate", 4);

		final WeaponPerformance performance =
				WeaponPerformanceCalculator.calculate(object);

		assertEquals(24, performance.getAttackPoints());
		assertEquals(20.0, performance.getBaseDps(), 0.0001);
		assertTrue(performance.isRanged());
	}

	@Test
	public void testDefaultRateAndNonWeaponFallback() {
		final RPObject weapon = new RPObject();
		weapon.put("atk", 15);
		final WeaponPerformance performance =
				WeaponPerformanceCalculator.calculate(weapon);

		assertEquals(5, performance.getAttackRate());
		assertEquals(10.0, performance.getBaseDps(), 0.0001);

		assertNull(WeaponPerformanceCalculator.calculate(new RPObject()));
		assertNull(WeaponPerformanceCalculator.calculate(null));
	}
}
