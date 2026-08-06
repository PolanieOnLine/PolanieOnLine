/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService.ArmorTier;

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

	private double multiplier(final String weaponClass, final int armor) {
		return WeaponArmorInteractionService.getDamageMultiplier(
				weaponClass, armor);
	}
}
