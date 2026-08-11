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
package games.stendhal.server.events;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;

/**
 * Read-only RPClass definitions for item-upgrade events that may have been
 * captured by a character save. RPEvents are transient, but Marauroa stores
 * the event list when a database save happens in the same turn.
 */
public final class ItemUpgradeEventCompatibility {
	public static final String LEGACY_EVENT = "item_upgrade";
	public static final String CURRENT_EVENT = "item_upgrade_event";

	private ItemUpgradeEventCompatibility() {
		// utility class
	}

	public static void generateRPClasses() {
		registerLegacyEventIfMissing();
		registerCurrentEventIfMissing();
	}

	private static void registerLegacyEventIfMissing() {
		if (RPClass.hasRPClass(LEGACY_EVENT)) {
			return;
		}
		final RPClass rpclass = new RPClass(LEGACY_EVENT);
		add(rpclass, "phase", Type.STRING);
		add(rpclass, "status", Type.STRING);
		add(rpclass, "message", Type.LONG_STRING);
		add(rpclass, "npc_id", Type.INT);
		add(rpclass, "request_token", Type.STRING);
		add(rpclass, "candidate_paths", Type.VERY_LONG_STRING);
		add(rpclass, "candidate_names", Type.VERY_LONG_STRING);
		add(rpclass, "selected_path", Type.LONG_STRING);
		add(rpclass, "name", Type.STRING);
		add(rpclass, "class", Type.STRING);
		add(rpclass, "subclass", Type.STRING);
		add(rpclass, "rarity_id", Type.STRING);
		add(rpclass, "upgrade_level", Type.INT);
		add(rpclass, "next_upgrade_level", Type.INT);
		add(rpclass, "max_upgrade_level", Type.INT);
		add(rpclass, "success_percent", Type.INT);
		add(rpclass, "fee", Type.INT);
		add(rpclass, "fee_text", Type.STRING);
		add(rpclass, "owned_money", Type.INT);
		add(rpclass, "can_upgrade", Type.BYTE);
		add(rpclass, "stat_names", Type.VERY_LONG_STRING);
		add(rpclass, "current_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "upgraded_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "material_names", Type.VERY_LONG_STRING);
		add(rpclass, "material_values", Type.VERY_LONG_STRING);
		add(rpclass, "owned_material_values", Type.VERY_LONG_STRING);
	}

	private static void registerCurrentEventIfMissing() {
		if (RPClass.hasRPClass(CURRENT_EVENT)) {
			return;
		}
		final RPClass rpclass = new RPClass(CURRENT_EVENT);
		add(rpclass, "phase", Type.STRING);
		add(rpclass, "status", Type.STRING);
		add(rpclass, "message", Type.LONG_STRING);
		add(rpclass, "npc_id", Type.INT);
		add(rpclass, "request_token", Type.STRING);
		add(rpclass, "candidate_paths", Type.VERY_LONG_STRING);
		add(rpclass, "selected_path", Type.LONG_STRING);
		add(rpclass, "name", Type.STRING);
		add(rpclass, "class", Type.STRING);
		add(rpclass, "subclass", Type.STRING);
		add(rpclass, "rarity_id", Type.STRING);
		add(rpclass, "upgrade_level", Type.INT);
		add(rpclass, "next_upgrade_level", Type.INT);
		add(rpclass, "max_upgrade_level", Type.INT);
		add(rpclass, "success_percent", Type.INT);
		add(rpclass, "fee", Type.INT);
		add(rpclass, "fee_text", Type.STRING);
		add(rpclass, "owned_money", Type.INT);
		add(rpclass, "can_upgrade", Type.BYTE);
		add(rpclass, "stat_names", Type.VERY_LONG_STRING);
		add(rpclass, "current_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "upgraded_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "material_names", Type.VERY_LONG_STRING);
		add(rpclass, "material_classes", Type.VERY_LONG_STRING);
		add(rpclass, "material_subclasses", Type.VERY_LONG_STRING);
		add(rpclass, "material_values", Type.VERY_LONG_STRING);
		add(rpclass, "owned_material_values", Type.VERY_LONG_STRING);
	}

	private static void add(final RPClass rpclass, final String name,
			final Type type) {
		rpclass.addAttribute(name, type, Definition.PRIVATE);
	}
}
