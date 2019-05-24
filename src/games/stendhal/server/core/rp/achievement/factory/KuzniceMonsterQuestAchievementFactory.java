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
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

public class KuzniceMonsterQuestAchievementFactory extends AbstractAchievementFactory {

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> questAchievements = new LinkedList<Achievement>();
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0010", "Pomocna dłoń", "Ukończył codzienne zadanie na potwory 10 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 9)));
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0025", "Ochroniaż dzielnicy", "Ukończył codzienne zadanie na potwory 25 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 24)));
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0050", "Strażnik Kuźnic", "Ukończył codzienne zadanie na potwory 50 razy",
												Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 49)));
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0100", "Bohater Kuźnic", "Ukończył codzienne zadanie na potwory 100 razy",
												Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 99)));
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0250", "Prawa ręka Sołtysa", "Ukończył codzienne zadanie na potwory 250 razy",
												Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 249)));
		questAchievements.add(createAchievement("quest.special.daily_kuznice.0500", "Bohater Kuźnic", "Ukończył codzienne zadanie na potwory 500 razy",
												Achievement.LEGENDARY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_kuznice", 2, 499)));

		return questAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.QUEST_KUZNICE_MONSTER;
	}

}
