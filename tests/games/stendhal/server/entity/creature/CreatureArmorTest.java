/***************************************************************************
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
