from pathlib import Path
import re


def replace_once(path, old, new):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f"{path}: expected one match, found {count}")
    target.write_text(text.replace(old, new, 1), encoding="utf-8")


replace_once(
    "data/conf/creatures.xsd",
    '\t\t\t<xsd:element name="armor" minOccurs="0" maxOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" type="xsd:nonNegativeInteger" use="required" />\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>',
    '\t\t\t<xsd:element name="armor" minOccurs="0" maxOccurs="1">\n\t\t\t\t<xsd:complexType>\n\t\t\t\t\t<xsd:attribute name="value" use="required">\n\t\t\t\t\t\t<xsd:simpleType>\n\t\t\t\t\t\t\t<xsd:restriction base="xsd:string">\n\t\t\t\t\t\t\t\t<xsd:enumeration value="none" />\n\t\t\t\t\t\t\t\t<xsd:enumeration value="light" />\n\t\t\t\t\t\t\t\t<xsd:enumeration value="medium" />\n\t\t\t\t\t\t\t\t<xsd:enumeration value="heavy" />\n\t\t\t\t\t\t\t</xsd:restriction>\n\t\t\t\t\t\t</xsd:simpleType>\n\t\t\t\t\t</xsd:attribute>\n\t\t\t\t</xsd:complexType>\n\t\t\t</xsd:element>')

replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\tprivate Integer armor;",
    "\tprivate String armorType;")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tarmor = null;",
    "\t\t\tarmorType = null;")
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    '\t\t\t} else if (qName.equals("armor")) {\n\t\t\t\tarmor = Integer.valueOf(attrs.getValue("value"));',
    '\t\t\t} else if (qName.equals("armor")) {\n\t\t\t\tarmorType = attrs.getValue("value");')
replace_once(
    "src/games/stendhal/server/core/config/CreaturesXMLLoader.java",
    "\t\t\tcreature.setArmor(armor);",
    "\t\t\tcreature.setArmorType(armorType);")

replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t/** Optional armor score overriding the defense fallback. */\n\tprivate Integer armor;",
    "\t/** Optional semantic creature armor type. */\n\tprivate String armorType;")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\tpublic void setArmor(final Integer armor) {\n\t\tthis.armor = armor;\n\t}\n\n\tpublic Integer getArmor() {\n\t\treturn armor;\n\t}",
    "\tpublic void setArmorType(final String armorType) {\n\t\tthis.armorType = armorType;\n\t}\n\n\tpublic String getArmorType() {\n\t\treturn armorType;\n\t}")
replace_once(
    "src/games/stendhal/server/core/rule/defaultruleset/DefaultCreature.java",
    "\t\tif (armor != null) {\n\t\t\tcreature.setArmor(armor.intValue());\n\t\t}",
    "\t\tif (armorType != null) {\n\t\t\tcreature.setArmorType(armorType);\n\t\t}")

replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\t\tif (copy.has(\"armor\")) {\n\t\t\tsetArmor(copy.getInt(\"armor\"));\n\t\t}",
    "\t\tif (copy.has(\"armor_type\")) {\n\t\t\tsetArmorType(copy.get(\"armor_type\"));\n\t\t}")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    "\t/** Sets an explicit armor score independent from DEF. */\n\tpublic void setArmor(final int armor) {\n\t\tput(\"armor\", Math.max(0, Math.min(Short.MAX_VALUE, armor)));\n\t}\n\n\t/** Returns explicit armor or falls back to the current DEF value. */\n\tpublic int getArmorScore() {\n\t\treturn has(\"armor\") ? Math.max(0, getInt(\"armor\"))\n\t\t\t\t: Math.max(0, getDef());\n\t}",
    "\t/** Sets the semantic armor type used by weapon matchups. */\n\tpublic void setArmorType(final String armorType) {\n\t\tif (armorType == null || \"none\".equals(armorType)) {\n\t\t\tremove(\"armor_type\");\n\t\t\treturn;\n\t\t}\n\t\tput(\"armor_type\", armorType);\n\t}\n\n\t/** Returns the semantic armor type. Missing armor is explicitly none. */\n\tpublic String getArmorType() {\n\t\treturn has(\"armor_type\") ? get(\"armor_type\") : \"none\";\n\t}")
replace_once(
    "src/games/stendhal/server/entity/creature/Creature.java",
    '\t\t\tnpc.addAttribute("armor", Type.SHORT, Definition.HIDDEN);',
    '\t\t\tnpc.addAttribute("armor_type", Type.STRING, Definition.HIDDEN);')

service = '''/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Applies explicit weapon-class advantages against semantic creature armor.
 * Creature DEF remains the normal defense stat and is intentionally not used
 * to infer armor: high DEF can represent agility, evasion or other defenses.
 */
public final class WeaponArmorInteractionService {
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
				((Creature) defender).getArmorType());
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
		return contribution;
	}

	static double adjustWeaponContribution(final double totalItemAttack,
			final double weaponContribution, final double multiplier) {
		final double safeWeaponContribution = Math.max(0.0,
				weaponContribution);
		return Math.max(0.0, totalItemAttack
				+ safeWeaponContribution * (multiplier - 1.0));
	}

	public static ArmorTier classify(final String armorType) {
		if ("light".equals(armorType)) {
			return ArmorTier.LIGHT;
		}
		if ("medium".equals(armorType)) {
			return ArmorTier.MEDIUM;
		}
		if ("heavy".equals(armorType)) {
			return ArmorTier.HEAVY;
		}
		return ArmorTier.NONE;
	}

	public static double getDamageMultiplier(final String weaponClass,
			final String armorType) {
		if (weaponClass == null) {
			return 1.0;
		}

		final ArmorTier tier = classify(armorType);
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

creature_test = '''/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.RPClass.CreatureTestHelper;

