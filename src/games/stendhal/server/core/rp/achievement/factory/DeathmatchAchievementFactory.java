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

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.entity.player.Player;


/**
 * Deathmatch related achievements.
 */
public class DeathmatchAchievementFactory extends AbstractAchievementFactory {

	private static final Logger logger = Logger.getLogger(DeathmatchAchievementFactory.class);

	public static final String HELPER_SLOT = "deathmatch_helper";

	public static final String ID_HELPER_25 = "deathmatch.helper.0025";
	public static final String ID_HELPER_50 = "deathmatch.helper.0050";
	public static final String ID_HELPER_100 = "deathmatch.helper.0100";


	@Override
	protected Category getCategory() {
		return Category.DEATHMATCH;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		/*
		// Ados Deathmatch
		// disabled. Currently the wrong index is being checked (it would be index 6)
		// and as per bug report https://sourceforge.net/tracker/?func=detail&aid=3148365&group_id=1111&atid=101111 the count is not saved anyway
		achievements.add(createAchievement(
				"quest.special.dm.025", "Gladiator", "Zawalczył 25 rund na deathmatchu",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("deathmatch", 1, 24)));
		*/

		achievements.add(createAchievement(
				"quest.deathmatch", "Bohater Deathmatchu", "Zdobył 100,000 punktów na arenie deathmatch",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("deathmatch_score", 0, 100000)));

		achievements.add(createAchievement(
				ID_HELPER_25, "Pomocnik w Deathmatchu", "Wsparł innych graczy w 25 rundach deathmatchu",
				Achievement.EASY_BASE_SCORE, true,
				new HasHelpedNumberOfTimes(25)));

		achievements.add(createAchievement(
				ID_HELPER_50, "Kompan w Deathmatchu", "Wsparł innych graczy w 50 rundach deathmatchu",
				Achievement.EASY_BASE_SCORE, true,
				new HasHelpedNumberOfTimes(50)));

		achievements.add(createAchievement(
				ID_HELPER_100, "Eskortnik w Deathmatchu", "Wsparł innych graczy w 100 rundach deathmatchu",
				Achievement.MEDIUM_BASE_SCORE, false,
				new HasHelpedNumberOfTimes(100)));

		return achievements;
	}


	/**
	 * Class to check if a player has helped in deathmatch a specified number of times.
	 */
	private class HasHelpedNumberOfTimes implements ChatCondition {

		private int requiredCount;


		private HasHelpedNumberOfTimes(final int count) {
			this.requiredCount = count;
		}

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			int count = 0;
			if (player.hasQuest(HELPER_SLOT)) {
				try {
					count = Integer.parseInt(player.getQuest(HELPER_SLOT, 0));
				} catch (final NumberFormatException e) {
					logger.error("Deathmatch helper quest slot value not an integer.");
					e.printStackTrace();
					return false;
				}
			}

			return count >= requiredCount;
		}
	};
}
