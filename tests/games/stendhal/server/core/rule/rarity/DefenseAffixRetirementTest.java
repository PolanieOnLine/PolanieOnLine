/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.WallOfGordService;
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
	public void bastionRemainsRegisteredButFreshPoolUsesWallOfGord() {
		final LegendaryItemAffixRegistry registry =
				LegendaryItemAffixRegistry.getInstance();
		assertNotNull(registry.get(
				LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE));
		assertNotNull(registry.get(WallOfGordService.ATTRIBUTE));

		final Item armour = new Item("wall of the gord availability", "armor", "test",
				new HashMap<String, String>());
		assertFalse(registry.get(
				LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE)
				.isEligible(armour));
		assertTrue(registry.get(WallOfGordService.ATTRIBUTE).isEligible(armour));
	}
}
