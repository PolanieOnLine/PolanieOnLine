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
import java.util.List;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class LevelRewardQuest extends AbstractQuest {
	private final int levelThreshold;
	private final String npcName;
	private final String region;
	private final String location;
	private final String rewardSlot;
	private final String slotName;
	private final String questName;

	public static List<LevelRewardQuest> getMilestoneQuests() {
		return Arrays.asList(
				new LevelRewardQuest(50, "Choros", Region.KOSCIELISKO, "jaskini Kościeliskiej", "ChorosReward",
						"level_reward_50", "Nagroda za poziom 50"),
				new LevelRewardQuest(100, "Altharis", Region.ATHOR_ISLAND, "jaskini na Wyspie Athor", "AltharisReward",
						"level_reward_100", "Nagroda za poziom 100"),
				new LevelRewardQuest(150, "Aenihata", Region.SEMOS_MINES, "kopalniach Semos", "AenihataReward",
						"level_reward_150", "Nagroda za poziom 150"),
				new LevelRewardQuest(350, "Festris", Region.SEMOS_SURROUNDS, "jaskini Kikareukin", "FestrisReward",
						"level_reward_350", "Nagroda za poziom 350"),
				new LevelRewardQuest(550, "Deviotis", Region.WIELICZKA, "kopalniach Wieliczki", "DeviotisReward",
						"level_reward_550", "Nagroda za poziom 550"));
	}

	public LevelRewardQuest(final int levelThreshold, final String npcName, final String region, final String location,
			final String rewardSlot, final String slotName, final String questName) {
		this.levelThreshold = levelThreshold;
		this.npcName = npcName;
		this.region = region;
		this.location = location;
		this.rewardSlot = rewardSlot;
		this.slotName = slotName;
		this.questName = questName;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(questName, "Po osiągnięciu poziomu " + levelThreshold + " odbierz nagrodę od " + npcName + " w "
				+ location + " (" + region + ").", false);
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
