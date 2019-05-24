package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerVisitedZonesInRegionCondition;
/**
 * Factory for interior zone achievements
 *
 * @author kymara
 */
public class InteriorZoneAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.INTERIOR_ZONE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		Collection<Achievement> list = new LinkedList<Achievement>();
		//All below ground achievements
		list.add(createAchievement("zone.interior.semos", "Domownik", "Odwiedził wszystkie pomieszczenia w regionie Semos",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("semos", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.nalwor", "Gość elfów", "Odwiedził wszystkie pomieszczenia w regionie Nalwor",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("nalwor", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.ados", "Gość centrum", "Odwiedził wszystkie pomieszczenia w regionie Ados",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("ados", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.wofolcity", "Miasto Kobold", "Odwiedził wszystkie pomieszczenia w regionie Wo'fol",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("wofol city", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.magiccity", "Magiczne Miasto", "Odwiedził wszystkie pomieszczenia w podziemnym mieście Magic",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("magic city", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.krakowcity", "Miasto Kraków", "Odwiedził wszystkie pomieszczenia w regionie Krakowa",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("krakow city", Boolean.FALSE, Boolean.FALSE)));
		//list.add(createAchievement("zone.interior.wieliczkacity", "Miasto Wieliczka", "Odwiedził wszystkie pomieszczenia w regionie Wieliczki",
		//							Achievement.MEDIUM_BASE_SCORE, true,
		//							new PlayerVisitedZonesInRegionCondition("wieliczka city", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.zakopanecity", "Miasto Zakopane", "Odwiedził wszystkie pomieszczenia w regionie Zakopanego",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("zakopane city", Boolean.FALSE, Boolean.FALSE)));
		//list.add(createAchievement("zone.interior.gdanskcity", "Miasto Gdańsk", "Odwiedził wszystkie pomieszczenia w regionie Gdańska",
		//							Achievement.MEDIUM_BASE_SCORE, true,
		//							new PlayerVisitedZonesInRegionCondition("gdansk city", Boolean.FALSE, Boolean.FALSE)));
		return list;
	}

}
