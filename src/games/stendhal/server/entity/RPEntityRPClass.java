/***************************************************************************
 *                      (C) Copyright 2003-2023 - Marauroa                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity;

import static games.stendhal.common.constants.General.PATHSET;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;

/**
 * Handles the RPClass registration.
 */
public class RPEntityRPClass {
	/**
	 * Generates the RPClass and specifies slots and attributes.
	 *
	 * @param ATTR_TITLE title attribute name
	 */
	public static void generateRPClass(String ATTR_TITLE) {
		final RPClass entity = new RPClass("rpentity");

		entity.isA(CombatEntity.RPCLASS_NAME);
		entity.addAttribute("name", Type.STRING);
		entity.addAttribute(ATTR_TITLE, Type.STRING);
		entity.addAttribute("gender", Type.STRING);

		entity.addAttribute("title_type", Type.STRING, Definition.VOLATILE);
		entity.addAttribute("base_speed", Type.FLOAT, Definition.VOLATILE);

		entity.addAttribute("ignore_collision", Type.FLAG, Definition.VOLATILE);

		entity.addAttribute("unnamed", Type.FLAG, Definition.VOLATILE);
		entity.addAttribute("no_hpbar", Type.FLAG, Definition.VOLATILE);
		entity.addAttribute("no_attack", Type.FLAG, Definition.VOLATILE);
		// client will suppress drawing shadow under entity if this is set
		entity.addAttribute("no_shadow", Type.FLAG, Definition.VOLATILE);
		// the shadow the client should draw for entity
		// defaults to sprite dimensions if "no_shadow" not set
		entity.addAttribute("shadow_style", Type.STRING, Definition.VOLATILE);

		// Jobs
		entity.addAttribute("job_merchant", Type.FLAG, Definition.VOLATILE);
		entity.addAttribute("job_healer", Type.FLAG, Definition.VOLATILE);
		entity.addAttribute("job_producer", Type.FLAG, Definition.VOLATILE);

		/* Movement */
		entity.addAttribute(PATHSET, Type.STRING, Definition.VOLATILE);
		// flying entities are not blocked by FlyOverArea
		entity.addAttribute("flying", Type.FLAG, Definition.VOLATILE);
		// animation should cycle even if entity is idle
		entity.addAttribute("active_idle", Type.FLAG, Definition.VOLATILE);
		
		/* Immortal creatures */
		entity.addAttribute("immortal", Type.FLAG, Definition.VOLATILE);

				/* Equipment */
		final String[] equipmentSlots = {
			"head",
			"neck",
			"rhand",
			"lhand",
			"armor",
			"finger",
			"fingerb",
			"cloak",
			"glove",
			"legs",
			"feet",
			"back",
			"pas"
		};
		for (String slot : equipmentSlots) {
			entity.addRPSlot(slot, 1, Definition.PRIVATE);
		}

		final String[] reserveSlots = {
			"neck_set",
			"rhand_set",
			"finger_set",
			"fingerb_set",
			"head_set",
			"armor_set",
			"pas_set",
			"legs_set",
			"feet_set",
			"cloak_set",
			"lhand_set",
			"glove_set",
			"pouch_set"
		};
		for (String slot : reserveSlots) {
			entity.addRPSlot(slot, 1, Definition.PRIVATE);
		}
		entity.addRPSlot("belt", 1, (byte) (Definition.PRIVATE | Definition.VOLATILE));

		entity.addRPSlot("money", 1, Definition.VOLATILE);

		entity.addRPSlot("bag", 42, Definition.PRIVATE);
		entity.addRPSlot("keyring", 18, Definition.PRIVATE);
		entity.addRPSlot("magicbag", 6, Definition.PRIVATE);

		/* Rune altar slots */
		entity.addRPSlot("offensive_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("defensive_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("resistance_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("utility_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("healing_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("control_rune", 1, Definition.PRIVATE);
		entity.addRPSlot("special_rune", 1, Definition.PRIVATE);

		entity.addRPSlot("portfolio", 9, Definition.VOLATILE);
	}
}
