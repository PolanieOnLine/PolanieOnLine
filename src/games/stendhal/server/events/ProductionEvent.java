package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.ProductionRecipe;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/** Private snapshot of the player's recipe and persisted order. */
public final class ProductionEvent extends RPEvent {
    public static void generateRPClass() {
        RPClass rpclass = new RPClass(Events.PRODUCTION);
        for (String key : new String[] {"phase", "status", "window_id", "request_id", "npc_name", "name", "class", "subclass", "order_state"})
            rpclass.addAttribute(key, Type.STRING, Definition.PRIVATE);
        rpclass.addAttribute("message", Type.LONG_STRING, Definition.PRIVATE);
        for (String key : new String[] {"npc_id", "amount", "max_amount", "duration", "order_amount", "remaining", "order_duration", "can_start"})
            rpclass.addAttribute(key, Type.INT, Definition.PRIVATE);
        for (String key : new String[] {"material_names", "material_classes", "material_subclasses", "material_values", "owned_material_values"})
            rpclass.addAttribute(key, Type.VERY_LONG_STRING, Definition.PRIVATE);
        // Append definitions to preserve existing packet and character-save field codes.
        rpclass.addAttribute("selected_recipe", Type.STRING, Definition.PRIVATE);
        rpclass.addAttribute("order_name", Type.STRING, Definition.PRIVATE);
        rpclass.addAttribute("amount_step", Type.INT, Definition.PRIVATE);
        for (String key : new String[] {"recipe_ids", "recipe_names"}) rpclass.addAttribute(key, Type.VERY_LONG_STRING, Definition.PRIVATE);
    }

    private ProductionEvent(int npcId, String window, String request, String phase, String status, String message) {
        super(Events.PRODUCTION);
        put("npc_id", npcId); put("window_id", window); put("request_id", request);
        put("phase", phase); put("status", status); put("message", message);
    }

    public static ProductionEvent rejected(int npcId, String window, String request, String status, String message) {
        return new ProductionEvent(npcId, window, request, "update", status, message);
    }

    public ProductionEvent(Player player, int worksiteId, SpeakerNPC npc, ProductionRecipe producer, String window,
            String request, String phase, int amount, String status, String message) {
        this(worksiteId, window, request, phase, status, message);
        Item product = SingletonRepository.getEntityManager().getItem(producer.getProductName(), ItemCreationContext.restore());
        put("npc_name", npc.getName()); put("name", product.getName());
        put("class", product.getItemClass()); put("subclass", product.getItemSubclass());
        put("amount", amount); put("max_amount", producer.getOrderLimit());
        put("amount_step", producer.getBatchSize()); put("selected_recipe", producer.getId());
        List<String> recipeIds = new ArrayList<>(), recipeNames = new ArrayList<>();
        for (ProductionRecipe entry : ProductionRecipe.forNPC(npc)) {
            recipeIds.add(entry.getId()); recipeNames.add(entry.getProductName());
        }
        put("recipe_ids", recipeIds); put("recipe_names", recipeNames);
        put("duration", producer.getProductionTime(amount));
        boolean active = producer.hasOrder(player);
        String reason = producer.unavailableReason(player);
        put("can_start", !active && reason.isEmpty() && producer.validAmount(amount) && producer.hasResources(player, amount) ? 1 : 0);
        if (!active && "ok".equals(status) && message.isEmpty() && !reason.isEmpty()) {
            put("status", "unavailable"); put("message", reason);
        }
        put("order_state", active ? producer.isReady(player) ? "ready" : "working" : "idle");
        put("order_name", active ? producer.getOrderName(player) : producer.getProductName());
        int count = active ? producer.getOrderAmount(player) : 0;
        put("order_amount", count);
        put("order_duration", active ? producer.getOrderDuration(player) : 0);
        put("remaining", active ? (int) ((producer.getRemainingMillis(player) + 999) / 1000) : 0);
        List<String> names = new ArrayList<>(), classes = new ArrayList<>(), subclasses = new ArrayList<>();
        List<String> required = new ArrayList<>(), owned = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : producer.getResources().entrySet()) {
            Item resource = SingletonRepository.getEntityManager().getItem(entry.getKey(), ItemCreationContext.restore());
            names.add("money".equals(entry.getKey()) ? "monety" : entry.getKey());
            classes.add(resource.getItemClass()); subclasses.add(resource.getItemSubclass());
            required.add(Integer.toString(producer.required(entry.getValue(), amount)));
            owned.add(Integer.toString(player.getNumberOfEquipped(entry.getKey())));
        }
        put("material_names", names); put("material_classes", classes); put("material_subclasses", subclasses);
        put("material_values", required); put("owned_material_values", owned);
    }
}
