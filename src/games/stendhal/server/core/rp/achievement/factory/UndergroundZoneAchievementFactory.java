/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
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
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		// All below ground achievements
		achievements.add(createAchievement("zone.underground.semos", "Kanarek", "Odwiedził wszystkie podziemne obszary w regionie Semos", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("semos", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.nalwor", "Nie Boi się Piekła", "Odwiedził wszystkie podziemne obszary w regionie Nalwor", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("nalwor", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.athor", "Wielbiciel Labiryntów", "Odwiedził wszystkie podziemne obszary w regionie Athor", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("athor", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.amazon", "Człowiek Kret", "Odwiedził wszystkie podziemne obszary w regionie Amazon", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("amazon", Boolean.TRUE, Boolean.FALSE)));		
		achievements.add(createAchievement("zone.underground.ados", "Kopacz", "Odwiedził wszystkie podziemne obszary w regionie Ados",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("ados", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.deniran", "Speleolog", "Odwiedził wszystkie podziemne obszary w regionie Deniran",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("deniran", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.fado", "Ogromne Jaskiniowce", "Odwiedził wszystkie podziemne obszary w regionie Fado",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("fado", Boolean.TRUE, Boolean.FALSE)));

		// Prasłowiańskie
		achievements.add(createAchievement("zone.underground.krakow", "Szperacz", "Odwiedził wszystkie podziemne obszary w regionie Kraków", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("krakow", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.zakopane", "Grotołaz", "Odwiedził wszystkie podziemne obszary w regionie Zakopane", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("zakopane", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.wieliczka", "Solny Zbieracz", "Odwiedził wszystkie podziemne obszary w regionie Wieliczka", 
				Achievement.HARD_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.gdansk", "Podmorski Szperacz", "Odwiedził wszystkie podziemne obszary w regionie Gdańsk", 
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("gdansk", Boolean.TRUE, Boolean.FALSE)));
		achievements.add(createAchievement("zone.underground.warszawa", "Badacz", "Odwiedził wszystkie podziemne obszary w regionie Warszawa", 
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("warszawa", Boolean.TRUE, Boolean.FALSE)));

		return achievements;
	}
}
