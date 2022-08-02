/***************************************************************************
 *                   (C) Copyright 2022 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Is the player's karma less than the specified one?
 */
@Dev(category=Category.STATS, label="Karma?")
public class KarmaLessThanCondition implements ChatCondition {

	private final int karma;

	/**
	 * Creates a new KarmaLessThanCondition.
	 *
	 * @param karma
	 *            karma
	 */
	public KarmaLessThanCondition(final int karma) {
		this.karma = karma;
	}

	/**
	 * @return true if players karma less then conditions Karma
	 */
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		return (player.getKarma() < karma);
	}

	@Override
	public String toString() {
		return "karma < " + karma + " ";
	}

	@Override
	public int hashCode() {
		return 43721 * karma;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof KarmaLessThanCondition)) {
			return false;
		}
		KarmaLessThanCondition other = (KarmaLessThanCondition) obj;
		return karma == other.karma;
	}

}
