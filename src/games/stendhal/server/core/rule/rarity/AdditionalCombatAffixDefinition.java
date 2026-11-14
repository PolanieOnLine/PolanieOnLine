/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.item.Item;

/** New combat affixes use the existing persistent map without extending the save schema. */
public enum AdditionalCombatAffixDefinition implements ItemAffixDefinition {
	STEADY_HAND(ItemTooltip.STEADY_HAND, 5, 10),
	POISON_POTENCY(ItemTooltip.POISON_POTENCY, 15, 25),
	BLEED_POTENCY(ItemTooltip.BLEED_POTENCY, 15, 25),
	VIGILANCE(ItemTooltip.VIGILANCE, 8, 12),
	AMMO_CONSERVATION(ItemTooltip.AMMO_CONSERVATION, 10, 20),
	INEVITABILITY(ItemTooltip.LEGENDARY_INEVITABILITY),
	IRON_RESOLVE(ItemTooltip.LEGENDARY_IRON_RESOLVE),
	DEFENSE_CIRCLE(ItemTooltip.LEGENDARY_DEFENSE_CIRCLE),
	HUNTERS_PACE(ItemTooltip.LEGENDARY_HUNTERS_PACE),
	LAST_STAND(ItemTooltip.LEGENDARY_LAST_STAND);

	private final String attribute;
	private final int minimum;
	private final int maximum;

	AdditionalCombatAffixDefinition(final String attribute) {
		this(attribute, 100, 100);
	}

	AdditionalCombatAffixDefinition(final String attribute, final int minimum, final int maximum) {
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
	public boolean hasMaterializedAttribute() {
		return false;
	}

	@Override
	public boolean isEligible(final Item item) {
		if (item == null || item.has(attribute) || ItemAffixState.has(item, attribute)) {
			return false;
		}
		final String type = item.getItemClass();
		switch (this) {
		case STEADY_HAND:
			return WeaponAffixService.isWeapon(item) && item.getDamageMax() > item.getDamageMin();
		case POISON_POTENCY:
		case BLEED_POTENCY:
			return WeaponAffixService.isWeapon(item) || "glove".equals(type) || "gloves".equals(type);
		case VIGILANCE:
			return "helmet".equals(type) || "cloak".equals(type);
		case AMMO_CONSERVATION:
			return "ranged".equals(type);
		case INEVITABILITY:
			return WeaponAffixService.isWeapon(item);
		case IRON_RESOLVE:
			return "armor".equals(type) || "belt".equals(type) || "belts".equals(type);
		case DEFENSE_CIRCLE:
			return "armor".equals(type) || "shield".equals(type);
		case HUNTERS_PACE:
			return "boots".equals(type);
		case LAST_STAND:
			return "belt".equals(type) || "belts".equals(type)
					|| "glove".equals(type) || "gloves".equals(type);
		default:
			return false;
		}
	}

	@Override
	public boolean apply(final Item item, final Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		if (!isEligible(item)) {
			return false;
		}
		final int percent = minimum == maximum ? minimum : minimum + random.nextInt(maximum - minimum + 1);
		item.put(ItemAffixState.ATTRIBUTE, attribute, Double.toString(percent / 100.0));
		return true;
	}

	public double value(final Item item) {
		if (item == null || !item.hasMap(ItemAffixState.ATTRIBUTE)) {
			return 0.0;
		}
		final String raw = item.getMap(ItemAffixState.ATTRIBUTE).get(attribute);
		if (raw == null) {
			return 0.0;
		}
		try {
			final double value = Double.parseDouble(raw);
			return Double.isFinite(value) ? Math.max(0.0, Math.min(1.0, value)) : 0.0;
		} catch (final NumberFormatException exception) {
			return 0.0;
		}
	}
}
