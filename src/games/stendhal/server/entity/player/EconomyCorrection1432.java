package games.stendhal.server.entity.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.CurrencyReform;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Owner-approved, one-time correction of the 18-19 October 2026 trade prices.
 * Quantities come from the supplied chat export and are an administrative
 * settlement, not a claim that chat alone proves the original NPC payouts.
 * Prices are frozen here: later shop changes must not change this settlement.
 */
public final class EconomyCorrection1432 {
    public static final String RECORD = "economy_correction_1432";
    private static final Logger LOGGER = Logger.getLogger(EconomyCorrection1432.class);

    private EconomyCorrection1432() { }

    private static long correction(final String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
        case "mroczny":
            return 20532L * (199 - 49) + 2575L * (399 - 99)
                    + 11000L * (1174 - 293) + 5727L * (48000 - 586)
                    + 24L * (64000 - 1099);
        case "zed destiny":
            return 1000L * (399 - 99) + 12500L * (48000 - 586)
                    + 2165000L * (20 - 5); // Onion profit, not the later purchase-price increase.
        case "otylka":
            return 372L * (1174 - 293);
        default:
            return 0;
        }
    }

    /** Called after all saved inventory and bank slots have been restored.
     * Coins and the completion record are persisted in the same player object.
     * A shortfall is forgiven, never carried forward as debt.
     */
    public static void onLogin(final Player player) {
        if (player == null || player.has(RECORD, "complete") || player.hasQuest(RECORD)) return;
        final long nominal = correction(player.getName());
        if (nominal == 0) return;

        final List<Debit> coins = new ArrayList<Debit>();
        final List<Debit> changed = new ArrayList<Debit>();
        StackableItem change = null;
        RPSlot changeSlot = null;
        try {
            collect(player, coins);
            coins.sort(Comparator.comparingInt(debit -> debit.unit));
            long before = 0;
            for (Debit debit : coins) before = Math.addExact(before, debit.value());
            final long collected = Math.min(before, nominal);
            long remaining = collected;
            for (Debit debit : coins) {
                if (remaining == 0) break;
                final long taken = Math.min(remaining, debit.value());
                final long left = debit.value() - taken;
                if (left % debit.unit == 0) {
                    debit.after = (int) (left / debit.unit);
                } else if (left <= Integer.MAX_VALUE) {
                    // Replace this stack in its own slot, even if every bag is full.
                    debit.replacement = changeCoins((int) left);
                    debit.after = 0;
                } else {
                    debit.after = (int) (left / debit.unit);
                    change = changeCoins((int) (left % debit.unit));
                }
                changed.add(debit);
                remaining -= taken;
            }
            if (change != null) {
                changeSlot = findChangeSlot(player, coins, changed);
                if (changeSlot == null) throw new IllegalStateException("No safe slot for currency change");
            }

            // Everything that can be validated or allocated is done before any debit.
            final Map<String, String> record = new LinkedHashMap<String, String>();
            record.put("nominal", Long.toString(nominal));
            record.put("before", Long.toString(before));
            record.put("collected", Long.toString(collected));
            record.put("after", Long.toString(before - collected));
            record.put("forgiven", Long.toString(nominal - collected));
            record.put("time", Long.toString(System.currentTimeMillis()));
            record.put("complete", "1");
            for (Debit debit : changed) {
                if (debit.after == 0) {
                    debit.slot.remove(debit.item.getID());
                    if (debit.replacement != null) debit.slot.add(debit.replacement);
                } else {
                    debit.item.setQuantity(debit.after);
                }
            }
            if (change != null) changeSlot.add(change);
            for (Map.Entry<String, String> entry : record.entrySet()) {
                player.put(RECORD, entry.getKey(), entry.getValue());
            }
            // Existing releases retain arbitrary quest slots. This backup marker
            // prevents a second debit after an older server drops a new audit map.
            player.setQuest(RECORD, "done;" + collected + ";" + (nominal - collected));
        } catch (RuntimeException failure) {
            // Do not leave a partial debit or a completion marker on failure.
            if (change != null && change.isContained()) change.getContainerSlot().remove(change.getID());
            for (Debit debit : changed) {
                if (debit.replacement != null && debit.replacement.isContained()) {
                    debit.slot.remove(debit.replacement.getID());
                }
                debit.item.setQuantity(debit.before);
                if (!debit.item.isContained()) debit.slot.addPreservingId(debit.item);
            }
            if (player.hasMap(RECORD)) {
                // removeMap only queues deletion in this Marauroa version; it
                // leaves hasMap true until the next update. Clear keys instead.
                for (String key : new ArrayList<String>(player.getMap(RECORD).keySet())) {
                    player.remove(RECORD, key);
                }
            }
            if (player.hasQuest(RECORD)) player.removeQuest(RECORD);
            LOGGER.error("Economy correction 1.43.2 deferred for " + player.getName(), failure);
            return;
        }

        // Logging failure must not undo an otherwise completed settlement.
        LOGGER.info("Economy correction 1.43.2 for " + player.getName() + ": " + player.getMap(RECORD));
        try {
            new GameEvent(player.getName(), "economy correction 1.43.2", "apply",
                    "nominal=" + nominal + " collected=" + player.get(RECORD, "collected")
                    + " forgiven=" + player.get(RECORD, "forgiven")
                    + " before=" + player.get(RECORD, "before")
                    + " after=" + player.get(RECORD, "after")).raise();
        } catch (RuntimeException failure) {
            LOGGER.error("Cannot queue correction audit event for " + player.getName(), failure);
        }
    }

    private static void collect(final RPObject owner, final List<Debit> coins) {
        for (RPSlot slot : owner.slots()) {
            for (RPObject object : slot) {
                final int unit = object.has("name") ? unit(object.get("name")) : 0;
                if (unit != 0) {
                    if (!(object instanceof StackableItem) || ((StackableItem) object).getQuantity() <= 0) {
                        throw new IllegalStateException("Currency was not restored correctly");
                    }
                    for (RPSlot child : object.slots()) {
                        if (!child.isEmpty()) throw new IllegalStateException("Currency contains other items");
                    }
                    coins.add(new Debit((StackableItem) object, slot, unit));
                }
                collect(object, coins);
            }
        }
    }

    private static int unit(final String name) {
        if ("money".equals(name) || "miedziak".equals(name)) return 1;
        if ("talar".equals(name)) return 100;
        if ("dukat".equals(name)) return 10000;
        return 0;
    }

    private static StackableItem changeCoins(final int quantity) {
        final String name = CurrencyReform.useReformedCurrency() ? "miedziak" : "money";
        final StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(name);
        item.setQuantity(quantity);
        return item;
    }

    private static RPSlot findChangeSlot(final Player player, final List<Debit> coins,
            final List<Debit> changed) {
        final List<RPSlot> candidates = new ArrayList<RPSlot>();
        candidates.add(player.getSlot("bag"));
        for (Debit debit : coins) if (!candidates.contains(debit.slot)) candidates.add(debit.slot);
        for (RPSlot slot : candidates) {
            if (!slot.isFull()) return slot;
            for (Debit debit : changed) {
                if (debit.slot == slot && debit.after == 0 && debit.replacement == null) return slot;
            }
        }
        return null;
    }

    private static final class Debit {
        private final StackableItem item;
        private final RPSlot slot;
        private final int unit;
        private final int before;
        private int after;
        private StackableItem replacement;

        private Debit(final StackableItem item, final RPSlot slot, final int unit) {
            this.item = item;
            this.slot = slot;
            this.unit = unit;
            before = item.getQuantity();
            after = before;
        }

        private long value() { return (long) before * unit; }
    }
}
