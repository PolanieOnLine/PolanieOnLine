/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import utilities.RPClass.CreatureTestHelper;

/** Live event state, exact odds and compatibility with old creature XML. */
public class MineTownPumpkinDropTest {
	private static final String PROPERTY = "stendhal.minetown";
	private static final String PUMPKIN = "straszna dynia";
	private String previousProperty;
	private final List<String> created = new ArrayList<String>();

	@BeforeClass
	public static void initialize() throws Exception {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
		if (!RPClass.hasRPClass("corpse")) {
			Corpse.generateRPClass();
		}
	}

	@Before
	public void saveEventState() {
		previousProperty = System.getProperty(PROPERTY);
		System.clearProperty(PROPERTY);
	}

	@After
	public void restoreEventState() {
		if (previousProperty == null) {
			System.clearProperty(PROPERTY);
		} else {
			System.setProperty(PROPERTY, previousProperty);
		}
	}

	@Test
	public void inactiveEventDoesNotRollOrCreatePumpkins() {
		final ControlledCreature creature = new ControlledCreature();
		assertTrue(creature.createDroppedItems(manager()).isEmpty());
		assertEquals(0, creature.rolls);
		assertTrue(created.isEmpty());
	}

	@Test
	public void exactlyFiveOfOneHundredRollValuesDropOnePumpkin() {
		System.setProperty(PROPERTY, "true");
		final ControlledCreature creature = new ControlledCreature();
		int successes = 0;
		for (int roll = 0; roll < 100; roll++) {
			creature.roll = roll;
			final List<Item> drops = creature.createDroppedItems(manager());
			assertEquals(roll < 5 ? 1 : 0, drops.size());
			if (!drops.isEmpty()) {
				successes++;
				assertEquals(PUMPKIN, drops.get(0).getName());
				assertEquals(1, ((StackableItem) drops.get(0)).getQuantity());
			}
		}
		assertEquals(5, successes);
		assertEquals(100, creature.rolls);
		assertEquals(5, created.size());
	}

	@Test
	public void existingCreatureRespondsToEventStartAndStopWithoutChangingDropTable() {
		final ControlledCreature creature = new ControlledCreature();
		creature.addDropItem("test reward", 100.0, 1);
		assertEquals(1, creature.createDroppedItems(manager()).size());
		System.setProperty(PROPERTY, "true");
		assertEquals(2, creature.createDroppedItems(manager()).size());
		System.clearProperty(PROPERTY);
		assertEquals(1, creature.createDroppedItems(manager()).size());
		assertEquals(1, creature.dropsItems.size());
		assertEquals("test reward", creature.dropsItems.get(0).name);
		assertEquals(1, creature.rolls);
	}

	@Test
	public void legacyPumpkinEntriesAreDisabledOutsideEventAndNeverStackWithEventRoll() {
		final ControlledCreature creature = new ControlledCreature();
		creature.addDropItem(PUMPKIN, 100.0, 10);
		creature.addDropItem(PUMPKIN, 100.0, 10);
		creature.addDropItem("test reward", 100.0, 1);
		assertEquals(1, creature.createDroppedItems(manager()).size());
		System.setProperty(PROPERTY, "true");
		final List<Item> drops = creature.createDroppedItems(manager());
		assertEquals(2, drops.size());
		assertEquals("test reward", drops.get(0).getName());
		assertEquals(PUMPKIN, drops.get(1).getName());
		assertEquals(1, ((StackableItem) drops.get(1)).getQuantity());
		creature.roll = 5;
		assertEquals(1, creature.createDroppedItems(manager()).size());
		assertEquals(2, creature.rolls);
		assertEquals(3, creature.dropsItems.size());
	}

	@Test
	public void missingPumpkinDefinitionKeepsOrdinaryLoot() {
		System.setProperty(PROPERTY, "true");
		final ControlledCreature creature = new ControlledCreature() {
			@Override
			protected Item createDroppedItem(final EntityManager manager, final String name) {
				return PUMPKIN.equals(name) ? null : super.createDroppedItem(manager, name);
			}
		};
		creature.addDropItem("test reward", 100.0, 1);
		final List<Item> drops = creature.createDroppedItems(manager());
		assertEquals(1, drops.size());
		assertEquals("test reward", drops.get(0).getName());
	}

	private EntityManager manager() {
		return (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
				new Class<?>[] { EntityManager.class }, (proxy, method, args) -> {
					if ("getItem".equals(method.getName()) && args.length == 2
							&& args[1] instanceof ItemCreationContext) {
						final String name = (String) args[0];
						created.add(name);
						return PUMPKIN.equals(name)
								? new StackableItem(name, "food", "pumpkin_halloween",
										Collections.singletonMap("quantity", "7"))
								: new Item(name, "armor", "test", Collections.<String, String>emptyMap());
					}
					throw new UnsupportedOperationException(method.getName());
				});
	}

	@Test
	public void pumpkinFitsBesideFourNormalDropsWithoutReplacingThem() {
		System.setProperty(PROPERTY, "true");
		final ControlledCreature creature = new ControlledCreature() {
			@Override
			List<Item> createDroppedItems(final EntityManager ignored) {
				return super.createDroppedItems(manager());
			}
		};
		creature.addDropItem("test reward", 100.0, 5);
		final Corpse corpse = new Corpse("animal", 0, 0);
		creature.dropItemsOn(corpse);
		assertEquals(5, corpse.size());
		int pumpkins = 0;
		for (final RPObject object : corpse.getSlot("content")) {
			final Item item = (Item) object;
			assertTrue(item.isFromCorpse());
			if (PUMPKIN.equals(item.getName())) {
				pumpkins++;
			}
		}
		assertEquals(1, pumpkins);
	}

	private static class ControlledCreature extends Creature {
		private int roll;
		private int rolls;

		@Override
		protected int rollMineTownPumpkinChance() {
			rolls++;
			return roll;
		}
	}
}
