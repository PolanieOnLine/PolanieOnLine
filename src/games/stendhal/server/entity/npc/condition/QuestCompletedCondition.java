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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConditionBuilder;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.IQuest;
import java.util.HashSet;
import java.util.Set;

/**
 * Was this quest completed?
 */
@Dev(category=Category.QUEST_SLOT, label="Completed?")
public class QuestCompletedCondition implements ChatCondition {
	private final String questName;
	private static final ThreadLocal<Set<String>> evaluating = ThreadLocal.withInitial(HashSet::new);

	/**
	 * Creates a new QuestCompletedCondition.
	 *
	 * @param questname
	 *				name of quest-slot
	 */
	public QuestCompletedCondition(final String questName) {
		this.questName = checkNotNull(questName);
	}

	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		final Set<String> active = evaluating.get();
		if (!active.add(questName)) {
			return player.isQuestCompleted(questName);
		}
		try {
			final IQuest quest = StendhalQuestSystem.get().getQuestFromSlot(questName);
			if (quest != null) {
				return quest.isCompleted(player);
			}
			return player.isQuestCompleted(questName);
		} finally {
			active.remove(questName);
			if (active.isEmpty()) {
				evaluating.remove();
			}
		}
	}

	@Override
	public String toString() {
		return "QuestCompleted <" + questName + ">";
	}

	@Override
	public int hashCode() {
		return 45779 * questName.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof QuestCompletedCondition)) {
			return false;
		}
		QuestCompletedCondition other = (QuestCompletedCondition) obj;
		return questName.equals(other.questName);
	}

	public static ConditionBuilder questCompleted(String questName) {
		return new ConditionBuilder(new QuestCompletedCondition(questName));
	}
}
