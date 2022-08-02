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

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import marauroa.common.Pair;

public class BringMultiItemsTask extends QuestTaskBuilder {
	private List<Pair<String, Integer>> requestItem = new LinkedList<>();

	/**
	 * request an item from the player
	 *
	 * @param quantity quantity of item
	 * @param name name of item
	 * @return BringMultiItemsTask
	 */
	public BringMultiItemsTask requestItem(int quantity, String name) {
		requestItem.add(new Pair<String, Integer>(name, quantity));
		return this;
	}

	@Override
	ChatAction buildStartQuestAction(String questSlot) {
		return null;
	}

	@Override
	ChatCondition buildQuestCompletedCondition(String questSlot) {
		List<ChatCondition> conditions = new LinkedList<>();
		for (Pair<String, Integer> item : requestItem) {
			conditions.add(new PlayerHasItemWithHimCondition(item.first(), item.second()));
		}
		return new AndCondition(conditions);
	}

	@Override
	ChatAction buildQuestCompleteAction(String questSlot) {
		List<ChatAction> dropItem = new LinkedList<>();
		for (Pair<String, Integer> item : requestItem) {
			dropItem.add(new DropItemAction(item.first(), item.second()));
		}
		return new MultipleActions(dropItem);
	}

	@Override
	void simulate(QuestSimulator simulator) {
		simulator.info("");
	}
}
