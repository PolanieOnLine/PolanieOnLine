/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.EquipmentStatusResistanceService;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.core.rule.rarity.EquipmentAffixService;
import games.stendhal.server.core.rule.rarity.ItemAffixDefinition;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.core.rule.rarity.LegendaryItemAffixRegistry;
import games.stendhal.server.entity.status.StatusType;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;

public class ItemInformation extends Item {


	/**
	 * copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public ItemInformation(final Item item) {
		super(item);
		setRPClass("item_information");
	}


	public static void generateRPClass() {
		/* ItemInformation is registered immediately after Item while RP classes
		 * are still mutable. Add presentation and per-instance extension metadata
		 * to the parent item class so normal inventory items can expose selected
		 * statistics without making the internal combat attributes public. */
		final RPClass itemClass = RPClass.getRPClass("item");
		itemClass.addAttribute(ItemTooltip.ATTRIBUTE,
				Type.MAP, Definition.VOLATILE);
		itemClass.addAttribute(ItemTooltip.CATEGORY_OVERRIDE,
				Type.STRING, Definition.HIDDEN);
		itemClass.addAttribute(ParryService.PARRY_CHANCE_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE,
				Type.FLOAT, Definition.HIDDEN);

		/* Legendary signature affixes are persisted as normal item attributes in
		 * addition to item_affixes. Register the complete registry here instead of
		 * maintaining a second hand-written list; otherwise adding a new signature
		 * can make perception serialization fail and disconnect clients. */
		for (final ItemAffixDefinition definition
				: LegendaryItemAffixRegistry.getInstance().getDefinitions()) {
			itemClass.addAttribute(definition.getAttribute(),
					legendaryAttributeType(definition), Definition.HIDDEN);
		}

		itemClass.addAttribute(EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE,
				Type.SHORT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE,
				Type.SHORT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.POISONED), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.BLEEDING), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.SHOCKED), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.CONFUSED), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.HEAVY), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(EquipmentStatusResistanceService.getResistanceAttribute(
				StatusType.STUNNED), Type.FLOAT, Definition.HIDDEN);
		itemClass.addAttribute(ItemAffixState.SEED_ATTRIBUTE,
				Type.STRING, Definition.HIDDEN);
		itemClass.addAttribute(ItemAffixState.ATTRIBUTE,
				Type.MAP, Definition.HIDDEN);

		final RPClass entity = new RPClass("item_information");
		entity.isA("item");

		// Some things may have a textual description
		entity.addAttribute("description_info", Type.LONG_STRING);

		// used for show_item_list events used as shop signs.
		entity.addAttribute("price", Type.INT, Definition.VOLATILE);
	}

	private static Type legendaryAttributeType(
			final ItemAffixDefinition definition) {
		final String attribute = definition.getAttribute();
		if (LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE.equals(attribute)
				|| LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE.equals(attribute)) {
			return Type.SHORT;
		}
		return Type.FLOAT;
	}
}
