package games.stendhal.server.core.rp.achievement.factory;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * factory for item ZAKOPANE related achievements.
 *
 * @author KarajuSs
 */
public class ZakopaneItemQuestAchievementsFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.QUEST_ZAKOPANE_ITEMS;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0010", "Młody Góral", "Ukończył codzienne zadanie na przedmiot 10 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 9)));
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0025", "Góral Pomocnik", "Ukończył codzienne zadanie na przedmiot 25 razy",
												Achievement.EASY_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 44)));
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0050", "Góral Magazynier", "Ukończył codzienne zadanie na przedmiot 50 razy",
												Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 49)));
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0100", "Starszy Góral", "Ukończył codzienne zadanie na przedmiot 100 razy",
												Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 99)));
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0250", "Juhas", "Ukończył codzienne zadanie na przedmiot 250 razy",
												Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 249)));
		achievements.add(createAchievement("quest.special.gazda_wojtek_daily_item.0500", "Dostawca Zakopane", "Ukończył codzienne zadanie na przedmiot 500 razy",
												Achievement.LEGENDARY_BASE_SCORE, true, new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 499)));
		return achievements;
	}

}
