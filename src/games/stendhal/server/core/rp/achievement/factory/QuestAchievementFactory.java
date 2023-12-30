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
			"Ukończono zadanie u księżniczki elfów 25 razy", 
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("elf_princess", 2, 24)));

		// Kill Monks quest achievement
		achievements.add(createAchievement(
			"quest.special.kill_monks.0025", "Heretyk",
			"Ukończono zadanie 'Zabij Mnichów' 25 razy",
			Achievement.HARD_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("kill_monks", 2, 24)));

		// Maze
		achievements.add(createAchievement(
			"quest.special.maze", "Kierunkowskaz",
			"Ukończono labirynt", 
			Achievement.EASY_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("maze", 2, 0)));

		// Hunting
		achievements.add(createAchievement(
			"quest.special.hunter", "Łowca Nagród",
			"Ukończono polowania Janisława 10 razy", 
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("hunting", 2, 9)));

		// Balloon for Bobby
		achievements.add(createAchievement(
			"quest.bobby.balloons.0005", "Uczestnik",
			"Przyniesiono 5 balonów Bobbiemu",
			Achievement.HARD_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("balloon_bobby", 1, 4)));

		// Meal for Groongo Rahnnt
		achievements.add(createAchievement(
			"quest.groongo.meals.0050", "Cierpliwie Czekający na Gderacza",
			"Zaserwowano 50 posiłków dla Groongo Rahnnt",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("meal_for_groongo", 7, 49)));

		// Restock the Flower Shop
		achievements.add(createAchievement(
			ID_FLOWERSHOP, "Zamiłowanie Kwiatkami",
			"Uzupełniono zapasy kwiaciarni w Nalwor 50 razy",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestStateGreaterThanCondition("restock_flowershop", 2, 49)));

		// have completed all quests in Semos City?
		achievements.add(createAchievement(
			"quest.special.semos", "Przyjaciel Semos",
			"Ukończono wszystkie zadania w mieście Semos",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.SEMOS_CITY)));

		// have completed all quests in Ados City?
		achievements.add(createAchievement(
			"quest.special.ados", "Przyjaciel Ados",
			"Ukończono wszystkie zadania w mieście Ados",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.ADOS_CITY)));

		achievements.add(createAchievement(
			"quest.special.zakopane", "Przyjaciel Zakopane",
			"Ukończono wszystkie zadania w mieście Zakopane",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.ZAKOPANE_CITY)));

		achievements.add(createAchievement(
			"quest.special.krakow", "Przyjaciel Krakowa",
			"Ukończono wszystkie zadania w mieście Kraków",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestsInRegionCompletedCondition(Region.KRAKOW_CITY)));

		// complete nearly all the quests in the game?
		achievements.add(createAchievement(
			"quest.count.050", "Zleceniobiorca",
			"Ukończono co najmniej 50 zadań",
			Achievement.EASY_BASE_SCORE, true,
			new QuestCountCompletedCondition(50)));

		achievements.add(createAchievement(
			"quest.count.80", "Zadaniowy Narkoman",
			"Ukończono co najmniej 80 zadań",
			Achievement.MEDIUM_BASE_SCORE, true,
			new QuestCountCompletedCondition(80)));

		achievements.add(createAchievement(
			"quest.count.100", "Weteran Wsparcia",
			"Ukończono co najmniej 100 zadań",
			Achievement.HARD_BASE_SCORE, true, new
			QuestCountCompletedCondition(100)));

		achievements.add(createAchievement(
			"quest.count.140", "Głodny Zleceń",
			"Ukończono co najmniej 140 zadań",
			Achievement.EXTREME_BASE_SCORE, true,
			new QuestCountCompletedCondition(140)));

		return achievements;
	}
}
