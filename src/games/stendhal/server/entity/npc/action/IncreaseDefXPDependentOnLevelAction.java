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
 * Created a new Increases DEF XP Dependent on level.
 */
@Dev(category=Category.STATS, label="DefXP+")
public class IncreaseDefXPDependentOnLevelAction implements ChatAction {

	private final double defXpDiff;
	private final double karmabonus;

	/**
	 * Creates a new IncreaseDefXPDependentOnLevelAction.
	 *
	 * @param defXpDiff - player will get 1/defXpDiff of difference between his and next levels xp amount.
	 * @param karmabonus - amount of karma to add instead def_xp if player have max level
	 */
	public IncreaseDefXPDependentOnLevelAction(final double defXpDiff, final double karmabonus) {
		this.defXpDiff = defXpDiff;
		this.karmabonus = karmabonus;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		final long start = Level.getXP(player.getLevel());
		final long next = Level.getXP(player.getLevel() + 1);
		if(!Occasion.SECOND_WORLD) {
			if (player.getDef() < 140) {
				long reward = (long) ((next - start) / (defXpDiff) / 20);
				long total = reward + player.getDefXP();
				player.setDefXP((int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, total)));
			} else {
				long reward = (long) ((next - start) / (defXpDiff) / 60);
				long total = reward + player.getDefXP();
				player.setDefXP((int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, total)));
			}
		}
		player.notifyWorldAboutChanges();
	}

	@Override
	public String toString() {
		return "IncreaseDefXPDependentOnLevel <" + defXpDiff + "," + karmabonus + ">";
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + (int)(defXpDiff);
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
		final IncreaseDefXPDependentOnLevelAction other = (IncreaseDefXPDependentOnLevelAction) obj;

		return defXpDiff == other.defXpDiff;
	}
}
