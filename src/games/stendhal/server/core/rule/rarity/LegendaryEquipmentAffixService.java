/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.server.entity.item.Item;

/** Materializes legendary-only equipment bonuses. */
public final class LegendaryEquipmentAffixService {
	public static final String BASTION_BONUS_ATTRIBUTE =
			"legendary_bastion_bonus";
	public static final String RELIC_POWER_ATTRIBUTE =
			"legendary_relic_power";
	public static final String IRON_WILL_ATTRIBUTE =
			"legendary_iron_will";
	public static final String UNYIELDING_PROTECTION_ATTRIBUTE =
			"legendary_unyielding_protection";
	public static final String HERO_EYE_ATTRIBUTE =
			"legendary_hero_eye";
	public static final String GUARDIAN_SEAL_ATTRIBUTE =
			"legendary_guardian_seal";

	public static final double UNIVERSAL_STATUS_RESISTANCE = 0.20;
	public static final double HERO_EYE_CRITICAL_CHANCE_BONUS = 8.0;

	private static final int MIN_BASTION_PERCENT = 20;
	private static final int MAX_BASTION_PERCENT = 30;
	private static final int MIN_RELIC_ATTACK = 4;
	private static final int MAX_RELIC_ATTACK = 7;

	private LegendaryEquipmentAffixService() {
		// utility class
	}

	public static boolean isBastionEligible(final Item item) {
		return EquipmentAffixService.isArmour(item)
				&& !item.has(BASTION_BONUS_ATTRIBUTE);
	}

	public static boolean applyBastion(final Item item, final Random random) {
		if (!isBastionEligible(item)) {
			return false;
		}
		final int currentDefense = item.has("def") ? Math.max(0, item.getInt("def")) : 0;
		final int percent = rollInclusive(random, MIN_BASTION_PERCENT,
				MAX_BASTION_PERCENT);
		final int bonus = Math.max(2,
				(int) Math.ceil(Math.max(1, currentDefense) * (percent / 100.0)));
		item.put(BASTION_BONUS_ATTRIBUTE, bonus);
		item.put("def", clampShort(currentDefense + bonus));
		return true;
	}

	public static boolean isRelicPowerEligible(final Item item) {
		return EquipmentAffixService.isAccessory(item)
				&& !item.has(RELIC_POWER_ATTRIBUTE);
	}

	public static boolean applyRelicPower(final Item item, final Random random) {
		if (!isRelicPowerEligible(item)) {
			return false;
		}
		final int bonus = rollInclusive(random, MIN_RELIC_ATTACK, MAX_RELIC_ATTACK);
		item.put(RELIC_POWER_ATTRIBUTE, bonus);
		item.put("atk", clampShort((item.has("atk") ? item.getInt("atk") : 0) + bonus));
		return true;
	}

	public static boolean isIronWillEligible(final Item item) {
		return EquipmentAffixService.isArmour(item)
				&& !item.has(IRON_WILL_ATTRIBUTE);
	}

	public static boolean applyIronWill(final Item item, final Random random) {
		return applyMarker(item, IRON_WILL_ATTRIBUTE,
				isIronWillEligible(item), random);
	}

	public static boolean isUnyieldingProtectionEligible(final Item item) {
		return EquipmentAffixService.isArmour(item)
				&& !item.has(UNYIELDING_PROTECTION_ATTRIBUTE);
	}

	public static boolean applyUnyieldingProtection(final Item item,
			final Random random) {
		return applyMarker(item, UNYIELDING_PROTECTION_ATTRIBUTE,
				isUnyieldingProtectionEligible(item), random);
	}

	public static boolean isHeroEyeEligible(final Item item) {
		return EquipmentAffixService.isAccessory(item)
				&& !item.has(HERO_EYE_ATTRIBUTE);
	}

	public static boolean applyHeroEye(final Item item, final Random random) {
		return applyMarker(item, HERO_EYE_ATTRIBUTE,
				isHeroEyeEligible(item), random);
	}

	public static boolean isGuardianSealEligible(final Item item) {
		return EquipmentAffixService.isAccessory(item)
				&& !item.has(GUARDIAN_SEAL_ATTRIBUTE);
	}

	public static boolean applyGuardianSeal(final Item item, final Random random) {
		return applyMarker(item, GUARDIAN_SEAL_ATTRIBUTE,
				isGuardianSealEligible(item), random);
	}

	private static boolean applyMarker(final Item item, final String attribute,
			final boolean eligible, final Random random) {
		if (!eligible) {
			return false;
		}
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		item.put(attribute, 1.0);
		return true;
	}

	private static int clampShort(final int value) {
		return Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, value));
	}

	private static int rollInclusive(final Random random, final int minimum,
			final int maximum) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
