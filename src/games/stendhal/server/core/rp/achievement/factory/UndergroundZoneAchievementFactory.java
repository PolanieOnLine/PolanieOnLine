package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerVisitedZonesInRegionCondition;
/**
 * Factory for underground zone achievements
 *
 * @author madmetzger
 */
public class UndergroundZoneAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.UNDERGROUND_ZONE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		Collection<Achievement> list = new LinkedList<Achievement>();
		//All below ground achievements
		list.add(createAchievement("zone.underground.semos", "Kanarek", "Odwiedził wszystkie podziemne obszary w regionie Semos", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("semos", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.nalwor", "Nie boi się piękła", "Odwiedził wszystkie podziemne obszary w regionie Nalwor", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("nalwor", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.athor", "Wielbiciel labiryntów", "Odwiedził wszystkie podziemne obszary w regionie Athor", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("athor", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.amazon", "Człowiek kret", "Odwiedził wszystkie podziemne obszary w regionie Amazon", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("amazon", Boolean.TRUE, Boolean.FALSE)));		
		list.add(createAchievement("zone.underground.ados", "Kopacz", "Odwiedził wszystkie podziemne obszary w regionie Ados",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("ados", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.krakow", "Szperacz", "Odwiedził wszystkie podziemne obszary w regionie Kraków", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("krakow", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.zakopane", "Grotołaz", "Odwiedził wszystkie podziemne obszary w regionie Zakopane", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("zakopane", Boolean.TRUE, Boolean.FALSE)));
		list.add(createAchievement("zone.underground.wieliczka", "Solny Zbieracz", "Odwiedził wszystkie podziemne obszary w regionie Wieliczka", 
									Achievement.HARD_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.TRUE, Boolean.FALSE)));
		return list;
	}

}
