/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

import marauroa.common.game.RPEvent;

/** Guaranteed quest rewards, without generated affixes or inventory objects. */
public final class QuestRewardPreview {
	public final int xp;
	public final int coins;
	public final boolean completed;
	public final List<Item> items;
	public final List<String> notes;

	public QuestRewardPreview(int xp, int coins, boolean completed,
			List<Item> items, List<String> notes) {
		this.xp = xp;
		this.coins = coins;
		this.completed = completed;
		this.items = Collections.unmodifiableList(new ArrayList<>(items));
		this.notes = Collections.unmodifiableList(new ArrayList<>(notes));
	}

	public static final class Item {
		public final String name;
		public final String itemClass;
		public final String subclass;
		public final int amount;
		public final String rarity;
		public final String description;
		public final boolean bound;
		public final Map<String, String> stats;

		public Item(String name, String itemClass, String subclass, int amount,
				String rarity, String description, boolean bound) {
			this(name, itemClass, subclass, amount, rarity, description, bound,
					Collections.emptyMap());
		}

		public Item(String name, String itemClass, String subclass, int amount,
				String rarity, String description, boolean bound,
				Map<String, String> stats) {
			this.name = name;
			this.itemClass = itemClass;
			this.subclass = subclass;
			this.amount = amount;
			this.rarity = rarity;
			this.description = description;
			this.bound = bound;
			this.stats = Collections.unmodifiableMap(
					new LinkedHashMap<>(stats));
		}

		/**
		 * Detached presentation data; it has no inventory identity or actions.
		 */
		public RPObject toRPObject() {
			RPObject object = new RPObject();
			object.setRPClass("item");
			object.put("name", name);
			object.put("class", itemClass);
			object.put("subclass", subclass);
			object.put("quantity", amount);
			if (!rarity.isEmpty()) {
				object.put("rarity_id", rarity);
			}
			for (Map.Entry<String, String> stat : stats.entrySet()) {
				object.put(ItemTooltip.ATTRIBUTE, stat.getKey(),
						stat.getValue());
			}
			return object;
		}
	}

	public void write(RPEvent event, boolean received) {
		event.put("reward_xp", xp);
		event.put("reward_coins", coins);
		event.put("reward_completed", received ? 1 : 0);
		event.put("reward_notes", notes);
		List<String> names = new ArrayList<>();
		List<String> classes = new ArrayList<>();
		List<String> subclasses = new ArrayList<>();
		List<String> amounts = new ArrayList<>();
		List<String> rarities = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		List<String> bindings = new ArrayList<>();
		List<String> stats = new ArrayList<>();
		for (Item item : items) {
			names.add(item.name);
			classes.add(item.itemClass);
			subclasses.add(item.subclass);
			amounts.add(Integer.toString(item.amount));
			rarities.add(item.rarity);
			descriptions.add(item.description);
			bindings.add(item.bound ? "1" : "0");
			stats.add(JSONValue.toJSONString(item.stats));
		}
		event.put("reward_names", names);
		event.put("reward_classes", classes);
		event.put("reward_subclasses", subclasses);
		event.put("reward_amounts", amounts);
		event.put("reward_rarities", rarities);
		event.put("reward_descriptions", descriptions);
		event.put("reward_bindings", bindings);
		event.put("reward_stats", stats);
	}

	/** Old servers and non-quest details do not carry a reward section. */
	public static QuestRewardPreview read(RPEvent event) {
		if (!event.has("reward_xp")) {
			return null;
		}
		List<String> names = readList(event, "reward_names");
		List<String> classes = readList(event, "reward_classes");
		List<String> subclasses = readList(event, "reward_subclasses");
		List<String> amounts = readList(event, "reward_amounts");
		List<String> rarities = readList(event, "reward_rarities");
		List<String> descriptions = readList(event, "reward_descriptions");
		List<String> bindings = readList(event, "reward_bindings");
		List<String> stats = readList(event, "reward_stats");
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			// Marauroa decodes an empty list as one empty string.
			if (names.get(i).isEmpty()) {
				continue;
			}
			items.add(new Item(names.get(i), classes.get(i), subclasses.get(i),
					Integer.parseInt(amounts.get(i)), rarities.get(i),
					descriptions.get(i), "1".equals(bindings.get(i)),
					readStats(i < stats.size() ? stats.get(i) : "")));
		}
		List<String> notes = new ArrayList<>(readList(event, "reward_notes"));
		notes.removeIf(String::isEmpty);
		return new QuestRewardPreview(event.getInt("reward_xp"),
				event.getInt("reward_coins"),
				event.getInt("reward_completed") != 0, items, notes);
	}

	private static Map<String, String> readStats(String json) {
		Object parsed = JSONValue.parse(json);
		Map<String, String> stats = new LinkedHashMap<>();
		if (parsed instanceof Map) {
			for (Map.Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
				if (entry.getKey() instanceof String
						&& entry.getValue() instanceof String) {
					stats.put((String) entry.getKey(),
							(String) entry.getValue());
				}
			}
		}
		return stats;
	}

	private static List<String> readList(RPEvent event, String key) {
		String value = event.get(key);
		if (value == null || value.length() < 2) {
			return Collections.emptyList();
		}
		// Keep trailing empty descriptions/rarities aligned with their item.
		return Arrays.asList(
				value.substring(1, value.length() - 1).split("\t", -1));
	}
}
