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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.player.Player;

/**
 * Represents the behaviour of a NPC who is able to sell items to a player.
 */
public class SellerBehaviour extends MerchantBehaviour {

	/** the logger instance. */
	private static final Logger logger = Logger.getLogger(SellerBehaviour.class);

	/** the factor extra that player killers pay for items. should be > 1 always */
	public static final double BAD_BOY_BUYING_PENALTY = 1.5;

	/**
	 * Creates a new SellerBehaviour with an empty pricelist.
	*/
	public SellerBehaviour() {
		this(new HashMap<String, Integer>());
	}

	/**
	 * Creates a new SellerBehaviour with a pricelist.
	 *
	 * @param priceList
	 *			list of item names and their prices
	*/
	public SellerBehaviour(final Map<String, Integer> priceList) {
		super(priceList);
		setShopType(resolveShopType(priceList, ShopType.ITEM_SELL));
	}

	/**
	 * Creates a new SellerBehaviour with price list & price factor.
	 *
	 * @param priceList
	 *   List of item names and their prices.
	 * @param priceFactor
	 *   Skews prices of all items for this merchant.
	*/
	public SellerBehaviour(final Map<String, Integer> priceList, final Float priceFactor) {
		super(priceList, priceFactor);
		setShopType(resolveShopType(priceList, ShopType.ITEM_SELL));
	}

	/**
	 * Transacts the sale that has been agreed on earlier via setChosenItem()
	 * and setAmount().
	 *
	 * @param seller
	 *			The NPC who sells
	 * @param player
	 *			The player who buys
	 * @return true iff the transaction was successful, that is when the player
	 *		 was able to equip the item(s).
	*/
	@Override
	public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser seller, final Player player) {
		String chosenItemName = res.getChosenItemName();
		int amount = res.getAmount();

		final Item item = getAskedItem(chosenItemName, player);
		if (item == null) {
			logger.error("Trying to sell an nonexistent item: " + chosenItemName);
			return false;
		}

		// set amount of stackable items
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(amount);
		} else if (amount > 1) {
			// Fixing user input with more than one item is already handled in SellerAdder, so this should not happen here at all.
			logger.error("Trying to sell more than one " + chosenItemName + " in one transaction.");
		}

		if (amount <= 0) {
			seller.say("Przepraszam, ale musisz kupić przynajmniej jeden przedmiot.");
			return false;
		}

		int price = getCharge(res, player);
		if (player.isBadBoy()) {
			price = (int) (BAD_BOY_BUYING_PENALTY * price);
		}

		int playerCoins = MoneyUtils.getTotalMoneyInCopper(player);
		if (playerCoins >= price) {
			if (player.equipToInventoryOnly(item)) {
				MoneyUtils.removeMoney(player, price);
				updatePlayerTransactions(player, seller.getName(), res);
				seller.say("Gratulacje! Oto twój " + chosenItemName + "!");
				if ((amount >= 10) || (price >= MoneyUtils.SILVER_VALUE * 5)) {
					seller.say("Oho! Dobry interes dziś mamy!");
				}
				return true;
			} else {
				seller.say("Przepraszam, ale nie możesz wziąć " + chosenItemName + ".");
				return false;
			}
		} else {
			seller.say("Przepraszam, ale nie masz wystarczająco dużo pieniędzy!");
			return false;
		}
	}

	public Item getAskedItem(final String askedItem, final Player player) {
		final Item item = SingletonRepository.getEntityManager().getItem(askedItem);
		if (item != null && item.has("autobind") && player != null) {
			// respect autobind attrubute
			item.setBoundTo(player.getName());
		}

		return item;
	}

	public Item getAskedItem(final String askedItem) {
		return getAskedItem(askedItem, null);
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
		player.incBoughtForItem(res.getChosenItemName(), res.getAmount());
		player.incCommerceTransaction(merchant, getCharge(res, player), false);
	}
}
