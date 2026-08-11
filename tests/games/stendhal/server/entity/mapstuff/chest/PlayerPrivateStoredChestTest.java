/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.chest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.transformer.StoredChestTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.EntitySlot;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;
import utilities.RPClass.ChestTestHelper;

public class PlayerPrivateStoredChestTest {

	@BeforeClass
	public static void beforeClass() {
		ChestTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
		MockStendlRPWorld.get();
	}

	@Test
	public void onlyOwnerCanOpenPrivateChest() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateStoredChest chest = new PlayerPrivateStoredChest(owner);
		final StendhalRPZone zone = new StendhalRPZone("test_private_chest_open", 10, 10);
		chest.setPosition(5, 5);
		owner.setPosition(5, 6);
		stranger.setPosition(6, 5);
		zone.add(chest);

		assertTrue(chest.onUsed(owner));
		assertTrue(chest.isOpen());
		assertFalse(chest.onUsed(stranger));
		assertTrue(chest.isOpen());
	}

	@Test
	public void contentSlotRejectsNonOwnerEvenWhenChestIsOpen() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateStoredChest chest = new PlayerPrivateStoredChest(owner);
		final StendhalRPZone zone = new StendhalRPZone("test_private_chest_slot", 10, 10);
		chest.setPosition(5, 5);
		owner.setPosition(5, 6);
		stranger.setPosition(6, 5);
		zone.add(chest);
		chest.open();

		final EntitySlot slot = (EntitySlot) chest.getSlot("content");
		assertTrue(slot.isReachableForTakingThingsOutOfBy(owner));
		assertFalse(slot.isReachableForTakingThingsOutOfBy(stranger));
	}

	@Test
	public void coLocatedPrivateChestsHaveIndependentContents() {
		final Player alice = PlayerTestHelper.createPlayer("Alice");
		final Player bob = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateStoredChest aliceChest = new PlayerPrivateStoredChest(alice);
		final PlayerPrivateStoredChest bobChest = new PlayerPrivateStoredChest(bob);
		aliceChest.setPosition(4, 2);
		bobChest.setPosition(4, 2);

		aliceChest.add(new Item("testowy przedmiot", "misc", "test", null));

		assertEquals(1, aliceChest.size());
		assertEquals(0, bobChest.size());
	}

	@Test
	public void transformerRestoresPrivateChestAndOwner() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final PlayerPrivateStoredChest chest = new PlayerPrivateStoredChest(owner);
		final RPObject persisted = (RPObject) chest.clone();

		final RPObject restored = new StoredChestTransformer().transform(persisted);

		assertTrue(restored instanceof PlayerPrivateStoredChest);
		assertEquals("Alice", ((PlayerPrivateStoredChest) restored).getOwnerName());
	}
}
