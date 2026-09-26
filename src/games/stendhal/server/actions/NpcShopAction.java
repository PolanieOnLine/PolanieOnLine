package games.stendhal.server.actions;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import games.stendhal.common.constants.Actions;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.NpcShopEvent;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/**
 * Otwiera okno sklepu i kieruje zamowienia do istniejacego dialogu handlarza.
 * Weryfikacja i finalizacja transakcji pozostaja w Buyer/SellerBehaviour.
 */
public final class NpcShopAction implements ActionListener {
    private static final long OFFER_LIFETIME_MS = 45000L;
    private static final Map<Player, PendingOffer> PENDING =
            Collections.synchronizedMap(new WeakHashMap<Player, PendingOffer>());
    private static final Map<Player, PendingOffer> CONFIRMING =
            Collections.synchronizedMap(new WeakHashMap<Player, PendingOffer>());
    private static final Map<Player, Boolean> RESULTS =
            Collections.synchronizedMap(new WeakHashMap<Player, Boolean>());

    private static final class PendingOffer {
        private final int npcId;
        private final String mode;
        private final String item;
        private final int quantity;
        private final int price;
        private final String token;
        private final long expiresAt;

        private PendingOffer(final SpeakerNPC npc, final String mode,
                final String item, final int quantity, final int price) {
            this.npcId = npc.getID().getObjectID();
            this.mode = mode;
            this.item = item;
            this.quantity = quantity;
            this.price = price;
            this.token = UUID.randomUUID().toString();
            this.expiresAt = System.currentTimeMillis() + OFFER_LIFETIME_MS;
        }
    }

    public static void register() {
        CommandCenter.register(Actions.NPC_SHOP, new NpcShopAction());
    }

    /** Uniewaznij oferte UI, jezeli gracz rozpoczal nowa wycene przez czat. */
    public static void invalidatePendingQuote(final Player player) {
        PENDING.remove(player);
    }

    /** Odbierz wynik z istniejacego mechanizmu kupna i sprzedazy. */
    public static void recordTransactionResult(final Player player,
            final SpeakerNPC npc, final boolean success) {
        final PendingOffer confirming = CONFIRMING.get(player);
        if (confirming != null
                && confirming.npcId == npc.getID().getObjectID()) {
            RESULTS.put(player, success);
        }
    }

    @Override
    public void onAction(final Player player, final RPAction action) {
        final SpeakerNPC npc = resolveMerchant(player, action);
        if (npc == null) {
            player.sendPrivateText("Nie ma tu tego handlarza.");
            return;
        }
        final String command = action.has("command") ? action.get("command") : "open";
        if ("close".equals(command)) {
            cancelOffer(player, npc);
            return;
        }
        if (!inRange(player, npc)) {
            PENDING.remove(player);
            player.sendPrivateText("Podejdź bliżej do handlarza.");
            return;
        }
        if (npc.getAttending() != null && npc.getAttending() != player) {
            player.sendPrivateText("Handlarz rozmawia teraz z innym graczem.");
            return;
        }
        if (npc.getAttending() == null) {
            npc.listenTo(player, "hi");
        }
        if (npc.getAttending() != player) {
            player.sendPrivateText("Nie możesz teraz handlowac z tym NPC.");
            return;
        }

        if ("open".equals(command)) {
            cancelOffer(player, npc);
            publish(player, npc, NpcShopEvent.OPEN, "", null);
        } else if ("refresh".equals(command)) {
            cancelOffer(player, npc);
            publish(player, npc, NpcShopEvent.REFRESH, "Oferta odświeżona.", null);
        } else if ("request".equals(command)) {
            request(player, npc, action);
        } else if ("confirm".equals(command)) {
            finish(player, npc, action, true);
        } else if ("cancel".equals(command)) {
            finish(player, npc, action, false);
        }
    }

