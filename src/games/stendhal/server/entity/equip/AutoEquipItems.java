package games.stendhal.server.entity.equip;

import java.util.Arrays;

import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.constants.Actions;
import games.stendhal.server.actions.equip.SourceObject;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPSlot;

public class AutoEquipItems {
	private final static String SLOT_POUCH = "pouch";
	private final static String SLOT_MAGICBAG = "magicbag";

	private final String money = "money";
	private final String[] potionsAndMagics = { "mały eliksir", "eliksir", "duży eliksir", "wielki eliksir",
			"gigantyczny eliksir", "eliksir miłości", "smoczy eliksir", "duży smoczy eliksir",
			"magia ziemi", "magia deszczu", "magia płomieni", "magia mrozu", "magia mroku", "magia światła",
			"zaklęcie pustelnika" };

	/**
	 * Automatically moves items to the appropriate slot.
	 *
	 * @param player
	 * 				a player
	 * @param action
	 * 				the action being called
	 * @param source
	 * 				item
	 */
	public AutoEquipItems(final Player player, final RPAction action, final SourceObject source) {
		final String targetPath = action.get(Actions.TARGET_PATH);
		String targetSlot = null;
		if (targetPath != null) {
			targetSlot = targetPath.substring(targetPath.indexOf("\t") + 1, targetPath.indexOf("]"));
		}

		// try to move money to pouch by default
		if (action.has(EquipActionConsts.CLICKED) && targetSlot != null && !targetSlot.equals(SLOT_POUCH)
				&& source.getEntityName().equals(money)) {
			// check if money can be moved to pouch
			// XXX: this check should be changed if we switch to containers
			itemMoveAction(player, action, SLOT_POUCH, money);
		}

		// try to move potions and magics to magic bag by default
		for (String item : potionsAndMagics) {
			if (action.has(EquipActionConsts.CLICKED) && targetSlot != null && !targetSlot.equals(SLOT_MAGICBAG)
					&& source.getEntityName().equals(item)) {
				RPSlot slot = player.getSlot(SLOT_MAGICBAG);
				if (!slot.isFull() || player.isEquippedItemInSlot(SLOT_MAGICBAG, item)) {
					itemMoveAction(player, action, SLOT_MAGICBAG, item);
				} else {
					itemMoveAction(player, action, "bag", item);
				}
			}
		}
	}

	/**
	 * Move the item to slot.
	 *
	 * @param player
	 * 				player
	 * @param action
	 * 				action
	 * @param slot
	 * 				slot
	 * @param item
	 * 				item
	 */
	void itemMoveAction(final Player player, final RPAction action, final String slot, final String item) {
		if (player.getFeature(slot) != null && player.hasSlot(slot)) {
			final boolean itemsInBag = player.isEquippedItemInSlot("bag", item);
			final boolean itemsInSlot = player.isEquippedItemInSlot(slot, item);
			// stack on slot
			if (itemsInSlot || (!itemsInSlot && !itemsInBag)) {
				targetSlot(player, action, slot);
			} else {
				targetSlot(player, action);
			}
		}
	}

	/**
	 * Target action to move the item to the specified slot.
	 * 
	 * @param player
	 * 				player
	 * @param action
	 * 				action
	 * @param slot
	 * 				slot
	 */
	void targetSlot(final Player player, final RPAction action, final String slot) {
		action.put(EquipActionConsts.TARGET_SLOT, slot);
		if (action.has(Actions.TARGET_PATH)) {
			action.put(Actions.TARGET_PATH, Arrays.asList(player.get("id"), slot));
		}
	}

	/**
	 * Target action to move the item to bag.
	 * 
	 * @param player
	 * 				player
	 * @param action
	 * 				action
	 */
	void targetSlot(final Player player, final RPAction action) {
		action.put(EquipActionConsts.TARGET_SLOT, "bag");
		if (action.has(Actions.TARGET_PATH)) {
			action.put(Actions.TARGET_PATH, Arrays.asList(player.get("id"), "bag"));
		}
	}
}
