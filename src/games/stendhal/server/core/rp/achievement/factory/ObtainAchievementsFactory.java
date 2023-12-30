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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.PlayerGotNumberOfItemsFromWellCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasHarvestedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.PlayerMinedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * factory for obtaining items related achievements.
 *
 * @author madmetzger
 */
public class ObtainAchievementsFactory extends AbstractAchievementFactory {
	public static final String ID_APPLES = "obtain.apple";

	@Override
	protected Category getCategory() {
		return Category.OBTAIN;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		// Wishing well achievement
		achievements.add(createAchievement(
				"obtain.wish", "Niech Spełni się Życzenie",
				"Zdobyto przedmiot ze studni życzeń",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerGotNumberOfItemsFromWellCondition(0)));

		// Vegetable harvest achievement
		achievements.add(createAchievement(
				"obtain.harvest.vegetable", "Farmer",
				"Zebrano po 8 sztuk z pośród wszystkich warzyw rosnących w Faiumoni",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(8,
						"marchew", "sałata", "brokuł", "kalafior", "por",
						"cebula", "cukinia", "szpinak", "kapusta", "czosnek", "karczoch")));

		// fruit harvest achievement
		achievements.add(createAchievement(
				"obtain.harvest.fruit", "Sałatka Owocowa",
				"Zebrano po 3 sztuki wszystkich owoców rosnących w Faiumoni",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(3,
						"jabłko", "banan", "wisienka", "kokos", "kiść winogron", "oliwka", "gruszka",
						"ananas", "granat", "pomidor", "arbuz")));

		// Fishing achievement
		achievements.add(createAchievement(
				"obtain.fish", "Rybak",
				"Wyłowiono po 15 ryb każdego rodzaju",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(15,
						"palia alpejska", "błazenek", "dorsz", "makrela", "okoń",
						"skrzydlica", "płotka", "pokolec", "pstrąg")));

		achievements.add(createAchievement(
				"obtain.prawdziwydrwal", "Prawdziwy Drwal",
				"Wycięto 100 polan",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(100, "polano")));

		achievements.add(createAchievement(
				"obtain.funguy", "Grzybobranie",
				"Zebrano po 20 każdego rodzaju grzyba",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(20,
						"pieczarka", "borowik", "muchomor")));

		achievements.add(createAchievement(
				"obtain.gornik", "Górnik z Krwi i Kości",
				"Wydobyto 50 każdego rodzaju kamienia szlachetnego",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerMinedNumberOfItemsCondition(50,
						"bryłka złota", "bryłka mithrilu", "kryształ ametystu",
						"kryształ rubinu", "kryształ szafiru", "kryształ szmaragdu",
						"kryształ obsydianu", "kryształ diamentu")));

		// ultimate collector quest achievement
		achievements.add(createAchievement(
				"quest.special.collector", "Największy Kolekcjoner",
				"Ukończono zadanie największego kolekcjonera",
				Achievement.EXTREME_BASE_SCORE, true,
				new QuestCompletedCondition("ultimate_collector")));

		// goralski kolekcjoner quest achievement
		achievements.add(createAchievement(
				"quest.special.goralcollector", "Góralski Kolekcjoner",
				"Ukończono ostatnie zadanie u góralskiego kolekcjonera",
				Achievement.EXTREME_BASE_SCORE, true,
				new QuestCompletedCondition("goralski_kolekcjoner3")));

		achievements.add(createAchievement(
				"quest.special.cloakscollector", "Kolekcjoner Płaszczy",
				"Ukończono ostatnie zadanie u kolekcjonerki płaszczy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestCompletedCondition("cloaks_collector_2")));

		achievements.add(createAchievement(
				"quest.special.beltscollector", "Kolekcjoner Pasów",
				"Ukończono zadanie u kolekcjonerki pasów",
				Achievement.HARD_BASE_SCORE, true,
				new QuestCompletedCondition("belts_collector")));

		achievements.add(createAchievement(
				"quest.special.glovescollector", "Kolekcjoner Rękawic",
				"Ukończono zadanie u kolekcjonerki rękawic",
				Achievement.HARD_BASE_SCORE, true,
				new QuestCompletedCondition("gloves_collector")));

		achievements.add(createAchievement(
				"quest.special.mithrilcloak", "Oszałamiający Płaszcz",
				"Ukończono zadanie na płaszcz z mithrilu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestCompletedCondition("mithril_cloak")));

		achievements.add(createAchievement(
				"quest.special.mithrilshield", "Najtwardsza Tarcza",
				"Ukończono zadanie na tarczę z mithrilu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestCompletedCondition("mithrilshield_quest")));

		achievements.add(createAchievement(
				"quest.special.gornictwo", "Kopalnia Niestraszna",
				"Ukończono zadanie na górnictwo",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestCompletedCondition("gornictwo")));

		// flower harvest
		achievements.add(createAchievement(
				"obtain.harvest.flower", "Zielony Kciuk",
				"Zebrano 20 każdego rodzaju uprawianego kwiatu",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(20,
						"stokrotki", "lilia", "bratek", "bielikrasa")));

		// herb harvest
		achievements.add(createAchievement(
				"obtain.harvest.herb", "Ziołolecznik",
				"Zebrano 20 każdego rodzaju rosnącego zioła w Faiumoni",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(20,
						"arandula", "kekik", "mandragora", "sclaria")));

		// loot or harvest apples
		achievements.add(createAchievement(
				ID_APPLES, "Kołysząc się na Jabłkach",
				"Zebrano lub zdobył 1,000 jabłek",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						final int harvested = player.getQuantityOfHarvestedItems("jabłko");
						final int looted = player.getNumberOfLootsForItem("jabłko");

						return harvested + looted >= 1000;
					}
				}));

		return achievements;
	}
}
