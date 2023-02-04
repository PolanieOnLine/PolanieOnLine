/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.WordList;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * The behaviour of an NPC who is able to produce something for a player if the
 * player brings the required resources. Production takes time, depending on the
 * amount of ordered products.
 *
 * @author daniel
 */
public class ProducerBehaviour extends TransactionBehaviour {
	/**
	 * To store the current status of a production order, each ProducerBehaviour
	 * needs to have an exclusive quest slot.
	 *
	 * This slot can have three states:
	 * <ul>
	 * <li>unset: if the player has never asked the NPC to produce anything.</li>
	 * <li>done: if the player's last order has been processed.</li>
	 * <li>number;product;time: if the player has given an order and has not
	 * yet retrieved the product. number is the amount of products that the
	 * player will get, product is the name of the ordered product, and time is
	 * the time when the order was given, in milliseconds since the epoch.</li>
	 * </ul>
	 *
	 * Note: The product name is stored although each ProductBehaviour only
	 * allows one type of product at the moment. We store it to make the system
	 * extensible.
	 */
	private final String questSlot;

	/**
	 * The name of the activity, e.g. "build", "forge", "bake"
	 */
	private final List<String> productionActivity;

	/** The item that is produced. */
	private final String productName;

	/** Item count per produced unit. */
	private final int productsPerUnit;

	/** Max produced items per time. */
	private int unitsPerTime;

	/** Waiting time to produce another items. */
	private int waitingTime;

	private boolean remind;

	/**
	 * Whether the produced item should be player bound.
	 */
	private final boolean productBound;

	/**
	 * A mapping which maps the name of each required resource (e.g. "iron ore")
	 * to the amount of this resource that is required for one unit of the
	 * product.
	 */
	private final Map<String, Integer> requiredResourcesPerUnit;

	/**
	 * The number of seconds required to produce one unit of the product.
	 */
	private final int productionTimePerUnit;

	/**
	 * Creates a new ProducerBehaviour.
	 *
	 * @param questSlot
	 *            the slot that is used to store the status
	 * @param productionActivity
	 *            the name of the activity, e.g. "build", "forge", "bake"
	 * @param productName
	 *            the name of the product, e.g. "plate armor". It must be a
	 *            valid item name.
	 * @param productsPerUnit
	 *            Item count per production.
	 * @param requiredResourcesPerUnit
	 *            a mapping which maps the name of each required resource (e.g.
	 *            "iron ore") to the amount of this resource that is required
	 *            for one unit of the product.
	 * @param productionTimePerUnit
	 *            the number of seconds required to produce one unit of the
	 *            product.
	 */
	public ProducerBehaviour(final String questSlot, final List<String> productionActivity,
			final String productName, final int productsPerUnit,
			final Map<String, Integer> requiredResourcesPerUnit,
			final int productionTimePerUnit) {
		this(questSlot, productionActivity, productName, productsPerUnit,
				requiredResourcesPerUnit, productionTimePerUnit, false);
	}

	/**
	 * Creates a new ProducerBehaviour.
	 *
	 * @param questSlot
	 *            the slot that is used to store the status
	 * @param productionActivity
	 *            the name of the activity, e.g. "build", "forge", "bake"
	 * @param productName
	 *            the name of the product, e.g. "plate armor". It must be a
	 *            valid item name.
	 * @param requiredResourcesPerUnit
	 *            a mapping which maps the name of each required resource (e.g.
	 *            "iron ore") to the amount of this resource that is required
	 *            for one unit of the product.
	 * @param productionTimePerUnit
	 *            the number of seconds required to produce one unit of the
	 *            product.
	 */
	public ProducerBehaviour(final String questSlot, final List<String> productionActivity,
			final String productName, final Map<String, Integer> requiredResourcesPerUnit,
			final int productionTimePerUnit) {
		this(questSlot, productionActivity, productName,
				requiredResourcesPerUnit, productionTimePerUnit, false);
	}

