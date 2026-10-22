/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixTooltipPresentationTest {
	@Test
	public void equipmentAttackPenaltiesAreNotMaskedByZeroOrMissingOtherAttack() {
		final String[][] attacks = {
				{"-5", null, "-5"}, {"-5", "0", "-5"},
				{null, "-4", "-4"}, {"0", "-4", "-4"},
				{"-5", "-3", "-3"}, {"4", "7", "+7"}
		};
		for (final String[] values : attacks) {
			final RPObject object = ItemTestHelper.createItem("attack tooltip armour");
			object.put("class", "armor");
			put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
			put(object, ItemTooltip.DEFENSE, "40");
			if (values[0] != null) put(object, ItemTooltip.ATTACK, values[0]);
			if (values[1] != null) put(object, ItemTooltip.RANGED_ATTACK, values[1]);

			final String tooltip = ItemRarityPresentation.buildItemToolTip(
					EntityFactory.createEntity(object), null);

			assertTrue(tooltip, tooltip.contains(values[2] + " ataku"));
			assertTrue(tooltip.contains("40 pkt. pancerza"));
		}
	}

	@Test
	public void attackComparisonUsesNegativeEquippedValue() {
		final RPObject current = ItemTestHelper.createItem("current ring");
		put(current, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(current, ItemTooltip.ATTACK, "-2");
		final RPObject equipped = ItemTestHelper.createItem("equipped ring");
		put(equipped, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(equipped, ItemTooltip.ATTACK, "-5");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(current), equipped);

		assertTrue(tooltip, tooltip.contains("-2 ataku"));
		assertTrue(tooltip, tooltip.contains("color='#62d26f'>+3</font>"));
	}

	@Test
	public void upgradedEquipmentDoesNotFallBackToCancelledRawAttackPenalty() {
		final games.stendhal.server.entity.item.Item item =
				ItemTestHelper.createItem("upgraded gloves");
		item.put("class", "glove");
		item.put("atk", -1);
		item.put(games.stendhal.server.entity.item.Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, 1);
		item.put(games.stendhal.server.entity.item.Item.UPGRADE_LEVEL_ATTRIBUTE, 1);
		games.stendhal.server.entity.item.ItemTooltipService.update(item);

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(item), null);

		assertFalse(tooltip, tooltip.contains(" ataku"));
	}

	@Test
	public void zeroAttackWithoutComparisonRemainsHidden() {
		final RPObject object = ItemTestHelper.createItem("neutral ring");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(object, ItemTooltip.ATTACK, "0");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object), null);

		assertFalse(tooltip, tooltip.contains(" ataku"));
	}

	@Test
	public void expandedWeaponAffixesUseReadablePercentLabels() {
		final RPObject object = ItemTestHelper.createItem("affix tooltip sword");
		object.put("class", "sword");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		put(object, ItemTooltip.ATTACK, "30");
		put(object, ItemTooltip.DAMAGE_MIN, "27");
		put(object, ItemTooltip.DAMAGE_MAX, "33");
		put(object, ItemTooltip.ATTACKS_PER_SECOND, "1.0");
		put(object, ItemTooltip.CRITICAL_DAMAGE_BONUS, "0.20");
		put(object, ItemTooltip.BLEED_ON_HIT, "0.10");
		put(object, ItemTooltip.EXECUTE_DAMAGE, "0.25");
		put(object, ItemTooltip.POISON_ON_HIT, "0.08");
		put(object, ItemTooltip.DISTANCE_DAMAGE, "0.15");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+20% obrażeń trafienia krytycznego"));
		assertTrue(tooltip.contains("+10% szansy na krwawienie"));
		assertTrue(tooltip.contains("+25% obrażeń poniżej 25% PW celu"));
		assertTrue(tooltip.contains("+8% szansy na zatrucie"));
		assertTrue(tooltip.contains("+15% obrażeń z dystansu"));
	}

	@Test
	public void equipmentAffixesUseReadableFlatAndResistanceLabels() {
		final RPObject object = ItemTestHelper.createItem("affix tooltip armour");
		object.put("class", "armor");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		put(object, ItemTooltip.DEFENSE, "45");
		put(object, ItemTooltip.FLAT_DEFENSE_BONUS, "5");
		put(object, ItemTooltip.RESIST_POISONED, "0.20");
		put(object, ItemTooltip.RESIST_BLEEDING, "0.15");
		put(object, ItemTooltip.RESIST_SHOCKED, "0.12");
		put(object, ItemTooltip.RESIST_CONFUSED, "0.18");
		put(object, ItemTooltip.RESIST_HEAVY, "0.25");
		put(object, ItemTooltip.RESIST_STUNNED, "0.30");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("45 pkt. pancerza"));
		assertTrue(tooltip.contains("+5 pancerza z affixu"));
		assertTrue(tooltip.contains("+20% odporności na zatrucie"));
		assertTrue(tooltip.contains("+15% odporności na krwawienie"));
		assertTrue(tooltip.contains("+12% odporności na szok"));
		assertTrue(tooltip.contains("+18% odporności na dezorientację"));
		assertTrue(tooltip.contains("+25% odporności na spowolnienie"));
		assertTrue(tooltip.contains("+30% odporności na ogłuszenie"));
	}

	@Test
	public void accessoryFlatAttackAffixIsDistinguishedFromFinalAttack() {
		final RPObject object = ItemTestHelper.createItem("affix tooltip ring");
		object.put("class", "ring");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(object, ItemTooltip.ATTACK, "4");
		put(object, ItemTooltip.FLAT_ATTACK_BONUS, "2");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+4 ataku"));
		assertTrue(tooltip.contains("+2 ataku z affixu"));
	}

	private void put(final RPObject object, final String key,
			final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
