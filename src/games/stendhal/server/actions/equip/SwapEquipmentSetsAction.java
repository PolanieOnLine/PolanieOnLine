/***************************************************************************
 *		   (C) Copyright 2024 - Stendhal		    *
 ***************************************************************************
 ***************************************************************************
 *									 *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.				   *
 *									 *
 ***************************************************************************/
package games.stendhal.server.actions.equip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.Constants;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

public class SwapEquipmentSetsAction implements ActionListener {
	private static final String ACTION_NAME = "swap_sets";
	private static final String SET_SUFFIX = "_set";
	private static final List<String> PRIMARY_SLOTS;

	static {
		List<String> bases = new ArrayList<String>(Constants.RESERVE_EQUIPMENT_SLOTS.length);
		for (String slot : Constants.RESERVE_EQUIPMENT_SLOTS) {
			if (slot.endsWith(SET_SUFFIX)) {
				bases.add(slot.substring(0, slot.length() - SET_SUFFIX.length()));
			} else {
				bases.add(slot);
			}
		}
		PRIMARY_SLOTS = Arrays.asList(bases.toArray(new String[0]));
	}

	public static void register() {
		CommandCenter.register(ACTION_NAME, new SwapEquipmentSetsAction());
	}

	@Override
	public void onAction(Player player, RPAction action) {
		boolean changed = swapEquipment(player);
		if (changed) {
			player.notifyWorldAboutChanges();
			player.updateItemAtkDef();
		}
	}

	private boolean swapEquipment(Player player) {
		boolean changed = false;
		for (String baseSlot : PRIMARY_SLOTS) {
			String reserveSlotName = baseSlot + SET_SUFFIX;
			EntitySlot primary = player.getEntitySlot(baseSlot);
			EntitySlot reserve = player.getEntitySlot(reserveSlotName);
			if ((primary == null) || (reserve == null)) {
				continue;
			}

			Item mainItem = asItem(firstItem(primary));
			Item reserveItem = asItem(firstItem(reserve));
			if ((mainItem == null) && (reserveItem == null)) {
				continue;
			}

			if (mainItem != null) {
				mainItem.onUnequipped();
				primary.remove(mainItem.getID());
			}
			if (reserveItem != null) {
				reserveItem.onUnequipped();
				reserve.remove(reserveItem.getID());
			}
			if (mainItem != null) {
				reserve.add(mainItem);
				mainItem.onEquipped(player, reserveSlotName);
			}
			if (reserveItem != null) {
				primary.add(reserveItem);
				reserveItem.onEquipped(player, baseSlot);
			}
			changed = true;
		}
		return changed;
	}

	private RPObject firstItem(EntitySlot slot) {
		for (RPObject object : slot) {
			return object;
		}
		return null;
	}

	private Item asItem(RPObject object) {
		if (object instanceof Item) {
			return (Item) object;
		}
		return null;
	}
}

