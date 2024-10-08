/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
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

/**
 * factory for item related achievements.
 *
 * @author madmetzger
 */
public class AdosItemQuestAchievementsFactory extends AbstractAchievementFactory {
	public static final String ID_SUPPORTER = "quest.special.daily_item.0010";
	public static final String ID_PROVIDER = "quest.special.daily_item.0050";
	public static final String ID_SUPPLIER = "quest.special.daily_item.0100";
	public static final String ID_STOCKPILER = "quest.special.daily_item.0250";
	public static final String ID_HOARDER = "quest.special.daily_item.0500";
	public static final String ID_LIFEBLOOD = "quest.special.daily_item.1000";

	@Override
	protected Category getCategory() {
		return Category.QUEST_ADOS_ITEMS;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_SUPPORTER, "Pomocnik Ados",
				"Ukończono dzienne zadanie na przedmiot 10 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 9)));

		achievements.add(createAchievement(
				ID_PROVIDER, "Dostawca Ados",
				"Ukończono dzienne zadanie na przedmiot 50 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 49)));

		achievements.add(createAchievement(
				ID_SUPPLIER, "Główny Dostawca Ados",
				"Ukończono dzienne zadanie na przedmiot 100 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 99)));

		achievements.add(createAchievement(
				ID_STOCKPILER, "Magazynier Ados",
				"Ukończono dzienne zadanie na przedmiot 250 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 249)));

		achievements.add(createAchievement(
				ID_HOARDER, "Skarbnik Ados",
				"Ukończono dzienne zadanie na przedmiot 500 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 499)));

		achievements.add(createAchievement(
				ID_LIFEBLOOD, "Krew Ados",
				"Ukończono dzienne zadanie na przedmiot 1,000 razy",
				Achievement.EXTREME_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_item", 2, 999)));

		return achievements;
	}
}
