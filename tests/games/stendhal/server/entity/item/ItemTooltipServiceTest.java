/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.common.constants.Nature;
import utilities.RPClass.ItemTestHelper;

public class ItemTooltipServiceTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void testWeaponPublishesCategoryAndTiming() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "2");
		final Item item = new Item("test axe", "axe", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals(ItemTooltip.CATEGORY_WEAPON,
				stat(item, ItemTooltip.CATEGORY));
		assertEquals(0.6, Double.parseDouble(stat(item,
				ItemTooltip.ATTACK_INTERVAL_SECONDS)), 0.0001);
		assertEquals(5.0 / 3.0, Double.parseDouble(stat(item,
				ItemTooltip.ATTACKS_PER_SECOND)), 0.0001);
	}

	@Test
	public void testParryChanceIsPublishedForTooltip() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("parry_chance", "0.15");
		final Item item = new Item("test sword", "sword", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("0.15", stat(item, ItemTooltip.PARRY_CHANCE));
	}

	@Test
	public void testArmorPenetrationIsPublishedForTooltip() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("armor_penetration", "0.25");
		final Item item = new Item("test dagger", "dagger", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("0.25", stat(item, ItemTooltip.ARMOR_PENETRATION));
	}

	@Test
	public void testExpandedWeaponAffixesArePublishedForTooltip() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put(ItemTooltip.CRITICAL_DAMAGE_BONUS, "0.20");
		attributes.put(ItemTooltip.BLEED_ON_HIT, "0.10");
		attributes.put(ItemTooltip.LEGENDARY_DEEP_WOUNDS, "1.0");
		attributes.put(ItemTooltip.EXECUTE_DAMAGE, "0.25");
		attributes.put(ItemTooltip.POISON_ON_HIT, "0.08");
		attributes.put(ItemTooltip.DISTANCE_DAMAGE, "0.15");
		final Item item = new Item("test affix weapon", "sword", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("0.2", stat(item, ItemTooltip.CRITICAL_DAMAGE_BONUS));
		assertEquals("0.1", stat(item, ItemTooltip.BLEED_ON_HIT));
		assertEquals("1.0", stat(item, ItemTooltip.LEGENDARY_DEEP_WOUNDS));
		assertEquals("0.25", stat(item, ItemTooltip.EXECUTE_DAMAGE));
		assertEquals("0.08", stat(item, ItemTooltip.POISON_ON_HIT));
		assertEquals("0.15", stat(item, ItemTooltip.DISTANCE_DAMAGE));
	}

	@Test
	public void testEquipmentAffixesArePublishedForTooltip() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", "40");
		attributes.put(ItemTooltip.FLAT_DEFENSE_BONUS, "5");
		attributes.put(ItemTooltip.RESIST_POISONED, "0.20");
		attributes.put(ItemTooltip.RESIST_BLEEDING, "0.15");
		attributes.put(ItemTooltip.RESIST_SHOCKED, "0.12");
		attributes.put(ItemTooltip.RESIST_CONFUSED, "0.18");
		attributes.put(ItemTooltip.RESIST_HEAVY, "0.25");
		final Item item = new Item("test affix armour", "armor", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("5", stat(item, ItemTooltip.FLAT_DEFENSE_BONUS));
		assertEquals("0.2", stat(item, ItemTooltip.RESIST_POISONED));
		assertEquals("0.15", stat(item, ItemTooltip.RESIST_BLEEDING));
		assertEquals("0.12", stat(item, ItemTooltip.RESIST_SHOCKED));
		assertEquals("0.18", stat(item, ItemTooltip.RESIST_CONFUSED));
		assertEquals("0.25", stat(item, ItemTooltip.RESIST_HEAVY));
	}

	@Test
	public void testAccessoryFlatAttackAffixIsPublishedForTooltip() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "4");
		attributes.put(ItemTooltip.FLAT_ATTACK_BONUS, "2");
		final Item item = new Item("test affix ring", "ring", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("2", stat(item, ItemTooltip.FLAT_ATTACK_BONUS));
	}

	@Test
	public void testPluralBeltsClassIsArmour() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", "21");
		final Item item = new Item("test belt", "belts", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals(ItemTooltip.CATEGORY_ARMOUR,
				stat(item, ItemTooltip.CATEGORY));
	}

	@Test
	public void testRealGloveClassIsArmour() {
		assertCategory("glove", ItemTooltip.CATEGORY_ARMOUR);
	}

	@Test
	public void testRingAndNecklaceAreAccessories() {
		assertCategory("ring", ItemTooltip.CATEGORY_ACCESSORY);
		assertCategory("necklace", ItemTooltip.CATEGORY_ACCESSORY);
	}

	@Test
	public void testUnknownClassIsOther() {
		assertCategory("container", ItemTooltip.CATEGORY_OTHER);
	}

	@Test
	public void testElementalSusceptibilitiesBecomeResistancePercentages() {
		final Item item = new Item("test armour", "armor", "test", null);
		final Map<Nature, Double> susceptibilities =
				new EnumMap<Nature, Double>(Nature.class);
		susceptibilities.put(Nature.LIGHT, Double.valueOf(0.8));
		susceptibilities.put(Nature.DARK, Double.valueOf(1.2));
		item.setSusceptibilities(susceptibilities);

		ItemTooltipService.update(item);

		assertEquals("120", stat(item, ItemTooltip.RESISTANCE_PREFIX + "light"));
		assertEquals("80", stat(item, ItemTooltip.RESISTANCE_PREFIX + "dark"));
		assertFalse(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.RESISTANCE_PREFIX + "fire"));
	}

	private void assertCategory(final String itemClass,
			final String expectedCategory) {
		final Item item = new Item("test item", itemClass, "test", null);
		ItemTooltipService.update(item);
		assertEquals(expectedCategory, stat(item, ItemTooltip.CATEGORY));
	}

	private String stat(final Item item, final String key) {
		return item.getMap(ItemTooltip.ATTRIBUTE).get(key);
	}
}
