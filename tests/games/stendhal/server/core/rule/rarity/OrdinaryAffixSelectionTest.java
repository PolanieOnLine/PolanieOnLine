package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class OrdinaryAffixSelectionTest {

	@BeforeClass
	public static void setup() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void resistancesShareOneQuarterOfThePoolRatherThanOneChanceEach() {
		int resistances = 0;
		for (int seed = 0; seed < 4000; seed++) {
			final Item item = new Item("test", "boots", "test", null);
			item.setRarity(ItemRarity.RARE);
			final List<String> rolls = new ItemAffixGenerator(new Random(1)).generate(item,
					ItemCreationContext.builder(ItemCreationContext.Source.ADMIN).generateAffixes(true)
							.withAffixSeed(new Random(seed).nextLong()).build());
			assertEquals(1, rolls.size());
			if (rolls.get(0).startsWith("resist_")) resistances++;
		}
		assertTrue("Resistance frequency: " + resistances, resistances > 850 && resistances < 1150);
	}

	@Test
	public void rerollsUseTheSameResistanceFamilyWeight() {
		int resistances = 0;
		for (int seed = 0; seed < 4000; seed++) {
			final Item item = new Item("test", "boots", "test", null);
			item.put(ItemAffixState.ATTRIBUTE, "potion_conservation", "0.05");
			final Item replacement = new ItemAffixRerollService(new Random(new Random(seed).nextLong()))
					.replacement(item, "potion_conservation");
			assertNotNull(replacement);
			assertEquals(1, ItemAffixState.getValues(replacement).size());
			if (ItemAffixState.getValues(replacement).keySet().iterator().next().startsWith("resist_")) resistances++;
		}
		assertTrue("Reroll resistance frequency: " + resistances, resistances > 850 && resistances < 1200);
	}

	@Test
	public void selectionIsSeededUniqueAndKeepsTheWholeEligiblePool() {
		final Item item = new Item("test", "boots", "test", null);
		final List<ItemAffixDefinition> pool = ItemAffixRegistry.getInstance().getEligible(item);
		final List<ItemAffixDefinition> first = OrdinaryAffixSelection.order(pool, new Random(123));
		assertEquals(first, OrdinaryAffixSelection.order(pool, new Random(123)));
		assertEquals(new HashSet<>(pool), new HashSet<>(first));
		assertEquals(pool.size(), first.size());
	}

	@Test
	public void resistanceOnlyPoolDoesNotDropEligibleOptions() {
		final Item item = new Item("test", "boots", "test", null);
		final List<ItemAffixDefinition> pool = ItemAffixRegistry.getInstance().getEligible(item);
		pool.removeIf(definition -> !definition.getId().startsWith("resist_"));
		assertFalse(pool.isEmpty());
		assertEquals(new HashSet<>(pool), new HashSet<>(OrdinaryAffixSelection.order(pool, new Random(3))));
	}
}
