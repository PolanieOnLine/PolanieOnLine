/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemAffixDefinition;
import games.stendhal.server.core.rule.rarity.ItemAffixRerollService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Container;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

/** Keeps the item instance intact across real, serialized inventory map deltas. */
public class AffixRerollPerceptionTest {
	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test public void rerollOfferAcceptanceAndKeepPreserveInventoryIdentity() throws Exception {
		for (final String affix : new String[] {"accuracy_bonus", "legendary_deep_wounds"}) {
			for (final boolean accept : new boolean[] {false, true}) {
				for (final boolean nested : new boolean[] {false, true}) {
					verifyReroll(affix, accept, nested);
				}
			}
		}
	}

	private void verifyReroll(final String affix, final boolean accept, final boolean nested) throws Exception {
		final Player player = PlayerTestHelper.createPlayer("reroll_perception");
		player.setID(new RPObject.ID(1, "reroll_perception"));
		final Item item = new Item("test reroll sword", "sword", "test", Collections.singletonMap("atk", "12"));
		item.setRarity(ItemRarity.LEGENDARY);
		final ItemAffixDefinition definition = ItemAffixRerollService.definition(affix);
		assertTrue(definition.apply(item, new Random(2)));
		ItemAffixState.record(item, definition);
		final Item bag = new Container("test bag", "container", "bag", null);
		if (nested) {
			player.getSlot("bag").add(bag);
			bag.getSlot("content").add(item);
		} else {
			player.getSlot("bag").add(item);
		}
		final StackableItem stones = new StackableItem(ItemAffixRerollService.STONE, "resource", "transformation_stone", null);
		stones.setQuantity(3);
		player.getSlot("bag").add(stones);
		final RPObject clientOwner = clientCopy(player);
		// The owner view is irrelevant; the real dispatcher still walks its complete inventory tree.
		clientOwner.setRPClass(RPClass.getBaseRPObjectDefault());
		final Constructor<GameObjects> constructor = GameObjects.class.getDeclaredConstructor(StaticGameLayers.class);
		constructor.setAccessible(true);
		final GameObjects objects = constructor.newInstance((StaticGameLayers) null);
		final RPObjectChangeDispatcher dispatcher = new RPObjectChangeDispatcher(objects, new games.stendhal.client.entity.Item());
		dispatcher.dispatchAdded(clientOwner);
		final RPSlot clientSlot = nested
				? clientOwner.getSlot("bag").get(bag.getID()).getSlot("content") : clientOwner.getSlot("bag");
		final RPObject original = clientSlot.get(item.getID());
		final IEntity originalEntity = objects.get(original);
		assertNotNull(originalEntity);
		player.resetAddedAndDeleted();
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		assertEquals("OFFER", service.roll(player, item, affix, service.select(player, item, affix)));
		perceive(player, clientOwner, dispatcher);
		assertIdentity(clientSlot, item, original, objects, originalEntity);
		assertEquals(accept ? "ACCEPTED" : "KEPT", service.finish(player, item,
				ItemAffixRerollService.state(item).get("token"), accept));
		perceive(player, clientOwner, dispatcher);
		assertIdentity(clientSlot, item, original, objects, originalEntity);
		assertEquals(item.getMap("tooltip_stats"), original.getMap("tooltip_stats"));
		item.getContainerSlot().remove(item.getID());
		perceive(player, clientOwner, dispatcher);
		assertNull("An actual inventory removal must still remove the item", clientSlot.get(item.getID()));
		assertNull(objects.get(original));
	}

	private void assertIdentity(final RPSlot slot, final Item item, final RPObject original,
			final GameObjects objects, final IEntity originalEntity) {
		assertSame("A map delta must not remove and recreate the carried item", original, slot.get(item.getID()));
		assertSame(originalEntity, objects.get(original));
		assertEquals("test reroll sword", originalEntity.getName());
		assertEquals("sword", originalEntity.getEntityClass());
		assertEquals("test", originalEntity.getEntitySubclass());
		assertEquals("legendary", original.get("rarity_id"));
	}

	private void perceive(final Player player, final RPObject owner, final RPObjectChangeDispatcher dispatcher) throws Exception {
		final RPObject added = new RPObject();
		final RPObject removed = new RPObject();
		player.getDifferences(added, removed);
		dispatcher.dispatchModifyRemoved(owner, clientCopy(removed));
		dispatcher.dispatchModifyAdded(owner, clientCopy(added));
		player.resetAddedAndDeleted();
	}

	private RPObject clientCopy(final RPObject object) throws Exception {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		object.writeObject(new OutputSerializer(bytes), DetailLevel.PRIVATE);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return copy;
	}
}
