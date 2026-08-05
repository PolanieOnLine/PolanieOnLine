package games.stendhal.server.actions.equip;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.OwnedItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Swaps equipment between the primary set and the reserve set slots.
 */
public class SetChangeAction implements ActionListener {

	private static final Logger LOGGER = Logger.getLogger(SetChangeAction.class);

	private static final List<String> EQUIPMENT_SLOTS = Arrays.asList(
			"neck", "head", "cloak", "lhand", "armor", "rhand",
			"finger", "pas", "legs", "glove", "fingerb", "feet", "pouch");

	private enum SwapOutcome {
		NONE,
		SWAPPED,
		BLOCKED
	}

	public static void register() {
		CommandCenter.register(Actions.SET_CHANGE, new SetChangeAction());
	}

	@Override
	public void onAction(Player player, RPAction action) {
		boolean swapped = false;
		boolean attempted = false;
		boolean blocked = false;

		for (String slot : EQUIPMENT_SLOTS) {
			SwapOutcome outcome = swapSlot(player, slot, slot + "_set");
			switch (outcome) {
			case SWAPPED:
				swapped = true;
				attempted = true;
				break;
			case BLOCKED:
				attempted = true;
				blocked = true;
				break;
			case NONE:
			default:
				break;
			}
		}

		if (swapped) {
			SingletonRepository.getRPWorld().modify(player);
			player.updateItemAtkDef();
			player.notifyWorldAboutChanges();
			player.sendPrivateText("Zamieniono aktywny zestaw wyposażenia.");
		} else if (!blocked && !attempted) {
			player.sendPrivateText("Nie znaleziono elementów do zamiany.");
		}
	}

	private SwapOutcome swapSlot(Player player, String primarySlot, String reserveSlot) {
		RPSlot primary = player.getSlot(primarySlot);
		RPSlot reserve = player.getSlot(reserveSlot);

		if ((primary == null) || (reserve == null)) {
			LOGGER.warn("Slot " + primarySlot + " or reserve slot " + reserveSlot
					+ " is missing for player " + player.getName());
			return SwapOutcome.NONE;
		}

		Item primaryItem = getFirstItem(primary);
		Item reserveItem = getFirstItem(reserve);

		if ((primaryItem == null) && (reserveItem == null)) {
			return SwapOutcome.NONE;
		}

		if (reserveItem != null) {
			if (!reserveItem.canBeEquippedIn(primarySlot)) {
				player.sendPrivateText("Nie możesz umieścić " + reserveItem.getDescriptionName()
						+ " w slocie \"" + primarySlot + "\".");
				return SwapOutcome.BLOCKED;
			}

			if (reserveItem.isBound() && !player.isBoundTo(reserveItem)) {
				player.sendPrivateText("Oto " + reserveItem.getDescriptionName()
						+ " jest specjalną nagrodą dla " + reserveItem.getBoundTo()
						+ ". Nie zasługujesz na to, aby jej używać.");
				return SwapOutcome.BLOCKED;
			}

			if (reserveItem instanceof OwnedItem) {
				OwnedItem owned = (OwnedItem) reserveItem;
				if (owned.hasOwner() && !owned.canEquipToSlot(player, primarySlot)) {
					owned.onEquipFail(player, primarySlot);
					return SwapOutcome.BLOCKED;
				}
			}

			if (Slots.CARRYING.getNames().contains(primarySlot) && !"bag".equals(primarySlot)) {
				int minUse = reserveItem.getMinUse();
				if (minUse > player.getLevel()) {
					player.sendPrivateText("Nie jesteś jeszcze wystarczająco doświadczony, aby używać "
							+ reserveItem.getDescriptionName() + ". Wymagany jest " + minUse
							+ " poziom.");
					return SwapOutcome.BLOCKED;
				}
			}
		}

		if (primaryItem != null) {
			primaryItem.removeFromWorld();
		}
		if (reserveItem != null) {
			reserveItem.removeFromWorld();
		}

		if (primaryItem != null) {
			addToSlot(reserve, primaryItem);
		}
		if (reserveItem != null) {
			addToSlot(primary, reserveItem);
			reserveItem.autobind(player.getName());
			reserveItem.onEquipped(player, primarySlot);
		}

		return SwapOutcome.SWAPPED;
	}

	private Item getFirstItem(RPSlot slot) {
		Iterator<RPObject> iterator = slot.iterator();
		if (iterator.hasNext()) {
			RPObject object = iterator.next();
			if (object instanceof Item) {
				return (Item) object;
			}
		}
		return null;
	}

	private void addToSlot(RPSlot slot, Item item) {
		item.setPosition(0, 0);
		slot.add(item);
	}
}
