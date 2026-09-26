package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.constants.Events;
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
    }

    private static void attribute(final RPClass type, final String name,
            final Type valueType) {
        type.addAttribute(name, valueType, Definition.PRIVATE);
    }

    public NpcShopEvent(final SpeakerNPC npc, final Player player,
            final SellerBehaviour seller, final BuyerBehaviour buyer,
            final String phase, final String message, final String mode,
            final String item, final int amount, final int price,
            final String requestToken) {
        super(Events.NPC_SHOP);
        put("phase", phase);
        put("npc_id", npc.getID().getObjectID());
        put("npc_name", npc.getName());
        put("message", message == null ? "" : message);
        final int money = MoneyUtils.getTotalMoneyInCopper(player);
        put("owned_money", money);
        put("owned_money_text", MoneyUtils.formatPrice(money));
        addItems("sell", seller);
        addItems("buy", buyer);
        if (requestToken != null) {
            put("request_token", requestToken);
            put("pending_mode", mode);
            put("pending_item", item);
            put("pending_amount", amount);
            put("pending_price", price);
            put("pending_price_text", MoneyUtils.formatPrice(price));
        }
    }

    private void addItems(final String prefix, final MerchantBehaviour merchant) {
        if (merchant == null) {
            return;
        }
        final List<String> names = new ArrayList<String>(merchant.dealtItems());
        Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
        final List<String> offeredNames = new ArrayList<String>();
        final List<String> prices = new ArrayList<String>();
        final List<String> classes = new ArrayList<String>();
        final List<String> subclasses = new ArrayList<String>();
        final List<String> stackable = new ArrayList<String>();
        for (final String name : names) {
            final int price = merchant.getUnitPrice(name);
            if (price < 0) {
                continue;
            }
            Item template = null;
            try {
                template = "sell".equals(prefix) && merchant instanceof SellerBehaviour
                        ? ((SellerBehaviour) merchant).getAskedItem(name)
                        : SingletonRepository.getEntityManager()
                                .getItem(name, ItemRarity.COMMON);
            } catch (final RuntimeException e) {
                // Nietypowe oferty (np. zwierzeta) moga nie miec ikony przedmiotu.
            }
            offeredNames.add(name);
            prices.add(MoneyUtils.formatPrice(price));
            classes.add(template == null ? "" : template.getItemClass());
            subclasses.add(template == null ? "" : template.getItemSubclass());
            stackable.add(template instanceof StackableItem ? "1" : "0");
        }
        if (!offeredNames.isEmpty()) {
            put(prefix + "_names", offeredNames);
            put(prefix + "_prices", prices);
            put(prefix + "_classes", classes);
            put(prefix + "_subclasses", subclasses);
            if ("sell".equals(prefix)) {
                put("sell_stackable", stackable);
            }
        }
    }
}
