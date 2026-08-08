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
	public void defenseIsDefaultArmorScore() {
		final Creature creature = new Creature();
		creature.setDef(55);

		assertEquals(55, creature.getArmorScore());
	}

	@Test
	public void explicitArmorOverridesDefense() {
		final Creature creature = new Creature();
		creature.setDef(55);
		creature.setArmor(12);

		assertEquals(12, creature.getArmorScore());
	}

	@Test
	public void zeroArmorCanMarkHighDefenseCreatureAsUnarmored() {
		final Creature creature = new Creature();
		creature.setDef(150);
		creature.setArmor(0);

		assertEquals(0, creature.getArmorScore());
	}

	@Test
	public void negativeArmorOverrideIsClamped() {
		final Creature creature = new Creature();
		creature.setDef(55);
		creature.setArmor(-10);

		assertEquals(0, creature.getArmorScore());
	}
}
