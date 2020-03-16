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
import games.stendhal.server.core.rp.achievement.condition.BoughtNumberOfCondition;

/**
 * Factory for buying & selling items.
 */
public class CommerceAchievementFactory extends AbstractAchievementFactory {

	public static final String[] ITEMS_HAPPY_HOUR = { "sok z chmielu", "napój z winogron" };
	public static final String ID_HAPPY_HOUR = "buy.drink.alcohol";

	@Override
	protected Category getCategory() {
		return Category.COMMERCE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_HAPPY_HOUR, "Gdzieś jest Szczęśliwa Godzina", "Zakupił po 100 butelek soku z chmielu oraz kieliszków napoju z winogron",
				Achievement.EASY_BASE_SCORE, true,
				new BoughtNumberOfCondition(100, ITEMS_HAPPY_HOUR)));

		return achievements;
	}
}