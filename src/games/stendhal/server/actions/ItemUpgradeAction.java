/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.actions;

import java.util.Collections;
import java.util.List;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradePreview;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.behaviour.adder.ItemUpgradeAdder.ItemUpgradeNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ItemUpgradeEvent;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

/** Handles server-authoritative item-upgrade preview and execution requests. */
public final class ItemUpgradeAction implements ActionListener {
	public static final String COMMAND = "command";
	public static final String OPEN = "open";
	public static final String PREVIEW = "preview";
	public static final String UPGRADE = "upgrade";
	public static final String NPC_ID = "npc_id";
	public static final String REQUEST_TOKEN = "request_token";
	public static final String TARGET_PATH = "target_path";

	private static final ItemUpgradeService SERVICE =
			ItemUpgradeService.getInstance();

	public static void register() {
		CommandCenter.register(Actions.ITEM_UPGRADE, new ItemUpgradeAction());
	}

	/** Opens the shared window from an attending item-upgrade NPC. */
	public static void openWindow(final Player player,
			final ItemUpgradeNPC npc, final String preferredName) {
		final List<Item> candidates = SERVICE.findUpgradeCandidates(player);
		Item selected = null;
		if (preferredName != null && preferredName.length() > 0) {
			for (final Item candidate : candidates) {
				if (preferredName.equalsIgnoreCase(candidate.getName())) {
					selected = candidate;
					break;
				}
			}
		}
		if (selected == null && !candidates.isEmpty()) {
			selected = preferredName != null && preferredName.length() > 0
					? candidates.get(0) : null;
		}
		final ItemUpgradePreview preview = selected == null ? null
				: SERVICE.createPreview(player, selected);
		player.addEvent(ItemUpgradeEvent.open(npc.getID().getObjectID(),
				candidates, preview));
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String command = action.has(COMMAND) ? action.get(COMMAND) : "";
		final ItemUpgradeNPC npc = resolveNpc(player, action);
		if (npc == null) {
			SERVICE.clearPendingAttempt(player);
			if (OPEN.equals(command)) {
				player.sendPrivateText(SERVICE.resultForStatus(
						ItemUpgradeResult.Status.NPC_TOO_FAR).getMessage());
				return;
			}
			sendResult(player, action, null,
					SERVICE.resultForStatus(ItemUpgradeResult.Status.NPC_TOO_FAR));
			return;
		}

		if (OPEN.equals(command)) {
			openFromContextMenu(player, npc);
			return;
		}
		if (npc.getAttending() != player) {
			SERVICE.clearPendingAttempt(player);
			sendResult(player, action, npc,
					SERVICE.resultForStatus(npc.getAttending() == null
							? ItemUpgradeResult.Status.NPC_NOT_ATTENDING
							: ItemUpgradeResult.Status.NPC_BUSY));
			return;
		}
		if (!npc.inConversationRange()) {
			SERVICE.clearPendingAttempt(player);
			sendResult(player, action, npc,
					SERVICE.resultForStatus(ItemUpgradeResult.Status.NPC_TOO_FAR));
			return;
		}

		final Item item = resolveOwnedItem(player, action);
		if (item == null || !containsSameInstance(
				SERVICE.findUpgradeCandidates(player), item)) {
			SERVICE.clearPendingAttempt(player);
			sendResult(player, action, npc,
					SERVICE.resultForStatus(ItemUpgradeResult.Status.INVALID_ITEM));
			return;
		}

		if (PREVIEW.equals(command)) {
			final ItemUpgradePreview preview = SERVICE.createPreview(player, item);
			player.addEvent(new ItemUpgradeEvent(npc.getID().getObjectID(),
					SERVICE.findUpgradeCandidates(player), preview, null));
			return;
		}
		if (!UPGRADE.equals(command) || !action.has(REQUEST_TOKEN)) {
			SERVICE.clearPendingAttempt(player);
			sendResult(player, action, npc,
					SERVICE.resultForStatus(ItemUpgradeResult.Status.INVALID_REQUEST));
			return;
		}

		final ItemUpgradeResult result = SERVICE.performUpgrade(player, item,
				action.get(REQUEST_TOKEN));
		if (result.isSuccess()) {
			npc.addEvent(new SoundEvent(SoundID.COMMERCE,
					SoundLayer.CREATURE_NOISE));
		}
		final List<Item> candidates = SERVICE.findUpgradeCandidates(player);
		final ItemUpgradePreview preview = containsSameInstance(candidates, item)
				? SERVICE.createPreview(player, item) : null;
		player.addEvent(new ItemUpgradeEvent(npc.getID().getObjectID(),
				candidates, preview, result));
	}

