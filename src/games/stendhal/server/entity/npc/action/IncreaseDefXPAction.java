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
 * Increases DEF XP (quest reward).
 */
@Dev(category=Category.STATS, label="DefXP+")
public class IncreaseDefXPAction implements ChatAction {

	private final int defXpDiff;

	/**
	 * Creates a new IncreaseDefXPAction.
	 *
	 * @param defXpDiff
	 *            amount of def xp to add (in quests)
	 */
	public IncreaseDefXPAction(final int defXpDiff) {
		this.defXpDiff = defXpDiff;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		player.setDefXP(defXpDiff + player.getDefXP());
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseDefXP <" + defXpDiff + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + defXpDiff;
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
		final IncreaseDefXPAction other = (IncreaseDefXPAction) obj;

		return defXpDiff == other.defXpDiff;
	}

	public static ChatAction increaseDefXP(int defXpDiff) {
		return new IncreaseDefXPAction(defXpDiff);
	}
}
