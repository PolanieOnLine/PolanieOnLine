/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
/**
 * Factory for experience achievements
 *
 * @author madmetzger
 */
public class ExperienceAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.EXPERIENCE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> xpAchievements = new LinkedList<Achievement>();
		xpAchievements.add(createAchievement("xp.level.000", "Parobek", "Osiągnął poziom 0", Achievement.EASY_BASE_SCORE, true,
												new LevelGreaterThanCondition(-1)));
		xpAchievements.add(createAchievement("xp.level.050", "Chłop", "Osiągnął poziom 50", Achievement.EASY_BASE_SCORE, true,
												new LevelGreaterThanCondition(49)));
		xpAchievements.add(createAchievement("xp.level.100", "Kmieć", "Osiągnął poziom 100", Achievement.EASY_BASE_SCORE, true,
												new LevelGreaterThanCondition(99)));
		xpAchievements.add(createAchievement("xp.level.150", "Mieszczanin", "Osiągnął poziom 150", Achievement.EASY_BASE_SCORE, true,
												new LevelGreaterThanCondition(149)));
		xpAchievements.add(createAchievement("xp.level.200", "Szlachcic", "Osiągnął poziom 200", Achievement.MEDIUM_BASE_SCORE, true,
												new LevelGreaterThanCondition(199)));
		xpAchievements.add(createAchievement("xp.level.250", "Rycerz", "Osiągnął poziom 250", Achievement.MEDIUM_BASE_SCORE, true,
												new LevelGreaterThanCondition(249)));
		xpAchievements.add(createAchievement("xp.level.300", "Baronet", "Osiągnął poziom 300", Achievement.MEDIUM_BASE_SCORE, true,
												new LevelGreaterThanCondition(299)));
		xpAchievements.add(createAchievement("xp.level.350", "Baron", "Osiągnął poziom 350", Achievement.MEDIUM_BASE_SCORE, true,
												new LevelGreaterThanCondition(349)));
		xpAchievements.add(createAchievement("xp.level.400", "Wicehrabia", "Osiągnął poziom 400", Achievement.HARD_BASE_SCORE, true,
												new LevelGreaterThanCondition(399)));
		xpAchievements.add(createAchievement("xp.level.450", "Hrabia", "Osiągnął poziom 450", Achievement.HARD_BASE_SCORE, true,
												new LevelGreaterThanCondition(449)));
		xpAchievements.add(createAchievement("xp.level.500", "Magnat", "Osiągnął poziom 500", Achievement.HARD_BASE_SCORE, true,
												new LevelGreaterThanCondition(499)));
		xpAchievements.add(createAchievement("xp.level.550", "Książe", "Osiągnął poziom 550", Achievement.HARD_BASE_SCORE, true,
												new LevelGreaterThanCondition(549)));
		return xpAchievements;
	}

}
