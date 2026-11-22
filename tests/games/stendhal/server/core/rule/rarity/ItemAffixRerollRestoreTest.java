/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixRerollRestoreTest {

	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test public void paidEquipmentOffersSurviveRepeatedSerializedLogins() throws Exception {
		for (final String name : new String[] {"pas skórzany", "płaszcz chaosu", "buty z mithrilu",
				"ciupaga", "złota ciupaga z trzema wąsami"}) {
			for (final boolean persistent : new boolean[] {false, true}) {
				assertOfferSurvivesRestore(name, persistent, false);
			}
		}
	}

	@Test public void paidEquipmentOffersWithoutMigrationMarkerStillSurviveLogin() throws Exception {
		assertOfferSurvivesRestore("pas skórzany", false, true);
	}

	private void assertOfferSurvivesRestore(final String name, final boolean persistent,
			final boolean withoutVersion) throws Exception {
		final Player player = PlayerTestHelper.createPlayer("restore_reroller");
		final ItemCreationContext context = ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
				.withRarity(ItemRarity.LEGENDARY).randomizeModifiers(false)
				.generateAffixes(true).withAffixSeed(42L).build();
		Item item = SingletonRepository.getEntityManager().getItem(name, context);
		assertNotNull(item);
		item.setPersistent(persistent);
		if (withoutVersion) item.remove(ItemLifestealService.VERSION_ATTRIBUTE);
		assertTrue(player.equipToInventoryOnly(item));
		final StackableItem stones = (StackableItem) SingletonRepository.getEntityManager()
				.getItem(ItemAffixRerollService.STONE);
		stones.setQuantity(3);
		assertTrue(player.equipToInventoryOnly(stones));
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		final String id = service.affixes(item).get(0);
		final Map<String, String> original = ItemAffixState.getValues(item);
		assertEquals("OFFER", service.roll(player, item, id, service.select(player, item, id)));
		final Map<String, String> offer = ItemAffixRerollService.state(item);
		final int remaining = service.stones(player);

		for (int login = 0; login < 3; login++) {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			item.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
			final RPObject saved = new RPObject();
			saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
			final Item restored = new ItemTransformer().transform(saved);
			item.getContainerSlot().remove(item.getID());
			assertTrue(player.equipToInventoryOnly(restored));
			item = restored;
			assertEquals(original, ItemAffixState.getValues(item));
			assertEquals(offer, ItemAffixRerollService.state(item));
			assertTrue(name + " persistent=" + persistent + " login=" + login,
					new ItemAffixRerollService(new Random(1)).validPending(player, item));
		}
		final ItemAffixRerollService restarted = new ItemAffixRerollService(new Random(1));
		assertEquals("ACCEPTED", restarted.finish(player, item, offer.get("token"), true));
		assertEquals(offer.get("new_value"), ItemAffixState.getValues(item).get(offer.get("new_id")));
		assertEquals(remaining, restarted.stones(player));
		assertFalse(restarted.hasPending(item));
	}
}
