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
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_ROYAL = "item.set.royal";
	public static final String[] ITEMS_ROYAL = {
			"zbroja monarchistyczna", "hełm monarchistyczny", "płaszcz monarchistyczny", "spodnie monarchistyczne",
			"buty monarchistyczne", "tarcza monarchistyczna"
	};

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement("item.money.100", "Pierwsze Kieszonkowe", "Zdobył 100 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100, "money")));

		itemAchievements.add(createAchievement("item.money.10000", "Złoty Prysznic", "Zdobył 10,000 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000, "money")));

		itemAchievements.add(createAchievement("item.money.100000", "Mała Fortuna", "Zdobył 100,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100000, "money")));

		itemAchievements.add(createAchievement("item.money.1000000", "Już Nie Potrzebujesz Więcej", "Zdobył 1,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000000, "money")));

		itemAchievements.add(createAchievement("item.money.10000000", "Masz ich Za Dużo:)", "Zdobył 10,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000000, "money")));

		itemAchievements.add(createAchievement("item.set.red", "Niebezpieczna Amazonia", "Zdobył cały karmazynowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja karmazynowa", "karmazynowy hełm", "płaszcz karmazynowy",
						"spodnie karmazynowe", "buty karmazynowe", "karmazynowa tarcza", "karmazynowe rękawice", "karmazynowy pas")));

		itemAchievements.add(createAchievement("item.set.blue", "Czuję Błękit", "Zdobył cały lazurowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja lazurowa", "lazurowy hełm", "prążkowany płaszcz lazurowy",
						"spodnie lazurowe", "buty lazurowe", "lazurowa tarcza", "lazurowe rękawice")));

		itemAchievements.add(createAchievement("item.set.elvish", "Zmora Nalwor", "Zdobył cały elficki zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja elficka", "kapelusz elficki", "płaszcz elficki", "spodnie elfickie",
						"buty elfickie", "tarcza elficka", "miecz elficki", "elficki naszyjnik", "pas elficki", "rękawice elfickie")));

		itemAchievements.add(createAchievement("item.set.shadow", "Mieszkaniec Cienia", "Zdobył cały zestaw cieni",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni",
						"buty cieni", "tarcza cieni", "miecz cieni", "rękawice cieni", "pas cieni")));

		itemAchievements.add(createAchievement("item.set.chaos", "Zdobywca Chaosu", "Zdobył cały zestaw chaosów",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja chaosu", "hełm chaosu", "płaszcz chaosu", "spodnie chaosu",
						"buty chaosu", "tarcza chaosu", "miecz chaosu", "topór chaosu", "młot chaosu")));

		itemAchievements.add(createAchievement("item.set.golden", "Złote Dziecko", "Zdobył cały złoty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "złota zbroja", "złoty hełm", "złoty płaszcz", "złote spodnie",
						"złote buty", "złota tarcza", "złota klinga", "złoty pyrlik", "złoty buzdygan", "złoty kiścień",
						"złote rękawice", "złoty pas")));

		itemAchievements.add(createAchievement("item.set.black", "Przejdź na Ciemną Stronę", "Zdobył cały czarny zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie",
						"czarne buty", "czarna tarcza", "czarny miecz", "czarna halabarda", "czarny sztylet", "czarna kosa",
						"czarne rękawice", "czarny pas")));

		itemAchievements.add(createAchievement("item.set.mithril", "Przejdź na Jasną Stronę", "Zdobył cały zestaw z mithrilu",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja z mithrilu", "hełm z mithrilu", "płaszcz z mithrilu",
						"spodnie z mithrilu", "buty z mithrilu", "tarcza z mithrilu", "rękawice z mithrilu", "pas z mithrilu")));

		itemAchievements.add(createAchievement("item.set.mainio", "Wspaniałe Rzeczy", "Zdobył cały mainiocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja mainiocyjska", "hełm mainiocyjski", "płaszcz mainiocyjski",
						"spodnie mainiocyjskie", "buty mainiocyjskie", "tarcza mainiocyjska", "rękawice mainiocyjskie")));

		itemAchievements.add(createAchievement("item.set.xeno", "Trochę Xenofobiczny?", "Zdobył cały xenocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja xenocyjska", "hełm xenocyjski", "płaszcz xenocyjski",
						"spodnie xenocyjskie", "buty xenocyjskie", "tarcza xenocyjska", "miecz xenocyjski", "rękawice xenocyjskie")));

		itemAchievements.add(createAchievement("item.set.goralskie", "Góralskie Dziecko", "Zdobył cały góralski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "cuha góralska", "góralski gorset", "góralski kapelusz", "portki bukowe",
						"góralska spódnica", "góralska biała spódnica", "chusta góralska", "kierpce", "korale", "pas zbójnicki",
						"ciupaga")));

		itemAchievements.add(createAchievement("item.cloak.dragon", "Pogromca Smoka", "Zdobył wszyskie smocze płaszcze",
				Achievement.MEDIUM_BASE_SCORE, false,
				new PlayerLootedNumberOfItemsCondition(1, "czarny płaszcz smoczy", "lazurowy płaszcz smoczy", "kościany płaszcz smoczy",
						"szmaragdowy płaszcz smoczy", "karmazynowy płaszcz smoczy")));

		itemAchievements.add(createAchievement("item.set.wampirze", "Wampir", "Zdobył cały wampirzy zestaw",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, "zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy", "rękawice wampirze",
						"buty wampirze", "pas wampirzy")));

		itemAchievements.add(createAchievement("item.cheese.2000", "Serowy Czarodziej", "Zdobył 2,000 sera",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "ser")));

		itemAchievements.add(createAchievement("item.ham.2500", "Stado Szynek", "Zdobył 2,500 szynki",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2500, "szynka")));

		itemAchievements.add(createAchievement(ID_ROYAL, "Królewsko Obdarowany", "Zdobył cały monarchistyczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ITEMS_ROYAL)));

		return itemAchievements;
	}

}
