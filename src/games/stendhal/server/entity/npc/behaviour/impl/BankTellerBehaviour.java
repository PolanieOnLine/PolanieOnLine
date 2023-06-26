/***************************************************************************
 *                       Copyright © 2023 - Stendhal                       *
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

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Behaviour for bank tellers.
 */
public class BankTellerBehaviour {
	private static final Logger logger = Logger.getLogger(BankTellerBehaviour.class);

	private static final String slot_name = "money_balance";

	/**
	 * Retrieves the deposited money in player bank account.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @return
	 *	 Money in account.
	 */
	private static StackableItem getBalance(final Player player) {
		// get current deposits from slot
		final RPSlot slot = player.getSlot(slot_name);
		if (slot.isEmpty()) {
			return null;
		}
		final RPObject obj = slot.getFirst();
		if (obj instanceof StackableItem && obj.has("name") && "money".equals(obj.get("name"))) {
			return (StackableItem) obj;
		}
		return null;
	}

	/**
	 * Sets the deposited money in player bank account.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @param money
	 *	 Money to be set in account balance.
	 * @return
	 *	 Money in account.
	 */
	private static StackableItem setBalance(final Player player, final StackableItem money) {
		final RPSlot slot = player.getSlot(slot_name);
		if (money == null) {
			slot.clear();
			return null;
		}
		StackableItem balance = BankTellerBehaviour.getBalance(player);
		if (!"money".equals(money.getName())) {
			logger.error("Cannot deposit item '" + money.getName() + "' into slot '" + slot_name + "'");
			return balance;
		}
		if (balance == null) {
			balance = money;
		}
		balance.setQuantity(money.getQuantity());
		if (!balance.equals(slot.getFirst())) {
			if (!slot.isEmpty()) {
				slot.clear();
			}
			slot.add(balance);
		}
		return BankTellerBehaviour.getBalance(player);
	}

	/**
	 * Sets contents of bank balance slot.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @param amount
	 *	 Amount of money to be set in account balance.
	 * @return
	 *	 Money in account.
	 */
	private static StackableItem setBalance(final Player player, final int amount) {
		StackableItem money = null;
		if (amount > 0) {
			money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
			money.setQuantity(amount);
		}
		return BankTellerBehaviour.setBalance(player, money);
	}

	/**
	 * Retrieves the amount of money player has deposited in bank account.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @return
	 *	 Amount of money in account.
	 */
	public static int getBalanceAmount(final Player player) {
		final StackableItem balance = BankTellerBehaviour.getBalance(player);
		return balance != null ? balance.getQuantity() : 0;
	}

	/**
	 * Adds money to player's account from inventory.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @param amount
	 *	 Amount of money to be added to account.
	 * @return
	 *	 Response from teller NPC.
	 */
	public static String deposit(final Player player, final Integer amount) {
		if (amount == null) {
			return "Podaj prawidłową kwotę do depozytu.";
		}
		if (amount < 1) {
			return "Nie możesz wpłacić " + amount + " pieniędzy.";
		}
		final int carrying_start = player.getNumberOfEquipped("money");
		if (carrying_start == 0) {
			return "Nie masz przy sobie żadnych pieniędzy do wpłaty.";
		}
		if (carrying_start < amount) {
			return "Nie masz przy sobie tyle pieniędzy.";
		}

		final int balance_start = BankTellerBehaviour.getBalanceAmount(player);
		final int deposited = BankTellerBehaviour.setBalance(player, balance_start + amount)
				.getQuantity() - balance_start;
		if (deposited != amount) {
			logger.error("Player " + player.getName() + " requested to deposit " + amount + " money, but"
					+ " actual amount was " + deposited);
		}
		player.drop("money", deposited);
		return "Wpłaciłeś " + deposited + " pieniędzy.";
	}

	/**
	 * Subtracts money from player's account & adds to inventory.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @param amount
	 *	 Amount of money to be subtracted from account.
	 * @return
	 *	 Response from teller NPC.
	 */
	public static String withdraw(final Player player, final Integer amount) {
		if (amount == null) {
			return "Podaj prawidłową kwotę do wypłaty.";
		}
		if (amount < 1) {
			return "Nie możesz wypłacić " + amount + " pieniędzy.";
		}
		final int balance_start = BankTellerBehaviour.getBalanceAmount(player);
		if (balance_start == 0) {
			return "Nie masz pieniędzy na koncie do wypłaty.";
		}
		if (balance_start < amount) {
			return "Nie masz tyle pieniędzy na koncie bankowym.";
		}

		final StackableItem balance = BankTellerBehaviour.setBalance(player, balance_start - amount);
		final int withdrawn = balance_start - (balance != null ? balance.getQuantity() : 0);
		if (withdrawn != amount) {
			logger.error("Player " + player.getName() + " requested to withdraw " + amount + " money, but"
					+ " actual amount was " + withdrawn);
		}
		final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
				.getItem("money");
		money.setQuantity(withdrawn);
		player.equipOrPutOnGround(money);
		return "Wypłaciłeś " + money.getQuantity() + " pieniędzy.";
	}

	/**
	 * Checks player's account balance.
	 *
	 * @param player
	 *	 Player requesting transaction.
	 * @return
	 *	 Response from teller NPC.
	 */
	public static String queryBalance(final Player player) {
		final int deposits = BankTellerBehaviour.getBalanceAmount(player);
		if (deposits == 0) {
			return "Nie masz pieniędzy na koncie.";
		}
		return "Saldo Twojego konta wynosi " + deposits + " money.";
	}
}