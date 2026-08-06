/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService.ArmorTier;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;

public class WeaponArmorInteractionServiceTest {
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
	public void attackUsesCreatureDefenseAsArmorScore() {
		final Creature defender = createMock(Creature.class);
		final Item weapon = createMock(Item.class);
		expect(defender.getDef()).andReturn(20);
		expect(weapon.getWeaponType()).andReturn("dagger");
		expect(weapon.getAverageDamage()).andReturn(30.0f);
		replay(defender, weapon);

		assertEquals(53.0, WeaponArmorInteractionService.adjustAttack(
				50.0, weapon, defender), 0.000001);

		verify(defender, weapon);
	}

	@Test
	public void nonCreatureTargetRemainsNeutral() {
		final Item weapon = createMock(Item.class);
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
		replay(weapon);

		assertEquals(50.0, WeaponArmorInteractionService.adjustAttack(
				50.0, weapon, playerTarget), 0.000001);

		verify(weapon);
	}

	@Test
	public void missingWeaponRemainsNeutral() {
		assertEquals(25.0, WeaponArmorInteractionService.adjustAttack(
				25.0, null, null), 0.000001);
	}

	@Test
	public void negativeWeaponContributionCannotCorruptAttack() {
		assertEquals(25.0,
				WeaponArmorInteractionService.adjustWeaponContribution(
						25.0, -10.0, 1.15), 0.000001);
	}

	private double multiplier(final String weaponClass, final int armor) {
		return WeaponArmorInteractionService.getDamageMultiplier(
				weaponClass, armor);
	}
}
