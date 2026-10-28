package games.stendhal.server.core.rule.glyph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LogoutListener;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult.Status;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Guaranteed, single-step upgrades of an existing glyph. */
public final class GlyphUpgradeService implements LogoutListener {
    private static final Logger LOGGER = Logger.getLogger(GlyphUpgradeService.class);
    private static final Map<String, Map<String, Integer>> MATERIALS = materials();
    private static final GlyphUpgradeService INSTANCE = new GlyphUpgradeService();
    static { SingletonRepository.getLogoutNotifier().addListener(INSTANCE); }
    private final Map<String, Offer> offers = new ConcurrentHashMap<String, Offer>();

    public static GlyphUpgradeService getInstance() { return INSTANCE; }

    private static Map<String, Map<String, Integer>> materials() {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<String, Map<String, Integer>>();
        add(result, "glif daru Mokoszy", "polano", 1000, "serce olbrzyma", 100, "magia ziemi", 1000);
        add(result, "glif siły", "sztabka żelaza", 200, "węgiel", 500, "magia ziemi", 1000);
        add(result, "glif Peruna", "sztabka miedzi", 200, "magia wiatru", 1500, "rubin", 50);
        add(result, "glif czaszy", "sztabka platyny", 30, "obsydian", 60, "magia mroku", 1000);
        add(result, "glif tarczy", "sztabka żelaza", 250, "sztabka mithrilu", 40, "węgiel", 500);
        add(result, "glif Swaroga", "sztabka czarnego żelaza", 150, "magia płomieni", 1500, "węgiel drzewny", 1000);
        add(result, "glif Tytana", "sztabka żelaza", 200, "sztabka mithrilu", 60, "serce olbrzyma", 100);
        add(result, "glif Strzyboga", "polano", 1500, "sztabka miedzi", 100, "magia wiatru", 1500);
        add(result, "glif Jarowita", "polano", 1000, "sztabka złota", 60, "magia światła", 1000, "magia mroku", 1000);
        add(result, "glif kryzysu", "sztabka złota", 60, "ametyst", 60, "magia światła", 1500);
        add(result, "glif krwi", "sztabka czarnego żelaza", 150, "wampirza krew", 60, "magia mroku", 1500);
        add(result, "glif świętego dębu", "polano", 2000, "sztabka mithrilu", 60, "magia ziemi", 1500);
        add(result, "glif żywej wody", "sztabka srebra", 100, "magia deszczu", 2000, "cudowna krew", 20);
        add(result, "glif Rodzanic", "polano", 1500, "bursztyn", 75, "magia deszczu", 1500);
        add(result, "glif wichru", "polano", 1500, "sztabka platyny", 30, "pióro anioła", 75, "magia wiatru", 2000);
        add(result, "glif wytrwałości", "sztabka żelaza", 200, "sztabka mithrilu", 60, "serce olbrzyma", 100);
        add(result, "glif ogniska", "polano", 2000, "węgiel drzewny", 1000, "magia płomieni", 2000);
        add(result, "glif korzeni", "polano", 2000, "sztabka miedzi", 150, "magia ziemi", 1000, "magia deszczu", 1000);
        add(result, "glif zmory", "sztabka czarnego żelaza", 100, "wampirza krew", 75, "magia mroku", 2000);
        add(result, "glif Świętowita", "sztabka złota", 80, "sztabka platyny", 50, "diament", 40, "magia światła", 1500);
        add(result, "glif przodków", "polano", 2000, "sztabka mithrilu", 80, "serce olbrzyma", 100, "magia ziemi", 1500);
        return Collections.unmodifiableMap(result);
    }

