/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Applies small, explicit weapon-class advantages against creature armor.
 * Creature DEF is the default armor score, with an optional explicit armor
 * override for exceptional creatures. The score selects a matchup tier only;
 * it does not add a second damage-reduction layer.
 */
public final class WeaponArmorInteractionService {
    private static final int LIGHT_ARMOR_MAX = 30;
    private static final int MEDIUM_ARMOR_MAX = 80;

    private WeaponArmorInteractionService() {
        // utility class
    }

    public enum ArmorTier {
        NONE,
        LIGHT,
        MEDIUM,
        HEAVY
    }

    /**
     * Adjusts only the damage rolled by the held weapon set. Flat attack from
     * rings, glyphs and other equipment stays outside the matchup multiplier.
     *
     * The stable attack value is the baseline used by RPEntity when replacing
     * average weapon damage with a per-hit roll. Reversing that replacement
     * lets this method recover the exact rolled weapon contribution without a
     * second random roll.
     */
    public static double adjustAttack(final double totalItemAttack,
            final double stableItemAttack, final List<Item> attackWeapons,
            final Item primaryWeapon, final RPEntity defender) {
        if (primaryWeapon == null || !(defender instanceof Creature)
                || attackWeapons == null || attackWeapons.isEmpty()) {
            return totalItemAttack;
        }

        final double multiplier = getDamageMultiplier(
                primaryWeapon.getWeaponType(),
                ((Creature) defender).getArmorScore());
        if (multiplier == 1.0) {
            return totalItemAttack;
        }

        final double stableWeaponContribution =
                getStableWeaponContribution(attackWeapons);
        final double stableNonWeaponAttack =
                stableItemAttack - stableWeaponContribution;
        final double rolledWeaponContribution = Math.max(0.0,
                totalItemAttack - stableNonWeaponAttack);

        return adjustWeaponContribution(totalItemAttack,
                rolledWeaponContribution, multiplier);
    }

    private static double getStableWeaponContribution(
            final List<Item> attackWeapons) {
        double contribution = 0.0;
        for (final Item weapon : attackWeapons) {
            if (weapon != null) {
                contribution += weapon.getAverageDamage();
            }
        }
        return contribution;
    }

    /**
     * Applies a matchup multiplier only to the weapon's part of the complete
     * equipment attack. Package visibility keeps the arithmetic directly
     * testable without constructing the entire combat engine.
     */
    static double adjustWeaponContribution(final double totalItemAttack,
            final double weaponContribution, final double multiplier) {
        final double safeWeaponContribution = Math.max(0.0,
                weaponContribution);
        return Math.max(0.0, totalItemAttack
                + safeWeaponContribution * (multiplier - 1.0));
    }

    public static ArmorTier classify(final int armorScore) {
        if (armorScore <= 0) {
            return ArmorTier.NONE;
        }
        if (armorScore <= LIGHT_ARMOR_MAX) {
            return ArmorTier.LIGHT;
        }
        if (armorScore <= MEDIUM_ARMOR_MAX) {
            return ArmorTier.MEDIUM;
        }
        return ArmorTier.HEAVY;
    }

    public static double getDamageMultiplier(final String weaponClass,
            final int armorScore) {
        if (weaponClass == null) {
            return 1.0;
        }

        final ArmorTier tier = classify(armorScore);
        if ("dagger".equals(weaponClass)) {
            return daggerMultiplier(tier);
        }
        if ("sword".equals(weaponClass)) {
            return swordMultiplier(tier);
        }
        if ("axe".equals(weaponClass) || "club".equals(weaponClass)) {
            return armorBreakerMultiplier(tier);
        }
        return 1.0;
    }

    private static double daggerMultiplier(final ArmorTier tier) {
        switch (tier) {
        case LIGHT:
            return 1.10;
        case MEDIUM:
            return 0.95;
        case HEAVY:
            return 0.80;
        case NONE:
        default:
            return 1.0;
        }
    }

    private static double swordMultiplier(final ArmorTier tier) {
        switch (tier) {
        case MEDIUM:
            return 1.10;
        case HEAVY:
            return 0.95;
        default:
            return 1.0;
        }
    }

    private static double armorBreakerMultiplier(final ArmorTier tier) {
        switch (tier) {
        case LIGHT:
            return 0.95;
        case HEAVY:
            return 1.15;
        case NONE:
        case MEDIUM:
        default:
            return 1.0;
        }
    }
}
