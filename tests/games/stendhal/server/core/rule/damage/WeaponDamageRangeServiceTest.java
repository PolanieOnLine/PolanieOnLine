/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.ItemTestHelper;

public class WeaponDamageRangeServiceTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void axeRangeIsDerivedFromLegacyAttack() {
		final Weapon weapon = weapon("axe", 20, 5);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(17, weapon.getInt("damage_min"));
		assertEquals(23, weapon.getInt("damage_max"));
		assertEquals(20.0, weapon.getAverageDamage(), 0.0);
	}

	@Test
	public void heavyWeaponUsesWiderRange() {
		final Weapon weapon = weapon("sword", 40, 9);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(32, weapon.getInt("damage_min"));
		assertEquals(48, weapon.getInt("damage_max"));
	}

	@Test
	public void daggerUsesNarrowRange() {
		final Weapon weapon = weapon("dagger", 40, 3);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(38, weapon.getInt("damage_min"));
		assertEquals(42, weapon.getInt("damage_max"));
	}

	@Test
	public void explicitRangeIsPreserved() {
		final Weapon weapon = weapon("axe", 20, 5);
		weapon.put("damage_min", 8);
		weapon.put("damage_max", 35);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(8, weapon.getInt("damage_min"));
		assertEquals(35, weapon.getInt("damage_max"));
	}

	@Test
	public void partialOverrideFallsBackToGeneratedPair() {
		final Weapon weapon = weapon("axe", 20, 5);
		weapon.put("damage_min", 2);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(17, weapon.getInt("damage_min"));
		assertEquals(23, weapon.getInt("damage_max"));
	}

	@Test
	public void restoreWaitsUntilSavedAttackHasBeenApplied() {
		final Weapon weapon = weapon("axe", 20, 5);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.restore());

		assertFalse(weapon.has("damage_min"));
		assertFalse(weapon.has("damage_max"));
	}

	@Test
	public void oldSavedWeaponIsMigratedFromItsRestoredAttack() {
		final Weapon weapon = weapon("axe", 20, 5);
		weapon.put("atk", 40);

		WeaponDamageRangeService.migrateRestored(weapon, 20, null, null);

		assertEquals(34, weapon.getInt("damage_min"));
		assertEquals(46, weapon.getInt("damage_max"));
		assertEquals(40.0, weapon.getAverageDamage(), 0.0);
	}

	@Test
	public void savedRangeIsAuthoritativeDuringMigration() {
		final Weapon weapon = weapon("axe", 40, 5);
		weapon.put("damage_min", 11);
		weapon.put("damage_max", 77);

		WeaponDamageRangeService.migrateRestored(weapon, 20,
				Integer.valueOf(8), Integer.valueOf(35));

		assertEquals(11, weapon.getInt("damage_min"));
		assertEquals(77, weapon.getInt("damage_max"));
	}

	@Test
	public void exceptionalDefinitionRangeScalesToRestoredAttack() {
		final Weapon weapon = weapon("axe", 40, 5);

		WeaponDamageRangeService.migrateRestored(weapon, 20,
				Integer.valueOf(8), Integer.valueOf(35));

		assertEquals(16, weapon.getInt("damage_min"));
		assertEquals(70, weapon.getInt("damage_max"));
	}

	@Test
	public void attackBonusOnAccessoryDoesNotBecomeWeaponDamage() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "20");
		final Item ring = new Item("test ring", "ring", "test", attributes);

		WeaponDamageRangeService.initialize(ring,
				ItemCreationContext.defaultCreation());
		WeaponDamageRangeService.migrateRestored(ring, 20, null, null);

		assertFalse(ring.has("damage_min"));
		assertFalse(ring.has("damage_max"));
	}

	@Test
	public void veryLowAttackRemainsStable() {
		final Weapon weapon = weapon("dagger", 2, 2);

		WeaponDamageRangeService.initialize(weapon,
				ItemCreationContext.defaultCreation());

		assertEquals(2, weapon.getInt("damage_min"));
		assertEquals(2, weapon.getInt("damage_max"));
	}

	private Weapon weapon(final String itemClass, final int attack,
			final int rate) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", Integer.toString(rate));
		return new Weapon("test weapon", itemClass, "test", attributes);
	}
}
