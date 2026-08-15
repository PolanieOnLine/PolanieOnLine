/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
	public void bastionStaysRegisteredForLegacyItemsButIsNotEligibleForFreshRolls() {
		assertNotNull(LegendaryItemAffixRegistry.getInstance().get(
				LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE));
		final Item armour = new Item("legacy bastion registration", "armor", "test",
				new HashMap<String, String>());
		for (final ItemAffixDefinition definition
				: LegendaryItemAffixRegistry.getInstance().getEligible(armour)) {
			assertFalse(LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE
					.equals(definition.getId()));
		}
	}
}
