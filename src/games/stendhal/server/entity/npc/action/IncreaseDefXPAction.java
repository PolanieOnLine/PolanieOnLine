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
package games.stendhal.server.entity.npc.action;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Increases DEF XP (quest reward).
 * @author Szygolek
 * 		@edited by KarajuSs
 * This script is based on another script already created by Arianne RPG developers. Thanks! Author: Szygolek * 
 */
@Dev(category=Category.STATS, label="DefXP+")
public class IncreaseDefXPAction implements ChatAction {

	private final int def_xpDiff;

	/**
	 * Creates a new IncreaseDefXPAction.
	 *
	 * @param def_xpdiff
	 *            amount of def xp to add (in quests)
	 */
	public IncreaseDefXPAction(final int def_xpDiff) {
		this.def_xpDiff = def_xpDiff;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		player.setDefXP(def_xpDiff + player.getDefXP());
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseDefXP <" + def_xpDiff + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + def_xpDiff;
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
		if (def_xpDiff != other.def_xpDiff) {
			return false;
		}
		return true;
	}

}
