package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.json.simple.JSONValue;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.item.ItemTooltipService;
import marauroa.common.game.RPObject;

import games.stendhal.common.constants.Events;
import games.stendhal.common.constants.CurrencyReform;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.MerchantBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/**
 * Katalog i stan okna handlu. Serwer decyduje, jakie przedmioty i ceny
 * sa dostepne. Klient nie przesyla wlasnych cen.
 */
public final class NpcShopEvent extends RPEvent {
    public static final String OPEN = "open";
    public static final String REFRESH = "refresh";
    public static final String OFFER = "offer";
    public static final String RESULT = "result";

    public static void generateRPClass() {
        final RPClass type = new RPClass(Events.NPC_SHOP);
        attribute(type, "phase", Type.STRING);
        attribute(type, "npc_id", Type.INT);
        attribute(type, "npc_name", Type.STRING);
        attribute(type, "message", Type.LONG_STRING);
        attribute(type, "owned_money", Type.INT);
        attribute(type, "owned_money_text", Type.STRING);
        attribute(type, "sell_names", Type.VERY_LONG_STRING);
        attribute(type, "sell_prices", Type.VERY_LONG_STRING);
        attribute(type, "sell_classes", Type.VERY_LONG_STRING);
        attribute(type, "sell_subclasses", Type.VERY_LONG_STRING);
        attribute(type, "sell_stackable", Type.VERY_LONG_STRING);
        attribute(type, "buy_names", Type.VERY_LONG_STRING);
        attribute(type, "buy_prices", Type.VERY_LONG_STRING);
        attribute(type, "buy_classes", Type.VERY_LONG_STRING);
        attribute(type, "buy_subclasses", Type.VERY_LONG_STRING);
        attribute(type, "pending_mode", Type.STRING);
        attribute(type, "pending_item", Type.STRING);
        attribute(type, "pending_amount", Type.INT);
        attribute(type, "pending_price", Type.INT);
        attribute(type, "pending_price_text", Type.STRING);
        attribute(type, "request_token", Type.STRING);
        attribute(type, "sell_descriptions", Type.VERY_LONG_STRING);
        attribute(type, "buy_descriptions", Type.VERY_LONG_STRING);
        attribute(type, "sell_price_values", Type.VERY_LONG_STRING);
        attribute(type, "buy_price_values", Type.VERY_LONG_STRING);
        attribute(type, "currency_reformed", Type.INT);
        for (final String prefix : new String[] {"sell", "buy"}) {
            for (final String suffix : new String[] {"keys", "quantities", "items"}) {
                attribute(type, prefix + "_" + suffix, Type.VERY_LONG_STRING);
            }
        }
        attribute(type, "buy_stackable", Type.VERY_LONG_STRING);
        attribute(type, "window_id", Type.STRING);
        attribute(type, "request_id", Type.STRING);
        attribute(type, "owned_money_value", Type.STRING);
        attribute(type, "buying_penalty", Type.STRING);
    }

    private static void attribute(final RPClass type, final String name,
            final Type valueType) {
        type.addAttribute(name, valueType, Definition.PRIVATE);
    }

    public NpcShopEvent(final SpeakerNPC npc, final Player player,
            final SellerBehaviour seller, final BuyerBehaviour buyer,
            final String phase, final String message, final String mode,
            final String item, final int amount, final int price,
            final String requestToken, final Function<Item, String> saleKeys) {
        super(Events.NPC_SHOP);
        put("phase", phase);
        put("npc_id", npc.getID().getObjectID());
        put("npc_name", npc.getName());
        put("message", message == null ? "" : message);
        final long money = MoneyUtils.getTotalMoneyInCopper(player);
        put("owned_money", (int) Math.min(Integer.MAX_VALUE, money));
        put("owned_money_text", MoneyUtils.formatPrice(money));
        put("currency_reformed", CurrencyReform.useReformedCurrency() ? 1 : 0);
        put("owned_money_value", Long.toString(money));
        put("buying_penalty", player.isBadBoy() ? Double.toString(SellerBehaviour.BAD_BOY_BUYING_PENALTY) : "1");
        addItems("sell", seller, player, saleKeys);
        addItems("buy", buyer, player, saleKeys);
        if (requestToken != null) {
            put("request_token", requestToken);
            put("pending_mode", mode);
            put("pending_item", item);
            put("pending_amount", amount);
            put("pending_price", price);
            put("pending_price_text", MoneyUtils.formatPrice(price));
        }
    }

