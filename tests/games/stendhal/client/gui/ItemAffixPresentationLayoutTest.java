/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixPresentationLayoutTest {
	@Test
	public void accessoryShowsCoreAttackSeparatelyFromRegularAndLegendaryAffixes() {
		final RPObject object = ItemTestHelper.createItem("pierścień z mithrilu");
		object.put("class", "ring");
		object.put("rarity_id", "legendary");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ACCESSORY);
		put(object, ItemTooltip.ATTACK, "3");
		put(object, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS, "1");
		put(object, ItemTooltip.LEGENDARY_RELIC_POWER, "5");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+3 ataku"));
		assertTrue(tooltip.contains("+1 dodatkowego ataku"));
		assertTrue(tooltip.contains("Relikt Mocy:"));
		assertTrue(tooltip.contains("pkt. dodatkowego ataku."));
		assertFalse(tooltip.contains("ataku z affixu"));
		assertFalse(tooltip.contains("+9 ataku"));
		assertTrue(tooltip.indexOf("+3 ataku")
				< tooltip.indexOf("+1 dodatkowego ataku"));
		assertTrue(tooltip.indexOf("+1 dodatkowego ataku")
				< tooltip.indexOf("Relikt Mocy:"));
	}

	@Test
	public void flatDefenseAndBastionUseAdditionalDefenseWording() {
		final RPObject object = ItemTestHelper.createItem("pancerz legendarny");
		object.put("class", "armor");
		object.put("rarity_id", "legendary");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		put(object, ItemTooltip.DEFENSE, "28");
		put(object, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS, "4");
		put(object, ItemTooltip.LEGENDARY_BASTION_BONUS, "10");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("28 pkt. pancerza"));
		assertTrue(tooltip.contains("+4 dodatkowego pancerza"));
		assertTrue(tooltip.contains("Niezłomny Bastion:"));
		assertTrue(tooltip.contains("pkt. dodatkowego pancerza."));
		assertFalse(tooltip.contains("pancerza z affixu"));
	}

	private void put(final RPObject object, final String key, final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
