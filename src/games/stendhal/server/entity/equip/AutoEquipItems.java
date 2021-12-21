package games.stendhal.server.entity.equip;

import java.util.Arrays;

import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.constants.Actions;
import games.stendhal.server.actions.equip.SourceObject;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class AutoEquipItems {
	final static String SLOT_POUCH = "pouch";
	final static String SLOT_MAGICBAG = "magicbag";

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
				&& source.getEntityName().equals("money")) {
			// check if money can be moved to pouch
			// XXX: this check should be changed if we switch to containers
			if (player.getFeature("pouch") != null && player.hasSlot("pouch")) {
				final boolean moneyInBag = player.isEquippedItemInSlot("bag", "money");
				final boolean moneyInPouch = player.isEquippedItemInSlot(SLOT_POUCH, "money");
				// stack on pouch
				if (moneyInPouch || (!moneyInPouch && !moneyInBag)) {
					action.put(EquipActionConsts.TARGET_SLOT, SLOT_POUCH);
					if (action.has(Actions.TARGET_PATH)) {
						action.put(Actions.TARGET_PATH,
								Arrays.asList(player.get("id"), SLOT_POUCH));
					}
				}
			}
		}

		final String[] potionsAndMagics = { "mały eliksir", "eliksir", "duży eliksir", "wielki eliksir",
				"gigantyczny eliksir", "eliksir miłości", "smoczy eliksir", "duży smoczy eliksir",
				"magia ziemi", "magia deszczu", "magia płomieni", "magia mroku", "magia światła", "zaklęcie pustelnika" };

		for (String item : potionsAndMagics) {
			if (action.has(EquipActionConsts.CLICKED) && targetSlot != null && !targetSlot.equals(SLOT_MAGICBAG)
					&& source.getEntityName().equals(item)) {
				if (player.getFeature(SLOT_MAGICBAG) != null && player.hasSlot(SLOT_MAGICBAG)) {
					final boolean itemsInBag = player.isEquippedItemInSlot("bag", item);
					final boolean itemsInMagicBag = player.isEquippedItemInSlot(SLOT_MAGICBAG, item);
					// stack on magicbag
					if (itemsInMagicBag || (!itemsInMagicBag && !itemsInBag)) {
						action.put(EquipActionConsts.TARGET_SLOT, SLOT_MAGICBAG);
						if (action.has(Actions.TARGET_PATH)) {
							action.put(Actions.TARGET_PATH, Arrays.asList(player.get("id"), SLOT_MAGICBAG));
						}
					} else {
						action.put(EquipActionConsts.TARGET_SLOT, "bag");
						if (action.has(Actions.TARGET_PATH)) {
							action.put(Actions.TARGET_PATH, Arrays.asList(player.get("id"), "bag"));
						}
					}
				}
			}
		}
	}
}
