package games.stendhal.server.actions;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.useable.ProductionWorksite;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ProductionEvent;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.Pair;
import marauroa.common.game.RPAction;

/** A window over existing producer orders; inventory and time remain server-owned. */
public final class ProductionAction implements ActionListener {
    private static final Map<Player, Session> SESSIONS = new WeakHashMap<>();

    private static final class Session {
        final int npcId;
        final String id = UUID.randomUUID().toString();
        final Set<String> mutations = new LinkedHashSet<>();
        Session(int npcId) { this.npcId = npcId; }
    }

    public static void register() { CommandCenter.register(Actions.PRODUCTION, new ProductionAction()); }

    public static boolean hasOrder(Player player, ProducerBehaviour producer) {
        return player.hasQuest(producer.getQuestSlot()) && !player.isQuestCompleted(producer.getQuestSlot());
    }

    private static ProducerBehaviour recipe(SpeakerNPC npc) {
        for (Pair<String, ProducerBehaviour> entry : SingletonRepository.getProducerRegister().getProducers()) {
            if (npc.getName().equals(entry.first())) return entry.second();
        }
        return null;
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
        ProductionWorksite worksite = resolve(player, action);
        SpeakerNPC npc = worksite == null ? null : worksite.getProducer();
        ProducerBehaviour producer = npc == null ? null : recipe(npc);
        boolean nearby = worksite != null && npc.getZone() == worksite.getZone() && player.nextTo(worksite);
        if (producer == null || !nearby) {
            if (session != null) {
                player.addEvent(ProductionEvent.rejected(session.npcId, session.id, value(action, "request_id"),
                        "too_far", "Podejdź do mielerza, aby obsłużyć zamówienie."));
                SESSIONS.remove(player);
            } else player.sendPrivateText("Podejdź do mielerza, aby obsłużyć zamówienie.");
            return;
        }
        if ("open".equals(command)) {
            session = new Session(worksite.getID().getObjectID());
            SESSIONS.put(player, session);
        } else if (session == null || session.npcId != worksite.getID().getObjectID()
                || !session.id.equals(value(action, "window_id"))) return;

        int amount = 1;
        try { if (action.has("amount")) amount = action.getInt("amount"); }
        catch (NumberFormatException e) { amount = 0; }
        String status = "ok", message = "";
        boolean validAmount = amount > 0 && amount <= producer.getOrderLimit();
        boolean mutation = "start".equals(command) || "collect".equals(command);
        String request = value(action, "request_id");
        if (mutation && (request.isEmpty() || request.length() > 96 || !session.mutations.add(request))) return;
        if (session.mutations.size() > 64) session.mutations.remove(session.mutations.iterator().next());
        if (!validAmount) {
            status = "invalid_amount";
            message = "Wybierz ilość od 1 do " + producer.getOrderLimit() + ".";
            amount = 1;
        } else if ("start".equals(command)) {
            if (hasOrder(player, producer)) {
                status = "active"; message = "Najpierw odbierz poprzednie zamówienie.";
            } else if (!hasResources(player, producer, amount)) {
                status = "missing"; message = "Brakuje składników do tego zamówienia.";
            } else if (producer.transactAgreedDeal(new ItemParserResult(true, producer.getProductName(), amount, null),
                    new EventRaiser(npc), player)) {
                message = "Zamówienie zostało przyjęte.";
                player.notifyWorldAboutChanges();
            }
        } else if ("collect".equals(command)) {
            if (!hasOrder(player, producer)) {
                message = "Nie masz zamówienia do odebrania.";
            } else if (!producer.isOrderReady(player)) {
                status = "working"; message = "Zamówienie jest jeszcze w trakcie realizacji.";
            } else {
                producer.giveProduct(new EventRaiser(npc), player);
                message = hasOrder(player, producer) ? "Zwolnij miejsce w ekwipunku, aby odebrać zamówienie."
                        : "Odebrano zamówienie.";
                if (hasOrder(player, producer)) status = "inventory_full";
            }
        } else if (!"open".equals(command) && !"refresh".equals(command)) return;
        player.addEvent(new ProductionEvent(player, session.npcId, npc, producer, session.id, request,
                "open".equals(command) ? "open" : "update", amount, status, message));
    }

    public static boolean hasResources(Player player, ProducerBehaviour producer, int amount) {
        for (Map.Entry<String, Integer> resource : producer.getRecipeResources().entrySet()) {
            if (player.getNumberOfEquipped(resource.getKey()) < (long) resource.getValue() * amount) return false;
        }
        return true;
    }

    private static String value(RPAction action, String key) { return action.has(key) ? action.get(key) : ""; }

    private static ProductionWorksite resolve(Player player, RPAction action) {
        if (player.getZone() == null || !action.has("npc_id")) return null;
        try {
            Entity entity = EntityHelper.entityFromZoneByID(action.getInt("npc_id"), player.getZone());
            return entity instanceof ProductionWorksite ? (ProductionWorksite) entity : null;
        } catch (RuntimeException e) { return null; }
    }
}
