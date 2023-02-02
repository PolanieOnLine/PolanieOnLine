/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

public class KuzniceMonsterQuestAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_HELPING_HAND = "quest.special.daily_kuznice_kill_monster.0010";
	public static final String ID_DISTRICT_GUARD = "quest.special.daily_kuznice_kill_monster.0025";
	public static final String ID_KUZNICE_GUARD = "quest.special.daily_kuznice_kill_monster.0050";
	public static final String ID_RIGHT_HAND = "quest.special.daily_kuznice_kill_monster.0100";

	@Override
	protected Category getCategory() {
		return Category.QUEST_KUZNICE_MONSTER;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_HELPING_HAND, "Pomocna Dłoń", "Ukończył co dwudniowe zadanie na potwory 10 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_kuznice_kill_monster", 2, 9)));

		achievements.add(createAchievement(
				ID_DISTRICT_GUARD, "Ochroniarz Dzielnicy", "Ukończył co dwudniowe zadanie na potwory 25 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_kuznice_kill_monster", 2, 24)));

		achievements.add(createAchievement(
				ID_KUZNICE_GUARD, "Strażnik Kuźnic", "Ukończył co dwudniowe zadanie na potwory 50 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_kuznice_kill_monster", 2, 49)));

		achievements.add(createAchievement(
				ID_RIGHT_HAND, "Prawa Ręka Sołtysa", "Ukończył co dwudniowe zadanie na potwory 100 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_kuznice_kill_monster", 2, 99)));

		return achievements;
	}
}
