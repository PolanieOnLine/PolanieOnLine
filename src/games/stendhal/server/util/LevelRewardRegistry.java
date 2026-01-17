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
package games.stendhal.server.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.server.entity.player.Player;

public final class LevelRewardRegistry {
	private static final String QUEST_STATUS_STARTED = "started";
	private static final String NOTIFY_PREFIX = "level_reward_notify_";

	private static final Map<Integer, LevelRewardDefinition> DEFINITIONS = buildDefinitions();

	private static Map<Integer, LevelRewardDefinition> buildDefinitions() {
		Map<Integer, LevelRewardDefinition> definitions = new LinkedHashMap<Integer, LevelRewardDefinition>();
		addDefinition(definitions, new LevelRewardDefinition(50, "ChorosReward", "ChorosFirstChat", "Choros", "-3_koscielisko_cave",
			"Twoje czyny niosą się pośród kapłanów. Przybądź po moje nauki do jaskiń.",
			"LevelReward50"));
		addDefinition(definitions, new LevelRewardDefinition(100, "AltharisReward", "AltharisFirstChat", "Altharis", "-1_athor_island",
			"Zebrałeś dość doświadczenia, by usłyszeć więcej. Szukaj mnie w mroku wyspy.",
			"LevelReward100"));
		addDefinition(definitions, new LevelRewardDefinition(150, "AenihataReward", "AenihataFirstChat", "Aenihata", "-2_semos_mine_w2",
			"Bariera słabnie, a ja potrzebuję wojownika. Spotkaj się ze mną w kopalni.",
			"LevelReward150"));
		addDefinition(definitions, new LevelRewardDefinition(350, "FestrisReward", "FestrisFirstChat", "Festris", "4_kikareukin_cave",
			"Twoja potęga dojrzewa. Przybądź do mojej groty, bym mogła przekazać ci tajemnice.",
			"LevelReward350"));
		addDefinition(definitions, new LevelRewardDefinition(550, "DeviotisReward", "DeviotisFirstChat", "Deviotis", "-1_wieliczka_salt_mines_e2",
			"Zasłużyłeś na dalsze nauki. Odnajdź mnie w solnych korytarzach Wieliczki.",
			"LevelReward550"));
		return Collections.unmodifiableMap(definitions);
	}

	private static void addDefinition(final Map<Integer, LevelRewardDefinition> definitions,
			final LevelRewardDefinition definition) {
		definitions.put(definition.getLevelThreshold(), definition);
	}

	private LevelRewardRegistry() {
	}

	public static Map<Integer, LevelRewardDefinition> getDefinitions() {
		return DEFINITIONS;
	}

	public static void checkAndInitiate(final Player player) {
		for (LevelRewardDefinition definition : DEFINITIONS.values()) {
			if (player.getLevel() >= definition.getLevelThreshold()
					&& !player.hasQuest(definition.getRewardQuestSlot())) {
				initiate(player, definition);
			}
		}
	}

	private static void initiate(final Player player, final LevelRewardDefinition definition) {
		if (!player.hasQuest(definition.getQuestSlot())) {
			player.setQuest(definition.getQuestSlot(), QUEST_STATUS_STARTED);
		}
		final String notifySlot = NOTIFY_PREFIX + definition.getLevelThreshold();
		if (!player.hasQuest(notifySlot)) {
			player.setQuest(notifySlot, "done");
			player.sendPrivateText(definition.getNpcName(), definition.getStoryMessage()
					+ " (Lokacja: " + definition.getNpcZoneId() + ")");
		}
	}

	public static final class LevelRewardDefinition {
		private final int levelThreshold;
		private final String rewardQuestSlot;
		private final String firstChatSlot;
		private final String npcName;
		private final String npcZoneId;
		private final String storyMessage;
		private final String questSlot;

		private LevelRewardDefinition(final int levelThreshold, final String rewardQuestSlot, final String firstChatSlot,
				final String npcName, final String npcZoneId, final String storyMessage, final String questSlot) {
			this.levelThreshold = levelThreshold;
			this.rewardQuestSlot = rewardQuestSlot;
			this.firstChatSlot = firstChatSlot;
			this.npcName = npcName;
			this.npcZoneId = npcZoneId;
			this.storyMessage = storyMessage;
			this.questSlot = questSlot;
		}

		public int getLevelThreshold() {
			return levelThreshold;
		}

		public String getRewardQuestSlot() {
			return rewardQuestSlot;
		}

		public String getFirstChatSlot() {
			return firstChatSlot;
		}

		public String getNpcName() {
			return npcName;
		}

		public String getNpcZoneId() {
			return npcZoneId;
		}

		public String getStoryMessage() {
			return storyMessage;
		}

		public String getQuestSlot() {
			return questSlot;
		}
	}
}