public class CreatureArmorTest {
	@BeforeClass
	public static void generateRPClasses() {
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void missingArmorIsUnarmoredRegardlessOfDefense() {
		final Creature creature = new Creature();
		creature.setDef(150);

		assertEquals("none", creature.getArmorType());
	}

	@Test
	public void explicitArmorTypeIsStored() {
		final Creature creature = new Creature();
		creature.setDef(1);
		creature.setArmorType("heavy");

		assertEquals("heavy", creature.getArmorType());
	}

	@Test
	public void settingNoneClearsExplicitArmorType() {
		final Creature creature = new Creature();
		creature.setArmorType("medium");
		creature.setArmorType("none");

		assertEquals("none", creature.getArmorType());
	}
}
'''
Path("tests/games/stendhal/server/entity/creature/CreatureArmorTest.java").write_text(creature_test, encoding="utf-8")

interaction_test = '''/***************************************************************************
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
import games.stendhal.server.entity.item.Item;
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
	public void classifiesSemanticArmorTiers() {
		assertEquals(ArmorTier.NONE,
				WeaponArmorInteractionService.classify(null));
		assertEquals(ArmorTier.NONE,
				WeaponArmorInteractionService.classify("none"));
		assertEquals(ArmorTier.LIGHT,
				WeaponArmorInteractionService.classify("light"));
		assertEquals(ArmorTier.MEDIUM,
				WeaponArmorInteractionService.classify("medium"));
		assertEquals(ArmorTier.HEAVY,
				WeaponArmorInteractionService.classify("heavy"));
	}

	@Test
	public void unarmoredTargetsAreNeutralForSupportedWeaponClasses() {
		assertEquals(1.0, multiplier("dagger", "none"), 0.0);
		assertEquals(1.0, multiplier("sword", "none"), 0.0);
		assertEquals(1.0, multiplier("axe", "none"), 0.0);
		assertEquals(1.0, multiplier("club", "none"), 0.0);
	}

	@Test
	public void daggerIsBestAgainstLightArmor() {
		final double dagger = multiplier("dagger", "light");
		assertTrue(dagger > multiplier("sword", "light"));
		assertTrue(dagger > multiplier("axe", "light"));
	}

	@Test
	public void swordIsBestAgainstMediumArmor() {
		final double sword = multiplier("sword", "medium");
		assertTrue(sword > multiplier("dagger", "medium"));
		assertTrue(sword > multiplier("axe", "medium"));
	}

	@Test
	public void axeAndClubAreBestAgainstHeavyArmor() {
		final double axe = multiplier("axe", "heavy");
		final double club = multiplier("club", "heavy");
		assertEquals(axe, club, 0.0);
		assertTrue(axe > multiplier("sword", "heavy"));
		assertTrue(axe > multiplier("dagger", "heavy"));
	}

	@Test
	public void unsupportedWeaponClassRemainsNeutral() {
		assertEquals(1.0, multiplier("wand", "heavy"), 0.0);
		assertEquals(1.0, multiplier(null, "heavy"), 0.0);
	}

	@Test
	public void highDefenseWithoutArmorTypeRemainsUnarmored() {
		final Creature defender = new Creature();
		defender.setDef(150);
		final Weapon weapon = weapon("dagger", 30);

		assertEquals(50.0, WeaponArmorInteractionService.adjustAttack(
				50.0, 50.0, Arrays.asList(weapon), weapon, defender),
				0.000001);
	}

	@Test
	public void explicitArmorTypeControlsMatchupIndependentlyOfDefense() {
		final Creature defender = new Creature();
		defender.setDef(1);
		defender.setArmorType("heavy");
		final Weapon weapon = weapon("axe", 30);

		assertEquals(expectedAdjusted(50.0, 50.0, weapon, 1.15),
				WeaponArmorInteractionService.adjustAttack(50.0, 50.0,
						Arrays.asList(weapon), weapon, defender), 0.000001);
	}

	@Test
	public void matchupUsesActualRolledWeaponContribution() {
		final Creature defender = new Creature();
		defender.setArmorType("light");
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
				25.0, 25.0, Collections.<Item>emptyList(), null, null),
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

	private double multiplier(final String weaponClass,
			final String armorType) {
		return WeaponArmorInteractionService.getDamageMultiplier(
				weaponClass, armorType);
	}

	private Weapon weapon(final String itemClass, final int attack) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		return new Weapon("test weapon", itemClass, "test", attributes);
	}
}
'''
Path("tests/games/stendhal/server/core/rule/damage/WeaponArmorInteractionServiceTest.java").write_text(interaction_test, encoding="utf-8")


def set_armor(path, creature_name, tier):
    target = Path(path)
    text = target.read_text(encoding="utf-8")
    pattern = re.compile(
        r'(<creature name="' + re.escape(creature_name) + r'">.*?<def value="[^"]+"/>)',
        re.DOTALL)
    replacement = r'\1\n\t\t\t<armor value="' + tier + r'"/>'
    text, count = pattern.subn(replacement, text, count=1)
    if count != 1:
        raise RuntimeError(f"{path}: creature not found exactly once: {creature_name}")
    target.write_text(text, encoding="utf-8")


knights = "data/conf/creatures/pol/human_knights.xml"
for name in [
    "rycerz szafirowy",
    "rycerz karmazynowy",
    "rycerz szmaragdowy",
    "rycerz w złotej zbroi",
    "czarny rycerz",
    "rycerz na białym koniu",
]:
    set_armor(knights, name, "heavy")

for name in ["strażnik", "strażnik grobli", "strażnik bramy"]:
    set_armor(knights, name, "medium")
