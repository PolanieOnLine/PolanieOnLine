package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.server.entity.item.BreakableWeapon;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class EquipmentUtilityAffixDefinitionTest {
	@BeforeClass public static void setup() { ItemTestHelper.generateRPClasses(); }
	private Item item(final String type) { return new Item("test", type, "test", null); }
	private ItemAffixDefinition definition(final String id) {
		final ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get(id);
		assertNotNull("Missing agreed affix: " + id, definition);
		return definition;
	}
	@Test public void prudenceFitsEveryNonWeaponEquipmentSlot() {
		final ItemAffixDefinition affix = definition("potion_conservation");
		for (final String type : new String[] {"armor","shield","helmet","cloak","boots","legs","glove","belt","ring","necklace"})
			assertTrue(type, affix.isEligible(item(type)));
		assertFalse(affix.isEligible(item("sword")));
		assertFalse(affix.isEligible(item("food")));
	}
	@Test public void advantageAndOtherAffixesKeepTheirAgreedEquipmentRoles() {
		final ItemAffixDefinition advantage = definition("single_combat_bonus");
		for (final String type : new String[] {"armor","shield","helmet","legs","boots"}) assertTrue(type, advantage.isEligible(item(type)));
		assertFalse(advantage.isEligible(item("sword")));
		assertTrue(definition("kill_recovery").isEligible(item("glove")));
		assertTrue(definition("kill_recovery").isEligible(item("belt")));
		assertFalse(definition("kill_recovery").isEligible(item("ring")));
		assertTrue(definition("coin_bonus").isEligible(item("necklace")));
		assertTrue(definition("coin_bonus").isEligible(item("cloak")));
		assertFalse(definition("coin_bonus").isEligible(item("axe")));
	}
	@Test public void careNeedsAnActualPositiveWeaponDurabilityAndDoesNotWasteEquipmentSlots() {
		final ItemAffixDefinition care = definition("durability_conservation");
		final Map<String,String> attributes = new HashMap<>(); attributes.put("durability","1000");
		final Item weapon = new BreakableWeapon("test", "sword", "test", attributes);
		assertTrue(care.isEligible(weapon));
		assertTrue("Przemir preserves durable weapon data on its calculation copy", care.isEligible(new Item(weapon)));
		assertFalse(care.isEligible(item("sword")));
		assertFalse(care.isEligible(item("glove")));
		weapon.put("durability",0); assertFalse(care.isEligible(weapon));
	}
	@Test public void rollsUseTheAgreedRangesWithoutChangingBaseStatsOrDuplicatingSlots() {
		final String[] ids = {"kill_recovery","potion_conservation","single_combat_bonus","coin_bonus","durability_conservation"};
		final String[] types = {"belt","ring","armor","cloak","sword"};
		final double[] min = {50,.05,.03,.03,.10}, max = {100,.08,.05,.06,.20};
		for (int i=0;i<ids.length;i++) for (int seed=0;seed<100;seed++) {
			final Item item = item(types[i]); if(i==4) item.put("durability",1000);
			item.put("def",20); item.put("atk",15);
			final ItemAffixDefinition affix = definition(ids[i]);
			assertFalse(affix.hasMaterializedAttribute());
			assertTrue(affix.apply(item,new Random(seed)));
			final double value = Double.parseDouble(ItemAffixState.getValues(item).get(ids[i]));
			assertTrue(value>=min[i] && value<=max[i]);
			assertFalse(affix.isEligible(item));
			assertEquals(20,item.getInt("def")); assertEquals(15,item.getInt("atk"));
		}
	}
}
