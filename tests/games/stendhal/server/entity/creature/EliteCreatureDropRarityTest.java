/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.creature.EliteCreatureService;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.RPClass.CreatureTestHelper;

/** Integration coverage for the creature drop -> rarity context hook. */
public class EliteCreatureDropRarityTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void everyEliteNonStackableDropGetsItsOwnTwoRollContext() {
		final Creature creature = creatureWithThreeGuaranteedDrops();
		creature.put("title_type", EliteCreatureService.ELITE_TITLE_TYPE);
		final List<ItemCreationContext> contexts = new ArrayList<ItemCreationContext>();

		final List<Item> drops = creature.createDroppedItems(managerCapturing(contexts));

		assertEquals(3, drops.size());
		assertEquals(3, contexts.size());
		for (final ItemCreationContext context : contexts) {
			assertSame(ItemCreationContext.Source.DROP, context.getSource());
			assertEquals(EliteCreatureService.ELITE_RARITY_ROLLS,
					context.getRarityRolls());
		}
	}

	@Test
	public void normalCreatureDropKeepsOneRoll() {
		final Creature creature = creatureWithThreeGuaranteedDrops();
		final List<ItemCreationContext> contexts = new ArrayList<ItemCreationContext>();

		creature.createDroppedItems(managerCapturing(contexts));

		assertEquals(3, contexts.size());
		for (final ItemCreationContext context : contexts) {
			assertEquals(1, context.getRarityRolls());
		}
	}

	private Creature creatureWithThreeGuaranteedDrops() {
		final Creature creature = new Creature();
		creature.clearDropItemList();
		creature.addDropItem("test reward", 100.0, 3);
		return creature;
	}

	private EntityManager managerCapturing(final List<ItemCreationContext> contexts) {
		return (EntityManager) Proxy.newProxyInstance(
				EntityManager.class.getClassLoader(),
				new Class<?>[] {EntityManager.class}, (proxy, method, args) -> {
					if ("getItem".equals(method.getName()) && args.length == 2
							&& args[1] instanceof ItemCreationContext) {
						contexts.add((ItemCreationContext) args[1]);
						return new Item((String) args[0], "armor", "test",
								Collections.<String, String>emptyMap());
					}
					throw new UnsupportedOperationException(method.getName());
				});
	}
}
