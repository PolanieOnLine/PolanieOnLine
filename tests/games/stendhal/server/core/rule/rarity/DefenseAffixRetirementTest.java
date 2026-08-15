/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class DefenseAffixRetirementTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void freshRandomPoolNoLongerContainsFlatDefense() {
		assertNull(ItemAffixRegistry.getInstance().get(
				EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE));
	}

	@Test
	public void bastionRemainsAvailableUntilWallOfTheGordMechanicsAreDefined() {
		assertNotNull(LegendaryItemAffixRegistry.getInstance().get(
				LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE));
		final Item armour = new Item("temporary bastion availability", "armor", "test",
				new HashMap<String, String>());
		boolean found = false;
		for (final ItemAffixDefinition definition
				: LegendaryItemAffixRegistry.getInstance().getEligible(armour)) {
			if (LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE
					.equals(definition.getId())) {
				found = true;
				break;
			}
		}
		assertTrue(found);
	}
}
