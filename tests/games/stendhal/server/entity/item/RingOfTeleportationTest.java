package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RingOfTeleportationTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void blocksOzoLabyrinth() {
		assertTrue(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("7_labirynt")));
	}

	@Test
	public void blocksHaizenMazeByReadableName() {
		final StendhalRPZone zone = new StendhalRPZone("instance_daily_player_12345");
		zone.getAttributes().put("readable_name", "Labirynt Haizena");

		assertTrue(RingOfTeleportation.isForbiddenReturnZone(zone));
	}

	@Test
	public void blocksLegacyMazeNames() {
		assertTrue(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("player_daily_maze")));
	}

	@Test
	public void leavesOrdinaryZonesAvailable() {
		assertFalse(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("0_semos_plains_n")));
	}

	@Test
	public void completedReturnClearsSavedPositionAndDimsRing() {
		final RingOfTeleportation ring = new RingOfTeleportation();
		final Player player = PlayerTestHelper.createPlayer("return-ring-test");
		ring.activeRing();
		ring.setItemData("0_semos_plains_n 10 20");

		assertFalse(ring.isUsed());
		assertEquals(1, ring.getInt("state"));

		ring.completeSuccessfulReturn(player);

		assertTrue(ring.isUsed());
		assertEquals(0, ring.getInt("state"));
		assertNull(ring.getItemData());
		assertTrue(ring.has("frequency"));
	}

	@Test
	public void returnToAnotherMapPublishesDimmedRingAfterTransfer() {
		assertReturnPublishesDimmedRing(false, "bag");
	}

	@Test
	public void returnWithinSameMapPublishesDimmedEquippedRing() {
		assertReturnPublishesDimmedRing(true, "finger");
	}

	private void assertReturnPublishesDimmedRing(final boolean sameZone, final String slot) {
		final StendhalRPWorld world = MockStendlRPWorld.get();
		final StendhalRPZone source = new StendhalRPZone("return_ring_source_" + slot, 30, 30);
		final StendhalRPZone destination = sameZone ? source
				: new StendhalRPZone("return_ring_destination_" + slot, 30, 30);
		world.addRPZone(source);
		if (!sameZone) {
			world.addRPZone(destination);
		}
		final Player player = PlayerTestHelper.createPlayer("return-ring-perception-test");
		final RingOfTeleportation ring = new RingOfTeleportation();
		final TurnNotifier notifier = TurnNotifier.get();
		final int transferTurn = notifier.getCurrentTurnForDebugging() + 1;
		TurnListener completion = null;
		try {
			player.setPosition(2, 3);
			if ("bag".equals(slot)) {
				player.getSlot(slot).add(new Item("before ring", "ring", "gold-ring", null));
			}
			player.getSlot(slot).add(ring);
			if ("bag".equals(slot)) {
				player.getSlot(slot).add(new Item("after ring", "ring", "silver-ring", null));
			}
			source.add(player);
			player.setKeyedSlot("!visited", destination.getName(), "visited");
			ring.activeRing();
			ring.setItemData(destination.getName() + " 10 20");
			source.nextTurn();
			if (!sameZone) {
				destination.nextTurn();
			}

			assertTrue(ring.onUsed(player));
			assertEquals(destination, player.getZone());
			assertEquals(10, player.getX());
			assertFalse("Repeated use must not start another teleport", ring.onUsed(player));
			for (final Map.Entry<Integer, Set<TurnListener>> entry
					: notifier.getEventListForDebugging().entrySet()) {
				for (final TurnListener listener : entry.getValue()) {
					if (listener.getClass().getEnclosingClass() == RingOfTeleportation.class) {
						completion = listener;
						assertEquals("Complete after the teleport perception, not in its turn",
								transferTurn + 1, entry.getKey().intValue());
					}
				}
			}
			assertNotNull("Successful return must schedule the state update", completion);

			// Actions run before endTurn. A zero-turn callback would run here,
			// before the transfer perception has been sent and its deltas reset.
			assertFalse("Do not mutate the ring in the teleport perception", ring.isUsed());
			destination.getPerception(player, Perception.SYNC);
			final RPObject clientPlayer = new RPObject(player);
			final RPObject.ID ringId = ring.getID();
			final List<Integer> originalOrder = itemOrder(clientPlayer, slot);
			destination.nextTurn();

			// The immediately following perception must contain the dimmed state,
			// without requiring any further movement, use action or map change.
			// Invoke only this return's callback, leaving other tests' timers alone.
			completion.onTurnReached(transferTurn + 1);
			final Perception delta = destination.getPerception(player, Perception.DELTA);
			assertEquals(1, delta.modifiedAddedList.size());
			final RPObject added = delta.modifiedAddedList.get(0);
			final RPObject removed = delta.modifiedDeletedList.get(0);
			assertEquals(0, added.getSlot(slot).get(ringId).getInt("amount"));
			assertEquals(0, added.getSlot(slot).get(ringId).getInt("state"));
			clientPlayer.applyDifferences(added, removed);
			assertEquals(0, clientPlayer.getSlot(slot).get(ringId).getInt("amount"));
			assertEquals("State deltas must not remove or reorder inventory items", originalOrder,
					itemOrder(clientPlayer, slot));
			assertNull(ring.getItemData());
			assertTrue(ring.has("frequency"));
		} finally {
			if (completion != null) {
				notifier.dontNotify(completion);
			}
			if (player.getZone() != null) {
				player.getZone().remove(player);
			}
			world.removeZone(source);
			if (!sameZone) {
				world.removeZone(destination);
			}
		}
	}

	private List<Integer> itemOrder(final RPObject player, final String slot) {
		final List<Integer> ids = new ArrayList<Integer>();
		for (final RPObject item : player.getSlot(slot)) {
			ids.add(item.getInt("id"));
		}
		return ids;
	}
}