    private void addItems(final String prefix, final MerchantBehaviour merchant,
            final Player player, final Function<Item, String> saleKeys) {
        if (merchant == null) {
            return;
        }
        final List<String> names = new ArrayList<String>(merchant.dealtItems());
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        final List<String> offeredNames = new ArrayList<String>();
        final List<String> prices = new ArrayList<String>();
        final List<String> rawPrices = new ArrayList<String>();
        final List<String> descriptions = new ArrayList<String>();
        final List<String> classes = new ArrayList<String>();
        final List<String> subclasses = new ArrayList<String>();
        final List<String> stackable = new ArrayList<String>();
        final List<String> keys = new ArrayList<>();
        final List<String> quantities = new ArrayList<>();
        final List<String> snapshots = new ArrayList<>();
        final List<Item> candidates = new ArrayList<>();
        final Map<Item, String> offerNames = new IdentityHashMap<>();
        final Set<Item> unavailable = Collections.newSetFromMap(new IdentityHashMap<Item, Boolean>());
        if ("buy".equals(prefix)) {
            final Set<String> carriedOffers = new HashSet<>();
            for (final RPObject object : player.getSlot("bag")) {
                if (object instanceof Item && names.contains(((Item) object).getName())) {
                    final Item item = (Item) object;
                    if (((BuyerBehaviour) merchant).getChargeForItem(item, 1, player) > 0) {
                        candidates.add(item);
                        carriedOffers.add(item.getName());
                    }
                }
            }
            // Keep the complete buying catalogue visible even with an empty bag.
            // Only real, accepted bag instances receive a transaction key.
            for (final String name : names) {
                if (!carriedOffers.contains(name) && merchant.getUnitPrice(name) > 0) {
                    final Item template = SingletonRepository.getEntityManager().getItem(name, ItemRarity.COMMON);
                    if (template != null) {
                        candidates.add(template);
                        unavailable.add(template);
                    }
                }
            }
        } else {
            for (final String name : names) {
                try { final Item item = ((SellerBehaviour) merchant).getAskedItem(name, player); if (item != null) { candidates.add(item); offerNames.put(item, name); } }
                catch (final RuntimeException unavailableOffer) { /* Special offers may not be ordinary items. */ }
            }
        }
        final boolean buying = "buy".equals(prefix);
        for (final Item candidate : candidates) {
            final String name = buying ? candidate.getName() : offerNames.get(candidate);
            final boolean missing = unavailable.contains(candidate);
            final int price;
            if (buying) {
                price = missing ? merchant.getUnitPrice(name)
                        : ((BuyerBehaviour) merchant).getChargeForItem(candidate, 1, player);
            } else {
                price = (int) Math.min(Integer.MAX_VALUE, (long) (merchant.getUnitPrice(name)
                        * (player.isBadBoy() ? SellerBehaviour.BAD_BOY_BUYING_PENALTY : 1.0)));
            }
            if (price <= 0) {
                continue;
            }
            final Item template = candidate;
            if (buying) {
                keys.add(missing ? "unavailable:" + name : saleKeys.apply(candidate));
                quantities.add(Integer.toString(missing ? 0
                        : saleLimit((BuyerBehaviour) merchant, candidate, player)));
            } else {
                keys.add("");
                quantities.add(candidate instanceof StackableItem ? "1000" : "1");
            }
            snapshots.add(snapshot(candidate));
            offeredNames.add(name);
            prices.add(MoneyUtils.formatPrice(price));
            rawPrices.add(Integer.toString("sell".equals(prefix) ? merchant.getUnitPrice(name) : price));
            String description = template != null && template.hasDescription()
                    ? template.getDescription() : "";
            if (description == null) {
                description = "";
            }
            descriptions.add(description.replaceAll("\\s+", " ").trim());
            classes.add(template == null ? "" : template.getItemClass());
            subclasses.add(template == null ? "" : template.getItemSubclass());
            stackable.add(template instanceof StackableItem ? "1" : "0");
        }
        if (!offeredNames.isEmpty()) {
            put(prefix + "_names", offeredNames);
            put(prefix + "_prices", prices);
            put(prefix + "_price_values", rawPrices);
            put(prefix + "_descriptions", descriptions);
            put(prefix + "_classes", classes);
            put(prefix + "_subclasses", subclasses);
            put(prefix + "_stackable", stackable);
            put(prefix + "_keys", keys);
            put(prefix + "_quantities", quantities);
            put(prefix + "_items", snapshots);
        }
    }

    /** The merchant validates aggregate prices and any wallet limits, not just the unit quote. */
    private static int saleLimit(final BuyerBehaviour merchant, final Item item, final Player player) {
        int accepted = 1;
        int upper = Math.min(1000, item.getQuantity());
        while (accepted < upper) {
            final int amount = (accepted + upper + 1) / 2;
            if (merchant.getChargeForItem(item, amount, player) > 0) { accepted = amount; }
            else { upper = amount - 1; }
        }
        return accepted;
    }

    private static String snapshot(final Item source) {
        // Rebuilding tooltip maps on a bag item creates perception deletions.
        // Catalogue presentation must never modify the live inventory instance.
        final Item item = (Item) source.clone();
        ItemTooltipService.update(item);
        final Map<String, Object> snapshot = new LinkedHashMap<>();
        for (final String key : new String[] {"name", "class", "subclass", "rarity_id", "improve", "max_improves", "itemdata", "bound"}) {
            if (item.has(key)) { snapshot.put(key, item.get(key)); }
        }
        if (item.hasMap(ItemTooltip.ATTRIBUTE)) { snapshot.put(ItemTooltip.ATTRIBUTE, item.getMap(ItemTooltip.ATTRIBUTE)); }
        return JSONValue.toJSONString(snapshot);
    }
}