    private static void add(Map<String, Map<String, Integer>> table, String name, Object... pairs) {
        Map<String, Integer> values = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < pairs.length; i += 2) values.put((String)pairs[i], (Integer)pairs[i + 1]);
        table.put(name, Collections.unmodifiableMap(values));
    }

    private boolean validTarget(Item item, int target) {
        return GlyphProgressionService.isUpgradeable(item) && target >= 2 && target <= 3;
    }

    public int getRequiredFragments(Item item, int targetLevel) {
        return !validTarget(item, targetLevel) ? 0 : targetLevel == 2 ? 24 : 60;
    }

    public long getRequiredCoins(Item item, int targetLevel) {
        return !validTarget(item, targetLevel) ? 0 : targetLevel == 2 ? 200000L : 500000L;
    }

    public Map<String, Integer> getRequiredMaterials(Item item, int targetLevel) {
        if (!validTarget(item, targetLevel)) return Collections.emptyMap();
        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        int multiplier = targetLevel == 3 ? 3 : 1;
        for (Map.Entry<String, Integer> entry : MATERIALS.get(item.getName()).entrySet()) {
            result.put(entry.getKey(), entry.getValue() * multiplier);
        }
        return Collections.unmodifiableMap(result);
    }

    public List<Item> findUpgradeCandidates(Player player) {
        List<Item> result = new ArrayList<Item>();
        if (player == null) return result;
        for (RPSlot slot : player.slots(Slots.CARRYING)) collect(player, slot, result);
        result.sort(Comparator.comparing(Item::getName).thenComparingInt(GlyphProgressionService::getLevel));
        return result;
    }

    private void collect(Player player, RPSlot slot, List<Item> result) {
        for (RPObject object : slot) {
            if (!(object instanceof Item)) continue;
            Item item = (Item)object;
            if (GlyphProgressionService.isUpgradeable(item) && GlyphProgressionService.getLevel(item) < 3
                    && ownedBy(player, item)) result.add(item);
            for (RPSlot child : item.slots()) collect(player, child, result);
        }
    }

    private static boolean ownedBy(Player player, Item item) {
        String owner = item.getBoundTo();
        return owner == null || owner.isEmpty() || owner.equals(player.getName());
    }

    private boolean available(Player player, Item item) {
        return player != null && item != null && GlyphProgressionService.isUpgradeable(item)
                && GlyphProgressionService.getLevel(item) < 3 && ownedBy(player, item)
                && player.getAllEquipped(item.getName()).stream().anyMatch(carried -> carried == item);
    }

    /** An unaffordable offer may be viewed; payment is checked on confirmation. */
    public boolean prepareUpgrade(Player player, Item item) {
        clearPending(player);
        if (!available(player, item)) return false;
        offers.put(player.getName(), new Offer(item));
        return true;
    }

    public void clearPending(Player player) {
        if (player != null) offers.remove(player.getName());
    }

    @Override public void onLoggedOut(Player player) { clearPending(player); }

    public Status getUpgradeStatus(Player player, Item item) {
        if (!GlyphProgressionService.isUpgradeable(item)) return Status.NOT_UPGRADEABLE;
        if (GlyphProgressionService.getLevel(item) >= 3) return Status.MAX_LEVEL;
        if (!available(player, item)) return Status.INVALID_ITEM;
        int target = GlyphProgressionService.getLevel(item) + 1;
        if (MoneyUtils.getTotalMoneyInCopper(player) < getRequiredCoins(item, target)) return Status.NOT_ENOUGH_MONEY;
        if (getResourceCount(player, "fragment glifu") < getRequiredFragments(item, target)) return Status.MISSING_RESOURCES;
        for (Map.Entry<String, Integer> material : getRequiredMaterials(item, target).entrySet()) {
            if (getResourceCount(player, material.getKey()) < material.getValue()) return Status.MISSING_RESOURCES;
        }
        return Status.READY;
    }

    public String getRequestToken(Player player, Item item) {
        Offer offer = offers.get(player.getName());
        return offer != null && offer.item == item && offer.matches(item) ? offer.token : null;
    }

    public boolean upgrade(Player player, Item item, int expectedLevel) {
        return upgrade(player, item, expectedLevel, player == null ? null : getRequestToken(player, item));
    }

    public boolean upgrade(Player player, Item item, int expectedLevel, String token) {
        if (player == null) return false;
        Offer offer = offers.remove(player.getName());
        if (offer == null || token == null || !offer.token.equals(token) || offer.item != item || offer.level != expectedLevel
                || !available(player, item) || GlyphProgressionService.getLevel(item) != expectedLevel
                || !offer.matches(item)) return false;
        int target = expectedLevel + 1;
        Map<String, Integer> required = new LinkedHashMap<String, Integer>(getRequiredMaterials(item, target));
        required.put("fragment glifu", getRequiredFragments(item, target));
        ResourcePayment payment = planPayment(player, required);
        long coins = getRequiredCoins(item, target);
        if (payment == null || MoneyUtils.getTotalMoneyInCopper(player) < coins) return false;
        if (!debitCoins(player, coins)) return false;
        if (!payment.consume(player)) {
            MoneyUtils.giveMoney(player, (int)coins);
            return false;
        }
        try {
            GlyphProgressionService.applyLevel(item, target);
        } catch (RuntimeException error) {
            GlyphProgressionService.applyLevel(item, expectedLevel);
            payment.restore(player);
            MoneyUtils.giveMoney(player, (int)coins);
            LOGGER.error("Unable to upgrade glyph " + item.getName(), error);
            return false;
        }
        if (player.getZone() != null) player.notifyWorldAboutChanges();
        try { new GameEvent(player.getName(), "upgraded-glyph", item.getName(), Integer.toString(target)).raise(); }
        catch (RuntimeException error) { LOGGER.warn("Unable to log glyph upgrade", error); }
        return true;
    }

    private boolean debitCoins(Player player, long amount) {
        long before = MoneyUtils.getTotalMoneyInCopper(player);
        try {
            if (MoneyUtils.removeMoney(player, amount)) return true;
        } catch (RuntimeException error) {
            LOGGER.error("Unable to debit glyph upgrade coins", error);
        }
        // MoneyUtils may have removed earlier stacks before a later debit fails.
        long removed = before - MoneyUtils.getTotalMoneyInCopper(player);
        if (removed > 0) MoneyUtils.giveMoney(player, Math.toIntExact(removed));
        return false;
    }

    private static boolean usableResource(Player player, Item item) {
        if (!item.isSubmittable() || !ownedBy(player, item) || item.getQuantity() <= 0) return false;
        if (item instanceof Glyph && GlyphProgressionService.getLevel(item) != 1) return false;
        return !"fragment glifu".equals(item.getName()) || item.getItemData() == null || item.getItemData().isEmpty();
    }

    public int getResourceCount(Player player, String name) {
        long count = 0;
        for (Item item : player.getAllEquipped(name)) {
            if (usableResource(player, item)) count += item.getQuantity();
        }
        return (int)Math.min(Integer.MAX_VALUE, count);
    }

    /** Original recipes accept only clean fragments and level-I glyph ingredients. */
    public boolean consumeResources(Player player, Map<String, Integer> required) {
        ResourcePayment payment = planPayment(player, required);
        return payment != null && payment.consume(player);
    }

    public boolean consumeResources(Player player, Map<String, Integer> required, BooleanSupplier reward) {
        ResourcePayment payment = planPayment(player, required);
        if (payment == null || !payment.consume(player)) return false;
        try {
            if (reward.getAsBoolean()) return true;
        } catch (RuntimeException error) {
            LOGGER.error("Unable to grant crafted glyph", error);
        }
        payment.restore(player);
        return false;
    }

    private ResourcePayment planPayment(Player player, Map<String, Integer> required) {
        List<Part> parts = new ArrayList<Part>();
        for (Map.Entry<String, Integer> entry : required.entrySet()) {
            int remaining = entry.getValue();
            if (remaining <= 0) return null;
            for (Item item : player.getAllEquipped(entry.getKey())) {
                if (!usableResource(player, item)) continue;
                int amount = Math.min(remaining, item.getQuantity());
                parts.add(new Part(item, amount));
                remaining -= amount;
                if (remaining == 0) break;
            }
            if (remaining != 0) return null;
        }
        return new ResourcePayment(player, parts);
    }

    private static final class Part {
        final Item item;
        final RPSlot slot;
        final int quantity;
        final int amount;
        Part(Item item, int amount) {
            this.item = item; this.slot = item.getContainerSlot();
            this.quantity = item.getQuantity(); this.amount = amount;
        }
    }

    private static final class ResourcePayment {
        final List<Part> parts;
        final int baseHP;
        final int hp;
        int consumed;
        ResourcePayment(Player player, List<Part> parts) {
            this.parts = parts; baseHP = player.getUncappedBaseHP(); hp = player.getHP();
        }
        boolean consume(Player player) {
            try {
                for (Part part : parts) {
                    consumed++;
                    if (!player.drop(part.item, part.amount)) { restore(player); return false; }
                }
                return true;
            } catch (RuntimeException error) {
                restore(player);
                LOGGER.error("Unable to consume glyph resources", error);
                return false;
            }
        }
        void restore(Player player) {
            for (int i = consumed - 1; i >= 0; i--) {
                Part part = parts.get(i);
                if (part.item instanceof StackableItem) ((StackableItem)part.item).setQuantity(part.quantity);
                if (!part.item.isContained()) part.slot.add(part.item);
            }
            player.setBaseHP(baseHP); player.setHP(hp); player.updateItemAtkDef();
            consumed = 0;
        }
    }

    private static final class Offer {
        final String token = UUID.randomUUID().toString();
        final Item item;
        final int level;
        final String boundTo;
        final long expires = System.currentTimeMillis() + 5L * 60L * 1000L;
        final List<RPObject> path = new ArrayList<RPObject>();
        final List<RPSlot> slots = new ArrayList<RPSlot>();
        Offer(Item item) {
            this.item = item; level = GlyphProgressionService.getLevel(item); boundTo = item.getBoundTo();
            for (RPObject object = item; object != null; object = object.getContainer()) {
                path.add(object); slots.add(object.getContainerSlot());
            }
        }
        boolean matches(Item item) {
            if (System.currentTimeMillis() >= expires || !Objects.equals(boundTo, item.getBoundTo())) return false;
            int index = 0;
            for (RPObject object = item; object != null; object = object.getContainer()) {
                if (index >= path.size() || path.get(index) != object || slots.get(index) != object.getContainerSlot()) return false;
                index++;
            }
            return index == path.size();
        }
    }
}
