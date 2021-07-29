/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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
import games.stendhal.server.entity.npc.condition.StatLevelComparisonCondition;

public class ExperienceMININGAchievementFactory extends AbstractAchievementFactory {
	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<>();

		String level = "20";
		achievements.add(createAchievement(
				"mining.level.0" + level, "Obeznanie z Kilofem", "Osiągnął " + level + " poziom górnictwa",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "40";
		achievements.add(createAchievement(
				"mining.level.0" + level, "Kilofioza", "Osiągnął " + level + " poziom górnictwa",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "60";
		achievements.add(createAchievement(
				"mining.level.0" + level, "Zaawansowany Kopacz", "Osiągnął " + level + " poziom górnictwa",
				Achievement.MEDIUM_BASE_SCORE, true,
				createComparison(level)));

		return achievements;
	}

	private StatLevelComparisonCondition createComparison(final String level) {
		return new StatLevelComparisonCondition("mining", ">=", Integer.parseInt(level));
	}

	@Override
	protected Category getCategory() {
		return Category.EXPERIENCE_MINING;
	}
}