package games.stendhal.server.entity.player;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.item.StackableItem;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** One-time migration of the obsolete reserve money slot, performed on login. */
public final class ReservePouchMigration {
	private static final Logger LOGGER = Logger.getLogger(ReservePouchMigration.class);
	private ReservePouchMigration() { }

	public static boolean migrate(final Player player) {
		if (!player.hasSlot("pouch_set")) {
			return true;
		}
		final RPSlot reserve = player.getSlot("pouch_set");
		if (reserve.size() == 0) {
			player.removeSlot("pouch_set");
			return true;
		}
		final RPSlot pouch = player.getSlot("pouch");
		if (reserve.size() != 1 || pouch == null || pouch.size() > 1) {
			return defer(player);
		}
		final RPObject stored = reserve.iterator().next();
		if (!(stored instanceof StackableItem)
				|| !"money".equals(((StackableItem) stored).getName())) {
			return defer(player);
		}
		final StackableItem money = (StackableItem) stored;
		if (pouch.size() == 0) {
			reserve.remove(money.getID());
			pouch.add(money);
		} else {
			final RPObject current = pouch.iterator().next();
			if (!(current instanceof StackableItem)) {
				return defer(player);
			}
			final StackableItem target = (StackableItem) current;
			final long quantity = (long) target.getQuantity() + money.getQuantity();
			if (!"money".equals(target.getName()) || !target.isStackable(money)
					|| quantity > Integer.MAX_VALUE || quantity <= 0) {
				return defer(player);
			}
			target.setQuantity((int) quantity);
			reserve.remove(money.getID());
		}
		player.setFeature("pouch", true);
		player.removeSlot("pouch_set");
		return true;
	}

	private static boolean defer(final Player player) {
		// Keep the original data rather than losing items or overflowing a stack.
		// PlayerSlot forbids new deposits into this legacy slot.
		LOGGER.error("Reserve pouch migration needs manual attention for " + player.getName());
		player.sendPrivateText("Zawartość starej sakwy schowka została bezpiecznie zachowana, ale nie udało się przenieść jej automatycznie. Skontaktuj się z administracją.");
		return false;
	}
}
