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
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPClass;

import org.apache.log4j.Logger;

/**
 * A sapphire source is a spot where a player can prospect for sapphire. He
 * needs a kilof and lina, time, and luck.
 *
 * Prospecting takes 7-11 seconds; during this time, the player keep standing
 * next to the sapphire source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two sapphire sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect for sapphire at several sites simultaneously.
 *
 * @author daniel
 * @changes artur
 */
public class SourceSapphire extends PlayerActivityEntity {
	private static final Logger logger = Logger.getLogger(SourceSapphire.class);

	/**
	 * The equipment needed.
	 */
	private static final String NEEDED_EQUIPMENT_1 = "kilof";
	private static final String NEEDED_EQUIPMENT_2 = "lina";

	/**
	 * The chance that prospecting is successful.
	 */
	private static final double FINDING_PROBABILITY = 0.02;

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a sapphire source.
	 */
	public SourceSapphire() {
		this("kryształ szafiru");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a obsidian source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceSapphire(final String itemName) {
		this.itemName = itemName;
		put("class", "source");
		put("name", "source_sapphire");
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");
		setResistance(100);
	}

	//
	// SourceSapphire
	//

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass("source_sapphire");
		rpclass.isA("entity");
	}

	/**
	 * Calculates the probability that the given player finds stone. This is
	 * based on the player's mining skills, however even players with no skills
	 * at all have a 5% probability of success.
	 *
	 * @param player
	 *            The player,
	 *
	 * @return The probability of success.
	 */
	private double getSuccessProbability(final Player player) {
		double probability = FINDING_PROBABILITY;

		final String skill = player.getSkill("mining");

		if (skill != null) {
			probability = Math.max(probability, Double.parseDouble(skill));
		}

		return probability + player.useKarma(0.02);
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
		return 8 + Rand.rand(4);
	}

	/**
	 * Decides if the activity can be done.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isPrepared(final Player player) {
		if (player.isEquipped(NEEDED_EQUIPMENT_1) && player.isEquipped(NEEDED_EQUIPMENT_2)) {
			return true;
		}

		player.sendPrivateText("Potrzebujesz kilofa i liny do wydobywania kamieni.");
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
			final Item item = SingletonRepository.getEntityManager().getItem(itemName);

			if (item != null) {
				player.equipOrPutOnGround(item);
				player.incMinedForItem(item.getName(), item.getQuantity());
				SingletonRepository.getAchievementNotifier().onObtain(player);
				player.sendPrivateText("Wydobyłeś "
						+ Grammar.a_noun(item.getTitle()) + ".");
			} else {
				logger.error("could not find item: " + itemName);
			}
		} else {
			player.sendPrivateText("Nic nie wydobyłeś.");
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
		player.sendPrivateText("Rozpocząłeś wydobywanie szafiru.");
	}
}
