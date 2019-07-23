/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
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
 * Created a new Increases RATK XP Dependent on level.
 * 
 * @author zekkeq
 */
@Dev(category=Category.STATS, label="RatkXP+")
public class IncreaseRatkXPDependentOnLevelAction implements ChatAction {

	private final double ratk_xpDiff;
	private final double karmabonus;
	/**
	 * Creates a new IncreaseRatkXPDependentOnLevelAction.
	 *
	 * @param atk_xpDiff - player will get 1/xpDiff of difference between his and next levels xp amount.
	 * @param karmabonus - amount of karma to add instead ratk_xp if player have max level
	 */
	public IncreaseRatkXPDependentOnLevelAction(final double ratk_xpDiff, final double karmabonus) {
		this.ratk_xpDiff = ratk_xpDiff;
		this.karmabonus = karmabonus;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final int start = Level.getXP(player.getLevel());
		final int next = Level.getXP(player.getLevel() + 1);
		int reward = (int) (((next - start) / (ratk_xpDiff) / 10) / 2);
		player.setRatkXP(reward + player.getRatkXP());
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseRatkXPDependentOnLevel <" + ratk_xpDiff + "," + karmabonus + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int)(ratk_xpDiff);
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
		final IncreaseRatkXPDependentOnLevelAction other = (IncreaseRatkXPDependentOnLevelAction) obj;
		if (ratk_xpDiff != other.ratk_xpDiff) {
			return false;
		}
		return true;
	}

}
