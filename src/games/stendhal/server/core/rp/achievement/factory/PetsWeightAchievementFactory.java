/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.PetsWithWeight;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
/**
 * Factory for required weight achievements
 *  
 * @author zekkeq
 */
public class PetsWeightAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.WEIGHT;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> weightAchievements = new LinkedList<Achievement>();
		weightAchievements.add(createAchievement("pet.condition", "Mój przyjaciel", "Przygarnął jakiekolwiek zwierzątko",
				Achievement.EASY_BASE_SCORE, true, new PlayerHasPetOrSheepCondition()));
		weightAchievements.add(createAchievement("weight.less.099", "Wypasiona owca", "Wypasił owcę do 100kg",
				Achievement.MEDIUM_BASE_SCORE, true, new PetsWithWeight(99, "sheep")));
		weightAchievements.add(createAchievement("weight.less.099", "Wypasiona koza", "Wypasił kozę do 100kg",
				Achievement.MEDIUM_BASE_SCORE, true, new PetsWithWeight(99, "goat")));
		return weightAchievements;
	}

}
