package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;

public class RebornAchievementFactory extends AbstractAchievementFactory {
	private static final String QUEST_SLOT = "reset_level";

	private static final int EASY_SCORE = 2500;
	private static final int MEDIUM_SCORE = 5000;
	private static final int HARD_SCORE = 7500;
	private static final int LEGENDARY_SCORE = 10000;

	public static final String ID_NEWBORN = "quest.special.reborn.1";
	public static final String ID_NEW_ADVENTURE = "quest.special.reborn.2";
	public static final String ID_COMING = "quest.special.reborn.3";
	public static final String ID_REPLAY = "quest.special.reborn.4";
	public static final String ID_NEW_HISTORY = "quest.special.reborn.5";

	@Override
	protected Category getCategory() {
		return Category.REBORN;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();
		achievements.add(createAchievement(
				ID_NEWBORN, "Nowa Przygoda", "Rozpoczął przygodę od nowa",
				EASY_SCORE, true,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, 1, "reborn_1"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_2"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_3"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_4"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_5"))));

		achievements.add(createAchievement(
				ID_NEW_ADVENTURE, "Druga Szansa", "Rozpoczął przygodę drugi raz od nowa",
				MEDIUM_SCORE, true,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, 1, "reborn_2"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_3"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_4"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_5"))));

		achievements.add(createAchievement(
				ID_COMING, "Nadchodzę", "Rozpoczął przygodę trzeci raz od nowa",
				MEDIUM_SCORE, true,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, 1, "reborn_3"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_4"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_5"))));

		achievements.add(createAchievement(
				ID_REPLAY, "Zdobywca Doświadczenia", "Rozpoczął przygodę czwarty raz od nowa",
				HARD_SCORE, true,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, 1, "reborn_4"),
						new QuestInStateCondition(QUEST_SLOT, 1, "reborn_5"))));

		achievements.add(createAchievement(
				ID_NEW_HISTORY, "Legendarny Wojownik", "Rozpoczął przygodę piąty raz od nowa",
				LEGENDARY_SCORE, true,
				new QuestInStateCondition(QUEST_SLOT, 1, "reborn_5")));

		return achievements;
	}
}
