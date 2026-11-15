/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import games.stendhal.client.entity.Chest;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.entity.User;
import games.stendhal.common.EquipActionConsts;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/** Open chest windows, in the order in which the player selected them. */
final class ChestQuickTransfer {
	private static final List<Target> targets = new ArrayList<>();

	private ChestQuickTransfer() { }

	static synchronized void register(final SlotWindow window, final IEntity owner, final String slot) {
		remove(window);
		if (owner instanceof Chest && User.get() != null) {
			targets.add(new Target(window, (Chest) owner, slot, User.get()));
		}
	}

	static synchronized void activate(final SlotWindow window) {
		for (int i = 0; i < targets.size(); i++) {
			if (targets.get(i).window.get() == window) {
				targets.add(targets.remove(i));
				return;
			}
		}
	}

	static synchronized void remove(final SlotWindow window) {
		for (Iterator<Target> iterator = targets.iterator(); iterator.hasNext();) {
			SlotWindow current = iterator.next().window.get();
			if (current == null || current == window) { iterator.remove(); }
		}
	}

	static synchronized RPAction createAction(final IEntity entity, final User user) {
		if (!(entity instanceof Item) || user == null || user.getRPObject() == null) {
			return null;
		}
		final RPObject item = entity.getRPObject();
		final RPObject player = user.getRPObject();
		if (item == null || !item.isContained() || !item.has("id") || !player.has("zoneid")) {
			return null;
		}
		final String zone = player.get("zoneid");
		if (!item.getBaseContainer().has("zoneid") || !zone.equals(item.getBaseContainer().get("zoneid"))) {
			return null;
		}
		for (int i = targets.size() - 1; i >= 0; i--) {
			final Target target = targets.get(i);
			final SlotWindow window = target.window.get();
			if (window == null) { targets.remove(i); continue; }
			final Chest owner = target.chest.get();
			if (owner == null) { targets.remove(i); continue; }
			final RPObject chest = owner.getRPObject();
			if (target.user.get() != user || !window.isVisible() || window.isMinimized()
					|| !owner.isOpen() || chest == null || !chest.has("id")
					|| !chest.has("zoneid") || !zone.equals(chest.get("zoneid"))
					|| !chest.hasSlot(target.slot)
					|| !SlotWindow.isCloseEnough(owner, user.getName(), user.getX(), user.getY())) {
				continue;
			}
			final List<String> sourcePath = entity.getPath();
			final List<String> destination = new ArrayList<>(owner.getPath());
			destination.add(target.slot);
			// Keep pickup-to-bag for this chest, including nested contents, and reject cycles.
			if (startsWith(sourcePath, destination) || startsWith(destination, sourcePath)) {
				return null;
			}
			final RPAction action = new RPAction();
			action.put(EquipActionConsts.TYPE, "equip");
			action.put(EquipActionConsts.SOURCE_PATH, sourcePath);
			action.put(Actions.TARGET_PATH, destination);
			action.put("zone", zone);
			action.put(EquipActionConsts.BASE_OBJECT, item.getContainer().getID().getObjectID());
			action.put(EquipActionConsts.BASE_SLOT, item.getContainerSlot().getName());
			action.put(EquipActionConsts.BASE_ITEM, item.getID().getObjectID());
			action.put(EquipActionConsts.TARGET_OBJECT, chest.getID().getObjectID());
			action.put(EquipActionConsts.TARGET_SLOT, target.slot);
			return action;
		}
		return null;
	}

	private static boolean startsWith(final List<String> path, final List<String> prefix) {
		return path.size() >= prefix.size() && path.subList(0, prefix.size()).equals(prefix);
	}

	private static final class Target {
		private final WeakReference<SlotWindow> window;
		private final WeakReference<Chest> chest;
		private final String slot;
		private final WeakReference<User> user;
		Target(final SlotWindow window, final Chest chest, final String slot, final User user) {
			this.window = new WeakReference<>(window);
			this.chest = new WeakReference<>(chest);
			this.slot = slot;
			this.user = new WeakReference<>(user);
		}
	}
}
