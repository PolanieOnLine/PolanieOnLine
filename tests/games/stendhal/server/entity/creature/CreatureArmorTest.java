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
	public void storesAndCopiesArmor() {
		final Creature template = new Creature();
		template.setArmor(75);

		final Creature copy = new Creature(template);

		assertEquals(75, template.getArmor());
		assertEquals(75, copy.getArmor());
	}

	@Test
	public void negativeArmorBecomesUnarmored() {
		final Creature creature = new Creature();
		creature.setArmor(-10);
		assertEquals(0, creature.getArmor());
	}
}
