package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;

public class RebornAchievementFactory extends AbstractAchievementFactory {
	private static final String QUEST_SLOT = "reset_level";

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();

		// first reborn
		achievements.add(createAchievement("reborn.quests.first", "Nowo narodzony", "Narodził się na nowo",
			Achievement.EASY_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done")));

		// second reborn
		achievements.add(createAchievement("reborn.quests.second", "Nowy ja, nowa przygoda", "Narodził się na nowo po raz drugi",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;2")));

		// third reborn
		achievements.add(createAchievement("reborn.quests.third", "Przygodo... ruszam!", "Narodził się na nowo po raz trzeci",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;3")));

		// fourth reborn
		achievements.add(createAchievement("reborn.quests.fourth", "Czwarta powtórka przygody", "Narodził się na nowo po raz czwarty",
			Achievement.HARD_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;4")));

		// fifth reborn
		achievements.add(createAchievement("reborn.quests.fifth", "Tym razem historia się nie powtórzy", "Narodził się na nowo po raz ostatni",
			Achievement.LEGENDARY_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;5")));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.REBORN;
	}
}