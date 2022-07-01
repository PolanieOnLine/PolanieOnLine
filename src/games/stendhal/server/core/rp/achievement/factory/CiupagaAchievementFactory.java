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
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

public class CiupagaAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_ZLOTA_CIUPAGA = "item.zlota.ciupaga";
	public static final String ID_ZLOTA_CIUPAGA_1 = "item.zlota.ciupaga.1";
	public static final String ID_ZLOTA_CIUPAGA_2 = "item.zlota.ciupaga.2";
	public static final String ID_ZLOTA_CIUPAGA_3 = "item.zlota.ciupaga.3";

	@Override
	protected Category getCategory() {
		return Category.ZLOTE_CIUPAGI;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(ID_ZLOTA_CIUPAGA,
				"Złota Ciupaga", "Wykonał złotą ciupagę u Kowala Andrzeja",
				Achievement.EASY_BASE_SCORE, true,
				new OrCondition(
					new QuestCompletedCondition("andrzej_make_zlota_ciupaga"),
					new QuestCompletedCondition("zlota_ciupaga_was"),
					new QuestCompletedCondition("ciupaga_dwa_wasy"),
					new QuestCompletedCondition("ciupaga_trzy_wasy"))));

		achievements.add(createAchievement(ID_ZLOTA_CIUPAGA_1,
				"Złota Ciupaga z Wąsem", "Wykonał złotą ciupagę z wąsem u Józka",
				Achievement.MEDIUM_BASE_SCORE, true,
				new OrCondition(
					new QuestCompletedCondition("zlota_ciupaga_was"),
					new QuestCompletedCondition("ciupaga_dwa_wasy"),
					new QuestCompletedCondition("ciupaga_trzy_wasy"))));

		achievements.add(createAchievement(ID_ZLOTA_CIUPAGA_2,
				"Złota Ciupaga z Dwoma Wąsami", "Wykonał złotą ciupagę z dwoma wąsami u Krasnoluda",
				Achievement.HARD_BASE_SCORE, true,
				new OrCondition(
					new QuestCompletedCondition("ciupaga_dwa_wasy"),
					new QuestCompletedCondition("ciupaga_trzy_wasy"))));

		achievements.add(createAchievement(ID_ZLOTA_CIUPAGA_3,
				"Złota Ciupaga z Trzema Wąsami", "Wykonał złotą ciupagę z trzema wąsami u Hadrina",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new QuestCompletedCondition("ciupaga_trzy_wasy")));

		return achievements;
	}
}
