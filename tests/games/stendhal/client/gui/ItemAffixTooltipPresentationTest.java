/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixTooltipPresentationTest {
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

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("45 pkt. pancerza"));
		assertTrue(tooltip.contains("+5 pancerza z affixu"));
		assertTrue(tooltip.contains("+20% odporności na zatrucie"));
		assertTrue(tooltip.contains("+15% odporności na krwawienie"));
		assertTrue(tooltip.contains("+12% odporności na szok"));
		assertTrue(tooltip.contains("+18% odporności na dezorientację"));
		assertTrue(tooltip.contains("+25% odporności na spowolnienie"));
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
