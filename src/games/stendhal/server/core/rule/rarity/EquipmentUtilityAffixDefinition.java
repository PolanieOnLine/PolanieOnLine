package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.server.entity.item.Item;

/** Ordinary utility rolls share the existing persistent affix map. */
public enum EquipmentUtilityAffixDefinition implements ItemAffixDefinition {
	KILL_RECOVERY("kill_recovery", 50, 100, false),
	POTION_CONSERVATION("potion_conservation", 5, 8, true),
	SINGLE_COMBAT("single_combat_bonus", 3, 5, true),
	COIN_BONUS("coin_bonus", 3, 6, true),
	DURABILITY_CONSERVATION("durability_conservation", 10, 20, true);

	private final String id;
	private final int minimum;
	private final int maximum;
	private final boolean percentage;

	EquipmentUtilityAffixDefinition(final String id, final int minimum, final int maximum, final boolean percentage) {
		this.id = id;
		this.minimum = minimum;
		this.maximum = maximum;
		this.percentage = percentage;
	}
	@Override public String getId() { return id; }
	@Override public String getAttribute() { return id; }
	@Override public boolean hasMaterializedAttribute() { return false; }

	@Override public boolean isEligible(final Item item) {
		if (item == null || item.has(id) || ItemAffixState.has(item, id)) return false;
		final String type = item.getItemClass();
		switch (this) {
		case KILL_RECOVERY:
			return "glove".equals(type) || "gloves".equals(type) || "belt".equals(type) || "belts".equals(type);
		case POTION_CONSERVATION:
			return EquipmentAffixService.isEquipment(item);
		case SINGLE_COMBAT:
			return "armor".equals(type) || "shield".equals(type) || "helmet".equals(type)
					|| "legs".equals(type) || "boots".equals(type);
		case COIN_BONUS:
			return "cloak".equals(type) || "belt".equals(type) || "belts".equals(type)
					|| EquipmentAffixService.isAccessory(item);
		case DURABILITY_CONSERVATION:
			// Przemir uses plain Item copies retaining the real weapon's durability.
			return WeaponAffixService.isWeapon(item) && item.has("durability") && item.getInt("durability") > 0;
		default:
			return false;
		}
	}
	@Override public boolean apply(final Item item, final Random random) {
		if (random == null) throw new IllegalArgumentException("Random source must not be null");
		if (!isEligible(item)) return false;
		final int roll = minimum + random.nextInt(maximum - minimum + 1);
		item.put(ItemAffixState.ATTRIBUTE, id, Double.toString(percentage ? roll / 100.0 : roll));
		return true;
	}
	public double value(final Item item) {
		if (item == null || !item.hasMap(ItemAffixState.ATTRIBUTE)) return 0.0;
		try {
			final String raw = item.getMap(ItemAffixState.ATTRIBUTE).get(id);
			if (raw == null) return 0.0;
			final double value = Double.parseDouble(raw);
			return Double.isFinite(value) ? Math.max(0.0, Math.min(value, percentage ? maximum / 100.0 : maximum)) : 0.0;
		} catch (final NumberFormatException exception) {
			return 0.0;
		}
	}
}
