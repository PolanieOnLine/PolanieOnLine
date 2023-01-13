/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
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
public class IncreaseBaseHPOnlyOnceAction implements ChatAction {
	private final String questSlot;
	private final int baseHpDiff;

	/**
	 * Increments player base hp.
	 *
	 * @param questSlot
	 * 		Name of quest_slot.
	 * @param baseHpDiff
	 * 		How many health to increase for player.
	 */
	public IncreaseBaseHPOnlyOnceAction(final String questSlot, final int baseHpDiff) {
		this.questSlot = questSlot;
		this.baseHpDiff = baseHpDiff;
	}

	@Override
	public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
		if (player.getQuest(questSlot + "_basehp") == null) {
			player.setBaseHP(player.getBaseHP() + baseHpDiff);
			player.heal(baseHpDiff);

			player.setQuest(questSlot + "_basehp", "done");
			player.notifyWorldAboutChanges();
		}
	}

	@Override
	public String toString() {
		return "IncreaseBaseHPOnlyOnceAction <" + baseHpDiff + ">";
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
		final IncreaseBaseHPOnlyOnceAction other = (IncreaseBaseHPOnlyOnceAction) obj;

		return baseHpDiff == other.baseHpDiff;
	}

	public static ChatAction increaseBaseHPOnlyOnce(String questSlot, int baseHpDiff) {
		return new IncreaseBaseHPOnlyOnceAction(questSlot, baseHpDiff);
	}
}
