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
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPClass;

/**
 * A well source is a spot where a player can make a wish to gain an item. He
 * needs time and luck.
 *
 * Wishing takes 10 seconds + randomized 4 seconds; during this time, the player keep standing next to
 * the well source. At every well are two sources next to each other, so the
 * player can actually make 2 wishes at once.
 *
 * @author kymara (based on FishSource by daniel)
 *
 */
public class WellSource extends PlayerActivityEntity {
	/**
	 * The list of possible rewards.
	 */
	private static final String[] items = { "money", "polano", "ruda żelaza",
			"bryłka złota", "eliksir", "zwój semos", "duży eliksir",
			"szafir", "rubin", "złoty hełm wikingów", "sztylet mroku",
			"prezent" };

	/**
	 * The chance that wishing is successful.
	 */
	private static final double FINDING_PROBABILITY = 0.05;

	/**
	 * How long it takes to wish at a well (in seconds).
	 */
	private static final int DURATION = 10;

	/**
	 * Create a wishing well source.
	 */
	public WellSource() {
		put("class", "source");
		put("name", "well_source");
		setMenu("Wypowiedz życzenie|Użyj");
		setDescription("Oto studnia życzeń. Twoje oko coś zauważyło.");
		setResistance(0);
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("wishing well");
	}

	//
	// WellSource
	//

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass("well_source");
		rpclass.isA("entity");
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
		return DURATION + Rand.rand(5);
	}

	/**
	 * Decides if the activity can be done.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isPrepared(final Player player) {
        /*
        * The player 'throws' money into the well, like a wishing well donation.
        * Check they have it before they use the well.
		*/
		if (player.isEquipped("money", 30)) {
			return true;
		} else {
			player.sendPrivateText("Potrzebujesz 30 monet, aby wypowiedzieć życzenie.");
			return false;
		}
	}

	/**
	 * Decides if the activity was successful.
	 *
	 * @return <code>true</code> if successful.
	 */
	@Override
	protected boolean isSuccessful(final Player player) {
		final int random = Rand.roll1D100();
        /*
        * Use some karma to help decide if the outcome is successful
        * We want to use up quite a bit karma at once, so scale it after
		*/
		double karma = player.useKarma(FINDING_PROBABILITY*10);

		// if player karma is > 0 it will always return at least 20% of FINDING_PROBABILITY*10, or karma, whichever is smaller
		// so we can safely say if the karma returned is <= 0, the player had <= 0 karma.
		// so we'll penalise these stronger
		if (karma <= 0) {
			karma = karma - FINDING_PROBABILITY*5;
		}
		karma = karma / 10;
		// right hand side could be negative, if karma was negative but thats ok.
		return random <= (FINDING_PROBABILITY + karma) * 100;
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
			final String itemName = items[Rand.rand(items.length)];
			final Item item = SingletonRepository.getEntityManager().getItem(itemName);
			int amount = 1;
			if (itemName.equals("sztylet mroku")
					|| itemName.equals("złoty hełm wikingów")) {
				/*
				 * Bound powerful items.
				 */
				item.setBoundTo(player.getName());
			} else if (itemName.equals("money")) {
				/*
				 * Assign a random amount of money from 1 to 100.
				 */
				amount = Rand.roll1D100();
				((StackableItem) item).setQuantity(amount);
			}

			player.equipOrPutOnGround(item);
			player.incObtainedForItem(item.getName(), item.getQuantity());
			SingletonRepository.getAchievementNotifier().onObtain(player);
			player.sendPrivateText("Miałeś szczęście i znalazłeś "
					+ Grammar.quantityplnoun(amount, itemName, "a")+ ".");
		} else {
			player.sendPrivateText("Twoje życzenie się nie spełniło.");
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
		// remove 30 money from player as they throw a coin into the
		// well
		player.drop("money", 30);
		player.sendPrivateText("Wrzuciłeś 30 monet do studni i wypowiedziałeś życzenie.");
	}
}
