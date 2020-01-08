package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;

public class RebornAchievementFactory extends AbstractAchievementFactory {
	private static final String QUEST_SLOT = "reset_level";

	@Override
	protected Category getCategory() {
		return Category.REBORN;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement("quest.special.reborn.1", "Nowo narodzony", "Narodził się na nowo",
			Achievement.EASY_BASE_SCORE, true, new QuestCompletedCondition(QUEST_SLOT)));
		achievements.add(createAchievement("quest.special.reborn.2", "Nowy ja, nowa przygoda", "Narodził się na nowo po raz drugi",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition(QUEST_SLOT, 2, 1)));
		achievements.add(createAchievement("quest.special.reborn.3", "Przygodo... ruszam!", "Narodził się na nowo po raz trzeci",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestStateGreaterThanCondition(QUEST_SLOT, 2, 2)));
		achievements.add(createAchievement("quest.special.reborn.4", "Powtórna przygoda", "Narodził się na nowo po raz czwarty",
			Achievement.HARD_BASE_SCORE, true, new QuestStateGreaterThanCondition(QUEST_SLOT, 2, 3)));
		achievements.add(createAchievement("quest.special.reborn.5", "Tym razem historia się nie powtórzy", "Narodził się na nowo po raz ostatni",
			Achievement.LEGENDARY_BASE_SCORE, true, new QuestStateGreaterThanCondition(QUEST_SLOT, 2, 4)));

		return achievements;
	}
}