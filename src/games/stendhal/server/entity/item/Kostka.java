/* $Id$ */
/***************************************************************************
 *                	(C) Copyright 2003-2018 - Stendhal                  	*
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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.player.Player;

public class Kostka extends Item {

	private static final int NUMBER_OF_KOSTKA = 1;

	private int[] topFaces;

	public Kostka(final Map<String, String> attributes) {
		super("kostka", "misc", "kostka", attributes);
		randomize();
	}

	public Kostka() {
		this((Map<String, String>) null);
	}

	/**
	 * copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public Kostka(final Kostka item) {
		super(item);
		randomize();
	}

	/**
	 * Get a list of the top faces as a readable string.
	 *
	 * @return list of top faces
	 */
	private String getTopFacesString() {
		final List<String> topFacesStrings = new LinkedList<String>();
		for (int i = 0; i < NUMBER_OF_KOSTKA; i++) {
			topFacesStrings.add(Integer.toString(topFaces[i]));
		}
		return Grammar.enumerateCollection(topFacesStrings);
	}

	/**
	 * Get the sum of the thrown kostka.
	 *
	 * @return sum of the set of kostki
	 */
	public int getSum() {
		int result = 0;
		for (int i = 0; i < NUMBER_OF_KOSTKA; i++) {
			result += topFaces[i];
		}
		return result;
	}

	/**
	 * Throw the kostka.
	 */
	private void randomize() {
		topFaces = new int[NUMBER_OF_KOSTKA];
		for (int i = 0; i < NUMBER_OF_KOSTKA; i++) {
			final int topFace = Rand.roll1D6();
			topFaces[i] = topFace;
		}
	}

	@Override
	public void onPutOnGround(final Player player) {
		super.onPutOnGround(player);
		randomize();
	}

	@Override
	public String describe() {
		return "Oto kostka. Na górze widnieje "
				+ getTopFacesString() + ".";
	}
}
