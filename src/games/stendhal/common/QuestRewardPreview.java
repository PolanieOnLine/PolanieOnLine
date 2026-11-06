/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

		public Item(String name, String itemClass, String subclass, int amount,
				String rarity, String description, boolean bound) {
			this.name = name;
			this.itemClass = itemClass;
			this.subclass = subclass;
			this.amount = amount;
			this.rarity = rarity;
			this.description = description;
			this.bound = bound;
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
		for (Item item : items) {
			names.add(item.name);
			classes.add(item.itemClass);
			subclasses.add(item.subclass);
			amounts.add(Integer.toString(item.amount));
			rarities.add(item.rarity);
			descriptions.add(item.description);
			bindings.add(item.bound ? "1" : "0");
		}
		event.put("reward_names", names);
		event.put("reward_classes", classes);
		event.put("reward_subclasses", subclasses);
		event.put("reward_amounts", amounts);
		event.put("reward_rarities", rarities);
		event.put("reward_descriptions", descriptions);
		event.put("reward_bindings", bindings);
	}

	/** Old servers and non-quest details do not carry a reward section. */
	public static QuestRewardPreview read(RPEvent event) {
		if (!event.has("reward_xp")) {
			return null;
		}
		List<String> names = event.getList("reward_names");
		List<String> classes = event.getList("reward_classes");
		List<String> subclasses = event.getList("reward_subclasses");
		List<String> amounts = event.getList("reward_amounts");
		List<String> rarities = event.getList("reward_rarities");
		List<String> descriptions = event.getList("reward_descriptions");
		List<String> bindings = event.getList("reward_bindings");
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < names.size(); i++) {
			items.add(new Item(names.get(i), classes.get(i), subclasses.get(i),
					Integer.parseInt(amounts.get(i)), rarities.get(i),
					descriptions.get(i), "1".equals(bindings.get(i))));
		}
		return new QuestRewardPreview(event.getInt("reward_xp"),
				event.getInt("reward_coins"),
				event.getInt("reward_completed") != 0, items,
				event.getList("reward_notes"));
	}
}
