/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class LevelRewardQuest extends AbstractQuest {
	private static final class LevelRewardMilestone {
		private final int levelThreshold;
		private final String npcName;
		private final String region;
		private final String location;
		private final String rewardSlot;
		private final String slotName;
		private final String questName;
		private final String serverZoneName;
		private final String clientMapId;

		private LevelRewardMilestone(final int levelThreshold, final String npcName, final String region,
				final String location, final String rewardSlot, final String slotName, final String questName,
				final String serverZoneName, final String clientMapId) {
			this.levelThreshold = levelThreshold;
			this.npcName = npcName;
			this.region = region;
			this.location = location;
			this.rewardSlot = rewardSlot;
			this.slotName = slotName;
			this.questName = questName;
			this.serverZoneName = serverZoneName;
			this.clientMapId = clientMapId;
		}
	}

	private static final List<LevelRewardMilestone> MILESTONES = Collections.unmodifiableList(Arrays.asList(
			new LevelRewardMilestone(50, "Choros", Region.KOSCIELISKO, "jaskini Kościeliskiej", "ChorosReward",
					"level_reward_50", "Nagroda za poziom 50", "-3_koscielisko_cave", "sub_3_koscielisko_cave"),
			new LevelRewardMilestone(100, "Altharis", Region.ATHOR_ISLAND, "jaskini na Wyspie Athor",
					"AltharisReward", "level_reward_100", "Nagroda za poziom 100", "-1_athor_island",
					"sub_1_athor_island"),
			new LevelRewardMilestone(150, "Aenihata", Region.SEMOS_MINES, "kopalniach Semos", "AenihataReward",
					"level_reward_150", "Nagroda za poziom 150", "-2_semos_mine_w2", "sub_2_semos_mine_w2"),
			new LevelRewardMilestone(350, "Festris", Region.KIKAREUKIN, "jaskini Kikareukin", "FestrisReward",
					"level_reward_350", "Nagroda za poziom 350", "4_kikareukin_cave", "4_kikareukin_cave"),
			new LevelRewardMilestone(550, "Deviotis", Region.WIELICZKA, "kopalniach Wieliczki", "DeviotisReward",
					"level_reward_550", "Nagroda za poziom 550", "-1_wieliczka_salt_mines_e2",
					"sub_1_wieliczka_salt_mines_e2")));

	private final int levelThreshold;
	private final String npcName;
	private final String region;
	private final String location;
	private final String rewardSlot;
	private final String slotName;
	private final String questName;
	private final String serverZoneName;
	private final String clientMapId;

	public static List<LevelRewardQuest> getMilestoneQuests() {
		final List<LevelRewardQuest> quests = new ArrayList<>();
		for (final LevelRewardMilestone milestone : MILESTONES) {
			quests.add(new LevelRewardQuest(milestone));
		}
		return quests;
	}

	public LevelRewardQuest(final LevelRewardMilestone milestone) {
		this.levelThreshold = milestone.levelThreshold;
		this.npcName = milestone.npcName;
		this.region = milestone.region;
		this.location = milestone.location;
		this.rewardSlot = milestone.rewardSlot;
		this.slotName = milestone.slotName;
		this.questName = milestone.questName;
		this.serverZoneName = milestone.serverZoneName;
		this.clientMapId = milestone.clientMapId;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(questName,
				"Po osiągnięciu poziomu " + levelThreshold + " odbierz nagrodę od " + npcName + " w " + location
						+ " (" + region + "). Mapa serwera: " + serverZoneName + ", mapa klienta: " + clientMapId + ".",
				false);
	}

	@Override
	public void updatePlayer(final Player player) {
		if (isRewardDone(player)) {
			if (!player.hasQuest(slotName) || !player.isQuestCompleted(slotName)) {
				player.setQuest(slotName, "done");
			}
			return;
		}

		if (player.getLevel() >= levelThreshold && !player.hasQuest(slotName)) {
			player.setQuest(slotName, "active");
		}
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(slotName) && !isRewardDone(player)) {
			return res;
		}

		res.add("Osiągnąłem poziom " + levelThreshold + ".");
		if (isRewardDone(player)) {
			res.add("Odebrałem nagrodę od " + npcName + " w " + location + " (" + region + ").");
		} else {
			res.add("Mam odebrać nagrodę u " + npcName + " w " + location + " (" + region + ").");
		}
		return res;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return isRewardDone(player) || super.isCompleted(player);
	}

	@Override
	public String getSlotName() {
		return slotName;
	}

	@Override
	public String getName() {
		return questName;
	}

	@Override
	public int getMinLevel() {
		return levelThreshold;
	}

	@Override
	public String getRegion() {
		return region;
	}

	@Override
	public String getNPCName() {
		return npcName;
	}

	private boolean isRewardDone(final Player player) {
		return player.hasQuest(rewardSlot) && player.isQuestCompleted(rewardSlot);
	}
}
