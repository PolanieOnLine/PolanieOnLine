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

import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.entity.npc.condition.PlayerLootedNumberOfItemsCondition;

/**
 * Factory for item related achievements.
 *
 * @author madmetzger
 */
public class ItemSetsAchievementFactory extends AbstractAchievementFactory {
	public static final String ID_RED = "item.set.red";
	public static final String ID_BLUE = "item.set.blue";
	public static final String ID_ELVISH = "item.set.elvish";
	public static final String ID_SHADOW = "item.set.shadow";
	public static final String ID_CHAOS = "item.set.chaos";
	public static final String ID_GOLDEN = "item.set.golden";
	public static final String ID_BLACK = "item.set.black";
	public static final String ID_MITHRIL = "item.set.mithril";
	public static final String ID_MAINIO = "item.set.mainio";
	public static final String ID_XENO = "item.set.xeno";
	public static final String ID_GORALSKIE = "item.set.goralskie";
	public static final String ID_DRAGON_CLOAKS = "item.set.dragonset";
	public static final String ID_VAMPIRE = "item.set.wampirze";
	public static final String ID_ROYAL = "item.set.royal";
	public static final String ID_MAGIC = "item.set.magic";
	public static final String ID_WANDS = "item.set.wands";
	public static final String ID_STONE = "item.set.stone";
	public static final String ID_FIRE = "item.set.fireset";
	public static final String ID_ICE = "item.set.iceset";
	public static final String ID_BARB = "item.set.barbset";
	public static final String ID_BOWS = "item.set.bowsset";
	public static final String ID_CROSSBOW = "item.set.crossbowset";

	public static final String[] RED = {
			"zbroja karmazynowa", "hełm karmazynowy", "płaszcz karmazynowy",
			"spodnie karmazynowe", "buty karmazynowe", "tarcza karmazynowa",
			"rękawice karmazynowe", "pas karmazynowy"
	};
	public static final String[] BLUE = {
			"zbroja lazurowa", "lazurowy hełm", "prążkowany płaszcz lazurowy",
			"spodnie lazurowe", "buty lazurowe", "tarcza lazurowa",
			"rękawice lazurowe"
	};
	public static final String[] ELVISH = {
			"zbroja elficka", "kapelusz elficki", "płaszcz elficki",
			"spodnie elfickie", "buty elfickie", "tarcza elficka",
			"elficki naszyjnik", "pas elficki", "rękawice elfickie"
	};
	public static final String[] SHADOW = {
			"zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni",
			"buty cieni", "tarcza cieni", "rękawice cieni", "pas cieni"
	};
	public static final String[] CHAOS = {
			"zbroja chaosu", "hełm chaosu", "płaszcz chaosu",
			"spodnie chaosu", "buty chaosu", "tarcza chaosu"
	};
	public static final String[] GOLDEN = {
			"złota zbroja", "złoty hełm", "złoty płaszcz", "złote spodnie",
			"złote buty", "złota tarcza", "złote rękawice", "złoty pas"
	};
	public static final String[] BLACK = {
			"czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie",
			"czarne buty", "czarna tarcza", "czarne rękawice", "czarny pas"
	};
	private static final String[] MITHRIL = {
			"zbroja z mithrilu", "hełm z mithrilu", "spodnie z mithrilu",
			"rękawice z mithrilu", "pas z mithrilu", "buty z mithrilu"
	};
	public static final String[] MAINIO = {
			"zbroja mainiocyjska", "hełm mainiocyjski", "płaszcz mainiocyjski",
			"spodnie mainiocyjskie", "buty mainiocyjskie", "tarcza mainiocyjska",
			"rękawice mainiocyjskie"
	};
	public static final String[] XENO = {
			"zbroja xenocyjska", "hełm xenocyjski", "płaszcz xenocyjski",
			"spodnie xenocyjskie", "buty xenocyjskie", "tarcza xenocyjska",
			"rękawice xenocyjskie"
	};
	private static final String[] GORALSKIE = {
			"cuha góralska", "góralski gorset", "góralski kapelusz",
			"portki bukowe", "góralska spódnica", "góralska biała spódnica",
			"chusta góralska", "kierpce", "korale", "pas zbójnicki"
	};
	private static final String[] VAMPIRE = {
			"zbroja wampirza", "spodnie wampirze", "płaszcz wampirzy",
			"rękawice wampirze", "buty wampirze", "pas wampirzy",
			"hełm wampirzy"
	};
	public static final String[] ROYAL = {
			"zbroja monarchistyczna", "hełm monarchistyczny", "płaszcz monarchistyczny",
			"spodnie monarchistyczne", "buty monarchistyczne", "tarcza monarchistyczna"
	};
	public static final String[] MAGIC = {
			"magiczna zbroja płytowa", "magiczny hełm kolczy", "magiczny płaszcz",
			"magiczne spodnie płytowe", "magiczne buty płytowe", "magiczna tarcza płytowa"
	};
	private static final String[] STONE = {
			"kamienna zbroja", "hełm kamienny", "płaszcz kamienny",
			"spodnie kamienne", "buty kamienne", "kamienna tarcza",
			"kamienne rękawice", "pas kamienny"
	};
	private static final String[] FIRE = {
			"ognista zbroja", "ogniste spodnie", "ogniste buty",
			"ognista tarcza", "ogniste rękawice", "ognisty amulet",
			"ognisty pas"
	};
	private static final String[] ICE = {
			"lodowa zbroja", "hełm lodowy", "lodowy płaszcz",
			"lodowe spodnie", "lodowe buty", "lodowa tarcza",
			"lodowe rękawice", "lodowy amulet", "lodowy pas"
	};
	private static final String[] BARB = {
			"zbroja barbarzyńcy", "zbroja szamana barbarzyńców", "hełm barbarzyńcy",
			"spodnie barbarzyńcy", "buty barbarzyńcy", "tarcza barbarzyńcy",
			"pas barbarzyńcy", "płaszcz barbarzyńcy", "rękawice barbarzyńcy"
	};

