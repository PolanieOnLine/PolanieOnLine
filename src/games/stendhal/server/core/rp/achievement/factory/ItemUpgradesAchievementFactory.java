/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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
import games.stendhal.server.entity.npc.condition.PlayerImprovesNumberOfItemCondition;

public class ItemUpgradesAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_SKETCH = "item.upgrade.sketch";
	public static final String ID_INVEST = "item.upgrade.investment";
	public static final String ID_PERUN = "item.upgrade.perun";
	public static final String ID_DAGGERS = "item.upgrade.daggers";
	public static final String ID_MITHRILRING = "item.upgrade.mithrilring";
	public static final String ID_MITHRIL = "item.upgrade.mithrils";
	public static final String ID_BLACK = "item.upgrade.blackset";
	public static final String ID_WANDS = "item.upgrade.wands";
	public static final String ID_MAGICSET = "item.upgrade.magicset";
	public static final String ID_STONE = "item.upgrade.stoneshield";
	public static final String ID_AXES = "item.upgrade.axes";
	public static final String ID_GORAL = "item.upgrade.goral";

	public static final String[] ITEMS_MIHTIRL = {
			"tarcza z mithrilu", "spodnie z mithrilu", "pas z mithrilu",
			"hełm z mithrilu", "buty z mithrilu", "płaszcz z mithrilu"
	};
	public static final String[] ITEMS_WANDS = {
			"różdżka", "trójząb Trzygłowa", "różdżka Strzyboga",
			"różdżka Wołosa", "różdżka Swaroga", "różdżka Peruna"
	};
	public static final String[] ITEMS_MAGICSET = {
			"magiczne rękawice płytowe", "magiczna zbroja płytowa",
			"magiczne buty płytowe", "magiczne spodnie płytowe"
	};
	public static final String[] ITEMS_BLACKSET = {
			"czarna zbroja", "czarne spodnie",
			"czarna tarcza", "czarne buty"
	};
	public static final String[] ITEMS_AXES = {
			"czarna kosa", "złota kosa", "kosa z mithrilu"
	};

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement(
				ID_SKETCH, "Zarys", "Ulepszył przedmioty co najmniej 10 razy",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(10)));

		itemAchievements.add(createAchievement(
				ID_INVEST, "Inwestycja", "Ulepszył przedmioty co najmniej 50 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(50)));

		itemAchievements.add(createAchievement(
				ID_PERUN, "Wspaniała Różdżka", "Ulepszył różdżkę Peruna do jej maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition("różdżka Peruna")));

		itemAchievements.add(createAchievement(
				ID_DAGGERS, "Potężne i Szybkie", "Ulepszył sztylecik z mithrilu oraz złotą klinge do ich maksymalnego poziomu",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition("sztylecik z mithrilu", "złota klinga")));

		itemAchievements.add(createAchievement(
				ID_MITHRILRING, "Światłość", "Ulepszył pierścień z mithrilu do jej maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition("pierścień z mithrilu")));

		itemAchievements.add(createAchievement(
				ID_MITHRIL, "Doskonałe Uzbrojenie", "Ulepszył tarczę, spodnie, pas, hełm, buty oraz płaszcz z mithrilu do maksymalnego poziomu",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(ITEMS_MIHTIRL)));

		itemAchievements.add(createAchievement(
				ID_BLACK, "Opanowany przez Mrok", "Ulepszył czarną tarczę, spodnie, zbroję oraz buty do maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(ITEMS_BLACKSET)));

		itemAchievements.add(createAchievement(
				ID_WANDS, "Zabawa w Czarodzieja", "Ulepszył wszystkie różdżki co najmniej raz",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(1, ITEMS_WANDS)));

		itemAchievements.add(createAchievement(
				ID_MAGICSET, "Jeszcze Bardziej Magicznie", "Ulepszył cały magiczny zestaw wyposażenia co najmniej raz",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(1, ITEMS_MAGICSET)));

		itemAchievements.add(createAchievement(
				ID_STONE, "Kamienna Broń", "Ulepszył kamienną tarczę na co najmniej drugi poziom",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(2, "kamienna tarcza")));

		itemAchievements.add(createAchievement(
				ID_AXES, "Uzbrojenie Kosiarza", "Ulepszył czarną, złotą oraz z mithrilu kosę do maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition(ITEMS_AXES)));

		itemAchievements.add(createAchievement(
				ID_GORAL, "Dusza Góralska", "Ulepszył złotą ciupagę z trzema wąsami do maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerImprovesNumberOfItemCondition("złota ciupaga z trzema wąsami")));

		return itemAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEMUPGRADES;
	}
}
