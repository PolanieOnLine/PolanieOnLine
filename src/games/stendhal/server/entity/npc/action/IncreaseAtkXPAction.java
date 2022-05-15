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
 * Increases ATK XP (quest reward).
 */
@Dev(category=Category.STATS, label="AtkXP+")
public class IncreaseAtkXPAction implements ChatAction {

	private final int atkXpDiff;

	/**
	 * Creates a new IncreaseAtkXPAction.
	 *
	 * @param atkXpDiff
	 *            amount of atk xp to add (in quests)
	 */
	public IncreaseAtkXPAction(final int atkXpDiff) {
		this.atkXpDiff = atkXpDiff;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		player.setAtkXP(atkXpDiff + player.getAtkXP());
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseAtkXP <" + atkXpDiff + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + atkXpDiff;
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
		final IncreaseAtkXPAction other = (IncreaseAtkXPAction) obj;
		
		return atkXpDiff == other.atkXpDiff;
	}

	public static ChatAction increaseAtkXP(int atkXpDiff) {
		return new IncreaseAtkXPAction(atkXpDiff);
	}
}