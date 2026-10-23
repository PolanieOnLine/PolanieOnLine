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

	@Test public void unarmedHitWithoutLifeStealDoesNotHeal() {
		hit(1000);
		assertEquals(500, attacker.getHP());
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
