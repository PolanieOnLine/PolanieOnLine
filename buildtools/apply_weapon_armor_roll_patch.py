from pathlib import Path


def replace_once(path, old, new):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected one match, found {count}")
    target.write_text(text.replace(old, new, 1), encoding="utf-8")


# Use the same stable attack baseline that produced the rolled attack so the
# matchup can recover the actual weapon roll instead of using average damage.
replace_once(
    "src/games/stendhal/server/core/rp/StendhalRPAction.java",
    "\t\t\tfinal List<Item> weapons = player.getWeapons();\n\t\t\tfinal float itemAtk;\n\n\t\t\tif (Testing.COMBAT && isRanged) {\n\t\t\t\titemAtk = player.getItemRatkForAttack();\n\t\t\t} else {\n\t\t\t\titemAtk = player.getItemAtkForAttack();\n\t\t\t}\n\n\t\t\tfinal double armorAdjustedItemAtk =\n\t\t\t\t\tWeaponArmorInteractionService.adjustAttack(\n\t\t\t\t\t\t\titemAtk, attackWeapon, defender);",
    "\t\t\tfinal List<Item> weapons = player.getWeapons();\n\t\t\tfinal float itemAtk;\n\t\t\tfinal float stableItemAtk;\n\n\t\t\tif (Testing.COMBAT && isRanged) {\n\t\t\t\titemAtk = player.getItemRatkForAttack();\n\t\t\t\tstableItemAtk = player.getItemRatk();\n\t\t\t} else {\n\t\t\t\titemAtk = player.getItemAtkForAttack();\n\t\t\t\tstableItemAtk = player.getItemAtk();\n\t\t\t}\n\n\t\t\tfinal double armorAdjustedItemAtk =\n\t\t\t\t\tWeaponArmorInteractionService.adjustAttack(\n\t\t\t\t\t\t\titemAtk, stableItemAtk, weapons, attackWeapon, defender);")

service = '''/***************************************************************************
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
'''
Path("src/games/stendhal/server/core/rule/damage/WeaponArmorInteractionService.java").write_text(service, encoding="utf-8")

