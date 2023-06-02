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

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.StatLevelComparisonCondition;

public class ExperienceDEFAchievementFactory extends AbstractAchievementFactory {
	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<>();

		String level = "25";
		achievements.add(createAchievement(
				"def.level.0" + level, "Początkujący Obrońca",
				"Osiągnięto " + level + " poziom OBR",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "50";
		achievements.add(createAchievement(
				"def.level.0" + level, "Obrońca",
				"Osiągnięto " + level + " poziom OBR",
				Achievement.EASY_BASE_SCORE, true,
				createComparison(level)));

		level = "75";
		achievements.add(createAchievement(
				"def.level.0" + level, "Zaawansowany Obrońca",
				"Osiągnięto " + level + " poziom OBR",
				Achievement.MEDIUM_BASE_SCORE, true,
				createComparison(level)));

		level = "100";
		achievements.add(createAchievement(
				"def.level." + level, "Ekspert Obrony",
				"Osiągnięto " + level + " poziom OBR",
				Achievement.MEDIUM_BASE_SCORE, true,
				createComparison(level)));

		level = "150";
		achievements.add(createAchievement(
				"def.level." + level, "Mistrz Obrony",
				"Osiągnięto " + level + " poziom OBR",
				Achievement.HARD_BASE_SCORE, true,
				createComparison(level)));

		return achievements;
	}

	private StatLevelComparisonCondition createComparison(final String level) {
		return new StatLevelComparisonCondition("def", ">=", Integer.parseInt(level));
	}

	@Override
	protected Category getCategory() {
		return Category.EXPERIENCE_DEF;
	}
}