	/**
	 * Creates a new ProducerBehaviour.
	 *
	 * @param questSlot
	 *            the slot that is used to store the status
	 * @param productionActivity
	 *            the name of the activity, e.g. "build", "forge", "bake"
	 * @param productName
	 *            the name of the product, e.g. "plate armor". It must be a
	 *            valid item name.
	 * @param productsPerUnit
	 *            Item count per production.
	 * @param requiredResourcesPerUnit
	 *            a mapping which maps the name of each required resource (e.g.
	 *            "iron ore") to the amount of this resource that is required
	 *            for one unit of the product.
	 * @param productionTimePerUnit
	 *            the number of seconds required to produce one unit of the
	 *            product.
	 * @param productBound
	 *            Whether the produced item should be player bound. Use only for
	 *            special one-time items.
	 */
	public ProducerBehaviour(final String questSlot, final List<String> productionActivity,
			final String productName, final int productsPerUnit, final Map<String, Integer> requiredResourcesPerUnit,
			final int productionTimePerUnit, final boolean productBound) {
		super(productName);

		this.questSlot = questSlot;
		this.productionActivity = productionActivity;
		this.productName = productName;
		this.productsPerUnit = productsPerUnit;
		this.requiredResourcesPerUnit = requiredResourcesPerUnit;
		this.productionTimePerUnit = productionTimePerUnit;
		this.productBound = productBound;

		// add the activity word as verb to the word list in case it is still missing there
		WordList.getInstance().registerVerb(productionActivity);

		for (final String itemName : requiredResourcesPerUnit.keySet()) {
			WordList.getInstance().registerName(itemName, ExpressionType.OBJECT);
		}
	}

	/**
	 * Creates a new ProducerBehaviour.
	 *
	 * @param questSlot
	 *            the slot that is used to store the status
	 * @param productionActivity
	 *            the name of the activity, e.g. "build", "forge", "bake"
	 * @param productName
	 *            the name of the product, e.g. "plate armor". It must be a
	 *            valid item name.
	 * @param requiredResourcesPerUnit
	 *            a mapping which maps the name of each required resource (e.g.
	 *            "iron ore") to the amount of this resource that is required
	 *            for one unit of the product.
	 * @param productionTimePerUnit
	 *            the number of seconds required to produce one unit of the
	 *            product.
	 * @param productBound
	 *            Whether the produced item should be player bound. Use only for
	 *            special one-time items.
	 */
	public ProducerBehaviour(final String questSlot, final List<String> productionActivity,
			final String productName, final Map<String, Integer> requiredResourcesPerUnit,
			final int productionTimePerUnit, final boolean productBound) {
		this(questSlot, productionActivity, productName, 1, requiredResourcesPerUnit,
				productionTimePerUnit, productBound);
	}

	public String getQuestSlot() {
		return questSlot;
	}

	protected Map<String, Integer> getRequiredResourcesPerUnit() {
		return requiredResourcesPerUnit;
	}

	public List<String> getProductionActivity() {
		return productionActivity;
	}

	/**
	 * Return item name of the product to produce.
	 *
	 * @return product name
	 */
	public String getProductName() {
		return productName;
	}

	private int getProductsPerUnit() {
		return productsPerUnit;
	}

	public int setUnitsPerTime(final int units) {
		return this.unitsPerTime = units;
	}
	private int getUnitsPerTime() {
		return unitsPerTime;
	}

	public int setWaitingTime(final int time) {
		return this.waitingTime = time;
	}
	private int getWaitingTime() {
		return waitingTime;
	}

	public boolean setRemind(boolean remind) {
		return this.remind = remind;
	}
	public boolean getRemind() {
		return remind;
	}

	public int getProductionTime(final int amount) {
		return productionTimePerUnit * amount / getProductsPerUnit();
	}

	/**
	 * Determine whether the produced item should be player bound.
	 *
	 * @return <code>true</code> if the product should be bound.
	 */
	public boolean isProductBound() {
		return productBound;
	}

