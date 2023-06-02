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
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		//All below ground achievements
		achievements.add(createAchievement(
				"zone.interior.semos", "Domownik",
				"Odwiedzono wszystkie pomieszczenia w regionie Semos",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("semos", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.nalwor", "Gość Elfów",
				"Odwiedzono wszystkie pomieszczenia w regionie Nalwor",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("nalwor", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.ados", "Gość Centrum",
				"Odwiedzono wszystkie pomieszczenia w regionie Ados",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("ados", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.wofolcity", "Miasto Kobold",
				"Odwiedzono wszystkie pomieszczenia w regionie Wo'fol",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("wofol city", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.magiccity", "Magiczne Miasto",
				"Odwiedzono wszystkie pomieszczenia w podziemnym mieście Magic",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("magic city", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.deniran", "Samotna Kraina",
				"Odwiedzono wszystkie pomieszczenia w regionie Deniran",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("deniran", Boolean.FALSE, Boolean.FALSE)));
	
		achievements.add(createAchievement(
				"zone.interior.kirdneh", "Centralny Rynkowicz",
				"Odwiedzono wszystkie pomieszczenia w regionie Kirdneh",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("kirdneh", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.kalavan", "Ogrodnik",
				"Odwiedzono wszystkie pomieszczenia w regionie Kalavan",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("kalavan", Boolean.FALSE, Boolean.FALSE)));

		// Prasłowiańskie
		achievements.add(createAchievement(
				"zone.interior.krakowcity", "Miasto Kraków",
				"Odwiedzono wszystkie pomieszczenia w regionie Krakowa",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("krakow", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.wieliczkacity", "Miasto Wieliczka",
				"Odwiedzono wszystkie pomieszczenia w regionie Wieliczki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("wieliczka", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.gdanskcity", "Miasto Gdańsk",
				"Odwiedzono wszystkie pomieszczenia w regionie Gdańska",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("gdansk", Boolean.FALSE, Boolean.FALSE)));

		achievements.add(createAchievement(
				"zone.interior.zakopanecity", "Miasto Zakopane",
				"Odwiedzono wszystkie pomieszczenia w regionie Zakopanego",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerVisitedZonesInRegionCondition("zakopane", Boolean.FALSE, Boolean.FALSE)));

		return achievements;
	}
}
