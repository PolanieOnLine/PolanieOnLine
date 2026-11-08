/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.item.Item;

/** Additional combat affixes; marker values are persisted without changing base stats. */
public enum CombatAffixDefinition implements ItemAffixDefinition {
	CRUSHING(ItemTooltip.ARMOR_CRUSHING, 10, 15),
	HARDENING(ItemTooltip.CRITICAL_HARDENING, 4, 8),
	CLEAVE(ItemTooltip.LEGENDARY_CLEAVE),
	TENACITY(ItemTooltip.LEGENDARY_TENACITY),
	BREACH(ItemTooltip.LEGENDARY_BREACH),
	QUICK_RECOVERY(ItemTooltip.LEGENDARY_QUICK_RECOVERY);

	private final String attribute;
	private final int minimum;
	private final int maximum;

	CombatAffixDefinition(final String attribute) {
		this(attribute, 100, 100);
	}

	CombatAffixDefinition(final String attribute, final int minimum, final int maximum) {
		this.attribute = attribute;
		this.minimum = minimum;
		this.maximum = maximum;
	}

	@Override
	public String getId() {
		return attribute;
	}

	@Override
	public String getAttribute() {
		return attribute;
	}

	@Override
	public boolean isEligible(final Item item) {
		if (item == null || item.has(attribute)) {
			return false;
		}
		switch (this) {
		case CRUSHING:
			return WeaponAffixService.isWeapon(item)
					|| "glove".equals(item.getItemClass()) || "gloves".equals(item.getItemClass());
		case HARDENING:
			return EquipmentAffixService.isArmour(item);
		case QUICK_RECOVERY:
			return "helmet".equals(item.getItemClass()) || "cloak".equals(item.getItemClass());
		case CLEAVE:
			return "sword".equals(item.getItemClass()) || "axe".equals(item.getItemClass())
					|| "club".equals(item.getItemClass());
		case TENACITY:
		case BREACH:
			return WeaponAffixService.isWeapon(item);
		default:
			return false;
		}
	}

	@Override
	public boolean apply(final Item item, final Random random) {
		if (random == null || !isEligible(item)) {
			return false;
		}
		final int percent = minimum == maximum ? minimum
				: minimum + random.nextInt(maximum - minimum + 1);
		item.put(attribute, percent / 100.0);
		return true;
	}
}
