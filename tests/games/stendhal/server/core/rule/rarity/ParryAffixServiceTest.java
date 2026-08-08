/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ParryAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void randomParryAffixIsSwordOnly() {
		assertTrue(ParryAffixService.isEligible(item("sword")));
		assertFalse(ParryAffixService.isEligible(item("axe")));
		assertFalse(ParryAffixService.isEligible(item("club")));
		assertFalse(ParryAffixService.isEligible(item("dagger")));
		assertFalse(ParryAffixService.isEligible(item("ranged")));
	}

	@Test
	public void existingParryValueIsNeverRerolled() {
		final Item sword = item("sword");
		sword.put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.07);

		assertFalse(ParryAffixService.isEligible(sword));
		assertEquals(0, ParryAffixService.applySelectedAffix(sword,
				new FixedRandom(0.99, 1)));
		assertEquals(0.07,
				sword.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0);
	}

	@Test
	public void selectedAffixStoresOnePermanentWholePercentRoll() {
		final Item sword = item("sword");

		assertEquals(15, ParryAffixService.applySelectedAffix(sword,
				new FixedRandom(0.95, 1)));
		assertEquals(0.15,
				sword.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0);
		assertEquals(0, ParryAffixService.applySelectedAffix(sword,
				new FixedRandom(0.0, 0)));
		assertEquals(0.15,
				sword.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0);
	}

	@Test
	public void weightedBandsStayInsideFiveToFifteenPercent() {
		assertEquals(5, ParryAffixService.rollPercent(new FixedRandom(0.00, 0)));
		assertEquals(7, ParryAffixService.rollPercent(new FixedRandom(0.24, 2)));
		assertEquals(8, ParryAffixService.rollPercent(new FixedRandom(0.25, 0)));
		assertEquals(10, ParryAffixService.rollPercent(new FixedRandom(0.64, 2)));
		assertEquals(11, ParryAffixService.rollPercent(new FixedRandom(0.65, 0)));
		assertEquals(13, ParryAffixService.rollPercent(new FixedRandom(0.89, 2)));
		assertEquals(14, ParryAffixService.rollPercent(new FixedRandom(0.90, 0)));
		assertEquals(15, ParryAffixService.rollPercent(new FixedRandom(0.99, 1)));
	}

	private Item item(final String itemClass) {
		return new Item("parry affix test item", itemClass, "test", null);
	}

	private static final class FixedRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final double doubleValue;
		private final int intValue;

		FixedRandom(final double doubleValue, final int intValue) {
			this.doubleValue = doubleValue;
			this.intValue = intValue;
		}

		@Override
		public double nextDouble() {
			return doubleValue;
		}

		@Override
		public int nextInt(final int bound) {
			return Math.min(intValue, bound - 1);
		}
	}
}
