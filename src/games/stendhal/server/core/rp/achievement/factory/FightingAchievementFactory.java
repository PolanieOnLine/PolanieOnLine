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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.constants.KillType;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.KilledRareCreatureCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSharedAllCreaturesCondition;
import games.stendhal.server.core.rp.achievement.condition.KilledSoloAllCreaturesCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasKilledNumberOfCreaturesCondition;
import games.stendhal.server.entity.player.Player;
/**
 * Factory for fighting achievements
 *
 * @author madmetzger
 */
public class FightingAchievementFactory extends AbstractAchievementFactory {

	// enemies required for David vs. Goliath
	public static final String[] ENEMIES_GIANTS = {
			"olbrzym", "olbrzym starszy", "amazonka olbrzymia", "olbrzym mistrz", "czarny olbrzym",
			"imperialny generał gigant", "kasarkutominubat", "kobold olbrzymi", "gigantyczny krasnal",
			"supeczłowiek olbrzym", "lodowy olbrzym", "lodowy starszy olbrzym", "Dhohr Nuggetcutter",
			"Lord Durin"
	};
	public static final String ID_GIANTS = "fight.solo.giant";
	public static final int COUNT_GIANTS = 20;

	// enemies required for Heavenly Wrath
	public static final String[] ENEMIES_ANGELS = {
			"anioł", "archanioł", "anioł ciemności", "archanioł ciemności", "upadły anioł"
	};
	public static final String ID_ANGELS = "fight.general.angels";
	public static final int COUNT_ANGELS = 100;

	public static final String ID_WEREWOLF = "fight.general.werewolf";
	public static final int COUNT_WEREWOLF = 500;

	// enemies required for Serenade the Siren
	public static final String[] ENEMIES_MERMAIDS = {
			"syrena ametystowa", "syrena szmaragdowa", "syrena rubinowa", "syrena szafirowa"
	};
	public static final String ID_MERMAIDS = "fight.general.mermaids";
	public static final int COUNT_MERMAIDS = 10000;

