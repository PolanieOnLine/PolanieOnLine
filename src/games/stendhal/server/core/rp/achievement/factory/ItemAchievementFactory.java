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
	public static final String ID_MAGIC = "item.set.magic";
	public static final String ID_WANDS = "item.set.wands";

	private static final String[] RED = {
			"zbroja karmazynowa", "hełm karmazynowy", "płaszcz karmazynowy", "spodnie karmazynowe",
			"buty karmazynowe", "tarcza karmazynowa", "rękawice karmazynowe", "pas karmazynowy"
	};
	
	private static final String[] BLUE = {
			"zbroja lazurowa", "lazurowy hełm", "prążkowany płaszcz lazurowy", "spodnie lazurowe",
			"buty lazurowe", "tarcza lazurowa", "rękawice lazurowe"
	};

	private static final String[] ELVISH = {
			"zbroja elficka", "kapelusz elficki", "płaszcz elficki", "spodnie elfickie", "buty elfickie",
			"tarcza elficka", "miecz elficki", "elficki naszyjnik", "pas elficki", "rękawice elfickie"
	};

	private static final String[] SHADOW = {
			"zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni", "buty cieni",
			"tarcza cieni", "miecz cieni", "rękawice cieni", "pas cieni"
	};

	private static final String[] CHAOS = {
			"zbroja chaosu", "hełm chaosu", "płaszcz chaosu", "spodnie chaosu", "buty chaosu",
			"tarcza chaosu", "miecz chaosu", "topór chaosu", "młot chaosu"
	};

	private static final String[] GOLDEN = {
			"złota zbroja", "złoty hełm", "złoty płaszcz", "złote spodnie", "złote buty",
			"złota tarcza", "złota klinga", "złoty pyrlik", "złoty buzdygan", "złoty kiścień",
			"złote rękawice", "złoty pas"
	};

	private static final String[] BLACK = {
			"czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie", "czarne buty",
			"czarna tarcza", "czarny miecz", "czarna halabarda", "czarny sztylet", "czarna kosa",
			"czarne rękawice", "czarny pas"
	};

	private static final String[] MITHRIL = {
			"zbroja z mithrilu", "hełm z mithrilu", "płaszcz z mithrilu", "spodnie z mithrilu",
			"buty z mithrilu", "tarcza z mithrilu", "rękawice z mithrilu", "pas z mithrilu"
	};

	private static final String[] MAINIO = {
			"zbroja mainiocyjska", "hełm mainiocyjski", "płaszcz mainiocyjski", "spodnie mainiocyjskie",
			"buty mainiocyjskie", "tarcza mainiocyjska", "rękawice mainiocyjskie"
	};

	private static final String[] XENO = {
			"zbroja xenocyjska", "hełm xenocyjski", "płaszcz xenocyjski", "spodnie xenocyjskie",
			"buty xenocyjskie", "tarcza xenocyjska", "miecz xenocyjski", "rękawice xenocyjskie"
	};

	private static final String[] GORALSKIE = {
			"cuha góralska", "góralski gorset", "góralski kapelusz", "portki bukowe", "góralska spódnica",
			"góralska biała spódnica", "chusta góralska", "kierpce", "korale", "pas zbójnicki",
			"ciupaga"
	};

	private static final String[] DRAGON_CLOAKS = {
			"czarny płaszcz smoczy", "lazurowy płaszcz smoczy", "kościany płaszcz smoczy",
			"szmaragdowy płaszcz smoczy", "karmazynowy płaszcz smoczy"
	};

	private static final String[] VAMPIRE = {
			"zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy", "rękawice wampirze",
			"buty wampirze", "pas wampirzy"
	};

	private static final String[] ROYAL = {
			"zbroja monarchistyczna", "hełm monarchistyczny", "płaszcz monarchistyczny", "spodnie monarchistyczne",
			"buty monarchistyczne", "tarcza monarchistyczna"
	};

	private static final String[] MAGIC = {
			"magiczna zbroja płytowa", "magiczny hełm kolczy", "magiczny płaszcz",
			"magiczne spodnie płytowe", "magiczne buty płytowe", "magiczna tarcza płytowa"
	};

	private static final String[] WANDS = {
			"różdżka", "trójząb Trzygłowa", "różdżka Strzyboga",
			"różdżka Wołosa", "różdżka Swaroga", "różdżka Peruna"
	};

	@Override
	public Collection<Achievement> createAchievements() {
		List<Achievement> itemAchievements = new LinkedList<Achievement>();

		itemAchievements.add(createAchievement(
				"item.money.00000100", "Pierwsze Kieszonkowe", "Zdobył 100 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100, "money")));

		itemAchievements.add(createAchievement(
				"item.money.00010000", "Złoty Prysznic", "Zdobył 10,000 monet na potworach",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.00100000", "Mała Fortuna", "Zdobył 100,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(100000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.01000000", "Już Nie Potrzebujesz Więcej", "Zdobył 1,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1000000, "money")));

		itemAchievements.add(createAchievement(
				"item.money.10000000", "Wielka Kąpiel w Złocie", "Zdobył 10,000,000 monet na potworach",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(10000000, "money")));

		itemAchievements.add(createAchievement(
				"item.set.red", "Niebezpieczna Amazonia", "Zdobył cały karmazynowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, RED)));

		itemAchievements.add(createAchievement(
				"item.set.blue", "Czuję Błękit", "Zdobył cały lazurowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLUE)));

		itemAchievements.add(createAchievement(
				"item.set.elvish", "Zmora Nalwor", "Zdobył cały elficki zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ELVISH)));

		itemAchievements.add(createAchievement(
				"item.set.shadow", "Mieszkaniec Cienia", "Zdobył cały zestaw cieni",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, SHADOW)));

		itemAchievements.add(createAchievement(
				"item.set.chaos", "Zdobywca Chaosu", "Zdobył cały zestaw chaosów",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, CHAOS)));

		itemAchievements.add(createAchievement(
				"item.set.golden", "Złote Dziecko", "Zdobył cały złoty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GOLDEN)));

		itemAchievements.add(createAchievement(
				"item.set.black", "Przejdź na Ciemną Stronę", "Zdobył cały czarny zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLACK)));

		itemAchievements.add(createAchievement(
				"item.set.mithril", "Przejdź na Jasną Stronę", "Zdobył cały zestaw z mithrilu",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MITHRIL)));

		itemAchievements.add(createAchievement(
				"item.set.mainio", "Wspaniałe Rzeczy", "Zdobył cały mainiocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAINIO)));

		itemAchievements.add(createAchievement(
				"item.set.xeno", "Trochę Xenofobiczny?", "Zdobył cały xenocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, XENO)));

		itemAchievements.add(createAchievement(
				"item.set.goralskie", "Góralskie Dziecko", "Zdobył cały góralski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GORALSKIE)));

		itemAchievements.add(createAchievement(
				"item.cloak.dragon", "Pogromca Smoka", "Zdobył wszyskie smocze płaszcze",
				Achievement.MEDIUM_BASE_SCORE, false,
				new PlayerLootedNumberOfItemsCondition(1, DRAGON_CLOAKS)));

		itemAchievements.add(createAchievement(
				"item.set.wampirze", "Wampir", "Zdobył cały wampirzy zestaw",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, VAMPIRE)));

		itemAchievements.add(createAchievement(
				"item.cheese.2000", "Serowy Czarodziej", "Zdobył 2,000 sera",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2000, "ser")));

		itemAchievements.add(createAchievement(
				"item.ham.2500", "Stado Szynek", "Zdobył 2,500 szynki",
				Achievement.EASY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(2500, "szynka")));

		itemAchievements.add(createAchievement(
				ID_ROYAL, "Królewsko Obdarowany", "Zdobył cały monarchistyczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ROYAL)));

		itemAchievements.add(createAchievement(
				ID_MAGIC, "Magiczne Zaopatrzenie", "Zdobył cały magiczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAGIC)));

		itemAchievements.add(createAchievement(
				ID_WANDS, "Magiczny Zaklinacz", "Zdobył wszystkie różdżki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, WANDS)));

		return itemAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEM;
	}
}
