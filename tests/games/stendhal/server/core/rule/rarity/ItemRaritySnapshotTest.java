/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.entity.item.Item;

public class ItemRaritySnapshotTest {
	@BeforeClass
	public static void createRPClasses() {
		new RPClassGenerator().createRPClassesWithoutBaking();
	}

	@Test
	public void preservesLifeStealAffixSeedAndMigrationMetadata() {
		final java.util.Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("lifesteal", "0.075");
		final Item source = new Item("snapshot life steal", "sword", "test", attributes);
		source.setRarity(ItemRarity.EPIC);
		source.put("lifesteal_effectiveness", 20);
		source.put("lifesteal_version", 1);
		source.put("lifesteal_legacy", "lifesteal", "0.09");
		source.put(ItemAffixState.ATTRIBUTE, "lifesteal_effectiveness", "20");
		ItemAffixState.setSeed(source, 123456L);
		final String encoded = ItemRaritySnapshot.encode(source);
		final Item restored = new Item("snapshot life steal", "sword", "test", attributes);

		ItemRaritySnapshot.restore(restored, encoded);

		assertEquals(ItemAffixState.getValues(source), ItemAffixState.getValues(restored));
		assertEquals(ItemAffixState.getSeed(source), ItemAffixState.getSeed(restored));
		assertEquals(source.getMap("lifesteal_legacy"), restored.getMap("lifesteal_legacy"));
		assertEquals(1, restored.getInt("lifesteal_version"));
		assertEquals(0.09, ItemLifestealService.getEffectiveLifesteal(restored), 0.000001);
		assertEquals(encoded, ItemRaritySnapshot.encode(restored));
	}

	@Test
	public void readsVersionTwoLifeStealWithoutReapplyingItsOldMultiplier() throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream output = new DataOutputStream(bytes);
		output.writeByte(2);
		output.writeUTF("epic");
		output.writeUTF("default");
		output.writeInt(1000);
		output.writeInt(1);
		output.writeUTF("lifesteal");
		output.writeDouble(1.2);
		output.writeInt(1);
		output.writeUTF("lifesteal");
		output.writeDouble(0.36);
		output.close();
		final Item item = new Item("old snapshot", "sword", "test",
				java.util.Collections.singletonMap("lifesteal", "0.30"));

		ItemRaritySnapshot.restore(item, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes.toByteArray()));

		assertEquals(0.30, item.getDouble("lifesteal"), 0.000001);
		assertEquals(null, item.getRarityModifier("lifesteal"));
		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void restoresFinalValuesWithoutApplyingModifiersAgain() {
		final Item source = eligibleItem();
		final ItemRarityModifiers fixed = ItemRarityModifiers.builder()
				.attackMultiplier(1.30)
				.defenseMultiplier(1.15)
				.valueMultiplier(2.0)
				.build();
		new ItemRarityService(new Random(1L)).initialize(source,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.LEGENDARY)
						.withModifiers(fixed).randomizeModifiers(false).build());
		// A supported instance stat may exist without originating in the rarity
		// modifier map (for example after an older gameplay upgrade).
		source.put("skill_atk", 7);
		final String snapshot = ItemRaritySnapshot.encode(source);

		final Item restored = eligibleItem();
		// Simulate a later XML definition which removed def and added range.
		restored.remove("def");
		restored.put("range", 99);
		ItemRaritySnapshot.restore(restored, snapshot);

		assertEquals(ItemRarity.LEGENDARY, restored.getRarity());
		assertEquals(source.getInt("atk"), restored.getInt("atk"));
		assertEquals(source.getInt("def"), restored.getInt("def"));
		assertEquals(7, restored.getInt("skill_atk"));
		assertFalse(restored.has("range"));
		assertEquals(source.getValue(), restored.getValue());
		assertEquals(source.getRarityModifiers(), restored.getRarityModifiers());
		assertFalse(restored.isPersistent());
		assertEquals(snapshot, ItemRaritySnapshot.encode(restored));
	}

	@Test
	public void blankSnapshotLeavesLegacyItemUntouched() {
		final Item item = eligibleItem();
		ItemRaritySnapshot.restore(item, "");
		assertFalse(item.has(Item.RARITY_ID));
		assertEquals(100, item.getInt("atk"));
		assertEquals(50, item.getInt("def"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void malformedSnapshotIsRejected() {
		ItemRaritySnapshot.restore(eligibleItem(), "not-a-snapshot");
	}

	@Test
	public void restoresLegacyVersionOneSnapshot() throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final DataOutputStream output = new DataOutputStream(bytes);
		output.writeByte(1);
		output.writeUTF("rare");
		output.writeUTF("default");
		output.writeInt(1200);
		output.writeInt(2);
		output.writeUTF("atk");
		output.writeDouble(1.2);
		output.writeBoolean(true);
		output.writeInt(120);
		output.writeUTF("value");
		output.writeDouble(1.2);
		output.writeBoolean(false);
		output.close();

		final Item item = eligibleItem();
		item.remove("atk");
		ItemRaritySnapshot.restore(item, Base64.getUrlEncoder().withoutPadding()
				.encodeToString(bytes.toByteArray()));

		assertEquals(ItemRarity.RARE, item.getRarity());
		assertEquals(120, item.getInt("atk"));
		assertEquals(1200, item.getValue());
		assertEquals(Double.valueOf(1.2), item.getRarityModifier("atk"));
	}

	private Item eligibleItem() {
		final Item item = new Item("guardian sword", "sword", "guardian",
				new HashMap<String, String>());
		item.setEquipableSlots(Arrays.asList("rhand"));
		item.put("atk", 100);
		item.put("def", 50);
		item.configureRarity(Boolean.TRUE, "default", 500);
		return item;
	}
}
