/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.condition;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

@Dev(category=Category.QUEST_SLOT, label="Completed?")
public class QuestsCountCompletedCondition implements ChatCondition {
	private final List<String> questIds;
	private final int requiredCount;

	/**
	 * Creates a new QuestsCountCompletedCondition.
	 *
	 * @param questIds
	 *		List of questSlot names
	 */
	public QuestsCountCompletedCondition(final List<String> questIds, final int requiredCount) {
		this.questIds = checkNotNull(questIds);
		this.requiredCount = requiredCount;
	}

	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		int completedCount = (int) questIds.stream()
			.filter(player::isQuestCompleted)
			.limit(requiredCount)
			.count();

		return completedCount >= requiredCount;
	}

	@Override
	public String toString() {
		return "QuestsCompleted <" + questIds + ">";
	}

	@Override
	public int hashCode() {
		return 45779 * questIds.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof QuestsCountCompletedCondition)) {
			return false;
		}
		QuestsCountCompletedCondition other = (QuestsCountCompletedCondition) obj;
		return questIds.equals(other.questIds);
	}
}
