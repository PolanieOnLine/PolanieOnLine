/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.action;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Increases the base hp of the current player.
 */
@Dev(category=Category.STATS, label="BaseHP+")
public class IncreaseBaseHPAction implements ChatAction {
	private final int baseHpDiff;

	/**
	 * Creates a new IncreaseBaseHPAction.
	 *
	 * @param baseHpDiff
	 *            amount of basehp to add
	 */
	public IncreaseBaseHPAction(final int baseHpDiff) {
		this.baseHpDiff = baseHpDiff;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		player.setBaseHP(player.getBaseHP() + baseHpDiff);
		player.heal(baseHpDiff);

		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseBaseHP <" + baseHpDiff + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + baseHpDiff;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IncreaseBaseHPAction other = (IncreaseBaseHPAction) obj;

		return baseHpDiff == other.baseHpDiff;
	}

	public static ChatAction increaseBaseHP(int baseHpDiff) {
		return new IncreaseBaseHPAction(baseHpDiff);
	}
}
