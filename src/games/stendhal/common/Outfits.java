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
package games.stendhal.common;

import java.util.Arrays;
import java.util.List;

/**
 * class to store the max outfit numbers for player available outfits.
 * @author kymara
 */
public class Outfits {

	/*
	 * Edit these fields to add new outfits.
	 * Note: Outfits are numbered starting at 0 and these
	 * variables are the total number of outfits.
	 */

	/** number of player selectable heads */
	public static final int HEAD_OUTFITS = 19;

	/** number of player selectable dresses */
	public static final int CLOTHES_OUTFITS = 91;

	/** number of player selectable hair styles */
	public static final int HAIR_OUTFITS = 46;

	/** number of player selectable body shapes */
	public static final int BODY_OUTFITS = 33;

	/** number of player selectable hats */
	public static final int HAT_OUTFITS = 23;

	/** number of player selectable masks */
	public static final int MASK_OUTFITS = 15;

	// hair should not be drawn with hat indexes in this list
	public static final List<Integer> HATS_NO_HAIR = Arrays.asList(3, 4, 5, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 996, 997);
	// dress should not be drawn with silme body indexes in this list
	public static final List<Integer> SLIMEBODY_NO_DRESS = Arrays.asList(13, 14, 15, 16, 17);
	// some outfit layers should not be drawn with cavalery dress indexes in this list
	public static final List<Integer> BODY_WITHOUT_OTHER_LAYERS = Arrays.asList(18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 50, 51);

	// layers used for building outfits
	public static final List<String> LAYER_NAMES = Arrays.asList(
					"body", "dress", "head", "mask",
					"hair", "hat", "detail");
	public static final int LAYERS_COUNT = LAYER_NAMES.size();

	// layers that can be re-colored
	public static final List<String> RECOLORABLE_OUTFIT_PARTS = Arrays.asList(
					"detail", "dress", "hair", "body", "head", "mask", "hat");
}
