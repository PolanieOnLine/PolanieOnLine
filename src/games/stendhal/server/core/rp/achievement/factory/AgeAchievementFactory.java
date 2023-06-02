/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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
				"age.less.00010", "Nowy Bywalec",
				"Spędzono 10 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(599)));

		achievements.add(createAchievement(
				"age.less.00100", "Młodzik",
				"Spędzono 100 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(5999)));

		achievements.add(createAchievement(
				"age.less.00250", "Growy Obywatel",
				"Spędzono 250 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
				new AgeGreaterThanCondition(14999)));

		achievements.add(createAchievement(
				"age.less.00500", "W Ciągłym Ruchu",
				"Spędzono 500 godzin w grze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AgeGreaterThanCondition(29999)));

		achievements.add(createAchievement(
				"age.less.01000", "Ku Legendzie",
				"Spędzono 1 000 godzin w grze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AgeGreaterThanCondition(59999)));

		achievements.add(createAchievement(
				"age.less.02500", "Pewna Ćwiara",
				"Spędzono 2 500 godzin w grze",
				Achievement.HARD_BASE_SCORE, true,
				new AgeGreaterThanCondition(149999)));

		achievements.add(createAchievement(
				"age.less.05000", "Wciągnięto w Zamęt",
				"Spędzono 5 000 godzin w grze",
				Achievement.HARD_BASE_SCORE, true,
				new AgeGreaterThanCondition(299999)));

		achievements.add(createAchievement(
				"age.less.10000", "Staruszek",
				"Spędzono 10 000 godzin w grze",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new AgeGreaterThanCondition(599999)));

		achievements.add(createAchievement(
				"age.less.20000", "❤️",
				"Spędzono ponad 20 000 godzin w grze!",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new AgeGreaterThanCondition(1199999)));

		return achievements;
	}
}
