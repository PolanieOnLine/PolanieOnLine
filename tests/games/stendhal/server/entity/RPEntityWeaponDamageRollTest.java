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
	private static final String OFFENSIVE_RUNE_SLOT = "offensive_rune";

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
				dagger, axe), 50);
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Heavy armor: dagger 40%, axe 130%. The extra 50 ATK represents
		// rings/glyphs/other equipment and must not receive either multiplier.
		final float expected = 50.0f + 20.0f * 0.40f + 10.0f * 1.30f;
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
				dagger, axe), 50);

		assertEquals(80.0f, attacker.getItemAtkForAttack(), 0.0001f);
	}

	@Test
	public void glyphAttackPercentageFollowsActualModifiedWeaponRoll() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestEntity attacker = new TestEntity(Arrays.<Item>asList(
				dagger, axe), 50);
		attacker.setGlyphAttackBonus(10.0);
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Stable equipment attack is 80, so the tooltip/stable value is 88 with
		// the +10% glyph. Against heavy armor the actual weapon contribution is
		// 20*0.40 + 10*1.30 = 21. The percentage must follow that actual 21-point
		// contribution: (50 + 21) * 1.10 = 78.1. Applying the glyph to the stable
		// weapon value before substituting the roll would incorrectly produce 79.
		final float expected = (50.0f + 20.0f * 0.40f + 10.0f * 1.30f) * 1.10f;
		final float attack = attacker.getItemAtkForAttack(weapon ->
				WeaponArmorInteractionService.getDamageMultiplier(
						weapon, defender));

		assertEquals(expected, attack, 0.0001f);
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
		private final Item attackGlyph;

		private TestEntity(final List<Item> weapons,
				final int nonWeaponAttack) {
			this.weapons = weapons;
			addSlot(OFFENSIVE_RUNE_SLOT);
			attackGlyph = new Item("test attack glyph", "glyph", "test", null);
			attackGlyph.put("atk", nonWeaponAttack);
			getSlot(OFFENSIVE_RUNE_SLOT).add(attackGlyph);
		}

		private void setGlyphAttackBonus(final double percentagePoints) {
			attackGlyph.put("atk_additional_bonus", percentagePoints);
		}

		@Override
		public List<Item> getWeapons() {
			return weapons;
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
