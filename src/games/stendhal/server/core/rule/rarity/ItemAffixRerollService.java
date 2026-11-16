/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;

import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.StatusResistantItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** One paid roll, one durable offer; closing a window never rolls again. */
public final class ItemAffixRerollService {
	public static final String ATTRIBUTE = "affix_reroll";
	public static final String STONE = "kamień przemiany";
	private static final ItemAffixRerollService INSTANCE = new ItemAffixRerollService(new Random());
	private static final Set<String> VOLATILE = new HashSet<String>(Arrays.asList("id", "zoneid", "x", "y"));
	private final Random random;
	private final Map<Player, Selection> selections = new WeakHashMap<Player, Selection>();

	public ItemAffixRerollService(final Random random) {
		this.random = random;
	}

	public static ItemAffixRerollService getInstance() { return INSTANCE; }

	/** Only carried equipment with genuine, known rolled affixes is offered. */
	public List<Item> candidates(final Player player) {
		final List<Item> result = new ArrayList<Item>();
		for (final RPSlot slot : player.slots(Slots.CARRYING)) collect(slot, result, false);
		result.removeIf(item -> !owned(player, item));
		Collections.sort(result, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
		return result;
	}

	private void collect(final RPSlot slot, final List<Item> result, final boolean stones) {
		for (final RPObject object : slot) {
			if (!(object instanceof Item)) continue;
			final Item item = (Item) object;
			if (stones ? isStone(item) : !affixes(item).isEmpty()) result.add(item);
			for (final RPSlot nested : item.slots()) collect(nested, result, stones);
		}
	}

	public List<String> affixes(final Item item) {
		final List<String> result = new ArrayList<String>();
		if (item == null || item instanceof StackableItem || "missile".equals(item.getItemClass())) return result;
		final String locked = state(item).get("locked");
		for (final String id : ItemAffixState.getValues(item).keySet()) {
			if (definition(id) != null && (locked == null || locked.equals(id))) result.add(id);
		}
		return result;
	}

	public int stones(final Player player) {
		long count = 0;
		for (final Item item : stoneStacks(player)) count += item.getQuantity();
		return (int) Math.min(Integer.MAX_VALUE, count);
	}

	private List<Item> stoneStacks(final Player player) {
		final List<Item> result = new ArrayList<Item>();
		for (final RPSlot slot : player.slots(Slots.CARRYING)) collect(slot, result, true);
		return result;
	}

	private boolean isStone(final Item item) {
		return item instanceof StackableItem && STONE.equals(item.getName())
				&& "resource".equals(item.getItemClass()) && "transformation_stone".equals(item.getItemSubclass());
	}

	public synchronized void clearSelection(final Player player) { selections.remove(player); }

	/** Selection tokens bind the exact instance, slot and current state. */
	public synchronized String select(final Player player, final Item item, final String id) {
		selections.remove(player);
		if (!owned(player, item) || !affixes(item).contains(id) || hasPending(item)) return "";
		final Selection selection = new Selection(item, id, location(item), fingerprint(item));
		selections.put(player, selection);
		return selection.token;
	}

	public synchronized String selectionToken(final Player player) {
		final Selection value = selections.get(player);
		return value == null ? "" : value.token;
	}

	/** Returns a status; all validation and random work precede payment. */
	public synchronized String roll(final Player player, final Item item, final String id, final String token) {
		if (!owned(player, item)) return "INVALID_ITEM";
		if (hasPending(item)) return validPending(player, item) ? "OFFER" : "CHANGED_ITEM";
		final Selection selected = selections.get(player);
		if (selected == null || selected.item.get() != item || !selected.id.equals(id)
				|| !selected.token.equals(token) || !selected.location.equals(location(item))
				|| !selected.fingerprint.equals(fingerprint(item)) || !affixes(item).contains(id)) return "INVALID_REQUEST";
		final List<Item> stones = stoneStacks(player);
		if (stones.isEmpty()) return "MISSING_STONE";
		final Item replacement = replacement(item, id);
		if (replacement == null) return "NO_REPLACEMENT";
		final Map<String, String> before = state(item);
		final Map<String, String> offer = new LinkedHashMap<String, String>(before);
		final String newId = changedId(item, replacement, id);
		offer.put("locked", id);
		offer.put("owner", player.getName());
		offer.put("token", UUID.randomUUID().toString());
		offer.put("old_id", id);
		offer.put("old_value", ItemAffixState.getValues(item).get(id));
		offer.put("new_id", newId);
		offer.put("new_value", ItemAffixState.getValues(replacement).get(newId));
		offer.put("location", location(item));
		offer.put("fingerprint", fingerprint(item));
		for (final String key : scalarKeys(item, replacement)) {
			if (sameAttribute(item, replacement, key)) continue;
			offer.put("patch." + key, replacement.has(key) ? replacement.get(key) : "");
		}
		// Both changes are to the same owning player's persisted object tree.
		// No callbacks, creation or random operation occur after payment.
		final StackableItem stone = (StackableItem) stones.get(0);
		if (stone.getQuantity() > 1) stone.setQuantity(stone.getQuantity() - 1);
		else stone.getContainerSlot().remove(stone.getID());
		replaceState(item, offer);
		selections.remove(player);
		if (player.getZone() != null) player.notifyWorldAboutChanges();
		return "OFFER";
	}

	public synchronized String finish(final Player player, final Item item, final String token, final boolean accept) {
		if (!owned(player, item) || !hasPending(item)) return "INVALID_REQUEST";
		final Map<String, String> offer = state(item);
		if (!token.equals(offer.get("token"))) return "INVALID_REQUEST";
		if (accept && !validPending(player, item)) return "CHANGED_ITEM";
		if (accept) {
			for (final Map.Entry<String, String> entry : offer.entrySet()) {
				if (!entry.getKey().startsWith("patch.")) continue;
				final String attribute = entry.getKey().substring(6);
				if (entry.getValue().isEmpty()) { if (item.has(attribute)) item.remove(attribute); }
				else item.put(attribute, entry.getValue());
			}
			item.remove(ItemAffixState.ATTRIBUTE, offer.get("old_id"));
			item.put(ItemAffixState.ATTRIBUTE, offer.get("new_id"), offer.get("new_value"));
		}
		final Map<String, String> completed = new LinkedHashMap<String, String>();
		completed.put("locked", accept ? offer.get("new_id") : offer.get("locked"));
		replaceState(item, completed);
		ItemTooltipService.update(item);
		if (accept) player.updateItemAtkDef();
		if (player.getZone() != null) player.notifyWorldAboutChanges();
		return accept ? "ACCEPTED" : "KEPT";
	}

	public boolean hasPending(final Item item) { return state(item).containsKey("token"); }

	public boolean validPending(final Player player, final Item item) {
		final Map<String, String> offer = state(item);
		return owned(player, item) && hasPending(item) && player.getName().equals(offer.get("owner"))
				&& location(item).equals(offer.get("location")) && fingerprint(item).equals(offer.get("fingerprint"));
	}

	public Item pending(final Player player) {
		for (final Item item : candidates(player)) if (hasPending(item)) return item;
		return null;
	}

	public static Map<String, String> state(final Item item) {
		return item != null && item.hasMap(ATTRIBUTE) ? new LinkedHashMap<String, String>(item.getMap(ATTRIBUTE))
				: new LinkedHashMap<String, String>();
	}

	public static void replaceState(final Item item, final Map<String, String> values) {
		final Map<String, String> snapshot = new LinkedHashMap<String, String>(values);
		if (item.hasMap(ATTRIBUTE)) for (final String key : new HashSet<String>(item.getMap(ATTRIBUTE).keySet())) item.remove(ATTRIBUTE, key);
		for (final Map.Entry<String, String> entry : snapshot.entrySet()) item.put(ATTRIBUTE, entry.getKey(), entry.getValue());
	}

	/** A transformation carries only the permanent slot lock, never a paid patch. */
	public static void transferLock(final Item target, final String locked) {
		final Map<String, String> values = new HashMap<String, String>();
		if (locked != null && !locked.isEmpty()) values.put("locked", ItemAffixState.has(target, locked) ? locked : "retired");
		replaceState(target, values);
	}

	public static ItemAffixDefinition definition(final String id) {
		final ItemAffixDefinition value = ItemAffixRegistry.getInstance().get(id);
		return value != null ? value : LegendaryItemAffixRegistry.getInstance().get(id);
	}

	/** Uniform weighting matches the existing affix generator. Eligibility is rechecked on the stripped copy. */
	Item replacement(final Item item, final String id) {
		final Item stripped = copy(item);
		remove(stripped, id);
		final List<ItemAffixDefinition> pool = new ArrayList<ItemAffixDefinition>(
				ItemAffixRegistry.getInstance().get(id) != null ? ItemAffixRegistry.getInstance().getDefinitions()
						: LegendaryItemAffixRegistry.getInstance().getDefinitions());
		Collections.shuffle(pool, random);
		for (final ItemAffixDefinition candidate : pool) {
			if (ItemAffixState.has(stripped, candidate.getId()) || !candidate.isEligible(stripped)) continue;
			final Item result = copy(stripped);
			if (!candidate.apply(result, random)) continue;
			ItemAffixState.record(result, candidate);
			return result;
		}
		return null;
	}

	private static Item copy(final Item item) {
		return item instanceof StatusResistantItem
				? new StatusResistantItem((StatusResistantItem) item) : new Item(item);
	}

	private String changedId(final Item old, final Item replacement, final String id) {
		for (final String current : ItemAffixState.getValues(replacement).keySet()) if (id.equals(current) || !ItemAffixState.has(old, current)) return current;
		throw new IllegalStateException("Missing rerolled affix");
	}

	/** Removes just this roll, retaining the item's own base statistic. */
	static void remove(final Item item, final String id) {
		final ItemAffixDefinition definition = definition(id);
		if (definition == null || !ItemAffixState.has(item, id)) throw new IllegalArgumentException("Unknown rolled affix");
		final String value = ItemAffixState.getValues(item).get(id);
		item.remove(ItemAffixState.ATTRIBUTE, id);
		if (!definition.hasMaterializedAttribute()) return;
		if (id.equals(EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE) || id.equals(LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE)) subtract(item, "atk", Double.parseDouble(value));
		if (id.equals(EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE) || id.equals(LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE)) subtract(item, "def", Double.parseDouble(value));
		if (id.equals(WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE)) subtract(item, "range", LegendaryWeaponAffixService.MERCILESS_REACH_RANGE_BONUS);
		if ("lifesteal".equals(id) && item.getDefinitionLifesteal() > 0.0) item.put("lifesteal", item.getDefinitionLifesteal());
		else if (item.has(definition.getAttribute())) item.remove(definition.getAttribute());
	}

	private static void subtract(final Item item, final String key, final double amount) {
		if (item.has(key)) item.put(key, (int) Math.round(item.getDouble(key) - amount));
	}

	private static Set<String> scalarKeys(final Item a, final Item b) {
		final Set<String> keys = new HashSet<String>();
		for (final String key : a) keys.add(key);
		for (final String key : b) keys.add(key);
		return keys;
	}

	private static boolean sameAttribute(final Item a, final Item b, final String key) {
		return a.has(key) == b.has(key) && (!a.has(key) || a.get(key).equals(b.get(key)));
	}

	static String fingerprint(final Item item) {
		final Map<String, String> values = new TreeMap<String, String>();
		for (final String key : item) if (!VOLATILE.contains(key)) values.put(key, item.get(key));
		values.put("affixes", new TreeMap<String, String>(ItemAffixState.getValues(item)).toString());
		values.put("rarity", new TreeMap<String, Double>(item.getRarityModifiers()).toString());
		try {
			final byte[] bytes = MessageDigest.getInstance("SHA-256").digest(values.toString().getBytes(StandardCharsets.UTF_8));
			return java.util.Base64.getEncoder().encodeToString(bytes);
		} catch (final NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
	}

	private static boolean owned(final Player player, final Item item) {
		if (item == null) return false;
		RPObject root = item;
		while (root.getContainer() != null) root = root.getContainer();
		return root == player && (!item.isBound() || player.isBoundTo(item));
	}

	/** Stable across session object IDs, but distinguishes bags using their durable identity. */
	private static String location(final Item item) {
		final List<String> parts = new ArrayList<String>();
		RPObject current = item;
		while (current.getContainer() != null) {
			parts.add(current.getContainerSlot().getName());
			if (current.getContainer() instanceof Item) {
				final Item bag = (Item) current.getContainer();
				final Map<String, String> identity = state(bag);
				if (!identity.containsKey("identity")) {
					identity.put("identity", UUID.randomUUID().toString()); replaceState(bag, identity);
				}
				parts.add(identity.get("identity"));
			}
			current = current.getContainer();
		}
		return parts.toString();
	}

	private static final class Selection {
		final WeakReference<Item> item;
		final String id, location, fingerprint, token = UUID.randomUUID().toString();
		Selection(final Item item, final String id, final String location, final String fingerprint) {
			this.item = new WeakReference<Item>(item); this.id = id; this.location = location; this.fingerprint = fingerprint;
		}
	}
}
