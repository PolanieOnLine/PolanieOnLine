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

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
		List<Achievement> ageAchievements = new LinkedList<Achievement>();
		ageAchievements.add(createAchievement("age.less.010", "Pierwsze Dziesięć Godzin", "Spędziłeś 10 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
												new AgeGreaterThanCondition(599)));
		ageAchievements.add(createAchievement("age.less.100", "Młodzieniec", "Spędziłeś 100 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
												new AgeGreaterThanCondition(5999)));
		ageAchievements.add(createAchievement("age.less.250", "Tyle Czasu Spędzonego przy Ognisku", "Spędziłeś 250 godzin w grze",
				Achievement.EASY_BASE_SCORE, true,
												new AgeGreaterThanCondition(14999)));
		ageAchievements.add(createAchievement("age.less.500", "Kiedy to Mineło", "Spędziłeś 500 godzin w grze",
				Achievement.MEDIUM_BASE_SCORE, true,
												new AgeGreaterThanCondition(29999)));
		ageAchievements.add(createAchievement("age.less.1000", "Dorosłość", "Spędziłeś 1 000 godzin w grze",
				Achievement.MEDIUM_BASE_SCORE, true,
												new AgeGreaterThanCondition(59999)));
		ageAchievements.add(createAchievement("age.less.2500", "Mały Staruszek", "Spędziłeś 2 500 godzin w grze",
				Achievement.HARD_BASE_SCORE, true,
												new AgeGreaterThanCondition(149999)));
		ageAchievements.add(createAchievement("age.less.5000", "Złodziej Czasu", "Spędziłeś 5 000 godzin w grze",
				Achievement.HARD_BASE_SCORE, true,
												new AgeGreaterThanCondition(299999)));
		ageAchievements.add(createAchievement("age.less.10000", "Który to już Rok", "Spędziłeś 10 000 godzin w grze",
				Achievement.LEGENDARY_BASE_SCORE, true,
												new AgeGreaterThanCondition(599999)));
		ageAchievements.add(createAchievement("age.less.20000", "Legenda w Grze", "Spędziłeś ponad 20 000 godzin w grze!",
				Achievement.LEGENDARY_BASE_SCORE, true,
												new AgeGreaterThanCondition(1199999)));
		return ageAchievements;
	}

}