test = '''/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService.ArmorTier;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class WeaponArmorInteractionServiceTest {
    @BeforeClass
    public static void generateRPClasses() {
        ItemTestHelper.generateRPClasses();
        CreatureTestHelper.generateRPClasses();
    }

    @Test
    public void classifiesArmorTiers() {
        assertEquals(ArmorTier.NONE,
                WeaponArmorInteractionService.classify(0));
        assertEquals(ArmorTier.LIGHT,
                WeaponArmorInteractionService.classify(30));
        assertEquals(ArmorTier.MEDIUM,
                WeaponArmorInteractionService.classify(31));
        assertEquals(ArmorTier.MEDIUM,
                WeaponArmorInteractionService.classify(80));
        assertEquals(ArmorTier.HEAVY,
                WeaponArmorInteractionService.classify(81));
    }

    @Test
    public void unarmoredTargetsAreNeutralForSupportedWeaponClasses() {
        assertEquals(1.0, multiplier("dagger", 0), 0.0);
        assertEquals(1.0, multiplier("sword", 0), 0.0);
        assertEquals(1.0, multiplier("axe", 0), 0.0);
        assertEquals(1.0, multiplier("club", 0), 0.0);
    }

    @Test
    public void daggerIsBestAgainstLightArmor() {
        final double dagger = multiplier("dagger", 20);
        assertTrue(dagger > multiplier("sword", 20));
        assertTrue(dagger > multiplier("axe", 20));
    }

    @Test
    public void swordIsBestAgainstMediumArmor() {
        final double sword = multiplier("sword", 50);
        assertTrue(sword > multiplier("dagger", 50));
        assertTrue(sword > multiplier("axe", 50));
    }

    @Test
    public void axeAndClubAreBestAgainstHeavyArmor() {
        final double axe = multiplier("axe", 100);
        final double club = multiplier("club", 100);
        assertEquals(axe, club, 0.0);
        assertTrue(axe > multiplier("sword", 100));
        assertTrue(axe > multiplier("dagger", 100));
    }

    @Test
    public void unsupportedWeaponClassRemainsNeutral() {
        assertEquals(1.0, multiplier("wand", 100), 0.0);
        assertEquals(1.0, multiplier(null, 100), 0.0);
    }

    @Test
    public void multiplierChangesOnlyPrimaryWeaponContribution() {
        final double equipmentAttack = 50.0;
        final double primaryWeapon = 30.0;

        assertEquals(53.0,
                WeaponArmorInteractionService.adjustWeaponContribution(
                        equipmentAttack, primaryWeapon, 1.10), 0.000001);
        assertEquals(44.0,
                WeaponArmorInteractionService.adjustWeaponContribution(
                        equipmentAttack, primaryWeapon, 0.80), 0.000001);
    }

    @Test
    public void attackUsesDefenseAsDefaultArmorScore() {
        final Creature defender = new Creature();
        defender.setDef(20);
        final Weapon weapon = weapon("dagger", 30);

        assertEquals(expectedAdjusted(50.0, 50.0, weapon, 1.10),
                WeaponArmorInteractionService.adjustAttack(50.0, 50.0,
                        Arrays.asList(weapon), weapon, defender), 0.000001);
    }

    @Test
    public void explicitArmorOverridesDefenseForMatchup() {
        final Creature defender = new Creature();
        defender.setDef(100);
        defender.setArmor(20);
        final Weapon weapon = weapon("dagger", 30);

        assertEquals(expectedAdjusted(50.0, 50.0, weapon, 1.10),
                WeaponArmorInteractionService.adjustAttack(50.0, 50.0,
                        Arrays.asList(weapon), weapon, defender), 0.000001);
    }

    @Test
    public void matchupUsesActualRolledWeaponContribution() {
        final Creature defender = new Creature();
        defender.setDef(20);
        final Weapon weapon = weapon("dagger", 30);
        final double stableAttack = 50.0;
        final double rolledAttack = 44.0;

        final double stableWeapon = weapon.getAverageDamage();
        final double rolledWeapon = rolledAttack
                - (stableAttack - stableWeapon);
        final double expected = rolledAttack + rolledWeapon * 0.10;

        assertEquals(expected, WeaponArmorInteractionService.adjustAttack(
                rolledAttack, stableAttack, Arrays.asList(weapon), weapon,
                defender), 0.000001);
        assertTrue(Math.abs(expected - (rolledAttack + stableWeapon * 0.10))
                > 0.000001);
    }

    @Test
    public void nonCreatureTargetRemainsNeutral() {
        final Weapon weapon = weapon("dagger", 30);
        final RPEntity playerTarget = new RPEntity() {
            @Override
            protected void dropItemsOn(final Corpse corpse) {
                // no items
            }

            @Override
            public void logic() {
                // no logic
            }
        };

        assertEquals(50.0, WeaponArmorInteractionService.adjustAttack(
                50.0, 50.0, Arrays.asList(weapon), weapon, playerTarget),
                0.000001);
    }

    @Test
    public void missingWeaponRemainsNeutral() {
        assertEquals(25.0, WeaponArmorInteractionService.adjustAttack(
                25.0, 25.0, Collections.<Weapon>emptyList(), null, null),
                0.000001);
    }

    @Test
    public void negativeWeaponContributionCannotCorruptAttack() {
        assertEquals(25.0,
                WeaponArmorInteractionService.adjustWeaponContribution(
                        25.0, -10.0, 1.15), 0.000001);
    }

    private double expectedAdjusted(final double rolledAttack,
            final double stableAttack, final Weapon weapon,
            final double multiplier) {
        final double rolledWeapon = rolledAttack
                - (stableAttack - weapon.getAverageDamage());
        return rolledAttack + rolledWeapon * (multiplier - 1.0);
    }

    private double multiplier(final String weaponClass, final int armor) {
        return WeaponArmorInteractionService.getDamageMultiplier(
                weaponClass, armor);
    }

    private Weapon weapon(final String itemClass, final int attack) {
        final Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("atk", Integer.toString(attack));
        attributes.put("rate", "5");
        return new Weapon("test weapon", itemClass, "test", attributes);
    }
}
'''
Path("tests/games/stendhal/server/core/rule/damage/WeaponArmorInteractionServiceTest.java").write_text(test, encoding="utf-8")
