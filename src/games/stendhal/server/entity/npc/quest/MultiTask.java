/***************************************************************************
 *                   (C) Copyright 2022 - PolanieOnLine                    *
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import marauroa.common.Pair;

public class MultiTask extends QuestTaskBuilder {
	private HashMap<String, Pair<Integer, Integer>> requestKill = new HashMap<>();
	private List<Pair<String, Integer>> requestItem = new LinkedList<>();

	public MultiTask requestKill(int count, String name) {
		requestKill.put(name, new Pair<Integer, Integer>(0, count));
		return this;
	}

	public MultiTask requestItem(int quantity, String name) {
		requestItem.add(new Pair<String, Integer>(name, quantity));
		return this;
	}

	@Override
	ChatAction buildStartQuestAction(String questSlot) {
		if (!requestKill.isEmpty()) {
			return new StartRecordingKillsAction(questSlot, 1, requestKill);
		}
		return null;
	}

	@Override
	ChatCondition buildQuestCompletedCondition(String questSlot) {
		List<ChatCondition> conditions = new LinkedList<>();
		if (!requestKill.isEmpty()) {
			conditions.add(new KilledForQuestCondition(questSlot, 1));
		}
		if (!requestItem.isEmpty()) {
			for (Pair<String, Integer> item : requestItem) {
				conditions.add(new PlayerHasItemWithHimCondition(item.first(), item.second()));
			}
		}
		return new AndCondition(conditions);
	}

	@Override
	ChatAction buildQuestCompleteAction(String questSlot) {
		if (!requestItem.isEmpty()) {
			List<ChatAction> dropItem = new LinkedList<>();
			for (Pair<String, Integer> item : requestItem) {
				dropItem.add(new DropItemAction(item.first(), item.second()));
			}
			return new MultipleActions(dropItem);
		}
		return null;
	}

	@Override
	void simulate(QuestSimulator simulator) {
		simulator.info("");
	}
}
