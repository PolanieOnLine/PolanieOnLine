package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.constants.Events;
import games.stendhal.server.actions.ProductionAction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
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
    }

    private ProductionEvent(int npcId, String window, String request, String phase, String status, String message) {
        super(Events.PRODUCTION);
        put("npc_id", npcId); put("window_id", window); put("request_id", request);
        put("phase", phase); put("status", status); put("message", message);
    }

    public static ProductionEvent rejected(int npcId, String window, String request, String status, String message) {
        return new ProductionEvent(npcId, window, request, "update", status, message);
    }

    public ProductionEvent(Player player, int worksiteId, SpeakerNPC npc, ProducerBehaviour producer, String window,
            String request, String phase, int amount, String status, String message) {
        this(worksiteId, window, request, phase, status, message);
        Item product = SingletonRepository.getEntityManager().getItem(producer.getProductName());
        put("npc_name", npc.getName()); put("name", product.getName());
        put("class", product.getItemClass()); put("subclass", product.getItemSubclass());
        put("amount", amount); put("max_amount", producer.getOrderLimit());
        put("duration", producer.getProductionTime(amount));
        boolean active = ProductionAction.hasOrder(player, producer);
        put("can_start", !active && ProductionAction.hasResources(player, producer, amount) ? 1 : 0);
        put("order_state", active ? producer.isOrderReady(player) ? "ready" : "working" : "idle");
        int count = active ? producer.getNumberOfProductItems(player) : 0;
        put("order_amount", count);
        put("order_duration", producer.getProductionTime(count));
        put("remaining", active ? (int) ((producer.getRemainingTimeMillis(player) + 999) / 1000) : 0);
        List<String> names = new ArrayList<>(), classes = new ArrayList<>(), subclasses = new ArrayList<>();
        List<String> required = new ArrayList<>(), owned = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : producer.getRecipeResources().entrySet()) {
            Item resource = SingletonRepository.getEntityManager().getItem(entry.getKey());
            names.add("money".equals(entry.getKey()) ? "monety" : entry.getKey());
            classes.add(resource.getItemClass()); subclasses.add(resource.getItemSubclass());
            required.add(Integer.toString(entry.getValue() * amount));
            owned.add(Integer.toString(player.getNumberOfEquipped(entry.getKey())));
        }
        put("material_names", names); put("material_classes", classes); put("material_subclasses", subclasses);
        put("material_values", required); put("owned_material_values", owned);
    }
}
