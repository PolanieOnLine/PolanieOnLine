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
	protected Category getCategory() {
		return Category.REBORN;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement("quest.special.reborn.1", "Nowo Narodzony", "Narodził się na nowo",
			Achievement.EASY_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done")));
		achievements.add(createAchievement("quest.special.reborn.2", "Nowy Ja, Nowa Przygoda", "Narodził się na nowo po raz drugi",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;2")));
		achievements.add(createAchievement("quest.special.reborn.3", "Przygodo, Ruszam!", "Narodził się na nowo po raz trzeci",
			Achievement.MEDIUM_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;3")));
		achievements.add(createAchievement("quest.special.reborn.4", "Poczwórna Powtórka", "Narodził się na nowo po raz czwarty",
			Achievement.HARD_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;4")));
		achievements.add(createAchievement("quest.special.reborn.5", "Tym Razem Historia się Nie Powtórzy", "Narodził się na nowo po raz ostatni",
			Achievement.LEGENDARY_BASE_SCORE, true, new QuestInStateCondition(QUEST_SLOT, "done;5")));

		return achievements;
	}
}