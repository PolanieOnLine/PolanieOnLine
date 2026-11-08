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
			String itemClass = definition == CombatAffixDefinition.QUICK_RECOVERY ? "helmet"
					: definition == CombatAffixDefinition.HARDENING ? "armor" : "sword";
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

	@Test
	public void lifeTideAndQuickRecoveryOnlyRollOnTheirApprovedEquipment() {
		final ItemAffixDefinition lifeTide = LegendaryItemAffixRegistry.getInstance()
				.get("legendary_life_tide");
		final ItemAffixDefinition quickRecovery = CombatAffixDefinition.QUICK_RECOVERY;
		for (final String itemClass : new String[] {"armor", "helmet", "belt", "belts", "cloak",
				"shield", "legs", "boots", "glove", "gloves", "ring", "necklace", "sword"}) {
			final Item item = new Item("affix pool gear", itemClass, "test", null);
			final boolean lifeTideAllowed = "armor".equals(itemClass) || "helmet".equals(itemClass)
					|| "belt".equals(itemClass) || "belts".equals(itemClass);
			final boolean quickRecoveryAllowed = "helmet".equals(itemClass) || "cloak".equals(itemClass);
			assertEquals("Life Tide on " + itemClass, lifeTideAllowed, lifeTide.isEligible(item));
			assertEquals("Quick Recovery on " + itemClass, quickRecoveryAllowed, quickRecovery.isEligible(item));
			assertEquals(lifeTideAllowed, LegendaryItemAffixRegistry.getInstance().getEligible(item).contains(lifeTide));
			assertEquals(quickRecoveryAllowed, LegendaryItemAffixRegistry.getInstance().getEligible(item).contains(quickRecovery));
		}
	}

	@Test
	public void olderSignaturesRemainRestorableOutsideTheirNewRollPools() {
		for (final String id : new String[] {"legendary_life_tide", "legendary_quick_recovery"}) {
			final ItemAffixDefinition definition = LegendaryItemAffixRegistry.getInstance().get(id);
			final Item saved = new Item("older signature gear", "boots", "test", null);
			saved.put(id, 1.0);
			ItemAffixState.record(saved, definition);
			final Item restored = new Item("older signature gear", "boots", "test", null);
			ItemAffixState.restore(restored, saved);
			assertEquals(1.0, restored.getDouble(id), 0.0);
			assertEquals(ItemAffixState.getValues(saved), ItemAffixState.getValues(restored));
			assertFalse(definition.isEligible(restored));
		}
	}
}
