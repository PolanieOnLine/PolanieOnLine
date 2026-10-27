/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.RPClass.ItemTestHelper;

public class LifestealRestoreMigrationTest {
	@BeforeClass public static void classes() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test public void guaranteedSlotIsCompensatedOnceWithoutRerollingOtherAffixes() throws Exception {
		for (final boolean persistent : new boolean[] {false, true}) {
			final Item old = definition("miecz chaosu");
			final double authored = old.getDouble("lifesteal");
			old.setID(new RPObject.ID(610, "lifesteal_test"));
			old.setRarity(ItemRarity.EPIC);
			old.setRarityModifier("lifesteal", 1.2);
			old.put("lifesteal", authored * 1.2);
			old.put(ItemAffixState.ATTRIBUTE, "lifesteal", old.get("lifesteal"));
			old.put("critical_chance", 4.0);
			old.put(ItemAffixState.ATTRIBUTE, "critical_chance", "4.0");
			ItemAffixState.setSeed(old, 1234L);
			old.put("improve", 1);
			old.put("bound", "owner");
			old.setPersistent(persistent);

			final Item first = restore(old);
			assertEquals(authored, first.getDouble("lifesteal"), 0.000001);
			assertNull(first.getRarityModifier("lifesteal"));
			assertFalse(ItemAffixState.has(first, "lifesteal"));
			assertEquals(2, ItemAffixState.getValues(first).size());
			assertEquals("4.0", ItemAffixState.getValues(first).get("critical_chance"));
			assertEquals(1, first.getUpgradeLevel());
			assertEquals("owner", first.getBoundTo());
			assertEquals(old.get("lifesteal"), first.getMap("lifesteal_legacy").get("lifesteal"));
			final Map<String, String> firstAffixes = ItemAffixState.getValues(first);
			final Item second = restore(first);
			assertEquals(firstAffixes, ItemAffixState.getValues(second));
			assertEquals(authored, second.getDouble("lifesteal"), 0.000001);
			assertEquals(first.getMap("lifesteal_legacy"), second.getMap("lifesteal_legacy"));
		}
	}

	@Test public void commonRecoversAuthoredLifeStealWithoutReceivingAffixes() throws Exception {
		final Item old = definition("miecz chaosu");
		final double authored = old.getDouble("lifesteal");
		old.setID(new RPObject.ID(611, "lifesteal_test"));
		old.setRarity(ItemRarity.COMMON);
		old.remove("lifesteal");
		final Item restored = restore(old);
		assertEquals(authored, restored.getDouble("lifesteal"), 0.000001);
		assertFalse(ItemAffixState.hasAny(restored));
	}

	@Test public void realRandomGrantRemainsAnAffixAndIsNeverCompensated() throws Exception {
		final Item old = definition("miecz zaczepny");
		old.setID(new RPObject.ID(612, "lifesteal_test"));
		old.setRarity(ItemRarity.RARE);
		old.put("lifesteal", 0.07);
		old.put(ItemAffixState.ATTRIBUTE, "lifesteal", "0.07");
		final Item restored = restore(restore(old));
		assertEquals(0.07, restored.getDouble("lifesteal"), 0.000001);
		assertEquals(ItemAffixState.getValues(old), ItemAffixState.getValues(restored));
	}

	@Test public void unrecordedIntrinsicValueDoesNotGrantFreeAffixes() throws Exception {
		final Item old = definition("miecz chaosu");
		old.setID(new RPObject.ID(613, "lifesteal_test"));
		old.setRarity(ItemRarity.RARE);
		old.setRarityModifier("lifesteal", 1.1);
		old.put("lifesteal", 0.33);
		final Item restored = restore(old);
		assertEquals(0.30, restored.getDouble("lifesteal"), 0.000001);
		assertFalse(ItemAffixState.hasAny(restored));
	}

	@Test public void nestedPersistentContainerMigratesItsWeaponOnlyOnce() throws Exception {
		final Item bag = definition("rzemyk");
		bag.setID(new RPObject.ID(614, "lifesteal_test"));
		bag.setPersistent(true);
		final Item weapon = definition("miecz chaosu");
		weapon.setID(new RPObject.ID(615, "lifesteal_test"));
		weapon.setRarity(ItemRarity.RARE);
		weapon.put("lifesteal", 0.33);
		weapon.setRarityModifier("lifesteal", 1.1);
		weapon.put(ItemAffixState.ATTRIBUTE, "lifesteal", "0.33");
		bag.getSlot("content").add(weapon);
		final Item first = restore(bag);
		final Item second = restore(first);
		assertEquals(1, second.getSlot("content").size());
		final Item firstWeapon = (Item) first.getSlot("content").getFirst();
		final Item secondWeapon = (Item) second.getSlot("content").getFirst();
		assertEquals(0.30, secondWeapon.getDouble("lifesteal"), 0.000001);
		assertEquals(1, ItemAffixState.getValues(secondWeapon).size());
		assertFalse(ItemAffixState.has(secondWeapon, "lifesteal"));
		assertEquals(ItemAffixState.getValues(firstWeapon), ItemAffixState.getValues(secondWeapon));
	}

	private static Item definition(final String name) {
		return SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
	}

	static Item restore(final Item source) throws Exception {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject saved = new RPObject();
		saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return new ItemTransformer().transform(saved);
	}
}
