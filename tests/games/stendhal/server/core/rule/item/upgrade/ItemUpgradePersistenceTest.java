/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.RPClass.ItemTestHelper;

public class ItemUpgradePersistenceTest {
	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void upgradedLegendaryRestoresTwiceWithoutReapplyingRarityOrUpgrade()
			throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem(
				"pogromca", ItemCreationContext
						.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.LEGENDARY)
						.withModifiers(ItemRarityModifiers.builder()
								.statMultiplier(1.25).valueMultiplier(2.0).build())
						.withAffixSeed(12345L).build());
		saved.setID(new ID(401, "item_upgrade_persistence"));
		saved.setUpgradeLevel(2);
		saved.setPersistent(true);
		final int rawAttack = saved.getInt("atk");
		final int finalAttack = saved.getAttack();
		final int damageMin = saved.getDamageMin();
		final int damageMax = saved.getDamageMax();
		final Map<String, String> affixes = ItemAffixState.getValues(saved);
		final Map<String, Double> modifiers = saved.getRarityModifiers();

		final Item first = new ItemTransformer().transform(serializedCopy(saved));
		final Item second = new ItemTransformer().transform(serializedCopy(first));

		assertSame(ItemRarity.LEGENDARY, second.getRarity());
		assertEquals(2, second.getUpgradeLevel());
		assertEquals(4, second.getMaxUpgradeLevel());
		assertEquals(rawAttack, second.getInt("atk"));
		assertEquals(finalAttack, second.getAttack());
		assertEquals(damageMin, second.getDamageMin());
		assertEquals(damageMax, second.getDamageMax());
		assertEquals(Long.valueOf(12345L), ItemAffixState.getSeed(second));
		assertEquals(affixes, ItemAffixState.getValues(second));
		assertEquals(modifiers, second.getRarityModifiers());
	}

	@Test
	public void legacyUpgradeKeysAndDamageRangeRemainCompatible()
			throws IOException {
		final Item legacy = SingletonRepository.getEntityManager().getItem(
				"pogromca");
		legacy.setID(new ID(402, "item_upgrade_persistence"));
		legacy.put("improve", 3);
		legacy.put("max_improves", 4);
		legacy.remove("damage_min");
		legacy.remove("damage_max");

		final Item restored = new ItemTransformer().transform(serializedCopy(legacy));

		assertEquals(3, restored.getUpgradeLevel());
		assertEquals(4, restored.getMaxUpgradeLevel());
		assertEquals(restored.getInt("atk") + 3, restored.getAttack());
		assertEquals(restored.getAttack(), restored.getAverageDamage(), 1.0);
	}

	private RPObject serializedCopy(final RPObject source) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(
				new ByteArrayInputStream(bytes.toByteArray())));
		return copy;
	}
}
