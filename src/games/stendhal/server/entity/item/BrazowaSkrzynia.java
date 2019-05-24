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
 * Brazowa skrzynia
 *
 * @author KarajuSs
 */
public class BrazowaSkrzynia extends Box {

	private static final String[] items = { "money",  "money", "money", "money", "money", "duży eliksir", "duży eliksir", "duży eliksir", "duży eliksir",
			"wielki eliksir", "wielki eliksir", "wielki eliksir", "gigantyczny eliksir", "gigantyczny eliksir", "maczuga",
			"maczuga", "skóra zielonego smoka", "skóra niebieskiego smoka", "futro", "korale", "ciupaga", "spodnie kamienne",
			"kamienna zbroja", "buty kamienne", "sztylet mroku", "kosa", "pyrlik", "skórzane rękawice", "złota kolczuga"};

	/**
	 * Creates a new present.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public BrazowaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		
		setContent(items[Rand.rand(items.length)]);
	}
	
	/**
	 * Sets content.
	 * @param type of item to be produced.
	 */
	public void setContent(final String type) {
		setInfoString(type);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public BrazowaSkrzynia(final BrazowaSkrzynia item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();

		final String itemName = getInfoString();
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		int amount = 1;
		if (itemName.equals("duży eliksir") || itemName.equals("wielki eliksir") 
				|| itemName.equals("gigantyczny eliksir") || itemName.equals("skóra zielonego smoka")
				|| itemName.equals("skóra niebieskiego smoka")) {
			amount = Rand.roll1D3();
			((StackableItem) item).setQuantity(amount);
		} else if (itemName.equals("money")) {
			amount = Rand.roll1D200();
			((StackableItem) item).setQuantity(amount);
		}
		if (itemName.equals(itemName) && !itemName.equals("money")
				&& !itemName.equals("skóra zielonego smoka") && !itemName.equals("skóra niebieskiego smoka")
				&& !itemName.equals("wielki eliksir") && !itemName.equals("gigantyczny eliksir")
				&& !itemName.equals("duży eliksir")) {
			/*
			 * Bound powerful items.
			 */
			item.setBoundTo(player.getName());
		}

		player.equipOrPutOnGround(item);
		player.incObtainedForItem(item.getName(), item.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Gratulacje! Ze skrzynki otrzymałeś #'"
				+ Grammar.quantityplnoun(amount, itemName, "a")+ "'!");

		return true;
	}

}
