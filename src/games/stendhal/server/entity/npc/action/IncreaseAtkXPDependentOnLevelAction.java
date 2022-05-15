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

import games.stendhal.common.Level;
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/**
 * Created a new Increases ATK XP Dependent on level.
 */
@Dev(category=Category.STATS, label="AtkXP+")
public class IncreaseAtkXPDependentOnLevelAction implements ChatAction {

	private final double atkXpDiff;
	private final double karmabonus;

	/**
	 * Creates a new IncreaseAtkXPDependentOnLevelAction.
	 *
	 * @param atkXpDiff - player will get 1/atkXpDiff of difference between his and next levels xp amount.
	 * @param karmabonus - amount of karma to add instead atk_xp if player have max level
	 */
	public IncreaseAtkXPDependentOnLevelAction(final double atkXpDiff, final double karmabonus) {
		this.atkXpDiff = atkXpDiff;
		this.karmabonus = karmabonus;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final int start = Level.getXP(player.getLevel());
		final int next = Level.getXP(player.getLevel() + 1);
		if(!Occasion.SECOND_WORLD) {
			if (player.getAtk() < 110) {
				int reward = (int) ((next - start) / (atkXpDiff) / 20);
				player.setAtkXP(reward + player.getAtkXP());
			} else {
				int reward = (int) ((next - start) / (atkXpDiff) / 60);
				player.setAtkXP(reward + player.getAtkXP());
			}
		}
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseAtkXPDependentOnLevel <" + atkXpDiff + "," + karmabonus + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int)(atkXpDiff);
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

		return atkXpDiff == other.atkXpDiff;
	}
}
