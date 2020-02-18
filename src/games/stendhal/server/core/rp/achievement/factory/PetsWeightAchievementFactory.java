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
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;
/**
 * Factory for required weight achievements of pets, or sheep, or goat
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
		weightAchievements.add(createAchievement("weight.sheep.100", "Wypasiona Owca", "Wypasił owcę do 100 wagi",
				Achievement.EASY_BASE_SCORE, true, 
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							final Sheep sheep = player.getSheep();
							if(!player.hasSheep()) {
								return false;
							}
							final int weight = 100 - sheep.getWeight();
							return weight <= 0;
						}
					}));
		weightAchievements.add(createAchievement("weight.goat.100", "Wypasiona Koza", "Wypasił kozę do 100 wagi",
				Achievement.EASY_BASE_SCORE, true,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							final Goat goat = player.getGoat();
							if(!player.hasGoat()) {
								return false;
							}
							final int weight = 100 - goat.getWeight();
							return weight <= 0;
						}
					}));
		weightAchievements.add(createAchievement("weight.pet.100", "Duże Zwierzątko", "Zwierzątko urosło do 100 wagi",
				Achievement.EASY_BASE_SCORE, true,
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							if(!player.hasPet()) {
								return false;
							}
							final int weight = 100 - player.getPet().getWeight();
							return weight <= 0;
						}
					}));
		return weightAchievements;
	}

}
