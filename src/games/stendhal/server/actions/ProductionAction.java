package games.stendhal.server.actions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.useable.ProductionWorksite;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.ProductionRecipe;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ProductionEvent;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/** A window over existing producer orders; inventory and time remain server-owned. */
public final class ProductionAction implements ActionListener {
    private static final Map<Player, Session> SESSIONS = new WeakHashMap<>();

    private static final class Session {
        final int npcId;
        final Entity target;
        final String id = UUID.randomUUID().toString();
        final Set<String> mutations = new LinkedHashSet<>();
        String recipe;
        Session(Entity target) { this.target = target; this.npcId = target.getID().getObjectID(); }
    }

    public static void register() { CommandCenter.register(Actions.PRODUCTION, new ProductionAction()); }

    public static boolean hasOrder(Player player, ProducerBehaviour producer) {
        return player.hasQuest(producer.getQuestSlot()) && !player.isQuestCompleted(producer.getQuestSlot());
    }

    /** Open the production window by using the worksite in the world. */
    public static void open(Player player, ProductionWorksite worksite) {
        RPAction action = new RPAction();
        action.put("command", "open");
        action.put("npc_id", worksite.getID().getObjectID());
        new ProductionAction().onAction(player, action);
    }

    @Override public void onAction(Player player, RPAction action) {
        String command = action.has("command") ? action.get("command") : "";
        Session session = SESSIONS.get(player);
        if ("close".equals(command)) {
            if (session != null && session.id.equals(value(action, "window_id"))) SESSIONS.remove(player);
            return;
        }
        Entity target = resolve(player, action);
        SpeakerNPC npc = target instanceof ProductionWorksite ? ((ProductionWorksite) target).getProducer() : null;
        List<ProductionRecipe> recipes = npc == null ? Collections.emptyList() : ProductionRecipe.forNPC(npc);
        boolean nearby = npc != null && npc.getZone() == target.getZone() && player.nextTo(target);
        if (recipes.isEmpty() || !nearby) {
            if (session != null) {
                player.addEvent(ProductionEvent.rejected(session.npcId, session.id, value(action, "request_id"),
                        "too_far", "Podejdź do rzemieślnika lub jego stanowiska, aby obsłużyć zamówienie."));
                SESSIONS.remove(player);
            } else player.sendPrivateText("Podejdź do rzemieślnika lub jego stanowiska, aby obsłużyć zamówienie.");
            return;
        }
        if ("open".equals(command)) {
            session = new Session(target);
            session.recipe = recipes.get(0).getId();
            for (ProductionRecipe entry : recipes) {
                if (entry.hasOrder(player) && entry.getProductName().equals(entry.getOrderName(player))) {
                    session.recipe = entry.getId(); break;
                }
            }
            SESSIONS.put(player, session);
        } else if (session == null || session.target != target
                || !session.id.equals(value(action, "window_id"))) return;

        String requestedRecipe = action.has("recipe") ? action.get("recipe") : session.recipe;
        boolean changedRecipe = !requestedRecipe.equals(session.recipe);
        ProductionRecipe producer = null;
        for (ProductionRecipe entry : recipes) if (entry.getId().equals(requestedRecipe)) producer = entry;
        if (producer == null) return;
        session.recipe = producer.getId();

        int amount = 1;
        try { if (action.has("amount")) amount = action.getInt("amount"); }
        catch (NumberFormatException e) { amount = 0; }
        String status = "ok", message = "";
        if (("open".equals(command) || (changedRecipe && "refresh".equals(command))) && !producer.validAmount(amount))
            amount = producer.getBatchSize();
        boolean validAmount = producer.validAmount(amount);
        boolean mutation = "start".equals(command) || "collect".equals(command);
        String request = value(action, "request_id");
        if (mutation && (request.isEmpty() || request.length() > 96 || !session.mutations.add(request))) return;
        if (session.mutations.size() > 64) session.mutations.remove(session.mutations.iterator().next());
        if (!validAmount) {
            status = "invalid_amount";
            message = "Wybierz ilość od " + producer.getBatchSize() + " do " + producer.getOrderLimit()
                    + ", co " + producer.getBatchSize() + " szt.";
            amount = producer.getBatchSize();
        } else if ("start".equals(command)) {
            if (producer.hasOrder(player)) {
                status = "active"; message = "Najpierw odbierz poprzednie zamówienie.";
            } else if (!producer.unavailableReason(player).isEmpty()) {
                status = "unavailable"; message = producer.unavailableReason(player);
            } else if (!producer.hasResources(player, amount)) {
                status = "missing"; message = "Brakuje składników do tego zamówienia.";
            } else if (producer.start(npc, player, amount)) {
                message = "Zamówienie zostało przyjęte.";
                player.notifyWorldAboutChanges();
            }
        } else if ("collect".equals(command)) {
            if (!producer.hasOrder(player)) {
                message = "Nie masz zamówienia do odebrania.";
            } else if (!producer.isReady(player)) {
                status = "working"; message = "Zamówienie jest jeszcze w trakcie realizacji.";
            } else {
                producer.collect(npc, player);
                message = producer.hasOrder(player) ? "Zwolnij miejsce w ekwipunku, aby odebrać zamówienie."
                        : "Odebrano zamówienie.";
                if (producer.hasOrder(player)) status = "inventory_full";
            }
        } else if (!"open".equals(command) && !"refresh".equals(command)) return;
        player.addEvent(new ProductionEvent(player, session.npcId, npc, producer, session.id, request,
                "open".equals(command) ? "open" : "update", amount, status, message));
    }

    private static String value(RPAction action, String key) { return action.has(key) ? action.get(key) : ""; }

    private static Entity resolve(Player player, RPAction action) {
        if (player.getZone() == null || !action.has("npc_id")) return null;
        try {
            Entity entity = EntityHelper.entityFromZoneByID(action.getInt("npc_id"), player.getZone());
            return entity instanceof ProductionWorksite ? entity : null;
        } catch (RuntimeException e) { return null; }
    }
}
