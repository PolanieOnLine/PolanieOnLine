package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ExpandedCombatAffixTest {
	@BeforeClass
	public static void setupClasses() {
		ItemTestHelper.generateRPClasses();
	}
	@Test
	public void newBonusesBelongToTheirApprovedRarityPools() {
		for (String id : new String[] {"armor_crushing", "critical_hardening"}) {
			assertNotNull(id, ItemAffixRegistry.getInstance().get(id));
		}
		for (String id : new String[] {"legendary_cleave", "legendary_tenacity",
				"legendary_breach", "legendary_quick_recovery"}) {
			assertNotNull(id, LegendaryItemAffixRegistry.getInstance().get(id));
		}
	}

	@Test
	public void eligibleAffixesKeepTheirExactRollAfterRestoreAndDoNotRollOnJewellery() {
		for (CombatAffixDefinition definition : CombatAffixDefinition.values()) {
			String itemClass = definition == CombatAffixDefinition.HARDENING
					|| definition == CombatAffixDefinition.QUICK_RECOVERY ? "armor" : "sword";
			Item saved = new Item("expanded affix", itemClass, "test", null);
			assertTrue(definition.getId(), definition.apply(saved, new Random(7)));
			assertFalse("one affix cannot be rolled twice", definition.apply(saved, new Random(8)));
			ItemAffixState.record(saved, definition);
			Item restored = new Item("expanded affix", itemClass, "test", null);
			ItemAffixState.restore(restored, saved);
			assertEquals(saved.get(definition.getAttribute()), restored.get(definition.getAttribute()));
			assertEquals(ItemAffixState.getValues(saved), ItemAffixState.getValues(restored));
			assertFalse(definition.getId(), definition.isEligible(new Item("ring", "ring", "test", null)));
		}
	}
}
