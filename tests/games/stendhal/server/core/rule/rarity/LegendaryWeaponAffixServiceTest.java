/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class LegendaryWeaponAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void mercilessReachAddsExactlyOneTileToWhipRange() {
		final Item whip = weapon("whip", 1);

		assertTrue(LegendaryWeaponAffixService.applyMercilessReach(
				whip, new Random(1L)));

		assertEquals(2, whip.getRange());
		assertTrue(whip.has(
				WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE));
	}

	@Test
	public void mercilessReachCannotBeAppliedTwice() {
		final Item whip = weapon("whip", 1);
		assertTrue(LegendaryWeaponAffixService.applyMercilessReach(
				whip, new Random(1L)));
		assertFalse(LegendaryWeaponAffixService.applyMercilessReach(
				whip, new Random(2L)));
		assertEquals(2, whip.getRange());
	}

	@Test
	public void mercilessReachRejectsNonWhips() {
		final Item sword = weapon("sword", 1);

		assertFalse(LegendaryWeaponAffixService.applyMercilessReach(
				sword, new Random(1L)));
		assertEquals(1, sword.getRange());
		assertFalse(sword.has(
				WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE));
	}

	private Item weapon(final String itemClass, final int range) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "20");
		attributes.put("rate", "5");
		attributes.put("range", Integer.toString(range));
		return new Item("legendary reach test", itemClass, "test", attributes);
	}
}