	public static final String[] DRAGON_CLOAKS = {
			"czarny płaszcz smoczy", "lazurowy płaszcz smoczy", "kościany płaszcz smoczy",
			"szmaragdowy płaszcz smoczy", "karmazynowy płaszcz smoczy"
	};
	private static final String[] WANDS = {
			"różdżka", "trójząb Trzygłowa", "różdżka Strzyboga",
			"różdżka Wołosa", "różdżka Swaroga", "różdżka Peruna"
	};
	private static final String[] BOWS = {
			"drewniany łuk", "długi łuk", "klejony łuk", "lodowy łuk",
			"łuk z mithrilu"
	};
	private static final String[] CROSSBOW = {
			"lekka kusza", "kusza", "kusza łowcy", "lodowa kusza",
			"kusza z mithrilu"
	};

	@Override
	public Collection<Achievement> createAchievements() {
		final LinkedList<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_RED, "Niebezpieczna Amazonia",
				"Zdobyto cały karmazynowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, RED)));

		achievements.add(createAchievement(
				ID_BLUE, "Czuję Błękit",
				"Zdobyto cały lazurowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLUE)));

		achievements.add(createAchievement(
				ID_ELVISH, "Zmora Nalwor",
				"Zdobyto cały elficki zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ELVISH)));

		achievements.add(createAchievement(
				ID_SHADOW, "Mieszkaniec Cienia",
				"Zdobyto cały zestaw cieni",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, SHADOW)));

		achievements.add(createAchievement(
				ID_CHAOS, "Zdobywca Chaosu",
				"Zdobyto cały zestaw chaosów",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, CHAOS)));

		achievements.add(createAchievement(
				ID_GOLDEN, "Złote Dziecko",
				"Zdobyto cały złoty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GOLDEN)));

		achievements.add(createAchievement(
				ID_BLACK, "Przejdź na Ciemną Stronę",
				"Zdobyto cały czarny zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLACK)));

		achievements.add(createAchievement(
				ID_MITHRIL, "Przejdź na Jasną Stronę",
				"Zdobyto cały zestaw z mithrilu",
				Achievement.EXTREME_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MITHRIL)));

		achievements.add(createAchievement(
				ID_MAINIO, "Wspaniałe Rzeczy",
				"Zdobyto cały mainiocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAINIO)));

		achievements.add(createAchievement(
				ID_XENO, "Trochę Xenofobiczny?",
				"Zdobyto cały xenocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, XENO)));

		achievements.add(createAchievement(
				ID_GORALSKIE, "Góralskie Dziecko",
				"Zdobyto cały góralski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GORALSKIE)));

		achievements.add(createAchievement(
				ID_VAMPIRE, "Wampir",
				"Zdobyto cały wampirzy zestaw",
				Achievement.EXTREME_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, VAMPIRE)));

		achievements.add(createAchievement(
				ID_ROYAL, "Królewsko Obdarowany",
				"Zdobyto cały monarchistyczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ROYAL)));

		achievements.add(createAchievement(
				ID_MAGIC, "Magiczne Zaopatrzenie",
				"Zdobyto cały magiczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAGIC)));

		achievements.add(createAchievement(
				ID_STONE, "Człowiek Skała",
				"Zdobyto cały kamienny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, STONE)));

		achievements.add(createAchievement(
				ID_FIRE, "Zapałka",
				"Zdobyto cały ognisty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, FIRE)));

		achievements.add(createAchievement(
				ID_ICE, "Skrawek Lodu",
				"Zdobyto cały lodowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ICE)));

		achievements.add(createAchievement(
				ID_BARB, "Mały Barbarzyńca",
				"Zdobyto cały barbarzyński zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BARB)));

		achievements.add(createAchievement(
				ID_DRAGON_CLOAKS, "Pogromca Smoka",
				"Zdobyto wszyskie smocze płaszcze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, DRAGON_CLOAKS)));

		achievements.add(createAchievement(
				ID_WANDS, "Magiczny Zaklinacz",
				"Zdobyto wszystkie różdżki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, WANDS)));

		achievements.add(createAchievement(
				ID_BOWS, "Napięta Cięciwa",
				"Zdobyto wszystkie łuki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BOWS)));

		achievements.add(createAchievement(
				ID_CROSSBOW, "Kuszownik",
				"Zdobyto wszystkie kusze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, CROSSBOW)));

		return achievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEMSETS;
	}
}
