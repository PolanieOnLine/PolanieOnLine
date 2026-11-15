/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.RingOfTeleportation;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Banks;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ReturnRingDestinationMigrationTest {
	private static int nextItemId = 1000;
	private static final String[] BLOCKED_ZONES = {
		"int_zakopane_mill_0", "int_zakopane_mill_1",
		"6_zakopane_clouds", "alt_6_zakopane_clouds"
	};

	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void restoresRingWithoutForbiddenTargetOrLossOfBindingAndCooldown() throws IOException {
		for (final String zone : BLOCKED_ZONES) {
			for (final boolean active : new boolean[] { false, true }) {
				final RingOfTeleportation saved = savedRing(zone, active);
				final Item restored = new ItemTransformer().transform(persisted(saved));
				assertCleared(restored);
				assertEquals("ring-migration-test", restored.getBoundTo());
				assertEquals(123456789, restored.getInt("frequency"));
				assertEquals(98765, restored.getInt("logid"));
				assertCleared(new ItemTransformer().transform(persisted(restored)));
			}
		}
	}

	@Test
	public void leavesOtherSavedDestinationsIntact() throws IOException {
		for (final String zone : new String[] { "0_semos_plains_n", "7_kikareukin_clouds", "0_zakopane_se" }) {
			final RingOfTeleportation restored = (RingOfTeleportation) new ItemTransformer()
					.transform(persisted(savedRing(zone, true)));
			assertEquals(zone + " 10 20", restored.getItemData());
			assertEquals(1, restored.getInt("amount"));
			assertEquals(1, restored.getInt("state"));
			assertEquals(123456789, restored.getInt("frequency"));
		}
	}

	@Test
	public void loginCleansBagEquippedBankAndNestedRings() throws IOException {
		final Player player = PlayerTestHelper.createPlayer("ring-migration-test");
		player.getSlot("bag").add(persisted(savedRing(BLOCKED_ZONES[0], true)));
		player.getSlot("finger").add(persisted(savedRing(BLOCKED_ZONES[1], true)));
		for (final Banks bank : Banks.values()) {
			final String name = bank.getSlotName();
			if (!player.hasSlot(name)) {
				player.addSlot(new RPSlot(name));
			}
			player.getSlot(name).add(persisted(savedRing(BLOCKED_ZONES[2], true)));
		}
		final Item container = SingletonRepository.getEntityManager()
				.getItem("skórzana zbroja", ItemCreationContext.restore());
		container.addSlot(new RPSlot("content"));
		container.getSlot("content").add(savedRing(BLOCKED_ZONES[3], true));
		player.getSlot("bag").add(persisted(container));

		new PlayerTransformer().loadItemsIntoSlots(player);

		assertEquals(2, player.getSlot("bag").size());
		assertCleared((Item) player.getSlot("bag").getFirst());
		assertCleared((Item) player.getSlot("finger").getFirst());
		for (final Banks bank : Banks.values()) {
			assertEquals(1, player.getSlot(bank.getSlotName()).size());
			assertCleared((Item) player.getSlot(bank.getSlotName()).getFirst());
		}
		boolean checkedNestedRing = false;
		for (final RPObject object : player.getSlot("bag")) {
			if (object.hasSlot("content")) {
				assertEquals(1, object.getSlot("content").size());
				assertCleared((Item) object.getSlot("content").getFirst());
				checkedNestedRing = true;
			}
		}
		assertTrue("The nested ring must remain in its container", checkedNestedRing);
	}

	private static RingOfTeleportation savedRing(final String zone, final boolean active) {
		final RingOfTeleportation ring = (RingOfTeleportation) SingletonRepository.getEntityManager()
				.getItem("pierścień powrotu", ItemCreationContext.restore());
		if (active) {
			ring.activeRing();
		}
		ring.setItemData(zone + " 10 20");
		ring.setBoundTo("ring-migration-test");
		ring.put("frequency", 123456789);
		ring.put("logid", 98765);
		return ring;
	}

	private static void assertCleared(final Item item) {
		assertNotNull(item);
		assertTrue(item instanceof RingOfTeleportation);
		final RingOfTeleportation ring = (RingOfTeleportation) item;
		assertNull(ring.getItemData());
		assertEquals(0, ring.getInt("amount"));
		assertEquals(0, ring.getInt("state"));
		assertEquals("", ring.getSavedPositionForTooltip());
	}

	private static RPObject persisted(final Item item) throws IOException {
		if (!item.has("id")) {
			item.setID(new RPObject.ID(nextItemId++, "ring_migration_test"));
		}
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		item.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject result = new RPObject();
		result.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
		return result;
	}
}
