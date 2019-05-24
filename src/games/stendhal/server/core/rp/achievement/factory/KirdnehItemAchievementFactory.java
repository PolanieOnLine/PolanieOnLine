package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

public class KirdnehItemAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.QUEST_KIRDNEH_ITEM;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		LinkedList<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement("quest.special.weekly_item.0005", "Archeolog", "Ukończył tygodniowe zadanie na przedmiot 5 razy",
				Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("weekly_item", 2, 4)));
		achievements.add(createAchievement("quest.special.weekly_item.0050", "Starszy Archeolog", "Ukończył tygodniowe zadanie na przedmiot 50 razy", 
				Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("weekly_item", 2, 49)));
		achievements.add(createAchievement("quest.special.weekly_item.0100", "Mistrz Archeolog", "Ukończył tygodniowe zadanie na przedmiot 100 razy", 
				Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition("weekly_item", 2, 99)));
		return achievements;
	}

}
