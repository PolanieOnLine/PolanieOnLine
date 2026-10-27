/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.PlayerSlot;
import utilities.PlayerTestHelper;

public class RPEntityLifestealEquipmentTest {
	private Player attacker;

	@Before public void setUp() {
		attacker = PlayerTestHelper.createPlayer("lifesteal equipment test");
		attacker.setBaseHP(10000);
		attacker.setHP(500);
	}

	@Test public void unarmedGlovesStealLife() {
		equip("glove", "glove", 0.05);
		hit(1000);
		assertEquals(550, attacker.getHP());
	}

	@Test public void unarmedRingStealsLifeInEitherSlot() {
		for (String slot : new String[] {"finger", "fingerb"}) {
			equip(slot, "ring", 0.025);
			attacker.setHP(500);
			hit(1000);
			assertEquals(slot, 525, attacker.getHP());
			attacker.getSlot(slot).clear();
		}
	}

	@Test public void unarmedEquipmentAndBloodGlyphBonusesCombine() {
		equip("glove", "glove", 0.05);
		equip("finger", "ring", 0.025);
		equipBloodGlyph(20.0);
		hit(1000);
		assertEquals(775, attacker.getHP());
	}

	@Test public void unarmedEquipmentKeepsLifeStealEffectiveness() {
		Item gloves = equip("glove", "glove", 0.05);
		gloves.put("lifesteal_effectiveness", 20);
		hit(1000);
		assertEquals(560, attacker.getHP());
	}

	@Test public void zeroAndNegativeLifeStealUseCorrectHealthChanges() {
		hit(1000);
		assertEquals(500, attacker.getHP());
		Item weapon = new Item("cursed weapon", "club", "test", null);
		weapon.put("atk", 48);
		weapon.put("lifesteal", -0.1);
		for (int damage : new int[] {5, 10, 100}) {
			attacker.setHP(500);
			attacker.handleLifesteal(attacker, Arrays.asList(weapon), damage);
			assertEquals("Damage=" + damage, damage == 100 ? 490 : 499, attacker.getHP());
		}
	}

	@Test public void armedWeaponWeightsAndEquipmentBonusesStayUnchanged() {
		Item first = new Item("first weapon", "sword", "test", null);
		first.put("atk", 100);
		first.put("lifesteal", 0.1);
		Item second = new Item("second weapon", "sword", "test", null);
		second.put("atk", 300);
		second.put("lifesteal", 0.2);
		equip("glove", "glove", 0.05);
		equip("finger", "ring", 0.025);
		equipBloodGlyph(20.0);
		attacker.handleLifesteal(attacker, Arrays.asList(first, second), 1000);
		// Weapon average: 17.5%; equipment and glyph: 5% + 2.5% + 20%.
		assertEquals(950, attacker.getHP());
	}

	@Test public void bothRingsStackRegardlessOfSlotOrHeldWeapon() {
		for (boolean armed : new boolean[] {false, true}) {
			if (armed) equipNonLifeStealWeapon();
			for (boolean swapped : new boolean[] {false, true}) {
				equip("finger", "ring", swapped ? 0.025 : 0.01);
				equip("fingerb", "ring", swapped ? 0.01 : 0.025);
				attacker.setHP(500);
				hit(1000);
				assertEquals("armed=" + armed + ", swapped=" + swapped, 535, attacker.getHP());
				attacker.getSlot("finger").clear();
				attacker.getSlot("fingerb").clear();
			}
		}
	}

	@Test public void zeroLifeStealInFirstRingDoesNotHideSecondRing() {
		equipNonLifeStealWeapon();
		equip("finger", "ring", 0.0);
		equip("fingerb", "ring", 0.025);
		hit(1000);
		assertEquals(525, attacker.getHP());
	}

	@Test public void bothRingsKeepEffectivenessAndCombineWithGlovesAndGlyph() {
		equip("finger", "ring", 0.01);
		Item secondRing = equip("fingerb", "ring", 0.025);
		secondRing.put("lifesteal_effectiveness", 20);
		equip("glove", "glove", 0.05);
		equipBloodGlyph(20.0);
		hit(1000);
		// 1% + (2.5% * 1.2) + 5% + 20% = 29%.
		assertEquals(790, attacker.getHP());
	}

	private void equipNonLifeStealWeapon() {
		Item weapon = equip("lhand", "sword", 0.0);
		weapon.put("atk", 100);
		weapon.put("rate", 5);
	}

	private void hit(int damage) {
		attacker.handleLifesteal(attacker, attacker.getWeapons(), damage);
	}

	private Item equip(String slot, String itemClass, double lifesteal) {
		Item item = new Item("test " + slot, itemClass, "test", null);
		item.put("lifesteal", lifesteal);
		attacker.getSlot(slot).add(item);
		return item;
	}

	private void equipBloodGlyph(double percent) {
		if (!attacker.hasSlot("offensive_rune")) attacker.addSlot(new PlayerSlot("offensive_rune"));
		Item glyph = new Item("test blood glyph", "glyph", "test", null);
		glyph.put("lifesteal_increase", percent);
		attacker.getSlot("offensive_rune").add(glyph);
	}
}
