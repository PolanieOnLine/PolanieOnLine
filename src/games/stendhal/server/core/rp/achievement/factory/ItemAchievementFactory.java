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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemAchievementFactory extends AbstractAchievementFactory {
	public static final String MONEY = "money";

	public static final String[] ITEMS_JEWELLERY = { "ametyst", "diament", "obsydian", "rubin", "szafir", "szmaragd" };
	public static final String[] ITEMS_MAGICSPELLS = {
			"magia deszczu", "magia mroku", "magia płomieni",
			"magia ziemi", "magia światła", "zaklęcie pustelnika"
	};
	public static final String[] ITEMS_DRAGONCLAWS = {
			"pazur arktycznego smoka", "pazur czarnego smoka", "pazur czerwonego smoka",
			"pazur niebieskiego smoka", "pazur zielonego smoka", "pazur złotego smoka"
	};
	public static final String[] ITEMS_ANGELFEATHERS = {
			"pióro anioła", "pióro archanioła", "pióro archanioła ciemności",
			"pióro azazela", "pióro mrocznego anioła", "pióro serafina",
			"pióro upadłego anioła"
	};
	public static final String[] ITEMS_HORNS = {
			"róg demona", "róg jednorożca"
	};

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				"item.money.00000100", "Pierwsze Kieszonkowe", "Zdobył 100 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100, MONEY)));

		achievements.add(createAchievement(
				"item.money.00010000", "Złoty Prysznic", "Zdobył 10,000 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000, MONEY)));

		achievements.add(createAchievement(
				"item.money.00100000", "Mała Fortuna", "Zdobył 100,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100000, MONEY)));

		achievements.add(createAchievement(
				"item.money.01000000", "Już Nie Potrzebujesz Więcej", "Zdobył 1,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000000, MONEY)));

		achievements.add(createAchievement(
				"item.money.10000000", "Wielka Kąpiel w Złocie", "Zdobył 10,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000000, MONEY)));

		achievements.add(createAchievement(
				"item.cheese.2000", "Serowy Czarodziej", "Zdobył 2,000 sera",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "ser")));

		achievements.add(createAchievement(
				"item.ham.2500", "Stado Szynek", "Zdobył 2,500 szynki",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2500, "szynka")));

		achievements.add(createAchievement(
				"item.cod.1500", "Pływanie w Dorszach", "Zdobył 1,500 dorszy",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1500, "dorsz")));

		achievements.add(createAchievement(
				"item.sausage.2000", "Parówkowy Król", "Zdobył 2,000 kiełbasy wiejskiej",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "kiełbasa wiejska")));

		achievements.add(createAchievement(
				"item.set.littlerings", "Mały Komplet Pierścionków", "Zdobył srebrny i złoty pierścień",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(new QuestCompletedCondition("zamowienie_strazy"), new QuestCompletedCondition("zloty_pierscien"))));

		achievements.add(createAchievement(
				"item.goldenblade", "Upragniona Nagroda", "Zdobył złotą klingę",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "złota klinga")));

		achievements.add(createAchievement(
				"item.jewellery", "Cenne Kamienie", "Zdobył 5 z każdego rodzaju drogocennych klejnotów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(5, ITEMS_JEWELLERY)));

		achievements.add(createAchievement(
				"item.magicspells", "Zaklinacz", "Zdobył po 1,000 z każdego rodzaju zaklęć",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000, ITEMS_MAGICSPELLS)));

		achievements.add(createAchievement(
				"item.dragonclaws", "Smocza Wystawa", "Zdobył łącznie 10 różnych smoczych pazurów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String dragonclaws: ITEMS_DRAGONCLAWS) {
							items += player.getNumberOfLootsForItem(dragonclaws);
						}
						return items >= 10;
					}
				}));

		achievements.add(createAchievement(
				"item.angelfeathers", "Anielskie Skrzydła", "Zdobył łącznie 1,000 różnych anielskich piór",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String angel: ITEMS_ANGELFEATHERS) {
							items += player.getNumberOfLootsForItem(angel);
						}
						return items >= 1000;
					}
				}));

		achievements.add(createAchievement(
				"item.horns", "Rogaty Władca", "Zdobył po 200 rogów demona i jednorożca",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(200, ITEMS_HORNS)));

		achievements.add(createAchievement(
				"item.potatoes", "Młoda Pyrka", "Zdobył 2,000 ziemniaków",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "ziemniaki")));

		achievements.add(createAchievement(
				"item.chicken.2000", "Deszcz Udek", "Zdobył 2,000 udek",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "udko")));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}
}
