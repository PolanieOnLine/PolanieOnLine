package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.ComparisonOperator;
import games.stendhal.server.entity.npc.condition.PlayerStatLevelCondition;
import games.stendhal.server.entity.player.RebornSystem;

public class RebornAchievementFactory extends AbstractAchievementFactory {
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
				ID_NEWBORN, "Nowa Przygoda",
				"Rozpoczęto przygodę od nowa",
				EASY_SCORE, true,
				atLeastReborns(1)));

		achievements.add(createAchievement(
				ID_NEW_ADVENTURE, "Druga Szansa",
				"Rozpoczęto przygodę drugi raz od nowa",
				MEDIUM_SCORE, true,
				atLeastReborns(2)));

		achievements.add(createAchievement(
				ID_COMING, "Nadchodzę",
				"Rozpoczęto przygodę trzeci raz od nowa",
				MEDIUM_SCORE, true,
				atLeastReborns(3)));

		achievements.add(createAchievement(
				ID_REPLAY, "Zdobywca Doświadczenia",
				"Rozpoczęto przygodę czwarty raz od nowa",
				HARD_SCORE, true,
				atLeastReborns(4)));

		achievements.add(createAchievement(
				ID_NEW_HISTORY, "Legendarny Wojownik",
				"Rozpoczęto przygodę piąty raz od nowa",
				LEGENDARY_SCORE, true,
				atLeastReborns(5)));

		return achievements;
	}

	private PlayerStatLevelCondition atLeastReborns(final int reborns) {
		return new PlayerStatLevelCondition(RebornSystem.ATTR_REBORNS,
				ComparisonOperator.GREATER_OR_EQUALS, reborns);
	}
}
