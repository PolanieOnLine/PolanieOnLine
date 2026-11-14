package games.stendhal.server.core.events.advent;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.PlayerBagSlot;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Adds a whole gift to the backpack or restores every changed stack. */
public final class AdventInventoryGrant {

	private AdventInventoryGrant() {
	}

	public static boolean give(final Player player, final List<Item> items) {
		final RPSlot bag = player.getSlot("bag");
		final List<RPObject> originals = new ArrayList<>();
		final Map<StackableItem, Integer> quantities = new IdentityHashMap<>();
		for (final RPObject object : bag) {
			originals.add(object);
			if (object instanceof StackableItem) {
				quantities.put((StackableItem) object, ((StackableItem) object).getQuantity());
			}
		}
		final List<RPObject> plannedObjects = new ArrayList<>(originals);
		final Map<StackableItem, Integer> plannedQuantities = new IdentityHashMap<>(quantities);
		final List<Addition> additions = new ArrayList<>();
		final int capacity = bag instanceof PlayerBagSlot ? ((PlayerBagSlot) bag).getMaxSize() : bag.getCapacity();
		int freeSlots = capacity >= 0 ? capacity - bag.size() : Integer.MAX_VALUE;
		for (final Item item : items) {
			StackableItem target = null;
			int newQuantity = 0;
			if (item instanceof StackableItem) {
				final StackableItem gift = (StackableItem) item;
				for (final RPObject object : plannedObjects) {
					if (object instanceof StackableItem) {
						final StackableItem candidate = (StackableItem) object;
						final long sum = (long) plannedQuantities.get(candidate) + gift.getQuantity();
						if (candidate.isStackable(gift) && sum <= Integer.MAX_VALUE) {
							target = candidate;
							newQuantity = (int) sum;
							plannedQuantities.put(candidate, newQuantity);
							break;
						}
					}
				}
			}
			if (target == null) {
				if (freeSlots-- <= 0) {
					return false;
				}
				plannedObjects.add(item);
				if (item instanceof StackableItem) {
					plannedQuantities.put((StackableItem) item, ((StackableItem) item).getQuantity());
				}
			}
			additions.add(new Addition(item, target, newQuantity));
		}
		// Capacity is checked for the whole bundle before changing the real slot.
		// This also keeps closed rewards out of rejected inventory deltas.
		try {
			for (final Addition addition : additions) {
				if (addition.target == null) {
					bag.add(addition.item);
				} else {
					addition.target.setQuantity(addition.quantity);
				}
			}
		} catch (final RuntimeException exception) {
			restore(bag, originals, quantities);
			throw exception;
		}
		for (final Addition addition : additions) {
			if (addition.target != null) {
				addition.target.setFromCorpse(false);
			}
		}
		return true;
	}

	private static final class Addition {
		private final Item item;
		private final StackableItem target;
		private final int quantity;

		private Addition(final Item item, final StackableItem target, final int quantity) {
			this.item = item;
			this.target = target;
			this.quantity = quantity;
		}
	}

	private static void restore(final RPSlot bag, final List<RPObject> originals,
			final Map<StackableItem, Integer> quantities) {
		final List<RPObject> current = new ArrayList<>();
		for (final RPObject object : bag) {
			current.add(object);
		}
		for (final RPObject object : current) {
			if (!originals.stream().anyMatch(original -> original == object)) {
				bag.remove(object.getID());
			}
		}
		for (final Map.Entry<StackableItem, Integer> entry : quantities.entrySet()) {
			entry.getKey().setQuantity(entry.getValue());
		}
	}
}
