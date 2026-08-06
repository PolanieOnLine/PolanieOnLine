/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityDamageRangeTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void oneRarityRollScalesAttackAndBothRangeEndpoints() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "100");
		attributes.put("rate", "5");
		final Weapon weapon = new Weapon("test axe", "axe", "test", attributes);
		weapon.setEquipableSlots(Arrays.asList("rhand", "bag"));
		weapon.configureRarity(null, "default", 1000);
		final CountingRandom random = new CountingRandom(0.5);

		new ItemRarityService(random).initialize(weapon,
				ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
						.withRarity(ItemRarity.RARE).build());

		assertEquals(91, weapon.getInt("damage_min"));
		assertEquals(124, weapon.getInt("damage_max"));
		assertEquals(Double.valueOf(1.075), weapon.getRarityModifier("atk"));
		assertEquals(weapon.getRarityModifier("atk"),
				weapon.getRarityModifier("damage_min"));
		assertEquals(weapon.getRarityModifier("atk"),
				weapon.getRarityModifier("damage_max"));
		// One roll for the complete damage group and one for attack speed.
		assertEquals(2, random.calls);
	}

	private static final class CountingRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final double value;
		private int calls;

		private CountingRandom(final double value) {
			this.value = value;
		}

		@Override
		public double nextDouble() {
			calls++;
			return value;
		}
	}
}
