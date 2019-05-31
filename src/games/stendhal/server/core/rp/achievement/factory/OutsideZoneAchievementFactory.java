package games.stendhal.server.core.rp.achievement.factory;

import java.util.Collection;
import java.util.LinkedList;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerVisitedZonesCondition;
import games.stendhal.server.entity.npc.condition.PlayerVisitedZonesInRegionCondition;
/**
 * Factory for zone achievements
 *
 * @author madmetzger
 */
public class OutsideZoneAchievementFactory extends AbstractAchievementFactory {

	@Override
	protected Category getCategory() {
		return Category.OUTSIDE_ZONE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		Collection<Achievement> list = new LinkedList<Achievement>();
		//All outside zone achievements
		list.add(createAchievement("zone.outside.semos", "Młodszy odkrywca", "Odwiedził wszystkie obszary w regionie Semos", 
									Achievement.EASY_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("semos", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.ados", "Odkrywca wielkich miast", "Odwiedził wszystkie obszary w regionie Ados", 
									Achievement.EASY_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("ados", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.fado", "Na południe", "Odwiedził wszystkie obszary w regionie Fado", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("fado", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.orril", "Harcerz", "Odwiedził wszystkie obszary w regionie Fado", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("orril", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.amazon", "Odkrywca dżungli", "Odwiedził wszystkie obszary w regionie Amazon", 
									Achievement.HARD_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("amazon", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.athor", "Turysta", "Odwiedził wszystkie obszary w regionie Athor", 
									Achievement.EASY_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("athor", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.kikareukin", "Podniebna wieża", "Odwiedził wszystkie obszary w regionie Kikareukin", 
									Achievement.HARD_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("kikareukin", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.krakow", "Królewskie miasto", "Odwiedził wszystkie obszary w regionie Kraków", 
									Achievement.EASY_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("krakow", Boolean.TRUE, Boolean.TRUE)));
		list.add(createAchievement("zone.outside.zakopane", "Zimowa kraina", "Odwiedził wszystkie obszary w regionie Zakopane", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("zakopane", Boolean.TRUE, Boolean.TRUE)));
		/**list.add(createAchievement("zone.outside.gdansk", "Nadmorska kraina", "Odwiedził wszystkie obszary w regionie Gdańsk", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("gdansk", Boolean.TRUE, Boolean.TRUE)));*/
		list.add(createAchievement("zone.outside.wieliczka", "Kraina soli", "Odwiedził wszystkie obszary w regionie Wieliczka", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.TRUE, Boolean.TRUE)));
		/**list.add(createAchievement("zone.outside.tatry", "Góral", "Odwiedził wszystkie obszary w regionie Tatry", 
									Achievement.HARD_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("tatry", Boolean.TRUE, Boolean.TRUE)));*/
		list.add(createAchievement("zone.outside.desert", "Wszędzie piasek?!", "Odwiedził wszystkie obszary w regionie pustynnym", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesInRegionCondition("desert", Boolean.TRUE, Boolean.TRUE)));
	
		//All interior zone achievements

		//Special zone achievements
		list.add(createAchievement("zone.special.bank", "Depozyt", "Odwiedził wszystkie banki", 
									Achievement.MEDIUM_BASE_SCORE, true,
									new PlayerVisitedZonesCondition("int_semos_bank", "int_nalwor_bank", "int_kirdneh_bank"/**, "int_gdansk_bank"*/, 
												"int_fado_bank", "int_magic_bank", "int_ados_bank", "int_zakopane_bank_0")));
		return list;
	}

}
