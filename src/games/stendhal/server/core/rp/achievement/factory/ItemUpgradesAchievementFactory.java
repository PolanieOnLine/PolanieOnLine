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
import games.stendhal.server.core.rp.achievement.condition.ItemImprovedNumberOfCondition;
import games.stendhal.server.core.rp.achievement.condition.ItemIsMaxImprovedCondition;

public class ItemUpgradesAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_SKETCH = "item.upgrade.sketch";
	public static final String ID_INVEST = "item.upgrade.investment";
	public static final String ID_PERUN = "item.upgrade.perun";
	public static final String ID_MITHRILRING = "item.upgrade.mithrilring";
	public static final String ID_MITHRIL = "item.upgrade.mithrils";

	public static final String[] ITEM_PERUN = { "różdżka Peruna" };
	public static final String[] ITEM_MITHRILRING = { "pierścień z mithrilu" };
	public static final String[] ITEMS_MIHTIRL = {
			"tarcza z mithrilu", "spodnie z mithrilu", "pas z mithrilu",
			"hełm z mithrilu", "buty z mithrilu", "płaszcz z mithrilu" };

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement(
				ID_SKETCH, "Zarys", "Ulepszył przedmioty co najmniej 10 razy",
				Achievement.EASY_BASE_SCORE, true,
				new ItemImprovedNumberOfCondition(10)));

		itemAchievements.add(createAchievement(
				ID_INVEST, "Inwestycja", "Ulepszył przedmioty co najmniej 50 razy",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ItemImprovedNumberOfCondition(50)));

		itemAchievements.add(createAchievement(
				ID_PERUN, "Wspaniała Różdżka", "Ulepszył różdżkę Peruna do jej maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ItemIsMaxImprovedCondition(ITEM_PERUN)));

		itemAchievements.add(createAchievement(
				ID_MITHRILRING, "Światłość", "Ulepszył pierścień z mithrilu do jej maksymalnego poziomu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ItemIsMaxImprovedCondition(ITEM_MITHRILRING)));

		itemAchievements.add(createAchievement(
				ID_MITHRIL, "Doskonałe Uzbrojenie", "Ulepszył tarczę, spodnie, pas, hełm, buty oraz płaszcz z mithrilu do maksymalnego poziomu",
				Achievement.HARD_BASE_SCORE, true,
				new ItemIsMaxImprovedCondition(ITEMS_MIHTIRL)));

		return itemAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEMUPGRADES;
	}
}
