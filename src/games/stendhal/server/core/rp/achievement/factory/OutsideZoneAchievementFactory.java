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
		list.add(createAchievement("zone.outside.semos", "Młodszy Odkrywca", "Odwiedził wszystkie obszary w regionie Semos",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("semos", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.ados", "Odkrywca Wielkich Miast", "Odwiedził wszystkie obszary w regionie Ados",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("ados", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.fado", "Na Południe", "Odwiedził wszystkie obszary w regionie Fado",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("fado", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.orril", "Harcerz", "Odwiedził wszystkie obszary w regionie Orril",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("orril", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.amazon", "Odkrywca Dżungli", "Odwiedził wszystkie obszary w regionie Amazon",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("amazon", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.athor", "Turysta", "Odwiedził wszystkie obszary w regionie Athor",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("athor", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.kikareukin", "Podniebna Wieża", "Odwiedził wszystkie obszary w regionie Kikareukin",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("kikareukin", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.deniran", "Człowiek z Zachodu", "Odwiedził wszystkie obszary w regionie Deniran",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("deniran", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.kirdneh", "Kulturalny Przybysz", "Odwiedził wszystkie obszary w regionie Kirdneh",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("kirdneh", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.kalavan", "Piękne Ogrody", "Odwiedził wszystkie obszary w regionie Kalavan",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("kalavan", Boolean.TRUE, Boolean.TRUE)));

		// Prasłowiańskie
		list.add(createAchievement("zone.outside.krakow", "Królewskie Miasto", "Odwiedził wszystkie obszary w regionie Kraków",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("krakow", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.zakopane", "Zimowa Kraina", "Odwiedził wszystkie obszary w regionie Zakopane",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("zakopane", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.gdansk", "Nadmorska Kraina", "Odwiedził wszystkie obszary w regionie Gdańsk",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("gdansk", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.wieliczka", "Kraina Soli", "Odwiedził wszystkie obszary w regionie Wieliczka",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.tatry", "Pasmo Górskie", "Odwiedził wszystkie obszary w regionie Tatry",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("tatry", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.desert", "Wszędzie Piasek", "Odwiedził wszystkie obszary w regionie pustynnym",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("desert", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.koscielisko", "Zwiedzacz", "Odwiedził wszystkie obszary w regionie Kościelisko",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("koscielisko", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.warszawa", "Warszawski Marszałek", "Odwiedził wszystkie obszary w regionie Warszawa",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("warszawa", Boolean.TRUE, Boolean.TRUE)));

		list.add(createAchievement("zone.outside.dragonland", "Smocza Kraina", "Odwiedził wszystkie obszary smoczej krainy",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesCondition("0_dragon_land_s", "0_dragon_land_n", "0_dragon_land_nw",
						"0_dragon_land_sw", "0_dragon_land_ne", "0_dragon_land_se")));

		//All interior zone achievements

		//Special zone achievements
		list.add(createAchievement("zone.special.bank", "Depozyt", "Odwiedził wszystkie banki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesCondition("int_semos_bank", "int_nalwor_bank", "int_kirdneh_bank", "int_gdansk_bank",
						"int_fado_bank", "int_magic_bank", "int_ados_bank", "int_deniran_bank_blue_roof", "int_zakopane_bank_0",
						"int_krakow_bank_0")));

		list.add(createAchievement("zone.special.afterlife", "Widziałem Światło", "Poznał życie pozagrobowe",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesCondition("int_afterlife")));

		list.add(createAchievement("zone.special.allvisited", "Wszystko już Widziałem", "Odwiedził niebo, piekło, chmury oraz więzienie",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesCondition("int_afterlife", "hell", "int_koscielisko_jail", "7_kikareukin_clouds")));

		return list;
	}

}
