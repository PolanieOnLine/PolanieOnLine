/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import java.util.List;

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.KilledRareCreatureCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSharedAllCreaturesCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSoloAllCreaturesCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasKilledNumberOfCreaturesCondition;
/**
 * Factory for fighting achievements
 *
 * @author madmetzger
 */
public class FightingAchievementFactory extends AbstractAchievementFactory {

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> fightingAchievements = new LinkedList<Achievement>();
		fightingAchievements.add(createAchievement("fight.general.rats", "Łowca szczurów", "Zabił 100 szczurów", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("szczur", 100)));
		fightingAchievements.add(createAchievement("fight.general.exterminator", "Eksterminator", "Zabił po 10 szczurów z każdego rodzaju", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "szczur", "szczur jaskiniowy", "wściekły szczur", "szczur zombie", "krwiożerczy szczur", "szczur olbrzymi", "człekoszczur", "człekoszczurzyca", "archiszczur")));
		fightingAchievements.add(createAchievement("fight.general.deer", "Łowca jeleni", "Zabij 25 jeleni", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("jeleń", 25)));
		fightingAchievements.add(createAchievement("fight.general.boars", "Łowca dzików", "Zabij 50 dzików", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("dzik", 50)));
		fightingAchievements.add(createAchievement("fight.general.bears", "Łowca niedźwiedzi", "Zabij 25 niedźwiedzi grizli, 25 niedźwiedzi i 25 misi", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(25, "niedźwiedź", "niedźwiedź grizli", "miś")));
		fightingAchievements.add(createAchievement("fight.general.foxes", "Łowca lisic", "Zabij 20 lisic", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("lisica", 20)));
		fightingAchievements.add(createAchievement("fight.general.safari", "Safari", "Zabij 30 tygrysów, 30 lwów i 50 słoni", Achievement.EASY_BASE_SCORE, true,
													new AndCondition(
															new PlayerHasKilledNumberOfCreaturesCondition("tygrys", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("lew", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("słoń", 50)
															)));
		fightingAchievements.add(createAchievement("fight.general.ents", "Drwal", "Zabij 10 drzewców, 10 drzewcowych and 10 uschłych drzewców", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "drzewiec", "drzewcowa", "uschły drzewiec")));
		fightingAchievements.add(createAchievement("fight.special.rare", "Kłusownik", "Zabił każdego rzadkiego potwora", Achievement.HARD_BASE_SCORE, true,
				new KilledRareCreatureCondition()));
		fightingAchievements.add(createAchievement("fight.general.dragonsslayer", "Pogroma smoków", "Zabił conajmniej 1 smoka każdego rodzaju", Achievement.HARD_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(1, "szkielet smoka", "zgniły szkielet smoka", "złoty smok", "zielony smok", "błękitny smok", "czerwony smok", "pustynny smok", "czarny smok", "czarne smoczysko", "smok arktyczny", "dwugłowy zielony smok", "dwugłowy czerwony smok", "niebieski dwugłowy smok", "dwugłowy czarny smok", "latający czarny smok", "latający złoty smok", "Smok Wawelski")));
		
		fightingAchievements.add(createAchievement("fight.special.all", "Legenda", "Zabił sam wszystkie potwory", Achievement.LEGENDARY_BASE_SCORE, true,
				new KilledSoloAllCreaturesCondition()));
		fightingAchievements.add(createAchievement("fight.special.allshared", "Wojownik drużyny", "Zabił z drużyną wszystkie potwory", Achievement.LEGENDARY_BASE_SCORE, true,
				new KilledSharedAllCreaturesCondition()));
		return fightingAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.FIGHTING;
	}

}
