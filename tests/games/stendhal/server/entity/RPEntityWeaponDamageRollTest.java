/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RPEntityWeaponDamageRollTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void mixedWeaponRollsUseIndependentArmorMultipliers() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestEntity attacker = new TestEntity(Arrays.<Item>asList(
				dagger, axe), 50.0f);
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Heavy armor: dagger 60%, axe 125%. The extra 50 ATK represents
		// rings/glyphs/other equipment and must not receive either multiplier.
		final float expected = 50.0f + 20.0f * 0.60f + 10.0f * 1.25f;
		final float attack = attacker.getItemAtkForAttack(weapon ->
				WeaponArmorInteractionService.getDamageMultiplier(
						weapon, defender));

		assertEquals(expected, attack, 0.0001f);
	}

	@Test
	public void unmodifiedRollPreservesStableAttack() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestEntity attacker = new TestEntity(Arrays.<Item>asList(
				dagger, axe), 50.0f);

		assertEquals(80.0f, attacker.getItemAtkForAttack(), 0.0001f);
	}

	private static Weapon fixedWeapon(final String weaponClass,
			final int damage) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(damage));
		attributes.put("damage_min", Integer.toString(damage));
		attributes.put("damage_max", Integer.toString(damage));
		attributes.put("rate", "5");
		return new Weapon("test " + weaponClass, weaponClass, "test",
				attributes);
	}

	private static final class TestEntity extends RPEntity {
		private final List<Item> weapons;
		private final float nonWeaponAttack;

		private TestEntity(final List<Item> weapons,
				final float nonWeaponAttack) {
			this.weapons = weapons;
			this.nonWeaponAttack = nonWeaponAttack;
		}

		@Override
		public List<Item> getWeapons() {
			return weapons;
		}

		@Override
		public float getItemAtk() {
			float attack = nonWeaponAttack;
			for (final Item weapon : weapons) {
				attack += weapon.getAverageDamage();
			}
			return attack;
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// no items
		}

		@Override
		public void logic() {
			// no logic
		}
	}
}