	/**
	 * Gets a nicely formulated string that describes the amounts and names of
	 * the resources that are required to produce <i>amount</i> units of the
	 * product, with hashes before the resource names in order to highlight
	 * them, e.g. "4 #wood, 2 #iron, and 6 #leather".
	 *
	 * @param amount
	 *            The amount of products that were requested
	 * @return A string describing the required resources woth hashes
	 */
	protected String getRequiredResourceNamesWithHashes(final int amount) {
		// use sorted TreeSet instead of HashSet
		final Set<String> requiredResourcesWithHashes = new TreeSet<String>();
		for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerUnit().entrySet()) {
			requiredResourcesWithHashes.add(Grammar.quantityplnounWithHash(amount / getProductsPerUnit()
					* entry.getValue(), entry.getKey()));
		}
		return Grammar.enumerateCollection(requiredResourcesWithHashes);
	}

	/**
	 * Gets a nicely formulated string that describes the amounts and names of
	 * the resources that are required to produce <i>amount</i> units of the
	 * product
	 *
	 * @param amount
	 *            The amount of products that were requested
	 * @return A string describing the required resources.
	 */
	public String getRequiredResourceNames(final int amount) {
		// use sorted TreeSet instead of HashSet
		final Set<String> requiredResources = new TreeSet<String>();
		for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerUnit().entrySet()) {
			requiredResources.add(Grammar.quantityplnoun(amount
					* entry.getValue(), entry.getKey()));
		}
		return Grammar.enumerateCollection(requiredResources);
	}

	/**
	 * Create a text representing a saying of approximate time until
	 * the order being produced is ready
	 *
	 * @param player
	 * @return A string describing the remaining time.
	 */
	public String getApproximateRemainingTime(final Player player) {
		final String orderString = player.getQuest(questSlot);
		final String[] order = orderString.split(";");
		final long orderTime = Long.parseLong(order[2]);
		final long timeNow = new Date().getTime();
		final int numberOfProductItems = Integer.parseInt(order[0]);
		// String productName = order[1];

		final long finishTime = orderTime
				+ (getProductionTime(numberOfProductItems) * 1000L);
		final int remainingSeconds = (int) ((finishTime - timeNow) / 1000);
		return TimeUtil.approxTimeUntil(remainingSeconds);
	}

	/**
	 * Is the order ready for this player?
	 *
	 * @param player
	 * @return true if the order is ready.
	 */
	public boolean isOrderReady(final Player player) {
		final String orderString = player.getQuest(questSlot);
		final String[] order = orderString.split(";");
		final int numberOfProductItems = Integer.parseInt(order[0]);
		// String productName = order[1];
		final long orderTime = Long.parseLong(order[2]);
		final long timeNow = new Date().getTime();
		return timeNow - orderTime >= getProductionTime(numberOfProductItems) * 1000L;
	}

	/**
	 * Checks how many items are being produced on this particular order
	 *
	 * @param player
	 * @return number of items
	 */
	public int getNumberOfProductItems(final Player player) {
		final String orderString = player.getQuest(questSlot);
		final String[] order = orderString.split(";");

		return Integer.parseInt(order[0]);
	}

	/**
	 * Checks how many items the NPC can offer to produce based on what the player is carrying
	 *
	 * @param player
	 * @return maximum number of items
	 */
	protected int getMaximalAmount(final Player player) {
		int maxAmount = Integer.MAX_VALUE;

		for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerUnit().entrySet()) {
			final int limitationByThisResource = player.getNumberOfEquipped(entry.getKey())
					/ entry.getValue() * getProductsPerUnit();
			maxAmount = Math.min(maxAmount, limitationByThisResource);
		}

		return maxAmount;
	}

	/**
	 * Tries to take all the resources required to produce <i>amount</i> units
	 * of the product from the player. If this is possible, asks the user if the
	 * order should be initiated.
	 *
	 * @param res
	 * @param npc
	 * @param player
	 * @return true if all resources can be taken
	 */
	public boolean askForResources(final ItemParserResult res, final EventRaiser npc, final Player player) {
		int amount = res.getAmount();
		final int perUnit = getProductsPerUnit();
		final int perTime = getUnitsPerTime();

		if (getWaitingTime() > 0) {
			if (player.hasQuest(getQuestSlot()) && player.getQuest(getQuestSlot()).startsWith("done;")) {
				final String[] tokens = player.getQuest(getQuestSlot()).split(";");
				final long delay = (Long.parseLong(tokens[1])) * (MathHelper.MILLISECONDS_IN_ONE_HOUR * getWaitingTime());
				final long timeRemaining = (Long.parseLong(tokens[2]) + delay) - System.currentTimeMillis();
				if (timeRemaining > 0) {
					npc.say("Wciąż jestem " + Grammar.genderVerb(npc.getGender(), "zajęty")
						+ " poprzednimi. Wróć proszę za "
						+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000)) + ".");
					return false;
				}
			}
		}

		if ((perTime > 0) && (amount > perTime)) {
			npc.say("Wybacz, ale nie mogę przyjąć więcej niżeli " + perTime + " naraz!");
			return false;
		} else if (amount % perUnit != 0) {
			npc.say("Mogę wykonać "
					+ Grammar.plural(getProductName())
					+ " jedynie w ilościach podzielnych przez " + perUnit + ".");
			return false;
		} else if (getMaximalAmount(player) < amount) {
			npc.say("Mogę zrobić "
					+ Grammar.quantityplnoun(amount, getProductName())
					+ " jeżeli przyniesiesz mi "
					+ getRequiredResourceNamesWithHashes(amount) + ".");
			return false;
		} else {
			res.setAmount(amount);
			npc.say("Potrzebuję, abyś " + Grammar.genderVerb(player.getGender(), "przyniósł") + " mi "
					+ getRequiredResourceNamesWithHashes(amount)
					+ " do tej pracy, która zajmie " + TimeUtil.approxTimeUntil(getProductionTime(amount)) + ". Posiadasz to przy sobie?");
			return true;
		}
	}

	/**
	 * Tries to take all the resources required to produce the agreed amount of
	 * the product from the player. If this is possible, initiates an order.
	 *
	 * @param npc
	 *            the involved NPC
	 * @param player
	 *            the involved player
	 */
	@Override
	public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
		if (getMaximalAmount(player) < res.getAmount()) {
			// The player tried to cheat us by placing the resource
			// onto the ground after saying "yes"
			npc.say("Hej! Mam nadzieję, że nie próbujesz mnie oszukać...");
			return false;
		} else {
			for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerUnit().entrySet()) {
				final int amountToDrop = res.getAmount() / getProductsPerUnit() * entry.getValue();
				player.drop(entry.getKey(), amountToDrop);
			}
			final long timeNow = new Date().getTime();
			player.setQuest(questSlot, res.getAmount() + ";" + getProductName() + ";" + timeNow);

			String agreed = "Dobrze, zrobię dla Ciebie "
					+ Grammar.quantityplnoun(res.getAmount(), getProductName())
					+ ", ale zajmie mi to trochę czasu. Wróć za "
					+ getApproximateRemainingTime(player) + ".";
			if (getRemind()) {
				npc.say(agreed + " Aha i NAJWAŻNIEJSZE - Jestem bardzo zajęty"
					+ " i MUSISZ mi przypomnieć mówiąc #przypomnij,"
					+ " abym dał Tobie " + getProductName() + ".");
			} else {
				npc.say(agreed);
			}
			return true;
		}
	}

	/**
	 * This method is called when the player returns to pick up the finished
	 * product. It checks if the NPC is already done with the order. If that is
	 * the case, the player is given the product. Otherwise, the NPC asks the
	 * player to come back later.
	 *
	 * @param npc
	 *            The producing NPC
	 * @param player
	 *            The player who wants to fetch the product
	 */
	public void giveProduct(final EventRaiser npc, final Player player) {
		final int numberOfProductItems = getNumberOfProductItems(player);
		// String productName = order[1];

		if (!isOrderReady(player)) {
			String stillWorking = "Wciąż zajmuje się twoim zleceniem "
					+ Grammar.quantityplnoun(numberOfProductItems, getProductName())
					+ ". Wróć za " + getApproximateRemainingTime(player) + ", aby odebrać.";
			if (getRemind()) {
				npc.say(stillWorking + " Nie zapomnij mi #'przypomnieć'...");
			} else {
				npc.say("Witaj z powrotem! " + stillWorking + ", aby odebrać.");
			}
		} else {
			final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(
					getProductName());

			products.setQuantity(numberOfProductItems);

			if (isProductBound()) {
				products.setBoundTo(player.getName());
			}

			if (player.equipToInventoryOnly(products)) {
				String reward = Grammar.genderVerb(npc.getGender(), "Skończyłem") + " twoje zlecenie. Trzymaj, oto "
						+ Grammar.quantityplnoun(numberOfProductItems, getProductName()) + ".";
				if (getRemind()) {
					npc.say("Ach tak, zapomniałem. " + reward);
				} else {
					npc.say("Witaj z powrotem! " + reward);
				}
				if (getWaitingTime() > 0) {
					player.setQuest(questSlot, "done" + ";" + numberOfProductItems + ";" + System.currentTimeMillis());
					player.addXP(15 * numberOfProductItems);
				} else {
					player.setQuest(questSlot, "done");
					// give some XP as a little bonus for industrious workers
					player.addXP(numberOfProductItems);
				}
				player.notifyWorldAboutChanges();
				player.incProducedForItem(getProductName(), products.getQuantity());
			} else {
				String notEnough = Grammar.genderVerb(npc.getGender(), "Skończyłem")
						+ " twoje zlecenie, ale w tym momencie nie możesz wziąć "
						+ Grammar.plnoun(numberOfProductItems, getProductName())
						+ ". Wróć, gdy będziesz miał wolne miejsce.";
				if (getRemind()) {
					npc.say("Ach tak, zapomniałem. " + notEnough);
				} else {
					npc.say("Witaj z powrotem! " + notEnough);
				}
			}
		}
	}

	/**
	 * Answer with an error message in case the request could not be fulfilled.
	 *
	 * @param res
	 * @param npcAction
	 * @return error message
	 */
	public String getErrormessage(final ItemParserResult res, final String npcAction) {
		return getErrormessage(res, getProductionActivity(), npcAction);
	}
}