	private void openFromContextMenu(final Player player,
			final ItemUpgradeNPC npc) {
		if (npc.getAttending() == null) {
			if (!withinPerceptionRange(player, npc)) {
				rejectOpen(player, ItemUpgradeResult.Status.NPC_TOO_FAR);
				return;
			}
			npc.listenTo(player, "hi");
		}
		if (npc.getAttending() != player) {
			rejectOpen(player, ItemUpgradeResult.Status.NPC_BUSY);
			return;
		}
		if (!npc.inConversationRange()) {
			rejectOpen(player, ItemUpgradeResult.Status.NPC_TOO_FAR);
			return;
		}
		openWindow(player, npc, null);
	}

	private boolean withinPerceptionRange(final Player player,
			final ItemUpgradeNPC npc) {
		final int range = npc.getPerceptionRange();
		return player.squaredDistance(npc) <= range * range;
	}

	private void rejectOpen(final Player player,
			final ItemUpgradeResult.Status status) {
		SERVICE.clearPendingAttempt(player);
		player.sendPrivateText(SERVICE.resultForStatus(status).getMessage());
	}

	private ItemUpgradeNPC resolveNpc(final Player player,
			final RPAction action) {
		if (!action.has(NPC_ID) || player.getZone() == null) {
			return null;
		}
		try {
			final Entity entity = EntityHelper.entityFromZoneByID(
					action.getInt(NPC_ID), player.getZone());
			return entity instanceof ItemUpgradeNPC
					? (ItemUpgradeNPC) entity : null;
		} catch (final RuntimeException e) {
			return null;
		}
	}

	static Item resolveOwnedItem(final Player player, final RPAction action) {
		if (!action.has(TARGET_PATH)) {
			return null;
		}
		final List<String> path = action.getList(TARGET_PATH);
		if (path == null || path.isEmpty()) {
			return null;
		}
		final Entity entity;
		try {
			entity = EntityHelper.getEntityFromPath(player, path);
		} catch (final RuntimeException e) {
			return null;
		}
		if (!(entity instanceof Item)) {
			return null;
		}
		RPObject root = entity;
		while (root.getContainer() != null) {
			root = root.getContainer();
		}
		return root == player ? (Item) entity : null;
	}

	private static boolean containsSameInstance(final List<Item> candidates,
			final Item item) {
		for (final Item candidate : candidates) {
			if (candidate == item) {
				return true;
			}
		}
		return false;
	}

	private void sendResult(final Player player, final RPAction action,
			final ItemUpgradeNPC npc, final ItemUpgradeResult result) {
		final int npcId = npc != null ? npc.getID().getObjectID()
				: getSafeNpcId(action);
		player.addEvent(new ItemUpgradeEvent(npcId,
				npc == null ? Collections.<Item>emptyList()
						: SERVICE.findUpgradeCandidates(player), null, result));
	}

	private int getSafeNpcId(final RPAction action) {
		if (!action.has(NPC_ID)) {
			return 0;
		}
		try {
			return action.getInt(NPC_ID);
		} catch (final RuntimeException e) {
			return 0;
		}
	}
}
