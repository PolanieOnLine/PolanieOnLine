/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.QuestRewardPreview;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemCreationContext.Source;
import games.stendhal.server.entity.item.Item;

/** Builds display metadata only; opening the journal never rolls a reward. */
public final class QuestRewards {
	private final int xp;
	private final int coins;
	private final List<QuestRewardPreview.Item> items = new ArrayList<>();
	private final List<String> notes = new ArrayList<>();

	public QuestRewards(int xp, int coins) {
		this.xp = xp;
		this.coins = coins;
	}

	public QuestRewards item(String name, int amount, ItemRarity rarity,
			boolean bound) {
		ItemCreationContext context = ItemCreationContext.restore();
		if (rarity != null) {
			context = ItemCreationContext.builder(Source.QUEST)
					.withQuestRarity(rarity)
					.randomizeModifiers(false)
					.generateAffixes(false)
					.build();
		}
		Item item = SingletonRepository.getEntityManager().getItem(name,
				context);
		items.add(new QuestRewardPreview.Item(name, item.getItemClass(),
				item.getItemSubclass(), amount,
				rarity == null ? "" : rarity.getId(), item.getDescription(),
				bound, item.getMap(ItemTooltip.ATTRIBUTE)));
		return this;
	}

	public QuestRewards note(String note) {
		notes.add(note);
		return this;
	}

	public QuestRewardPreview build() {
		return new QuestRewardPreview(xp, coins, false, items, notes);
	}
}
