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

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.economy.CommerceType;
import games.stendhal.server.core.economy.EconomyBalanceManager;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Represents the behaviour of a NPC who is able to buy items from a player.
 */
public class BuyerBehaviour extends MerchantBehaviour {
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
		int copperValue = getCharge(res, player);
		MoneyUtils.giveMoney(player, copperValue);
		EconomyBalanceManager.getInstance().recordMerchantTransaction(getMerchantName(), copperValue, CommerceType.NPC_BUYING);
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
		if (player.drop(res.getChosenItemName(), res.getAmount())) {
			payPlayer(res, player);
			updatePlayerTransactions(player, seller.getName(), res);
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
		player.incSoldForItem(res.getChosenItemName(), res.getAmount());
		player.incCommerceTransaction(merchant, getCharge(res, player), true);
	}
}