	// enemies required for Deep Sea Fisherman
	public static final String[] ENEMIES_DEEPSEA = {
			"rekin", "kraken", "neo kraken"
	};
	public static final String ID_DEEPSEA = "fight.general.deepsea";
	public static final int COUNT_DEEPSEA = 500;

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> fightingAchievements = new LinkedList<Achievement>();
		fightingAchievements.add(createAchievement("fight.general.rats", "Łowca Szczurów", "Zabił 100 szczurów", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("szczur", 100)));
		fightingAchievements.add(createAchievement("fight.general.exterminator", "Eksterminator", "Zabił po 10 szczurów z każdego rodzaju", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "szczur", "szczur jaskiniowy", "wściekły szczur", "szczur zombie", "krwiożerczy szczur", "szczur olbrzymi", "człekoszczur", "człekoszczurzyca", "archiszczur")));
		fightingAchievements.add(createAchievement("fight.general.deer", "Łowca Jeleni", "Zabij 25 jeleni", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("jeleń", 25)));
		fightingAchievements.add(createAchievement("fight.general.boars", "Łowca Dzików", "Zabij 50 dzików", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("dzik", 50)));
		fightingAchievements.add(createAchievement("fight.general.bears", "Łowca Niedźwiedzi", "Zabij 25 niedźwiedzi grizli, 25 niedźwiedzi i 25 misi", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(25, "niedźwiedź", "niedźwiedź grizli", "miś")));
		fightingAchievements.add(createAchievement("fight.general.foxes", "Łowca Lisic", "Zabij 20 lisic", Achievement.EASY_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition("lisica", 20)));
		fightingAchievements.add(createAchievement("fight.general.safari", "Safari", "Zabij 30 tygrysów, 30 lwów i 50 słoni", Achievement.EASY_BASE_SCORE, true,
													new AndCondition(
															new PlayerHasKilledNumberOfCreaturesCondition("tygrys", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("lew", 30),
															new PlayerHasKilledNumberOfCreaturesCondition("słoń", 50)
															)));
		fightingAchievements.add(createAchievement("fight.general.ents", "Drwal", "Zabij 10 drzewców, 10 drzewcowych i 10 uschłych drzewców", Achievement.MEDIUM_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(10, "drzewiec", "drzewcowa", "uschły drzewiec")));
		fightingAchievements.add(createAchievement("fight.special.rare", "Kłusownik", "Zabił każdego rzadkiego potwora", Achievement.HARD_BASE_SCORE, true,
				new KilledRareCreatureCondition()));
		fightingAchievements.add(createAchievement("fight.general.dragonsslayer", "Pogroma Smoków", "Zabił conajmniej 1 smoka każdego rodzaju", Achievement.HARD_BASE_SCORE, true,
													new PlayerHasKilledNumberOfCreaturesCondition(1, "szkielet smoka", "zgniły szkielet smoka", "złoty smok", "zielony smok", "błękitny smok", "czerwony smok", "pustynny smok", "czarny smok", "czarne smoczysko", "smok arktyczny", "dwugłowy zielony smok", "dwugłowy czerwony smok", "dwugłowy niebieski smok", "dwugłowy czarny smok", "dwugłowy lodowy smok", "lodowy smok", "latający czarny smok", "latający złoty smok", "Smok Wawelski")));
		fightingAchievements.add(createAchievement("fight.general.angels", "Diabeł Wcielony", "Zabił conajmniej 1 aniołka, anioła, archanioła i serafina", Achievement.HARD_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(1, "aniołek", "anioł", "archanioł", "serafin")));
		fightingAchievements.add(createAchievement("fight.general.darkangels", "Łowca Mrocznych Aniołów", "Zabił conajmniej 1 upadłego anioła, szkielet anioła, anioła ciemność, archanioła ciemności i azazela", Achievement.HARD_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(1, "upadły anioł", "szkielet anioła", "anioł ciemności", "archanioł ciemności", "azazel")));
		fightingAchievements.add(createAchievement("fight.general.deaths", "Władca Śmierci", "Zabił conajmniej 1 śmierć, czarną śmierć, złotą śmierć, kostuchę, kostuchę różową, kostuchę wielką, kostuchę różową wielką i kostuchę złotą wielką", Achievement.HARD_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(1, "śmierć", "czarna śmierć", "złota śmierć", "kostucha", "kostucha różowa", "kostucha wielka", "kostucha różowa wielka", "kostucha złota wielka")));
		fightingAchievements.add(createAchievement("fight.special.all", "Legenda", "Zabił sam wszystkie potwory", Achievement.LEGENDARY_BASE_SCORE, true,
				new KilledSoloAllCreaturesCondition()));
		fightingAchievements.add(createAchievement("fight.special.allshared", "Wojownik Drużyny", "Zabił z drużyną wszystkie potwory", Achievement.LEGENDARY_BASE_SCORE, true,
				new KilledSharedAllCreaturesCondition()));

		fightingAchievements.add(createAchievement(
				ID_GIANTS, "Dawid kontra Goliat", "Zabił conajmniej 20 każdego rodzaju olbrzyma solo",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(COUNT_GIANTS, KillType.SOLO, ENEMIES_GIANTS)));

		fightingAchievements.add(createAchievement(
				ID_ANGELS, "Niebiański Gniew", "Zabił conajmniej 100 każdego rodzaju anioła",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(COUNT_ANGELS, ENEMIES_ANGELS)));

		fightingAchievements.add(createAchievement(
				ID_WEREWOLF, "Srebrny Pocisk", "Zabił 500 wilkołaków",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(COUNT_WEREWOLF, "wilkołak")));

		fightingAchievements.add(createAchievement(
				ID_MERMAIDS, "Serenada Syren", "Zabił 10,000 klejnotowych rodzai syren",
				Achievement.HARD_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						int kills = 0;

						for (final String mermaid: ENEMIES_MERMAIDS) {
							kills += player.getSoloKill(mermaid) + player.getSharedKill(mermaid);
						}

						return kills >= COUNT_MERMAIDS;
					}
				}));

		fightingAchievements.add(createAchievement(
				ID_DEEPSEA, "Rybak Głębinowy", "Zabił 500 rekinów, krakenów oraz neo krakenów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerHasKilledNumberOfCreaturesCondition(COUNT_DEEPSEA, ENEMIES_DEEPSEA)));

		return fightingAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.FIGHTING;
	}

}
