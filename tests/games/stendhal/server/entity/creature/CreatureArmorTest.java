/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import utilities.RPClass.CreatureTestHelper;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.RPClass;

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
		assertEquals("none", creature.get("armor_type"));
	}

	@Test
	public void armorIsPublishedWithoutExposingCombatMultipliers() {
		final Definition definition = RPClass.getRPClass("creature")
				.getDefinition(DefinitionClass.ATTRIBUTE, "armor_type");
		assertTrue(definition.isVisible());
		assertFalse(definition.isPrivate());
		assertFalse(definition.isHidden());
		assertEquals("none", new Creature().get("armor_type"));
	}

	@Test
	public void restoredCreatureRetainsItsConfiguredArmor() {
		final Creature creature = new Creature();
		creature.setArmorType("heavy");
		assertEquals("heavy", new Creature((marauroa.common.game.RPObject) creature).getArmorType());
	}

	@Test
	public void lookDescriptionUsesPolishArmorNamesAndKeepsOriginalDescription() {
		final Creature creature = new Creature();
		creature.setDescription("Oto przeciwnik testowy.");
		creature.setLevel(20);
		for (final String[] entry : new String[][] {
				{"light", "Pancerz lekki"}, {"medium", "Pancerz średni"},
				{"heavy", "Pancerz ciężki"}, {"none", "Brak pancerza"}}) {
			creature.setArmorType(entry[0]);
			assertEquals("Oto przeciwnik testowy. Poziom 20. " + entry[1] + ".", creature.describe());
		}
		creature.setArmorType(null);
		assertEquals("none", creature.get("armor_type"));
	}
}
