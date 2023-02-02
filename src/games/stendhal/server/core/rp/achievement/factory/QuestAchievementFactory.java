/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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
import games.stendhal.server.core.rp.achievement.condition.QuestCountCompletedCondition;
import games.stendhal.server.core.rp.achievement.condition.QuestsInRegionCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.maps.Region;

/**
 * Factory for quest achievements
 *
 * @author madmetzger
 */
public class QuestAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_FLOWERSHOP = "quest.flowershop.0050";

	@Override
	protected Category getCategory() {
		return Category.QUEST;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		// Elf Princess quest achievement
		achievements.add(createAchievement(
			"quest.special.elf_princess.0025", "Kasanowa Faiumoni",
			"Ukończył zadanie u księżniczki elfów 25 razy", 
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("elf_princess", 2, 24)));

		// Kill Monks quest achievement
		achievements.add(createAchievement(
			"quest.special.kill_monks.0025", "Heretyk",
			"Ukończył zadanie 'Zabij Mnichów' 25 razy",
			Achievement.HARD_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("kill_monks", 2, 24)));

		// Maze
		achievements.add(createAchievement(
			"quest.special.maze", "Kierunkowskaz",
			"Ukończył labirynt", 
			Achievement.EASY_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("maze", 2, 0)));

		// Hunting
		achievements.add(createAchievement(
			"quest.special.hunter", "Łowca Nagród",
			"Ukończył polowania Janisława 10 razy", 
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("hunting", 2, 9)));

		// Balloon for Bobby
		achievements.add(createAchievement(
			"quest.bobby.balloons.0005", "Uczestnik",
			"Przyniósł 5 balonów Bobbiemu",
			Achievement.HARD_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("balloon_bobby", 1, 4)));

		// Meal for Groongo Rahnnt
		achievements.add(createAchievement(
			"quest.groongo.meals.0050", "Cierpliwie Czekający na Gderacza",
			"Zaserwował 50 posiłków dla Groongo Rahnnt",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("meal_for_groongo", 7, 49)));

		// Restock the Flower Shop
		achievements.add(createAchievement(
			ID_FLOWERSHOP, "Zamiłowanie Kwiatkami",
			"Pomógł uzupełnić zapasy w kwiaciarni Nalwor 50 razy",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("restock_flowershop", 2, 49)));

		// have completed all quests in Semos City?
		achievements.add(createAchievement(
			"quest.special.semos", "Przyjaciel Semos",
			"Ukończył wszystkie zadania w mieście Semos",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.SEMOS_CITY)));

		// have completed all quests in Ados City?
		achievements.add(createAchievement(
			"quest.special.ados", "Przyjaciel Ados",
			"Ukończył wszystkie zadania w mieście Ados",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.ADOS_CITY)));

		achievements.add(createAchievement(
			"quest.special.zakopane", "Przyjaciel Zakopane",
			"Ukończył wszystkie zadania w mieście Zakopane",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.ZAKOPANE_CITY)));

		achievements.add(createAchievement(
			"quest.special.krakow", "Przyjaciel Krakowa",
			"Ukończył wszystkie zadania w mieście Kraków",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.KRAKOW_CITY)));

		// complete nearly all the quests in the game?
		achievements.add(createAchievement(
			"quest.count.050", "Pierwsze Zlecenia",
			"Ukończył conajmniej 50 zadań",
			Achievement.EASY_BASE_SCORE, true,
			new QuestCountCompletedCondition(50)));

		achievements.add(createAchievement(
			"quest.count.100", "Duuuuuużo Ukończonych Zadań",
			"Ukończył conajmniej 100 zadań",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestCountCompletedCondition(100)));

		achievements.add(createAchievement(
			"quest.count.150", "Jeszcze Więcej Zadań",
			"Ukończył conajmniej 150 zadań",
			Achievement.HARD_BASE_SCORE, true, new
			QuestCountCompletedCondition(150)));

		achievements.add(createAchievement(
			"quest.count.200", "Pogromca Zadań",
			"Ukończył conajmniej 200 zadań",
			Achievement.LEGENDARY_BASE_SCORE, true,
			new QuestCountCompletedCondition(200)));

		return achievements;
	}
}