    private void request(final Player player, final SpeakerNPC npc,
            final RPAction action) {
        cancelOffer(player, npc);
        if (npc.getEngine().getCurrentState() != ConversationStates.ATTENDING
                || !action.has("mode") || !action.has("item")
                || !action.has("quantity")) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Zakończ poprzednią rozmowę przed rozpoczeciem handlu.", null);
            return;
        }

        final String mode = action.get("mode");
        final String item = action.get("item");
        final int quantity;
        try {
            quantity = action.getInt("quantity");
        } catch (final RuntimeException e) {
            publish(player, npc, NpcShopEvent.REFRESH, "Nieprawidłowa ilość.", null);
            return;
        }
        if (quantity < 1 || quantity > 1000) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Możesz wybrać od 1 do 1000 sztuk.", null);
            return;
        }

        final MerchantsRegister registry = SingletonRepository.getMerchantsRegister();
        final SellerBehaviour seller = registry.getSellerFor(npc);
        final BuyerBehaviour buyer = registry.getBuyerFor(npc);
        final boolean purchasing = "buy".equals(mode);
        if (!purchasing && !"sell".equals(mode)) {
            return;
        }
        if ((purchasing && (seller == null || !seller.dealtItems().contains(item)))
                || (!purchasing && (buyer == null
                        || !buyer.dealtItems().contains(item)))) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Handlarz nie oferuje teraz tego przedmiotu.", null);
            return;
        }

        if (purchasing) {
            Item template = null;
            try {
                template = seller.getAskedItem(item, player);
            } catch (final RuntimeException e) {
                // Niektore oferty wymagaja specjalnego przedmiotu.
            }
            if (template == null) {
                publish(player, npc, NpcShopEvent.REFRESH,
                        "Przedmiot jest niedostępny.", null);
                return;
            }
            if (quantity > 1 && !(template instanceof StackableItem)) {
                publish(player, npc, NpcShopEvent.REFRESH,
                        "Ten przedmiot można kupować tylko pojedynczo.", null);
                return;
            }
        }

        final int price = calculatePrice(player, mode, item, quantity, seller, buyer);
        if (price <= 0) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Nie można ustalić ceny tego przedmiotu.", null);
            return;
        }

        npc.listenTo(player, (purchasing ? "buy " : "sell ")
                + quantity + " " + item);
        final ConversationStates expected = purchasing
                ? ConversationStates.BUY_PRICE_OFFERED
                : ConversationStates.SELL_PRICE_OFFERED;
        if (npc.getAttending() != player
                || npc.getEngine().getCurrentState() != expected) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Handlarz odrzucił zapytanie. Sprawdź jego odpowiedź.", null);
            return;
        }

        final PendingOffer pending = new PendingOffer(npc, mode, item, quantity, price);
        PENDING.put(player, pending);
        publish(player, npc, NpcShopEvent.OFFER,
                "Oferta gotowa. Potwierdź lub anuluj w oknie.",
                pending);
    }

    private void finish(final Player player, final SpeakerNPC npc,
            final RPAction action, final boolean confirm) {
        final PendingOffer pending = PENDING.get(player);
        if (pending == null || pending.npcId != npc.getID().getObjectID()
                || !action.has("request_token")
                || !pending.token.equals(action.get("request_token"))) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Oferta wygasła. Wybierz przedmiot ponownie.", null);
            return;
        }
        PENDING.remove(player);
        final ConversationStates expected = "buy".equals(pending.mode)
                ? ConversationStates.BUY_PRICE_OFFERED
                : ConversationStates.SELL_PRICE_OFFERED;
        if (npc.getAttending() != player
                || npc.getEngine().getCurrentState() != expected) {
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Oferta nie jest już aktualna.", null);
            return;
        }
        if (!confirm || System.currentTimeMillis() > pending.expiresAt) {
            npc.listenTo(player, "no");
            publish(player, npc, NpcShopEvent.REFRESH,
                    confirm ? "Oferta wygasła. Zapytaj o cene ponownie."
                            : "Anulowano transakcje.", null);
            return;
        }
        final MerchantsRegister registry = SingletonRepository.getMerchantsRegister();
        final int currentPrice = calculatePrice(player, pending.mode,
                pending.item, pending.quantity, registry.getSellerFor(npc),
                registry.getBuyerFor(npc));
        if (currentPrice != pending.price) {
            npc.listenTo(player, "no");
            publish(player, npc, NpcShopEvent.REFRESH,
                    "Cena uległa zmianie. Poproś o nową ofertę.", null);
            return;
        }
        // Ta sama transakcja, ktora jest wywolywana przez zwykla rozmowe.
        CONFIRMING.put(player, pending);
        try {
            npc.listenTo(player, "yes");
        } finally {
            CONFIRMING.remove(player);
        }
        final Boolean success = RESULTS.remove(player);
        publish(player, npc, NpcShopEvent.RESULT,
                success == null ? "Sprawdź odpowiedź handlarza."
                        : success.booleanValue() ? "Transakcja zakończona."
                                : "Transakcja nie powiodła się.", null);
    }

    private int calculatePrice(final Player player, final String mode,
            final String item, final int quantity,
            final SellerBehaviour seller, final BuyerBehaviour buyer) {
        if ("buy".equals(mode) && seller != null) {
            final long basic = (long) seller.getUnitPrice(item) * quantity;
            if (basic <= 0 || basic > Integer.MAX_VALUE) {
                return -1;
            }
            final double penalty = player.isBadBoy()
                    ? SellerBehaviour.BAD_BOY_BUYING_PENALTY : 1.0;
            final double total = basic * penalty;
            return total > Integer.MAX_VALUE ? -1 : (int) total;
        }
        if ("sell".equals(mode) && buyer != null) {
            return buyer.getCharge(new ItemParserResult(true, item, quantity,
                    Collections.<String>emptySet()), player);
        }
        return -1;
    }

    private void cancelOffer(final Player player, final SpeakerNPC npc) {
        final PendingOffer pending = PENDING.get(player);
        if (pending == null || pending.npcId != npc.getID().getObjectID()) {
            return;
        }
        PENDING.remove(player);
        if (npc.getAttending() == player
                && (npc.getEngine().getCurrentState() == ConversationStates.BUY_PRICE_OFFERED
                    || npc.getEngine().getCurrentState() == ConversationStates.SELL_PRICE_OFFERED)) {
            npc.listenTo(player, "no");
        }
    }

    private void publish(final Player player, final SpeakerNPC npc,
            final String phase, final String message, final PendingOffer offer) {
        final MerchantsRegister registry = SingletonRepository.getMerchantsRegister();
        player.addEvent(new NpcShopEvent(npc, player,
                registry.getSellerFor(npc), registry.getBuyerFor(npc),
                phase, message, offer == null ? null : offer.mode,
                offer == null ? null : offer.item,
                offer == null ? 0 : offer.quantity,
                offer == null ? 0 : offer.price,
                offer == null ? null : offer.token));
    }

    private SpeakerNPC resolveMerchant(final Player player,
            final RPAction action) {
        if (!action.has("npc_id") || player.getZone() == null) {
            return null;
        }
        try {
            final Entity entity = EntityHelper.entityFromZoneByID(
                    action.getInt("npc_id"), player.getZone());
            if (!(entity instanceof SpeakerNPC) || !entity.has("job_item_merchant")) {
                return null;
            }
            final SpeakerNPC npc = (SpeakerNPC) entity;
            final MerchantsRegister registry = SingletonRepository.getMerchantsRegister();
            return registry.getSellerFor(npc) != null || registry.getBuyerFor(npc) != null
                    ? npc : null;
        } catch (final RuntimeException e) {
            return null;
        }
    }

    private boolean inRange(final Player player, final SpeakerNPC npc) {
        final int range = npc.getPerceptionRange();
        return player.squaredDistance(npc) <= range * range;
    }
}
