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
import games.stendhal.common.constants.Testing;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Created a new Increases RATK XP Dependent on level.
 */
@Dev(category=Category.STATS, label="RatkXP+")
public class IncreaseRatkXPDependentOnLevelAction implements ChatAction {

	private final double ratkXpDiff;
	private final double karmabonus;

	/**
	 * Creates a new IncreaseRatkXPDependentOnLevelAction.
	 *
	 * @param ratkXpDiff - player will get 1/ratkXpDiff of difference between his and next levels xp amount.
	 * @param karmabonus - amount of karma to add instead ratk_xp if player have max level
	 */
	public IncreaseRatkXPDependentOnLevelAction(final double ratkXpDiff, final double karmabonus) {
		this.ratkXpDiff = ratkXpDiff;
		this.karmabonus = karmabonus;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final long start = Level.getXP(player.getLevel());
		final long next = Level.getXP(player.getLevel() + 1);
		if(Testing.COMBAT) {
			if (player.getRatk() < 90) {
				long reward = (long) ((next - start) / (ratkXpDiff) / 20);
				long total = reward + player.getRatkXP();
				player.setRatkXP((int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, total)));
			} else {
				long reward = (long) ((next - start) / (ratkXpDiff) / 60);
				long total = reward + player.getRatkXP();
				player.setRatkXP((int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, total)));
			}
		}
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseRatkXPDependentOnLevel <" + ratkXpDiff + "," + karmabonus + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int)(ratkXpDiff);
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

		return ratkXpDiff == other.ratkXpDiff;
	}
}
