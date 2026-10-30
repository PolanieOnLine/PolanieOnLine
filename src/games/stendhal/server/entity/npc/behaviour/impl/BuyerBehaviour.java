/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.impl;

import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.function.Supplier;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.rule.item.ItemValueService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/**
 * Represents the behaviour of a NPC who is able to buy items from a player.
 */
public class BuyerBehaviour extends MerchantBehaviour {
	private static final ThreadLocal<SaleSelection> SELECTION = new ThreadLocal<>();
	private static final class SaleSelection {
		private final Player player;
		private final Item item;
		SaleSelection(final Player player, final Item item) { this.player = player; this.item = item; }
	}

	/** Restricts the existing quote and transaction hooks to one server-selected bag item. */
	public static <T> T withSelectedItem(final Player player, final Item item, final Supplier<T> work) {
		final SaleSelection previous = SELECTION.get();
		SELECTION.set(new SaleSelection(player, item));
		try { return work.get(); }
		finally { if (previous == null) { SELECTION.remove(); } else { SELECTION.set(previous); } }
	}

	public int getChargeForItem(final Item item, final int quantity, final Player player) {
		return withSelectedItem(player, item, () -> getCharge(new ItemParserResult(true,
				item.getName(), quantity, Collections.<String>emptySet()), player));
	}

	protected List<Item> getSaleItems(final ItemParserResult request, final Player player) {
		final SaleSelection selection = SELECTION.get();
		if (selection == null || selection.player != player) {
			return player.getAllEquipped(request.getChosenItemName());
		}
		if (selection.item.getName().equals(request.getChosenItemName())
				&& request.getAmount() > 0 && request.getAmount() <= selection.item.getQuantity()) {
			for (final RPObject object : player.getSlot("bag")) {
				if (object == selection.item) { return Collections.singletonList(selection.item); }
			}
		}
		return Collections.emptyList();
	}
	/**
	 * Creates a new BuyerBehaviour with price list.
	 *
	 * @param priceList
	 *   List of item names and their prices.
	 */
	public BuyerBehaviour(final Map<String, Integer> priceList) {
		super(priceList);
	}

	/**
	 * Creates a new BuyerBehaviour with price list & price factor.
	 *
	 * @param priceList
	 *   List of item names and their prices.
	 * @param priceFactor
	 *   Skews prices of all items for this merchant.
	 */
	public BuyerBehaviour(final Map<String, Integer> priceList, final Float priceFactor) {
		super(priceList, priceFactor);
	}

	/** Allows specialised buyers to phrase their own offer without changing other shops. */
	public String getPriceOffer(final ItemParserResult res, final int price) {
		return "Dam ci " + MoneyUtils.formatPayment(price) + " za "
				+ Grammar.itemQuantityAccusative(res.getAmount(), res.getChosenItemName()) + ". Sprzedajesz?";
	}

	/** Reply when this buyer cannot quote the requested inventory. */
	public String getUnavailableOffer(final ItemParserResult res) {
		return "Nie mogę odkupić tych przedmiotów.";
	}

	/**
	 * Gives the money for the deal to the player. If the player can't carry the
	 * money, puts it on the ground.
	 *
	 * @param res
	 *
	 * @param player
	 *			The player who sells
	 */
	protected void payPlayer(ItemParserResult res, final Player player) {
		payPlayer(getCharge(res, player), player);
	}

	private void payPlayer(final int copperValue, final Player player) {
		MoneyUtils.giveMoney(player, copperValue);
	}

	/**
	 * Applies the saved value multiplier to the concrete instances which the
	 * normal drop operation will consume, in the same inventory order.
	 */
	@Override
	public int getCharge(final ItemParserResult res, final Player player) {
		if (res.getChosenItemName() == null || player == null) {
			return super.getCharge(res, player);
		}

		final int unitPrice = getUnitPrice(res.getChosenItemName());
		if (SELECTION.get() != null && getSaleItems(res, player).isEmpty()) { return 0; }
		if (unitPrice <= 0) {
			return super.getCharge(res, player);
		}
		int remaining = res.getAmount();
		long total = 0L;
		for (final Item item : getSaleItems(res, player)) {
			if (remaining <= 0) {
				break;
			}
			final int quantity = Math.min(remaining, item.getQuantity());
			final double multiplier = ItemValueService.getValueMultiplier(item);
			final long adjustedUnitPrice = Math.min(Integer.MAX_VALUE,
					Math.round(unitPrice * multiplier));
			if (quantity > 0 && adjustedUnitPrice
					> (Integer.MAX_VALUE - total) / quantity) {
				return Integer.MAX_VALUE;
			}
			total += quantity * adjustedUnitPrice;
			remaining -= quantity;
		}

		// Preserve the old quote behaviour when the player does not currently
		// carry all requested units; the transaction itself will still reject it.
		if (remaining > 0 && unitPrice > (Integer.MAX_VALUE - total) / remaining) {
			return Integer.MAX_VALUE;
		}
		total += (long) remaining * unitPrice;
		return total > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
	}

	/**
	 * Transacts the deal that is described in BehaviourResult.
	 *
	 * @param seller
	 *			The NPC who buys
	 * @param player
	 *			The player who sells
	 * @return true iff the transaction was successful, that is when the player
	 *		 has the item(s).
	 */
	@Override
	public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
		final int copperValue = getCharge(res, player);
		final SaleSelection selected = SELECTION.get();
		final boolean removed = selected != null && selected.player == player
				? copperValue > 0 && !getSaleItems(res, player).isEmpty() && player.drop(selected.item, res.getAmount())
				: player.drop(res.getChosenItemName(), res.getAmount());
		if (removed) {
			payPlayer(copperValue, player);
			updatePlayerTransactions(player, seller.getName(), res, copperValue);
			seller.say("Dziękuję! Oto twoje pieniądze.");
			return true;
		} else {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Przepraszam! Nie masz ");
			if (res.getAmount() == 1) {
				stringBuilder.append("żadnego");
			} else {
				stringBuilder.append("tyle");
			}

			stringBuilder.append(" ");
			stringBuilder.append(res.getChosenItemName());
			stringBuilder.append(".");
			seller.say(stringBuilder.toString());
			return false;
		}
	}

	/**
	 * Updates stored information about Player-NPC commerce transactions.
	 *
	 * @param player
	 *	 Player to be updated.
	 * @param merchant
	 *	 Name of merchant involved in transaction.
	 * @param res
	 *	 Information about the transaction.
	 */
	protected void updatePlayerTransactions(final Player player, final String merchant,
			final ItemParserResult res) {
		updatePlayerTransactions(player, merchant, res, getCharge(res, player));
	}

	private void updatePlayerTransactions(final Player player, final String merchant,
			final ItemParserResult res, final int copperValue) {
		player.incSoldForItem(res.getChosenItemName(), res.getAmount());
		player.incCommerceTransaction(merchant, copperValue, true);
	}
}
