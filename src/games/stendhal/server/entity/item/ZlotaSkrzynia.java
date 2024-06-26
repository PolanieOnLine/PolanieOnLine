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
package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

/**
 * Zlota skrzynia
 *
 * @author KarajuSs
 */
public class ZlotaSkrzynia extends Box {

	private static final String[] items = { "money", "money", "gigantyczny eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "wielki eliksir", "wielki eliksir", "ciupaga", "ciupaga", "złote spodnie",  
			"złota zbroja", "zbroja cieni", "tarcza płytowa", "sztylet mroku", "skórzane wzmocnione rękawice", "skóra złotego smoka", "skóra zielonego smoka", "skóra niebieskiego smoka", 
			"skóra czerwonego smoka", "skóra czarnego smoka", "skóra arktycznego smoka", "rękawice cieni", "pas zbójnicki", "miecz lodowy", "korale", "czarny sztylet", "czarny płaszcz", 
			"czarne spodnie", "czarne buty", "czarna zbroja" };

	/**
	 * Creates a new present.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public ZlotaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		
		setContent(items[Rand.rand(items.length)]);
	}

	/**
	 * Sets content.
	 * @param type of item to be produced.
	 */
	public void setContent(final String type) {
		setItemData(type);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public ZlotaSkrzynia(final ZlotaSkrzynia item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();

		final String itemName = getItemData();
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		int amount = 1;
		if (itemName.equals("wielki eliksir") || itemName.equals("gigantyczny eliksir")
				|| itemName.equals("skóra zielonego smoka") || itemName.equals("skóra niebieskiego smoka")
				|| itemName.equals("skóra czerwonego smoka") || itemName.equals("skóra czarnego smoka")
				|| itemName.equals("skóra złotego smoka") || itemName.equals("skóra arktycznego smoka")) {
			amount = Rand.roll1D20();
			((StackableItem) item).setQuantity(amount);
		} else if (itemName.equals("money")) {
			amount = Rand.roll1D2000();
			((StackableItem) item).setQuantity(amount);
		}
		if (itemName.equals(itemName) && !itemName.equals("money")
				&& !itemName.equals("skóra zielonego smoka") && !itemName.equals("skóra niebieskiego smoka")
				&& !itemName.equals("skóra czerwonego smoka") && !itemName.equals("skóra czarnego smoka")
				&& !itemName.equals("skóra złotego smoka") && !itemName.equals("skóra arktycznego smoka")
				&& !itemName.equals("wielki eliksir") && !itemName.equals("gigantyczny eliksir")) {
			/*
			 * Bound powerful items.
			 */
			item.setBoundTo(player.getName());
		}

		player.equipOrPutOnGround(item);
		player.incObtainedForItem(item.getName(), item.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Gratulacje! Ze skrzynki otrzymałeś #'"
				+ Grammar.quantityplnoun(amount, itemName)+ "'!");

		return true;
	}
}
