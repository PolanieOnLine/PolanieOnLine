package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

/**
 * factory for item GDANSK related achievements.
 *
 * @author KarajuSs
 */

public class GdanskJewelleryQuestAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_YOUNG_TRAVELER = "quest.special.daily_museum_gdansk_quest.0005";
	public static final String ID_TRAVELER = "quest.special.daily_museum_gdansk_quest.0025";
	public static final String ID_OLD_TRAVELER = "quest.special.daily_museum_gdansk_quest.0050";
	public static final String ID_DEPOSITOR = "quest.special.daily_museum_gdansk_quest.0100";
	public static final String ID_TREASURER = "quest.special.daily_museum_gdansk_quest.0200";

	@Override
	protected Category getCategory() {
		return Category.QUEST_GDANSK_JEWELLERY;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		LinkedList<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement(
				ID_YOUNG_TRAVELER, "Młody Podróżnik", "Ukończył co dwudniowe zadanie na przedmiot 5 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 4)));

		achievements.add(createAchievement(
				ID_TRAVELER, "Podróżnik", "Ukończył co dwudniowe zadanie na przedmiot 25 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 24)));

		achievements.add(createAchievement(
				ID_OLD_TRAVELER, "Starszy Podróżnik", "Ukończył co dwudniowe zadanie na przedmiot 50 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 49)));

		achievements.add(createAchievement(
				ID_DEPOSITOR, "Depozytor Gdańska", "Ukończył co dwudniowe zadanie na przedmiot 100 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 99)));

		achievements.add(createAchievement(
				ID_TREASURER, "Skarbnik Gdańska", "Ukończył co dwudniowe zadanie na przedmiot 200 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 199)));

		return achievements;
	}
}
