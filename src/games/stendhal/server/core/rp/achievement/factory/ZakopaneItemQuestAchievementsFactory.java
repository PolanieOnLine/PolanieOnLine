package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

/**
 * factory for item ZAKOPANE related achievements.
 *
 * @author KarajuSs
 */
public class ZakopaneItemQuestAchievementsFactory extends AbstractAchievementFactory {

	public static final String ID_YOUNG_HIGHLANDER = "quest.special.gazda_wojtek_daily_item.0010";
	public static final String ID_HELPER = "quest.special.gazda_wojtek_daily_item.0025";
	public static final String ID_STOREKEEPER = "quest.special.gazda_wojtek_daily_item.0050";
	public static final String ID_OLD_HIGHLANDER = "quest.special.gazda_wojtek_daily_item.0100";
	public static final String ID_JUHAS = "quest.special.gazda_wojtek_daily_item.0250";
	public static final String ID_SUPPLIER = "quest.special.gazda_wojtek_daily_item.0500";

	@Override
	protected Category getCategory() {
		return Category.QUEST_ZAKOPANE_ITEMS;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_YOUNG_HIGHLANDER, "Młody Góral", "Ukończył codzienne zadanie na przedmiot 10 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 9)));

		achievements.add(createAchievement(
				ID_HELPER, "Góral Pomocnik", "Ukończył codzienne zadanie na przedmiot 25 razy",
				Achievement.EASY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 24)));

		achievements.add(createAchievement(
				ID_STOREKEEPER, "Góral Magazynier", "Ukończył codzienne zadanie na przedmiot 50 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 49)));

		achievements.add(createAchievement(
				ID_OLD_HIGHLANDER, "Starszy Góral", "Ukończył codzienne zadanie na przedmiot 100 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 99)));

		achievements.add(createAchievement(
				ID_JUHAS, "Juhas", "Ukończył codzienne zadanie na przedmiot 250 razy",
				Achievement.HARD_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 249)));

		achievements.add(createAchievement(
				ID_SUPPLIER, "Dostawca Zakopane", "Ukończył codzienne zadanie na przedmiot 500 razy",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new QuestStateGreaterThanCondition("gazda_wojtek_daily_item", 2, 499)));

		return achievements;
	}

}
