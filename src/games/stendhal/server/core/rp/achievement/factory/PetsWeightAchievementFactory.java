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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;
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
		weightAchievements.add(createAchievement("weight.less.099", "Wypasiona owca", "Wypasił owcę do 100kg",
				Achievement.MEDIUM_BASE_SCORE, true, 
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							if(!player.hasSheep()) {
								return false;
							}
							final int weight = 99 - player.getSheep().getWeight();
							return weight <= 0;
						}
					}));
		/**weightAchievements.add(createAchievement("weight.less.099", "Wypasiona koza", "Wypasił kozę do 100kg",
				Achievement.MEDIUM_BASE_SCORE, true,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							final int weight = 100 - player.getGoat().getWeight();
							return weight <= 0;
						}
					}));*/
		return weightAchievements;
	}

}
