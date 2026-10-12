/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.item;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemRarityProfile;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemValueServiceTest {
	@BeforeClass public static void initialize() { ItemTestHelper.generateRPClasses(); }

	@Test public void bothBuiltInProfilesUseTheRequestedMultipliers() {
		final double[] expected = {1.0, 1.25, 1.5, 2.0};
		for (final ItemRarityProfile profile : new ItemRarityProfile[] {
				ItemRarityProfile.defaultProfile(), ItemRarityProfile.bossProfile()}) {
			for (final ItemRarity rarity : ItemRarity.values()) {
				assertEquals(expected[rarity.ordinal()], profile.getTier(rarity).getValueMultiplier(), 0);
			}
		}
	}

	@Test public void oldRarePricesAreMigratedOnceWithoutChangingItemProgress() {
		for (final String profile : new String[] {"default", "boss"}) {
			final Item item = item(ItemRarity.RARE);
			item.put(Item.RARITY_PROFILE, profile);
			item.setRarityModifier("value", 1.20);
			item.setRarityModifier("atk", 1.08);
			item.put("max_improves", 4);
			item.put("improve", 3);
			item.put("lifesteal", 0.30);
			item.put(ItemAffixState.ATTRIBUTE, "lifesteal", "0.30");
			ItemAffixState.setSeed(item, 12345L);
			item.setBoundTo("seller");
			item.setDescription("Pamiątka.");
			final Map<String, String> affixes = ItemAffixState.getValues(item);
			for (int repeat = 0; repeat < 3; repeat++) {
				ItemValueService.refresh(item);
				assertEquals(1250, item.getValue());
				assertEquals(Double.valueOf(1.25), item.getRarityModifier("value"));
				assertEquals(Double.valueOf(1.08), item.getRarityModifier("atk"));
				assertEquals(20, item.getInt("atk"));
				assertEquals(3, item.getUpgradeLevel());
				assertEquals(23, item.getAttack());
				assertEquals(affixes, ItemAffixState.getValues(item));
				assertEquals(Long.valueOf(12345L), ItemAffixState.getSeed(item));
				assertEquals("seller", item.getBoundTo());
				assertEquals("Pamiątka.", item.getDescription());
			}
		}
	}

	@Test public void missingOrInvalidPriceMetadataUsesCurrentTier() {
		final double[] expected = {1.0, 1.25, 1.5, 2.0};
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Item item = item(rarity);
			ItemValueService.refresh(item);
			assertEquals((int) (1000 * expected[rarity.ordinal()]), item.getValue());
			item.put(Item.RARITY_MODIFIERS, "value", "NaN");
			ItemValueService.refresh(item);
			assertEquals(Double.valueOf(expected[rarity.ordinal()]), item.getRarityModifier("value"));
		}
	}

	@Test public void otherExplicitAndCustomProfileMultipliersRemainUntouched() {
		final Item explicit = item(ItemRarity.RARE);
		explicit.setRarityModifier("value", 1.8);
		ItemValueService.refresh(explicit);
		assertEquals(1800, explicit.getValue());
		final Item custom = item(ItemRarity.RARE);
		custom.put(Item.RARITY_PROFILE, "custom_sale_profile");
		custom.setRarityModifier("value", 1.2);
		ItemValueService.refresh(custom);
		assertEquals(1200, custom.getValue());
		assertEquals(Double.valueOf(1.2), custom.getRarityModifier("value"));
	}

	@Test public void epicAndLegendaryPricesDoNotChangeAndLargeValuesDoNotOverflow() {
		final Item epic = item(ItemRarity.EPIC);
		epic.setRarityModifier("value", 1.5);
		ItemValueService.refresh(epic);
		assertEquals(1500, epic.getValue());
		final Item legendary = item(ItemRarity.LEGENDARY);
		legendary.setRarityModifier("value", 2.0);
		ItemValueService.refresh(legendary);
		assertEquals(2000, legendary.getValue());
		legendary.setRarityModifier("value", 1.0e308);
		ItemValueService.refresh(legendary);
		assertEquals(Integer.MAX_VALUE, legendary.getValue());
	}

	@Test public void itemsWithoutRarityKeepTheOrdinaryPrice() {
		final Item item = item(null);
		ItemValueService.refresh(item);
		assertEquals(1000, item.getValue());
		assertNull(item.getRarityModifier("value"));
		ItemValueService.refresh(null);
	}

	private Item item(final ItemRarity rarity) {
		final Item item = new Item("test sword", "sword", "test", Collections.singletonMap("atk", "20"));
		item.configureRarity(Boolean.TRUE, "default", 1000);
		if (rarity != null) item.setRarity(rarity);
		return item;
	}
}
