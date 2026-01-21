/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

/**
 * Srebrna skrzynia
 *
 * @author KarajuSs
 */
public class SrebrnaSkrzynia extends Box {

	private static final double SINGLE_CHANCE = 100.0 / 24.0;

	private static final List<WeightedDropTable.Entry> DROP_TABLE = List.of(
			new WeightedDropTable.Entry("money", 100.0 * 3 / 24.0, 1, 1000),
			new WeightedDropTable.Entry("wielki eliksir", 100.0 * 3 / 24.0, 1, 6),
			new WeightedDropTable.Entry("gigantyczny eliksir", 100.0 * 2 / 24.0, 1, 6),
			new WeightedDropTable.Entry("skóra zielonego smoka", SINGLE_CHANCE, 1, 6),
			new WeightedDropTable.Entry("skóra niebieskiego smoka", SINGLE_CHANCE, 1, 6),
			new WeightedDropTable.Entry("skóra czerwonego smoka", SINGLE_CHANCE, 1, 6),
			new WeightedDropTable.Entry("skóra czarnego smoka", SINGLE_CHANCE, 1, 6),
			new WeightedDropTable.Entry("korale", SINGLE_CHANCE),
			new WeightedDropTable.Entry("lodowy miecz", SINGLE_CHANCE),
			new WeightedDropTable.Entry("zbroja cieni", SINGLE_CHANCE),
			new WeightedDropTable.Entry("rękawice cieni", SINGLE_CHANCE),
			new WeightedDropTable.Entry("spodnie cieni", SINGLE_CHANCE),
			new WeightedDropTable.Entry("buty cieni", SINGLE_CHANCE),
			new WeightedDropTable.Entry("złote spodnie", SINGLE_CHANCE),
			new WeightedDropTable.Entry("sztylet mroku", SINGLE_CHANCE),
			new WeightedDropTable.Entry("ciupaga", SINGLE_CHANCE),
			new WeightedDropTable.Entry("kamienne spodnie", SINGLE_CHANCE),
			new WeightedDropTable.Entry("skórzane wzmocnione rękawice", SINGLE_CHANCE),
			new WeightedDropTable.Entry("kamienna tarcza", SINGLE_CHANCE));

	private static final Set<String> UNBOUND_ITEMS = Set.of(
			"money",
			"wielki eliksir",
			"gigantyczny eliksir",
			"skóra zielonego smoka",
			"skóra niebieskiego smoka",
			"skóra czerwonego smoka",
			"skóra czarnego smoka");

	private static final char DATA_SEPARATOR = ';';

	/**
	 * Creates a new present.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public SrebrnaSkrzynia(final String name, final String clazz, final String subclass,
				final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);

		setContent(WeightedDropTable.roll(DROP_TABLE));
	}

	/**
	 * Sets content.
	 *
	 * @param drop
	 *            result of the random roll
	 */
	public void setContent(final WeightedDropTable.Result drop) {
		setItemData(encodeDrop(drop));
	}

	/**
	 * Sets content.
	 *
	 * @param type
	 *            name of the item to be produced
	 */
	public void setContent(final String type) {
		setContent(new WeightedDropTable.Result(type, 1));
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public SrebrnaSkrzynia(final SrebrnaSkrzynia item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		this.removeOne();

		final WeightedDropTable.Result drop = decodeDrop(getItemData());
		final String itemName = drop.getItemName();
		final int amount = drop.getQuantity();
		final Item item = SingletonRepository.getEntityManager().getItem(itemName);
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(amount);
		}
		if (!UNBOUND_ITEMS.contains(itemName)) {
			item.setBoundTo(player.getName());
		}

		player.equipOrPutOnGround(item);
		player.incObtainedForItem(item.getName(), item.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Gratulacje! Ze skrzynki otrzymałeś #'"
				+ Grammar.quantityplnoun(amount, itemName) + "'!");

		return true;
	}

	private static String encodeDrop(final WeightedDropTable.Result drop) {
		return drop.getItemName() + DATA_SEPARATOR + drop.getQuantity();
	}

	private static WeightedDropTable.Result decodeDrop(final String data) {
		if (data == null || data.isEmpty()) {
			throw new IllegalStateException("Missing drop data");
		}
		final int separatorIndex = data.lastIndexOf(DATA_SEPARATOR);
		if (separatorIndex < 0) {
			return new WeightedDropTable.Result(data, 1);
		}
		final String itemName = data.substring(0, separatorIndex);
		final String quantityText = data.substring(separatorIndex + 1);
		try {
			return new WeightedDropTable.Result(itemName, Integer.parseInt(quantityText));
		} catch (final NumberFormatException ex) {
			return new WeightedDropTable.Result(data, 1);
		}
	}
}
