package games.stendhal.server.actions;

import java.util.Collections;
import games.stendhal.server.core.rule.glyph.GlyphCraftingService;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.glyph.GlyphUpgradeService;
import games.stendhal.server.core.rule.glyph.GlyphWorkbenchCatalog;
import games.stendhal.server.core.rule.glyph.GlyphWorkbenchCatalog.Entry;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult.Status;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.useable.GlyphWorkbench;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ItemUpgradeEvent;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/** Server-owned recipe selection for the two workbench categories. */
public final class GlyphUpgradeAction {
    private static final GlyphUpgradeService SERVICE = GlyphUpgradeService.getInstance();
    private GlyphUpgradeAction() { }
    private static void clear(Player player) { SERVICE.clearPending(player); GlyphCraftingService.clear(player); }
    public static void open(Player player, GlyphWorkbench table) {
        clear(player); send(player, table.getID().getObjectID(), null, null, "open", null, "");
    }
    public static void handle(Player player, RPAction action) {
        String command = action.has("command") ? action.get("command") : "";
        if ("clear".equals(command) || "close".equals(command)) { clear(player); return; }
        int tableId; Entity table;
        try {
            tableId = action.getInt("npc_id");
            table = player.getZone() == null ? null : EntityHelper.entityFromZoneByID(tableId, player.getZone());
        } catch (RuntimeException invalid) { clear(player); return; }
        if (!(table instanceof GlyphWorkbench) || !player.nextTo(table)) {
            clear(player);
            player.addEvent(ItemUpgradeEvent.workbench(player, tableId, Collections.<Item>emptyList(), null, null,
                    "preview", Status.NPC_TOO_FAR, "Podejdź do stołu runicznego, aby rozwijać glify.", null)); return;
        }
        if ("open".equals(command)) { open(player, (GlyphWorkbench)table); return; }
        if ("refresh".equals(command)) { clear(player); send(player, tableId, null, null, "refresh", null, ""); return; }
        Item requested = ItemUpgradeAction.resolveOwnedItem(player, action);
        Entry entry = action.has("glyph_recipe") ? GlyphWorkbenchCatalog.byId(action.get("glyph_recipe")) : null;
        if (entry == null && !action.has("glyph_recipe") && "preview".equals(command)
                && requested != null && GlyphProgressionService.isUpgradeable(requested)) {
            entry = GlyphWorkbenchCatalog.find(requested.getName(), GlyphProgressionService.getLevel(requested) + 1);
        }
        if (entry == null || (!entry.creates() && !"preview".equals(command) && action.has("target_path") && !entry.accepts(player, requested))) {
            clear(player); send(player, tableId, entry, null, "result", Status.INVALID_ITEM, "Wybierz glif z listy receptur."); return;
        }
        Item source = entry.creates() ? null : entry.accepts(player, requested) ? requested : entry.source(player);
        if ("preview".equals(command)) { clear(player); send(player, tableId, entry, source, "preview", null, ""); return; }
        if (!"upgrade".equals(command) || !action.has("request_token")) {
            clear(player); send(player, tableId, entry, source, "result", Status.STALE_PREVIEW, "Sprawdź ponownie moc glifu i wymagane składniki."); return;
        }
        boolean success;
        if (entry.creates()) {
            success = GlyphCraftingService.craft(player, entry.name, action.get("request_token")) != null;
        } else {
            success = requested != null && SERVICE.upgrade(player, requested, entry.level - 1, action.get("request_token"));
        }
        if (!success) clear(player);
        send(player, tableId, entry, entry.creates() ? null : entry.source(player), "result",
                success ? Status.SUCCESS : Status.STALE_PREVIEW,
                success ? (entry.creates() ? "Glif został utworzony na poziomie I." : "Glif otrzymał wyższy poziom i większą moc.")
                        : "Nie wykonano operacji. Sprawdź składniki i miejsce w plecaku, a następnie odśwież podgląd.");
    }
    private static void send(Player player, int tableId, Entry entry, Item source, String phase, Status result, String message) {
        Status state = entry == null ? Status.SELECT_ITEM : entry.creates() ? entry.status(player)
                : source == null ? Status.MISSING_RESOURCES : SERVICE.getUpgradeStatus(player, source);
        String token = null;
        if (state == Status.READY && (result == null || result == Status.SUCCESS)) {
            if (entry.creates()) token = GlyphCraftingService.prepare(player, entry.name);
            else if (SERVICE.prepareUpgrade(player, source)) token = SERVICE.getRequestToken(player, source);
        }
        player.addEvent(ItemUpgradeEvent.workbench(player, tableId, SERVICE.findUpgradeCandidates(player), entry, source,
                phase, result == null ? state : result, message, token));
    }
}
