package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerGotNumberOfItemsFromWellCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasHarvestedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.PlayerMinedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;

/**
 * factory for obtaining items related achievements.
 *
 * @author madmetzger
 */
public class ObtainAchievementsFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.OBTAIN;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		// Wishing well achievement
		achievements.add(createAchievement("obtain.wish", "Niech spełni się życzenie", "Zdobył przedmiot ze studni życzeń",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerGotNumberOfItemsFromWellCondition(0)));

		// Vegetable harvest achievement
		achievements.add(createAchievement("obtain.harvest.vegetable", "Farmer", "Zebrał 8 z pośród wszystkich warzyw rosnących w Faiumoni",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(8, "marchew", "sałata", "brokuł", "kalafior", "por",
						"cebula", "cukinia", "szpinak", "kapusta", "czosnek", "karczoch")));

		// Fishing achievement
		achievements.add(createAchievement("obtain.fish", "Rybak", "Złapał po 15 ryb każdego rodzaju",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(15, "palia alpejska", "błazenek", "dorsz", "makrela", "okoń",
						"skrzydlica", "płotka", "pokolec", "pstrąg")));
						
		achievements.add(createAchievement("obtain.prawdziwydrwal", "Prawdziwy Drwal", "Wyciął 100 polan",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasHarvestedNumberOfItemsCondition(100, "polano")));
				
		achievements.add(createAchievement("obtain.gornik", "Górnik z krwi i kości", "Wydobył 50 każdego rodzaju kamienia szlachetnego",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerMinedNumberOfItemsCondition(50, "bryłka złota", "bryłka mithrilu", "kryształ ametystu", "kryształ rubinu", "kryształ szafiru", "kryształ szmaragdu", "kryształ obsydianu")));

		//ultimate collector quest achievement
		achievements.add(createAchievement("quest.special.collector", "Największy kolekcjoner", "Ukończył zadanie największego kolekcjonera", 
				Achievement.LEGENDARY_BASE_SCORE, true,
				new QuestCompletedCondition("ultimate_collector")));
				
		//goralski kolekcjoner quest achievement
		achievements.add(createAchievement("quest.special.goralcollector", "Góralski kolekcjoner", "Ukończył ostatnie zadanie u góralskiego kolekcjonera", 
				Achievement.LEGENDARY_BASE_SCORE, true,
				new QuestCompletedCondition("goralski_kolekcjoner3")));

		return achievements;
	}

}
