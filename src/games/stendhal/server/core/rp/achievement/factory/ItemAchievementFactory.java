/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemAchievementFactory extends AbstractAchievementFactory {
	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement(
				"item.money.00000100", "Pierwsze Kieszonkowe", "Zdobył 100 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100, "money")));

		itemAchievements.add(createAchievement(
				"item.money.00010000", "Złoty Prysznic", "Zdobył 10,000 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.00100000", "Mała Fortuna", "Zdobył 100,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.01000000", "Już Nie Potrzebujesz Więcej", "Zdobył 1,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.10000000", "Wielka Kąpiel w Złocie", "Zdobył 10,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000000, "money")));

		itemAchievements.add(createAchievement(
				"item.cheese.2000", "Serowy Czarodziej", "Zdobył 2,000 sera",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "ser")));

		itemAchievements.add(createAchievement(
				"item.ham.2500", "Stado Szynek", "Zdobył 2,500 szynki",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2500, "szynka")));

		itemAchievements.add(createAchievement(
				"item.cod.1500", "Pływanie w Dorszach", "Zdobył 1,500 dorszy",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1500, "dorsz")));

		itemAchievements.add(createAchievement(
				"item.set.littlerings", "Mały Komplet Pierścionków", "Zdobył srebrny i złoty pierścień",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(new QuestCompletedCondition("zamowienie_strazy"), new QuestCompletedCondition("zloty_pierscien"))));

		return itemAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}
}
