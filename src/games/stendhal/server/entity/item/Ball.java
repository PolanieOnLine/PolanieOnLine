/***************************************************************************
 *                      (C) Copyright 2019 - Marauroa                      *
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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.entity.player.Player;

public class Ball extends Item {

	private static final int NUMBER_OF_BALL = 1;

	private int[] topFaces;

	private CroupierNPC croupierNPC;

	public Ball(final Map<String, String> attributes) {
		super("biała bila", "misc", "whiteball", attributes);
		randomize();
	}

	public Ball() {
		this((Map<String, String>) null);
	}

	/**
	 * copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public Ball(final Ball item) {
		super(item);
		randomize();
	}

	public void setCroupierNPC(final CroupierNPC croupierNPC) {
		this.croupierNPC = croupierNPC;
		setItemData(croupierNPC.getName());
	}

	private void updateCroupierNPC() {
		if (croupierNPC == null) {
			final String name = getItemData();

			if (name != null) {
				croupierNPC = (CroupierNPC) SingletonRepository.getNPCList().get(name);
			}
		}
	}

	/**
	 * Get the sum of the thrown dice.
	 *
	 * @return sum of the set of dices
	 */
	public int getSum() {
		int result = 0;
		for (int i = 0; i < NUMBER_OF_BALL; i++) {
			result += topFaces[i];
		}
		return result;
	}

	/**
	 * Player throws the dice, get the new values on the faces.
	 *
	 * @param player player throwing the dice
	 */
	private void randomize(final Player player) {
		topFaces = new int[NUMBER_OF_BALL];
		double karma = player.useKarma(2);
		for (int i = 0; i < NUMBER_OF_BALL; i++) {
			int topFace = Rand.roll1D20();
			// if the player has no karma or bad karma, then re-roll a 6
			if (topFace == 6 && karma <= 0) {
				topFace = Rand.roll1D20();
			}
			topFaces[i] = topFace;
		}
		// if the player has no karma or bad karma, don't give them the booby prize
		if (getSum() == NUMBER_OF_BALL && karma <= 0) {
			// topFaces must have been 1,1,1...1, so just change one of them.
			topFaces[0] = 2;
		}
	}

	/**
	 * A new die needs values on the faces
	 */
	private void randomize() {
		topFaces = new int[NUMBER_OF_BALL];
		for (int i = 0; i < NUMBER_OF_BALL; i++) {
			int topFace = Rand.roll1D20();
			topFaces[i] = topFace;
		}
	}

	@Override
	public void onPutOnGround(final Player player) {
		super.onPutOnGround(player);
		randomize(player);
		updateCroupierNPC();
		if (croupierNPC != null) {
			croupierNPC.onThrownBill(this, player);
		}
	}

	@Override
	public String describe() {
		return "Oto biała bila do gry w bilarda.";
	}
}
