/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import games.stendhal.client.entity.User;
import games.stendhal.common.Constants;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Finds the equipped item that should be compared with a hovered item. */
final class EquipmentComparisonResolver {
	private static final Set<String> EQUIPMENT_SLOT_NAMES =
			Collections.unmodifiableSet(new LinkedHashSet<String>(
					Arrays.asList(Constants.EQUIPMENT_SLOTS)));

	private EquipmentComparisonResolver() {
		// utility class
	}

	static RPObject resolve(final RPObject item) {
		if (User.isNull()) {
			return null;
		}
		return resolve(item, User.get().getRPObject());
	}

	static RPObject resolve(final RPObject item, final RPObject player) {
		if (item == null || player == null || isDirectlyEquipped(item, player)) {
			return null;
		}

		final String category = ItemRarityPresentation.resolveCategory(item);
		for (final String slotName : orderedSlots(item, category)) {
			if (!player.hasSlot(slotName)) {
				continue;
			}
			final RPSlot slot = player.getSlot(slotName);
			for (final RPObject equipped : slot) {
				if (category.equals(ItemRarityPresentation.resolveCategory(equipped))) {
					return equipped;
				}
			}
		}
		return null;
	}

	private static boolean isDirectlyEquipped(final RPObject item,
			final RPObject player) {
		if (!item.isContained() || item.getContainerSlot() == null
				|| !EQUIPMENT_SLOT_NAMES.contains(item.getContainerSlot().getName())) {
			return false;
		}
		final RPObject container = item.getContainer();
		return container == player || (container != null && container.equals(player));
	}

	private static List<String> orderedSlots(final RPObject item,
			final String category) {
		final Set<String> slots = new LinkedHashSet<String>();
		final String published = WeaponPerformanceCalculator.getTooltipValue(item,
				ItemTooltip.EQUIPMENT_SLOTS);
		if (published != null) {
			for (final String slot : published.split(";")) {
				if (!slot.isEmpty()) {
					slots.add(slot);
				}
			}
		}

		final List<String> ordered = new ArrayList<String>();
		if (ItemTooltip.CATEGORY_WEAPON.equals(category)) {
			moveFirst(slots, ordered, "rhand");
			moveFirst(slots, ordered, "lhand");
		} else if ("shield".equals(item.has("class") ? item.get("class") : null)) {
			moveFirst(slots, ordered, "lhand");
			moveFirst(slots, ordered, "rhand");
		}
		ordered.addAll(slots);
		return ordered;
	}

	private static void moveFirst(final Set<String> available,
			final List<String> ordered, final String slot) {
		if (available.remove(slot)) {
			ordered.add(slot);
		}
	}
}
