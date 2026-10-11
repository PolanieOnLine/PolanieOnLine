package games.stendhal.server.entity.npc.behaviour.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.item.ItemValueService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.OwnedItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** A buyer whose catalogue and prices come from item definitions, not a shop list. */
public final class ValueBuyerBehaviour extends BuyerBehaviour {
	public ValueBuyerBehaviour(final Collection<DefaultItem> definitions) {
		super(createCatalogue(definitions));
	}

	private static Map<String, Integer> createCatalogue(final Collection<DefaultItem> definitions) {
		final Map<String, Integer> prices = new TreeMap<String, Integer>();
		for (final DefaultItem item : definitions) {
			if (item.getValue() > 0 && !item.isUnattainable()
					&& !"money".equals(item.getItemClass())
					&& !"key".equals(item.getItemClass())
					&& item.getEquipableSlots() != null && item.getEquipableSlots().contains("trade")
					&& (item.getAttributes() == null || !item.getAttributes().containsKey("autobind"))) {
				prices.put(item.getItemName(), Integer.valueOf(item.getValue()));
			}
		}
		return prices;
	}

	@Override
	public int getCharge(final ItemParserResult res, final Player player) {
		if (res == null || player == null || res.getAmount() <= 0 || res.getAmount() > 1000
				|| res.getChosenItemName() == null || !hasItem(res.getChosenItemName())) {
			return 0;
		}
		int remaining = res.getAmount();
		long total = 0;
		for (final Item item : player.getAllEquipped(res.getChosenItemName())) {
			if (remaining == 0) { break; }
			if (item.isBound() || item.has("autobind") || item instanceof OwnedItem
					|| !item.canBeEquippedIn("trade")) {
				return 0;
			}
			ItemValueService.refresh(item);
			if (item.getValue() <= 0) { return 0; }
			final int quantity = Math.min(remaining, item.getQuantity());
			total += (long) quantity * item.getValue();
			remaining -= quantity;
			if (total > Integer.MAX_VALUE) { return 0; }
		}
		if (remaining != 0) { return 0; }
		long balance = 0;
		for (final Item money : player.getAllEquipped("money")) {
			balance += money.getQuantity();
		}
		return balance + total > Integer.MAX_VALUE ? 0 : (int) total;
	}

	@Override
	public boolean transactAgreedDeal(final ItemParserResult res,
			final EventRaiser seller, final Player player) {
		if (getCharge(res, player) <= 0) {
			seller.say("Nie mogę kupić tych przedmiotów. Sprawdź ilość, przypisanie do postaci i miejsce na monety.");
			return false;
		}
		return super.transactAgreedDeal(res, seller, player);
	}
}
