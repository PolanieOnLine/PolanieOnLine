/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class TacticalEquipmentAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void flatDefenseRemainsLegacyButIsNoLongerInRandomRegistry() {
		assertNull(ItemAffixRegistry.getInstance().get(
				EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE));
		assertNotNull(ItemAffixRegistry.getInstance().get(
				EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE));
		assertNotNull(ItemAffixRegistry.getInstance().get(
				EquipmentAffixService.HUNTER_MARK_ATTRIBUTE));
		assertNotNull(ItemAffixRegistry.getInstance().get(
				EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE));
	}

	@Test
	public void spikedPlatingRollsTwoToFourPercentOnlyOnBodyArmorAndShields() {
		final Item armour = item("armor", 100);
		final Item shield = item("shield", 100);
		final Item helmet = item("helmet", 100);

		assertTrue(EquipmentAffixService.applySpikedPlating(armour,
				new Random(7L)));
		assertTrue(EquipmentAffixService.applySpikedPlating(shield,
				new Random(11L)));
		assertBetween(armour.getDouble(EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE),
				0.02, 0.04);
		assertBetween(shield.getDouble(EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE),
				0.02, 0.04);
		assertFalse(EquipmentAffixService.applySpikedPlating(helmet,
				new Random(13L)));
	}

	@Test
	public void hunterMarkAndGiantSlayerAreArmorAffixesNotJewelleryAffixes() {
		final Item helmet = item("helmet", 20);
		final Item ring = item("ring", 0);

		assertTrue(EquipmentAffixService.applyHunterMark(helmet,
				new Random(17L)));
		assertTrue(helmet.has(EquipmentAffixService.HUNTER_MARK_ATTRIBUTE));
		assertFalse(EquipmentAffixService.applyHunterMark(ring,
				new Random(19L)));

		final Item boots = item("boots", 20);
		assertTrue(EquipmentAffixService.applyGiantSlayer(boots,
				new Random(23L)));
		assertTrue(boots.has(EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE));
		assertFalse(EquipmentAffixService.applyGiantSlayer(ring,
				new Random(29L)));
	}

	private Item item(final String itemClass, final int defense) {
		final Map<String, String> attributes = new HashMap<String, String>();
		if (defense > 0) {
			attributes.put("def", Integer.toString(defense));
		}
		return new Item("tactical affix test", itemClass, "test", attributes);
	}

	private void assertBetween(final double value, final double minimum,
			final double maximum) {
		assertTrue("Expected " + value + " >= " + minimum, value >= minimum);
		assertTrue("Expected " + value + " <= " + maximum, value <= maximum);
	}
}
