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

import games.stendhal.common.Level;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Created a new Increases ATK XP Dependent on level.
 * 
 * @author KarajuSs
 */
@Dev(category=Category.STATS, label="AtkXP+")
public class IncreaseAtkXPDependentOnLevelAction implements ChatAction {

	private final double atk_xpDiff;
	private final double karmabonus;
	/**
	 * Creates a new IncreaseAtkXPDependentOnLevelAction.
	 *
	 * @param atk_xpDiff - player will get 1/xpDiff of difference between his and next levels xp amount.
	 * @param karmabonus - amount of karma to add instead atk_xp if player have max level
	 */
	public IncreaseAtkXPDependentOnLevelAction(final double atk_xpDiff, final double karmabonus) {
		this.atk_xpDiff = atk_xpDiff;
		this.karmabonus = karmabonus;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final int start = Level.getXP(player.getLevel());
		final int next = Level.getXP(player.getLevel() + 1);
		int reward = (int) (((next - start) / (atk_xpDiff) / 10) / 2);
		player.setAtkXP(reward + player.getAtkXP());
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseAtkXPDependentOnLevel <" + atk_xpDiff + "," + karmabonus + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int)(atk_xpDiff);
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
		final IncreaseAtkXPDependentOnLevelAction other = (IncreaseAtkXPDependentOnLevelAction) obj;
		if (atk_xpDiff != other.atk_xpDiff) {
			return false;
		}
		return true;
	}

}
