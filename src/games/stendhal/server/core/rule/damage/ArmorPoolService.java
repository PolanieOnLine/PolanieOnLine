/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import games.stendhal.server.entity.RPEntity;

/** Splits a resolved hit between breakable armor and health. */
public final class ArmorPoolService {
	private ArmorPoolService() { }

	public static final class DamageResult {
		public static final DamageResult NONE = new DamageResult(0, 0);
		private final int armorDamage;
		private final int healthDamage;

		private DamageResult(final int armorDamage, final int healthDamage) {
			this.armorDamage = armorDamage;
			this.healthDamage = healthDamage;
		}

		public int getArmorDamage() { return armorDamage; }
		public int getHealthDamage() { return healthDamage; }
		public int getTotalDamage() {
			return (int) Math.min(Integer.MAX_VALUE, (long) armorDamage + healthDamage);
		}
	}

	/** Incoming damage already includes weapon matchups; armor-only bonuses apply to the absorbed part. */
	public static DamageResult resolve(final RPEntity target, final int damage,
			final double armorMultiplier, final boolean bypassArmor) {
		if (damage <= 0 || target.getHP() <= 0) {
			return DamageResult.NONE;
		}
		final int armor = bypassArmor ? 0 : target.getArmorHP();
		if (armor <= 0) {
			return new DamageResult(0, Math.min(damage, target.getHP()));
		}
		final double multiplier = Double.isFinite(armorMultiplier) && armorMultiplier > 0
				? armorMultiplier : 1.0;
		final long potentialArmorDamage = Math.round(damage * multiplier);
		final int absorbed = (int) Math.min(armor, potentialArmorDamage);
		// The part spent breaking armor cannot also damage health. In particular,
		// an armor-breaking affix does not become bonus HP damage on the last hit.
		final int remainder = potentialArmorDamage <= armor ? 0
				: (int) Math.max(0L, Math.round(damage - absorbed / multiplier));
		return new DamageResult(absorbed, Math.min(remainder, target.getHP()));
	}
}
