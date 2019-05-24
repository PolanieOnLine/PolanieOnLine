package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

/**
 * factory for item related achievements.
 *
 * @author madmetzger
 */
public class AdosItemQuestAchievementsFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.QUEST_ADOS_ITEMS;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		//daily item quest achievements
		achievements.add(createAchievement("quest.special.daily_item.0010", "Pomocnik Ados", "Ukończył codzienne zadanie na przedmiot 10 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_item", 2, 9)));
		achievements.add(createAchievement("quest.special.daily_item.0050", "Dostawca Ados", "Ukończył codzienne zadanie na przedmiot 50 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_item", 2, 49)));
		achievements.add(createAchievement("quest.special.daily_item.0100", "Główny Dostawca Ados", "Ukończył codzienne zadanie na przedmiot 100 razy",
												Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_item", 2, 99)));
		achievements.add(createAchievement("quest.special.daily_item.0250", "Magazynier Ados", "Ukończył codzienne zadanie na przedmiot 250 razy",
												Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_item", 2, 249)));
		achievements.add(createAchievement("quest.special.daily_item.0500", "Skarbnik Ados", "Ukończył codzienne zadanie na przedmiot 500 razy",
												Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("daily_item", 2, 499)));
		return achievements;
	}

}
