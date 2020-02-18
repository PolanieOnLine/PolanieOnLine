package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerVisitedZonesCondition;
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
		list.add(createAchievement("zone.interior.nalwor", "Gość Elfów", "Odwiedził wszystkie pomieszczenia w regionie Nalwor",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("nalwor", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.ados", "Gość Centrum", "Odwiedził wszystkie pomieszczenia w regionie Ados",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("ados", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.wofolcity", "Miasto Kobold", "Odwiedził wszystkie pomieszczenia w regionie Wo'fol",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("wofol city", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.magiccity", "Magiczne Miasto", "Odwiedził wszystkie pomieszczenia w podziemnym mieście Magic",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("magic city", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.deniran", "Samotna Kraina", "Odwiedził wszystkie pomieszczenia w regionie Deniran",
									Achievement.EASY_BASE_SCORE, false,
									new PlayerVisitedZonesInRegionCondition("deniran", false, false)));

		list.add(createAchievement("zone.interior.krakowcity", "Miasto Kraków", "Odwiedził wszystkie pomieszczenia w regionie Krakowa",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("krakow", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.wieliczkacity", "Miasto Wieliczka", "Odwiedził wszystkie pomieszczenia w regionie Wieliczki",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.FALSE, Boolean.FALSE)));
		list.add(createAchievement("zone.interior.gdanskcity", "Miasto Gdańsk", "Odwiedził wszystkie pomieszczenia w regionie Gdańska",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("gdansk", Boolean.FALSE, Boolean.FALSE)));

		list.add(createAchievement("zone.interior.zakopanecity", "Miasto Zakopane", "Odwiedził wszystkie pomieszczenia w regionie Zakopanego",
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesCondition("int_zakopane_home_0", "int_zakopane_home_-1", "int_zakopane_townhall", "int_zakopane_postoffice",
											"int_zakopane_postoffice", "int_zakopane_bank_0", "int_zakopane_bank_1", "int_zakopane_bat_tower_-1", "int_zakopane_bat_tower_0",
											"int_zakopane_bat_tower_1", "int_zakopane_bat_tower_2", "int_zakopane_bat_tower_3", "int_zakopane_blacksmith", "int_zakopane_hostel",
											"int_zakopane_hospital", "int_zakopane_seller_house", "int_zakopane_mill_0", "int_zakopane_playroom", "int_zakopane_shop",
											"int_zakopane_stable", "int_zakopane_tavern", "int_zakopane_church", "int_zakopane_chapel", "int_zakopane_trainstation",
											"int_zakopane_mountain_room", "int_zakopane_bakery", "int_zakopane_maisonette_0", "int_zakopane_maisonette_1", "int_zakopane_dobromir_house")));
		return list;
	}

}
