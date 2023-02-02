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

public class KirdnehItemAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_ARCHAEOLOGIST = "quest.special.weekly_item.0005";
	public static final String ID_DEDICATED = "quest.special.weekly_item.0025";
	public static final String ID_SENIOR = "quest.special.weekly_item.0050";
	public static final String ID_MASTER = "quest.special.weekly_item.0100";

	@Override
	protected Category getCategory() {
		return Category.QUEST_KIRDNEH_ITEM;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_ARCHAEOLOGIST, "Archeolog", "Ukończył tygodniowe zadanie na przedmiot 5 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("weekly_item", 2, 4)));

		achievements.add(createAchievement(
				ID_DEDICATED, "Dedykowany Archeolog", "Ukończył tygodniowe zadanie na przedmiot 25 razy", 
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("weekly_item", 2, 24)));

		achievements.add(createAchievement(
				ID_SENIOR, "Starszy Archeolog", "Ukończył tygodniowe zadanie na przedmiot 50 razy", 
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("weekly_item", 2, 49)));

		achievements.add(createAchievement(
				ID_MASTER, "Mistrz Archeolog", "Ukończył tygodniowe zadanie na przedmiot 100 razy", 
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("weekly_item", 2, 99)));

		return achievements;
	}

}
