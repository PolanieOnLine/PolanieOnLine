/***************************************************************************
 *                 (C) Copyright 2023-2024 - PolanieOnLine                 *
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
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AlwaysFalseCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KarmaGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasKilledNumberOfCreaturesCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.StringUtils;
import marauroa.common.Pair;

public class CraftItemTask extends QuestTaskBuilder {
	private String itemName;

	private int waitingTime = 0;

	private int playerMinLevel = -1;
	private int playerMinKarma = -1;

	private List<String> playerCompletedQuest = new LinkedList<>();
	private List<String> playerRequiredMonster = new LinkedList<>();

	private List<Pair<String, Integer>> requiredItem = new LinkedList<>();

	private String respondToCraft;
	private String respondToCraftReject;

	public CraftItemTask craftItem(String itemName) {
		this.itemName = itemName;
		return this;
	}

	public CraftItemTask waitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
		return this;
	}

	public CraftItemTask playerMinLevel(int playerMinLevel) {
		this.playerMinLevel = playerMinLevel;
		return this;
	}

	public CraftItemTask playerMinKarma(int playerMinKarma) {
		this.playerMinKarma = playerMinKarma;
		return this;
	}

	public CraftItemTask completedQuest(String questSlot) {
		playerCompletedQuest.add(questSlot);
		return this;
	}

	public CraftItemTask completedQuest(String... questSlots) {
		for (String questSlot : questSlots) {
			playerCompletedQuest.add(questSlot);
		}

		return this;
	}

	public CraftItemTask requiredItem(int quantity, String name) {
		requiredItem.add(new Pair<String, Integer>(name, quantity));
		return this;
	}

	public CraftItemTask requestMonster(String creature) {
		playerRequiredMonster.add(creature);
		return this;
	}

	public CraftItemTask requestMonster(String... creatures) {
		for (String creature : creatures) {
			playerRequiredMonster.add(creature);
		}
		return this;
	}

	public CraftItemTask respondToCraft(String respondToCraft) {
		this.respondToCraft = respondToCraft;
		return this;
	}

	public CraftItemTask respondToCraftReject(String respondToCraftReject) {
		this.respondToCraftReject = respondToCraftReject;
		return this;
	}

	// hide constructor
	CraftItemTask() {
		super();
	}

	private String getQuestName() {
		for (String questSlot : playerCompletedQuest) {
			return questSlot;
		}
		return null;
	}

	private String getMonsterName() {
		for (String creature : playerRequiredMonster) {
			return creature;
		}
		return null;
	}

	ChatCondition requiredConditionsBeforeForge() {
		List<ChatCondition> conditions = new LinkedList<>();
		if (getQuestName() != null) {
			conditions.add(new QuestCompletedCondition(getQuestName()));
		}
		if (playerMinLevel > -1) {
			conditions.add(new LevelGreaterThanCondition(playerMinLevel - 1));
		}
		if (playerMinKarma > -1) {
			conditions.add(new KarmaGreaterThanCondition(playerMinKarma - 1));
		}
		if (getMonsterName() != null) {
			conditions.add(new PlayerHasKilledNumberOfCreaturesCondition(1, getMonsterName()));
		}
		return new AndCondition(conditions);
	}

	boolean playerHaveConditions(Player player) {
		boolean isTrue = true;

		if (playerMinLevel > -1 && player.getLevel() < playerMinLevel) {
			isTrue = false;
		}

		if (playerMinKarma > -1 && player.getKarma() < playerMinKarma) {
			isTrue = false;
		}

		if (getQuestName() != null && !player.isQuestCompleted(getQuestName())) {
			isTrue = false;
		}

		if (getMonsterName() != null && !player.hasKilled(getMonsterName())) {
			isTrue = false;
		}

		return isTrue;
	}

	ChatCondition requeredItemsToStartForging() {
		List<ChatCondition> condition = new LinkedList<>();
		for (Pair<String, Integer> item : requiredItem) {
			condition.add(new PlayerHasItemWithHimCondition(item.first(), item.second()));
		}
		return new AndCondition(condition);
	}
	
	boolean playerIsEquippedRequiredItems(Player player) {
		for (Pair<String, Integer> item : requiredItem) {
			if (player.isEquipped(item.first(), item.second())) {
				return true;
			}
		}
		return false;
	}

	ChatAction dropRequeredItemsToForge() {
		List<ChatAction> dropItem = new LinkedList<>();
		for (Pair<String, Integer> item : requiredItem) {
			dropItem.add(new DropItemAction(item.first(), item.second()));
		}
		return new MultipleActions(dropItem);
	}

	@Override
	void simulate(QuestSimulator simulator) {
	}

	@Override
	ChatAction buildStartQuestAction(String questSlot) {
		throw new RuntimeException("not implemented");
	}

	ChatAction buildStartQuestAction(String questSlot, String respondToStart, String respondToReject) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				Map<String, String> params = new HashMap<>();
				params.put("itemName", getItemName());

				if (playerHaveConditions(player)) {
					npc.say(StringUtils.substitute(respondToStart, params));
					player.setQuest(questSlot, 0, "start");
				} else {
					npc.say(respondToReject);
				}
			}
		};
	}

	ChatAction buildForgeQuestAction(String questSlot) {
		return new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				Map<String, String> params = new HashMap<>();
				params.put("itemName", getItemName());
				params.put("time", Grammar.quantityplnoun(getProductionTime(), "minuta"));

				if (playerIsEquippedRequiredItems(player)) {
					npc.say(StringUtils.substitute(respondToCraft, params));
					player.setQuest(questSlot, 0, "forging");
					new SetQuestToTimeStampAction(questSlot, 1).fire(player, null, npc);
				} else {
					npc.say(respondToCraftReject);
				}
			}
		};
	}

	@Override
	ChatCondition buildQuestCompletedCondition(String questSlot) {
		if (getProductionTime() > -1) {
			return new TimePassedCondition(questSlot, 1, getProductionTime());
		}
		return new AlwaysFalseCondition();
	}

	@Override
	ChatAction buildQuestCompleteAction(String questSlot) {
		return new EquipItemAction(getItemName(), 1, true);
	}

	String getItemName() {
		return itemName;
	}

	int getProductionTime() {
		return waitingTime;
	}

	@Override
	List<String> calculateHistoryProgress(QuestHistoryBuilder historyBuilder, Player player, String questSlot) {
		CraftItemQuestHistoryBuilder history = (CraftItemQuestHistoryBuilder) historyBuilder;
		List<String> res = new LinkedList<>();
		final String questState = player.getQuest(questSlot, 0);
		List<String> requirements = new LinkedList<>();

		if (playerMinLevel > -1) {
			int current = Math.min(player.getLevel(), playerMinLevel);
			requirements.add("Poziom: " + current + "/" + playerMinLevel);
		}
		if (playerMinKarma > -1) {
			int current = (int) Math.min(player.getKarma(), playerMinKarma);
			requirements.add("Karma: " + current + "/" + playerMinKarma);
		}
		if (getQuestName() != null) {
			boolean completed = player.isQuestCompleted(getQuestName());
			requirements.add("Ukończ zadanie: " + getQuestName() + " (" + (completed ? "tak" : "nie") + ")");
		}
		if (getMonsterName() != null) {
			int totalKills = player.getSoloKill(getMonsterName()) + player.getSharedKill(getMonsterName());
			int current = Math.min(totalKills, 1);
			requirements.add("Pokonaj: " + getMonsterName() + " (" + current + "/1)");
		}
		for (Pair<String, Integer> item : requiredItem) {
			int current = Math.min(player.getNumberOfSubmittableEquipped(item.first()), item.second());
			requirements.add(item.first() + ": " + current + "/" + item.second());
		}

		String block = buildRequirementsBlock(requirements);
		if (block != null) {
			res.add(block);
		}

		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(questSlot, 1, getProductionTime()).fire(player, null, null)) {
				res.add(history.whenTimeWasPassed());
			} else {
				res.add(history.whenTimeWasNotEnded());
			}
		}
		return res;
	}
}
