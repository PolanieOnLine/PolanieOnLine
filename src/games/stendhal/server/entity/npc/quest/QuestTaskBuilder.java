/***************************************************************************
 *                 (C) Copyright 2022-2023 - Faiumoni e.V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AlwaysTrueCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * abstact base class for QuestTasks
 *
 * @author hendrik
 */
public abstract class QuestTaskBuilder {
	static final String REQUIREMENTS_MARKER = "<requirements>";

	private final List<String> requiredCompletedQuests = new LinkedList<>();

	// hide constructor
	QuestTaskBuilder() {
		super();
	}

	/**
	 * Requires another quest to be completed before this quest can be started.
	 *
	 * @param questSlot quest slot which has to be completed
	 * @return this task builder
	 */
	public QuestTaskBuilder requireCompletedQuest(String questSlot) {
		requiredCompletedQuests.add(questSlot);
		return this;
	}

	/**
	 * Requires other quests to be completed before this quest can be started.
	 *
	 * @param questSlots quest slots which have to be completed
	 * @return this task builder
	 */
	public QuestTaskBuilder requireCompletedQuest(String... questSlots) {
		for (String questSlot : questSlots) {
			requiredCompletedQuests.add(questSlot);
		}
		return this;
	}

	abstract void simulate(QuestSimulator simulator);

	ChatCondition buildQuestPreCondition(@SuppressWarnings("unused") String questSlot) {
		if (requiredCompletedQuests.isEmpty()) {
			return new AlwaysTrueCondition();
		}

		List<ChatCondition> conditions = new LinkedList<>();
		for (String requiredQuest : requiredCompletedQuests) {
			conditions.add(new QuestCompletedCondition(requiredQuest));
		}
		return new AndCondition(conditions);
	}

	abstract ChatAction buildStartQuestAction(String questSlot);

	ChatAction buildRejectQuestAction(@SuppressWarnings("unused") String questSlot) {
		return null;
	}

	abstract ChatCondition buildQuestCompletedCondition(String questSlot);

	abstract ChatAction buildQuestCompleteAction(String questSlot);

	boolean isCompleted(Player player, String questSlot) {
		return buildQuestCompletedCondition(questSlot).fire(player, null, null);
	}

	List<String> calculateHistoryProgress(@SuppressWarnings("unused") QuestHistoryBuilder history,
			@SuppressWarnings("unused") Player player, @SuppressWarnings("unused") String questSlot) {
		return null;
	}

	String buildRequirementsBlock(List<String> requirements) {
		if (requirements == null || requirements.isEmpty()) {
			return null;
		}

		StringBuilder block = new StringBuilder(REQUIREMENTS_MARKER);
		block.append("<br><br><big><b>Wymagania:</b></big>");
		for (String requirement : requirements) {
			block.append("<br>• ").append(requirement);
		}
		return block.toString();
	}
}
