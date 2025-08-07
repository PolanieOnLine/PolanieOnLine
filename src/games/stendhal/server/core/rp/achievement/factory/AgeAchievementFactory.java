/***************************************************************************
 *                   (C) Copyright 2003-2025 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;

/**
 * Factory for age achievements
 *  
 * @author KarajuSs
 */
public class AgeAchievementFactory extends AbstractAchievementFactory {
	@Override
	protected Category getCategory() {
		return Category.AGE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				"age.less.00010", "Dziecko Polany",
				"Spędź w grze 10 godzin",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(599)));

		achievements.add(createAchievement(
				"age.less.00050", "Syn Ziemi",
				"Spędź w grze 50 godzin",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(2999)));

		achievements.add(createAchievement(
				"age.less.00100", "Duch Lasu",
				"Spędź w grze 100 godzin",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(4999)));

		achievements.add(createAchievement(
				"age.less.00500", "Wieczny Strażnik",
				"Spędź w grze 500 godzin",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AgeGreaterThanCondition(29999)));

		return achievements;
	}
}
