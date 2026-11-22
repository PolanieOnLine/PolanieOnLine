/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.actions;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import games.stendhal.server.core.rule.rarity.ItemAffixRerollService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AffixRerollEvent;
import games.stendhal.server.util.EntityHelper;
import games.stendhal.server.maps.tarnow.enchanter.EnchanterNPC;
import marauroa.common.game.RPAction;

/** Correlates requests with the exact enchanter instance which opened the window. */
public final class AffixRerollAction implements ActionListener {
	private static final ItemAffixRerollService SERVICE = ItemAffixRerollService.getInstance();
	private static final Map<Player, Window> WINDOWS = new WeakHashMap<Player, Window>();

	public static void register() { CommandCenter.register("affix_reroll_action", new AffixRerollAction()); }

	public static void open(final Player player, final SpeakerNPC npc) {
		if (npc != NPCList.get().get(EnchanterNPC.NAME) || !near(player, npc)) return;
		final Window window = new Window(npc);
		WINDOWS.put(player, window);
		SERVICE.clearSelection(player);
		final Item pending = SERVICE.pending(player);
		window.item = new WeakReference<Item>(pending);
		send(player, window, "", "open", pending == null ? "SELECT_ITEM" : SERVICE.validPending(player, pending) ? "OFFER" : "CHANGED_ITEM");
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final Window window = WINDOWS.get(player);
		if (window == null || !window.id.equals(value(action, "window_id"))) return;
		final String command = value(action, "command");
		if ("close".equals(command)) {
			WINDOWS.remove(player); SERVICE.clearSelection(player); return;
		}
		final String request = value(action, "request_id");
		if (!resolve(player, action, window)) {
			SERVICE.clearSelection(player); send(player, window, request, "result", "NPC_TOO_FAR"); return;
		}
		if ("refresh".equals(command)) {
			SERVICE.clearSelection(player);
			final Item pending = SERVICE.pending(player);
			if (pending != null) {
				window.item = new WeakReference<Item>(pending);
				send(player, window, request, "offer", SERVICE.validPending(player, pending) ? "OFFER" : "CHANGED_ITEM");
				return;
			}
			final Item selected = window.item.get();
			if (selected == null || !SERVICE.candidates(player).contains(selected)) {
				window.item = new WeakReference<Item>(null); window.affix = "";
				send(player, window, request, "select", "SELECT_ITEM");
				return;
			}
			final String token = SERVICE.select(player, selected, window.affix);
			send(player, window, request, "select", token.isEmpty() ? "SELECT_AFFIX"
					: SERVICE.priceStatus(player, selected, window.affix));
			return;
		}
		final Item item = ItemUpgradeAction.resolveOwnedItem(player, action);
		if (item == null || !SERVICE.candidates(player).contains(item)) {
			SERVICE.clearSelection(player); window.item = new WeakReference<Item>(null); send(player, window, request, "result", "INVALID_ITEM"); return;
		}
		window.item = new WeakReference<Item>(item);
		if ("select".equals(command)) {
			if (SERVICE.hasPending(item)) {
				SERVICE.clearSelection(player); send(player, window, request, "offer", SERVICE.validPending(player, item) ? "OFFER" : "CHANGED_ITEM"); return;
			}
			window.affix = value(action, "affix_id");
			final String token = SERVICE.select(player, item, window.affix);
			send(player, window, request, "select", token.isEmpty() ? "SELECT_AFFIX" : SERVICE.priceStatus(player, item, window.affix)); return;
		}
		if ("roll".equals(command)) {
			window.affix = value(action, "affix_id");
			final String status = SERVICE.roll(player, item, window.affix, value(action, "request_token"));
			send(player, window, request, SERVICE.hasPending(item) ? "offer" : "result", status); return;
		}
		if ("accept".equals(command) || "keep".equals(command)) {
			final String status = SERVICE.finish(player, item, value(action, "offer_token"), "accept".equals(command));
			window.affix = "";
			send(player, window, request, "result", status); return;
		}
		SERVICE.clearSelection(player); send(player, window, request, "result", "INVALID_REQUEST");
	}

	private static boolean near(final Player player, final SpeakerNPC npc) {
		return player.getZone() != null && player.getZone() == npc.getZone()
				&& player.squaredDistance(npc) <= 25;
	}

	private static boolean resolve(final Player player, final RPAction action, final Window window) {
		if (!near(player, window.npc) || window.npc != NPCList.get().get(EnchanterNPC.NAME)) return false;
		try {
			final Entity entity = EntityHelper.entityFromZoneByID(action.getInt("npc_id"), player.getZone());
			return entity == window.npc;
		} catch (final RuntimeException e) { return false; }
	}

	private static String value(final RPAction action, final String key) { return action.has(key) ? action.get(key) : ""; }

	private static void send(final Player player, final Window window, final String request, final String phase, final String status) {
		player.addEvent(new AffixRerollEvent(player, window.npc.getID().getObjectID(), window.id, request, phase, status, window.item.get(), window.affix));
	}

	private static final class Window {
		final String id = UUID.randomUUID().toString();
		final SpeakerNPC npc;
		WeakReference<Item> item = new WeakReference<Item>(null);
		String affix = "";
		Window(final SpeakerNPC npc) { this.npc = npc; }
	}
}
