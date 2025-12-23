/***************************************************************************
 *                   (C) Copyright 2022 - Faiumoni e.V.                    *
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
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Pair;

/**
 * requests that the player kills creatures
 *
 * @author hendrik
 */
public class KillCreaturesTask extends QuestTaskBuilder {
	private LinkedHashMap<String, Pair<Integer, Integer>> requestKill = new LinkedHashMap<>();

	// hide constructor
	KillCreaturesTask() {
		super();
	}

	/**
	 * request killing creatures
	 *
	 * @param count number of this creature
	 * @param name  name of a creature
	 * @return KillCreaturesTask
	 */
	public KillCreaturesTask requestKill(int count, String name) {
		requestKill.put(name, new Pair<Integer, Integer>(0, count));
		return this;
	}

	@Override
	void simulate(QuestSimulator simulator) {
		for (Map.Entry<String, Pair<Integer, Integer>> entry : requestKill.entrySet()) {
			simulator.info("Player killed " + entry.getValue().first() + "+" + entry.getValue().second() + " "
					+ entry.getKey() + ".");
		}
		simulator.info("");
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
		return new KilledForQuestCondition(questSlot, 1);
	}

	@Override
	ChatAction buildQuestCompleteAction(String questSlot) {
		return null;
	}

	@Override
	List<String> calculateHistoryProgress(QuestHistoryBuilder history, Player player, String questSlot) {
		if (isCompleted(player, questSlot)) {
			return null;
		}

		List<String> res = new LinkedList<>();
		if (requestKill.isEmpty()) {
			return res;
		}

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

		List<String> requirements = new LinkedList<>();
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
				requirements.add(creatureName + ": " + Math.min(currentSolo, requiredSolo) + "/" + requiredSolo
						+ " (solo), " + Math.min(currentTotal, requiredShared) + "/" + requiredShared + " (łącznie)");
			} else if (requiredSolo > 0) {
				requirements.add(creatureName + ": " + Math.min(currentSolo, requiredSolo) + "/" + requiredSolo);
			} else {
				requirements.add(creatureName + ": " + Math.min(currentTotal, requiredShared) + "/" + requiredShared);
			}
		}

		String block = buildRequirementsBlock(requirements);
		if (block != null) {
			res.add(block);
		}

		return res;
	}
}
