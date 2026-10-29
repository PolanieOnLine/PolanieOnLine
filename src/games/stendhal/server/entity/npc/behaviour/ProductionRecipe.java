package games.stendhal.server.entity.npc.behaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.ref.WeakReference;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.useable.ProductionWorksite;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.player.Player;

/** Read-only window adapter; the original producer owns charging, saves and rewards. */
public final class ProductionRecipe {
    private static final Map<String, Registration> CATALOG = new HashMap<>();
    private static final class Registration {
        final WeakReference<SpeakerNPC> owner;
        final List<ProductionRecipe> recipes = new ArrayList<>();
        ProductionWorksite worksite;
        boolean scheduled, active = true;
        Registration(SpeakerNPC npc) { owner = new WeakReference<>(npc); }
    }
    private final ProducerBehaviour single;
    private final MultiProducerBehaviour multi;
    private final String product, prerequisite;

    private ProductionRecipe(ProducerBehaviour single, MultiProducerBehaviour multi, String product, String prerequisite) {
        this.single = single; this.multi = multi; this.product = product; this.prerequisite = prerequisite;
    }

    public static void register(SpeakerNPC npc, ProducerBehaviour producer, String prerequisite) {
        add(npc, new ProductionRecipe(producer, null, producer.getProductName(), prerequisite));
    }

    public static void register(SpeakerNPC npc, MultiProducerBehaviour producer, String prerequisite) {
        for (String product : producer.getProductsNames()) add(npc, new ProductionRecipe(null, producer, product, prerequisite));
    }

    private static void add(SpeakerNPC npc, ProductionRecipe recipe) {
        Registration registration = CATALOG.get(npc.getName());
        if (registration == null || registration.owner.get() != npc) {
            registration = new Registration(npc); CATALOG.put(npc.getName(), registration);
        }
        List<ProductionRecipe> recipes = registration.recipes;
        recipes.removeIf(existing -> existing.getId().equals(recipe.getId()));
        recipes.add(recipe); recipes.sort(Comparator.comparing(ProductionRecipe::getProductName));
        requestWorksite(npc);
    }

    public static List<ProductionRecipe> forNPC(SpeakerNPC npc) {
        Registration registration = CATALOG.get(npc.getName());
        return registration != null && registration.owner.get() == npc
                ? Collections.unmodifiableList(registration.recipes) : Collections.emptyList();
    }

    public static void ensureWorksite(SpeakerNPC npc) {
        Registration registration = CATALOG.get(npc.getName());
        if (registration != null && registration.owner.get() == npc && registration.active && npc.getZone() != null
                && npc.getZone().has(npc.getID())
                && registration.worksite == null && !"Smolisław".equals(npc.getName())) {
            registration.worksite = ProductionWorksite.placeFor(npc);
        }
    }

    public static void requestWorksite(SpeakerNPC npc) {
        Registration registration = CATALOG.get(npc.getName());
        if (registration == null || registration.owner.get() != npc) return;
        registration.active = true;
        if (!registration.scheduled && registration.worksite == null && !"Smolisław".equals(npc.getName())) {
            registration.scheduled = true;
            // Wait until map configurators have placed portals and every NPC path.
            SingletonRepository.getTurnNotifier().notifyInTurns(1, turn -> {
                registration.scheduled = false; ensureWorksite(npc);
            });
        }
    }

    public static void removeWorksite(SpeakerNPC npc) {
        Registration registration = CATALOG.get(npc.getName());
        if (registration != null && registration.owner.get() == npc) registration.active = false;
        if (registration != null && registration.owner.get() == npc && registration.worksite != null) {
            ProductionWorksite worksite = registration.worksite; registration.worksite = null;
            if (worksite.getZone() != null) worksite.getZone().remove(worksite);
        }
    }

    public String getId() { return getQuestSlot() + "|" + product; }
    public String getProductName() { return product; }
    public String getQuestSlot() { return single != null ? single.getQuestSlot() : multi.getQuestSlot(); }
    public int getBatchSize() { return single != null ? single.getBatchSize() : 1; }
    public int getOrderLimit() {
        int limit = single != null && single.getOrderLimit() > 0 ? Math.min(1000, single.getOrderLimit()) : 1000;
        if (!(SingletonRepository.getEntityManager().getItem(product, ItemCreationContext.restore()) instanceof StackableItem)) limit = 1;
        return limit / getBatchSize() * getBatchSize();
    }
    public int getProductionTime(int amount) { return single != null ? single.getProductionTime(amount) : multi.getProductionDuration(product, amount); }
    public Map<String, Integer> getResources() { return single != null ? single.getRecipeResources() : multi.getRecipeResources(product); }
    public int required(int perBatch, int amount) { return perBatch * (amount / getBatchSize()); }
    public boolean validAmount(int amount) { return amount >= getBatchSize() && amount <= getOrderLimit() && amount % getBatchSize() == 0; }
    public boolean hasOrder(Player player) { return player.hasQuest(getQuestSlot()) && !player.isQuestCompleted(getQuestSlot()); }
    public String getOrderName(Player player) { return player.getQuest(getQuestSlot()).split(";")[1]; }
    public int getOrderAmount(Player player) { return single != null ? single.getNumberOfProductItems(player) : multi.getNumberOfProductItems(player); }
    public boolean isReady(Player player) { return single != null ? single.isOrderReady(player) : multi.isOrderReady(player); }
    public int getOrderDuration(Player player) {
        return single != null ? single.getProductionTime(getOrderAmount(player)) : multi.getProductionDuration(getOrderName(player), getOrderAmount(player));
    }
    public long getRemainingMillis(Player player) { return single != null ? single.getRemainingTimeMillis(player) : multi.getRemainingTimeMillis(player); }
    public String unavailableReason(Player player) {
        if (prerequisite != null && !player.isQuestCompleted(prerequisite)) return "Najpierw ukończ zadanie dla tego rzemieślnika.";
        if (single != null && single.getCooldownRemainingMillis(player) > 0) return "Rzemieślnik potrzebuje przerwy przed kolejnym zamówieniem.";
        return "";
    }
    public boolean hasResources(Player player, int amount) {
        for (Map.Entry<String, Integer> resource : getResources().entrySet())
            if (player.getNumberOfEquipped(resource.getKey()) < (long) resource.getValue() * (amount / getBatchSize())) return false;
        return true;
    }
    public boolean start(SpeakerNPC npc, Player player, int amount) {
        ItemParserResult request = new ItemParserResult(true, product, amount, null);
        EventRaiser raiser = new EventRaiser(npc);
        return single != null ? single.transactAgreedDeal(request, raiser, player) : multi.transactAgreedDeal(request, raiser, player);
    }
    public void collect(SpeakerNPC npc, Player player) {
        if (single != null) single.giveProduct(new EventRaiser(npc), player); else multi.giveProduct(new EventRaiser(npc), player);
    }
}
