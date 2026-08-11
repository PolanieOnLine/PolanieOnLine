/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import utilities.RPClass.ItemTestHelper;

public class EquipmentComparisonResolverTest {
	@Test
	public void testWeaponUsesEquippedWeaponInsteadOfShield() {
		final RPObject player = new RPObject();
		final RPObject sword = item("equipped sword", ItemTooltip.CATEGORY_WEAPON,
				"rhand;lhand");
		final RPObject shield = item("equipped shield", ItemTooltip.CATEGORY_ARMOUR,
				"lhand;rhand");
		add(player, "lhand", shield);
		add(player, "rhand", sword);
		final RPObject candidate = item("candidate sword",
				ItemTooltip.CATEGORY_WEAPON, "lhand;rhand");

		assertSame(sword, EquipmentComparisonResolver.resolve(candidate, player));
	}

	@Test
	public void testShieldUsesLeftHandAndSameCategory() {
		final RPObject player = new RPObject();
		final RPObject shield = item("equipped shield", ItemTooltip.CATEGORY_ARMOUR,
				"lhand;rhand");
		shield.put("class", "shield");
		add(player, "lhand", shield);
		final RPObject candidate = item("candidate shield",
				ItemTooltip.CATEGORY_ARMOUR, "rhand;lhand");
		candidate.put("class", "shield");

		assertSame(shield, EquipmentComparisonResolver.resolve(candidate, player));
	}

	@Test
	public void testEquippedItemIsNotComparedWithItself() {
		final RPObject player = new RPObject();
		final RPObject equipped = item("equipped ring",
				ItemTooltip.CATEGORY_ACCESSORY, "finger;fingerb");
		add(player, "finger", equipped);

		assertNull(EquipmentComparisonResolver.resolve(equipped, player));
	}

	@Test
	public void testNonEquipmentItemHasNoComparison() {
		final RPObject player = new RPObject();
		add(player, "rhand", item("sword", ItemTooltip.CATEGORY_WEAPON,
				"rhand"));
		final RPObject potion = item("potion", ItemTooltip.CATEGORY_OTHER, null);

		assertNull(EquipmentComparisonResolver.resolve(potion, player));
	}

	@Test
	public void testItemInPlayerBagIsComparedWithEquipment() {
		final RPObject player = new RPObject();
		final RPObject equipped = item("equipped sword",
				ItemTooltip.CATEGORY_WEAPON, "rhand;lhand");
		add(player, "rhand", equipped);
		final RPObject candidate = item("candidate sword",
				ItemTooltip.CATEGORY_WEAPON, "rhand;lhand");
		add(player, "bag", candidate);

		assertSame(equipped, EquipmentComparisonResolver.resolve(candidate, player));
	}

	private RPObject item(final String name, final String category,
			final String slots) {
		final RPObject item = ItemTestHelper.createItem(name);
		item.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY, category);
		if (slots != null) {
			item.put(ItemTooltip.ATTRIBUTE, ItemTooltip.EQUIPMENT_SLOTS, slots);
		}
		return item;
	}

	private void add(final RPObject player, final String slotName,
			final RPObject item) {
		if (!player.hasSlot(slotName)) {
			player.addSlot(new RPSlot(slotName));
		}
		player.getSlot(slotName).add(item);
	}
}
