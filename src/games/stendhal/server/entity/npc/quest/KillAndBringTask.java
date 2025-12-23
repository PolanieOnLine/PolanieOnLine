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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Pair;

public class KillAndBringTask extends QuestTaskBuilder {
	private LinkedHashMap<String, Pair<Integer, Integer>> requestKill = new LinkedHashMap<>();
	private List<Pair<String, Integer>> requestItem = new LinkedList<>();

	public KillAndBringTask requestKill(int count, String name) {
		requestKill.put(name, new Pair<Integer, Integer>(0, count));
		return this;
	}

	public KillAndBringTask requestItem(int quantity, String name) {
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
	List<String> calculateHistoryProgress(QuestHistoryBuilder history, Player player, String questSlot) {
		if (isCompleted(player, questSlot)) {
			return null;
		}

		List<String> res = new LinkedList<>();
		if (requestKill.isEmpty() && requestItem.isEmpty()) {
			return res;
		}

		List<String> requirements = new LinkedList<>();

		if (!requestKill.isEmpty()) {
			Map<String, int[]> recordedKills = new LinkedHashMap<>();
			String questData = player.getQuest(questSlot, 1);
			if (questData != null && !questData.isEmpty()) {
				List<String> tokens = Arrays.asList(questData.split(","));
				if (tokens.size() % 5 == 0) {
					for (int i = 0; i < tokens.size() / 5; i++) {
						String creatureName = tokens.get(i * 5);
						try {
							int requiredSolo = Integer.parseInt(tokens.get(i * 5 + 1));
							int requiredShared = Integer.parseInt(tokens.get(i * 5 + 2));
							int killedSolo = Integer.parseInt(tokens.get(i * 5 + 3));
							int killedShared = Integer.parseInt(tokens.get(i * 5 + 4));
							recordedKills.put(creatureName,
									new int[] { requiredSolo, requiredShared, killedSolo, killedShared });
						} catch (NumberFormatException ignored) {
							recordedKills.clear();
							break;
						}
					}
				}
			}

			for (Map.Entry<String, Pair<Integer, Integer>> entry : requestKill.entrySet()) {
				String creatureName = entry.getKey();
				int requiredSolo = entry.getValue().first();
				int requiredShared = entry.getValue().second();
				int baseSolo = 0;
				int baseShared = 0;
				int[] recorded = recordedKills.get(creatureName);
				if (recorded != null) {
					requiredSolo = recorded[0];
					requiredShared = recorded[1];
					baseSolo = recorded[2];
					baseShared = recorded[3];
				}
				int currentSolo = Math.max(0, player.getSoloKill(creatureName) - baseSolo);
				int currentShared = Math.max(0, player.getSharedKill(creatureName) - baseShared);
				int currentTotal = Math.max(0, currentSolo + currentShared);
				if (requiredSolo > 0 && requiredShared > requiredSolo) {
					requirements.add(
							creatureName + ": " + Math.min(currentSolo, requiredSolo) + "/" + requiredSolo + " (solo), "
									+ Math.min(currentTotal, requiredShared) + "/" + requiredShared + " (łącznie)");
				} else if (requiredSolo > 0) {
					requirements.add(creatureName + ": " + Math.min(currentSolo, requiredSolo) + "/" + requiredSolo);
				} else {
					requirements
							.add(creatureName + ": " + Math.min(currentTotal, requiredShared) + "/" + requiredShared);
				}
			}
		}

		if (!requestItem.isEmpty()) {
			for (Pair<String, Integer> item : requestItem) {
				int current = Math.min(player.getNumberOfSubmittableEquipped(item.first()), item.second());
				requirements.add(item.first() + ": " + current + "/" + item.second());
			}
		}

		String block = buildRequirementsBlock(requirements);
		if (block != null) {
			res.add(block);
		}

		return res;
	}

	@Override
	void simulate(QuestSimulator simulator) {
		simulator.info("");
	}
}
