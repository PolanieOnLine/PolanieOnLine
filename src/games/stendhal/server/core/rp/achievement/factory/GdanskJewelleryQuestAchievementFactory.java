package games.stendhal.server.core.rp.achievement.factory;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
//import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

import java.util.Collection;
import java.util.LinkedList;

/**
 * factory for item GDANSK related achievements.
 *
 * @author KarajuSs
 */

public class GdanskJewelleryQuestAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.QUEST_GDANSK_JEWELLERY;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		LinkedList<Achievement> achievements = new LinkedList<Achievement>();
		/**achievements.add(createAchievement("quest.special.daily_museum_gdansk_quest.0005", "Młody Podróżnik", "Ukończył co dwu-dniowe zadanie na przedmiot 5 razy",
				Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 4)));
		achievements.add(createAchievement("quest.special.daily_museum_gdansk_quest.0025", "Podróżnik", "Ukończył co dwu-dniowe zadanie na przedmiot 25 razy", 
				Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 24)));
		achievements.add(createAchievement("quest.special.daily_museum_gdansk_quest.0050", "Starszy Podróżnik", "Ukończył co dwu-dniowe zadanie na przedmiot 50 razy", 
				Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 49)));
		achievements.add(createAchievement("quest.special.daily_museum_gdansk_quest.0100", "Depozytor Gdańska", "Ukończył co dwu-dniowe zadanie na przedmiot 100 razy", 
				Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 99)));
		achievements.add(createAchievement("quest.special.daily_museum_gdansk_quest.0200", "Skarbnik Gdańska", "Ukończył co dwu-dniowe zadanie na przedmiot 200 razy", 
				Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_museum_gdansk_quest", 2, 199)));*/
		return achievements;
	}
}
