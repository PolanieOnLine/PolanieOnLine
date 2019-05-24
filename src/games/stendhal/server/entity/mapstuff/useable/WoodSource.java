/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPClass;

/**
 * A wood source is a spot where a player can get wood. He needs a axe, time
 * and luck.
 * 
 * Woodcutting takes 5-9 seconds; during this time, the player keep standing next to
 * the wood source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two wood sources
 * are always at least 8 sec of walking away from each other, so that the player
 * can't find at several sites simultaneously.
 * 
 * @author dine
 */
public class WoodSource extends PlayerActivityEntity {
	/**
	 * The equipment needed.
	 */
	private static final String NEEDED_EQUIPMENT_1 = "siekierka";
	private static final String NEEDED_EQUIPMENT_2 = "ciupaga";

	/**
	 * The name of the item to be caught.
	 */
	private final String itemName;

	/**
	 * Create a wood source.
	 * 
	 * @param itemName
	 *            The name of the item to be caught.
	 */
	public WoodSource(final String itemName) {
		this.itemName = itemName;
		put("class", "source");
		put("name", "wood_source");
		setMenu("Wytnij|Użyj");
		setDescription("To drzewo przeznaczone jest do wycięcia.");
		setResistance(100);
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("drzewa");
	}

	//
	// WoodSource
	//

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass("wood_source");
		rpclass.isA("entity");
	}

	/**
	 * Calculates the probability that the given player finds wood. This is
	 * based on the player's woodcutting skills, however even players with no skills
	 * at all have a 5% probability of success.
	 * 
	 * @param player
	 *            The player,
	 * 
	 * @return The probability of success.
	 */
	private double getSuccessProbability(final Player player) {
		double probability = 0.05;

		final String skill = player.getSkill("woodcutting");

		if (skill != null) {
			probability = Math.max(probability, Double.parseDouble(skill));
		}

		return probability + player.useKarma(0.05);
	}

	//
	// PlayerActivityEntity
	//

	/**
	 * Get the time it takes to perform this activity.
	 * 
	 * @return The time to perform the activity (in seconds).
	 */
	@Override
	protected int getDuration() {
		return 5 + Rand.rand(4);
	}

	/**
	 * Decides if the activity can be done.
	 * 
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isPrepared(final Player player) {
		if (player.isEquipped(NEEDED_EQUIPMENT_1) || player.isEquipped(NEEDED_EQUIPMENT_2)) {
			return true;
		}

		player.sendPrivateText("Potrzebujesz siekierki lub ciupagi do ścięcia drzewa.");
		return false;
	}

	/**
	 * Decides if the activity was successful.
	 * 
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		final int random = Rand.roll1D100();
		return (random <= (getSuccessProbability(player) * 100));
	}

	/**
	 * Called when the activity has finished.
	 * 
	 * @param player
	 *            The player that did the activity.
	 * @param successful
	 *            If the activity was successful.
	 */
	@Override
	protected void onFinished(final Player player, final boolean successful) {
		if (successful) {
			final Item item = SingletonRepository.getEntityManager().getItem(
					itemName);

			player.equipOrPutOnGround(item);
			player.incHarvestedForItem(itemName, 1);
		    SingletonRepository.getAchievementNotifier().onObtain(player);
			player.sendPrivateText("Zdobyłeś drewno.");
		} else {
			player.sendPrivateText("Nie zdobyłeś drewna.");
		}
	}

	/**
	 * Called when the activity has started.
	 * 
	 * @param player
	 *            The player starting the activity.
	 */
	@Override
	protected void onStarted(final Player player) {
		// some feedback is needed.
		player.sendPrivateText("Rozpocząłeś wycinanie drzewa.");
	}
}
