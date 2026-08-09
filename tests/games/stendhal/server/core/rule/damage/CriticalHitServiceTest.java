/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class CriticalHitServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void baseChanceIsExactlyTenPercent() {
		final Player player = player();
		assertEquals(10.0, CriticalHitService.getCriticalChance(player), 0.0);
		assertEquals(10.0, CriticalHitService.getCriticalChance(null), 0.0);
	}

	@Test
	public void weaponCriticalChanceAddsPercentagePoints() {
		final Player player = player();
		final Weapon weapon = weapon(7.0);
		assertTrue(player.equip("rhand", weapon));

		assertEquals(17.0, CriticalHitService.getCriticalChance(player), 0.0);
	}

	@Test
	public void weaponAndGlyphBonusesAreAdditive() {
		final Player player = player();
		assertTrue(player.equip("rhand", weapon(7.0)));
		final Item glyph = new Item("critical glyph", "glyph", "test", null);
		glyph.put(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE, 15.0);
		assertTrue(player.equip("offensive_rune", glyph));

		assertEquals(32.0, CriticalHitService.getCriticalChance(player), 0.0);
	}

	@Test
	public void finalChanceIsCappedAtFiftyPercent() {
		final Player player = player();
		assertTrue(player.equip("rhand", weapon(90.0)));

		assertEquals(50.0, CriticalHitService.getCriticalChance(player), 0.0);
	}

	@Test
	public void criticalDamageAffixRaisesMultiplierAboveBaseDoubleDamage() {
		final Player player = player();
		final Weapon weapon = weapon(0.0);
		weapon.put(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE, 0.20);
		assertTrue(player.equip("rhand", weapon));

		assertEquals(2.20, CriticalHitService.getCriticalDamageMultiplier(player),
				0.0000001);
		assertEquals(220, CriticalHitService.applyCriticalDamage(player, 100));
	}

	@Test
	public void flatGlyphCriticalDamageBonusIsStillPreserved() {
		final Player player = player();
		final Item glyph = new Item("critical damage glyph", "glyph", "test", null);
		glyph.put("critical_additional_bonus", 7.0);
		assertTrue(player.equip("offensive_rune", glyph));

		assertEquals(207, CriticalHitService.applyCriticalDamage(player, 100));
	}

	@Test
	public void deterministicRollUsesExactPercentageBoundary() {
		assertTrue(CriticalHitService.isCriticalSuccessful(10.0, 10));
		assertFalse(CriticalHitService.isCriticalSuccessful(10.0, 11));
		assertTrue(CriticalHitService.isCriticalSuccessful(50.0, 50));
		assertFalse(CriticalHitService.isCriticalSuccessful(50.0, 51));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsRollBelowD100Range() {
		CriticalHitService.isCriticalSuccessful(10.0, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsRollAboveD100Range() {
		CriticalHitService.isCriticalSuccessful(10.0, 101);
	}

	private Player player() {
		return PlayerTestHelper.createPlayer("critical tester");
	}

	private Weapon weapon(final double criticalChance) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		final Weapon weapon = new Weapon("critical sword", "sword", "test",
				attributes);
		weapon.put(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE, criticalChance);
		return weapon;
	}
}
