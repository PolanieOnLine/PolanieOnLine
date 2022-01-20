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

	private static final String[] RED = {
			"zbroja karmazynowa", "hełm karmazynowy", "płaszcz karmazynowy",
			"spodnie karmazynowe", "buty karmazynowe", "tarcza karmazynowa",
			"rękawice karmazynowe", "pas karmazynowy"
	};
	private static final String[] BLUE = {
			"zbroja lazurowa", "lazurowy hełm", "prążkowany płaszcz lazurowy",
			"spodnie lazurowe", "buty lazurowe", "tarcza lazurowa",
			"rękawice lazurowe"
	};
	private static final String[] ELVISH = {
			"zbroja elficka", "kapelusz elficki", "płaszcz elficki",
			"spodnie elfickie", "buty elfickie", "tarcza elficka",
			"elficki naszyjnik", "pas elficki", "rękawice elfickie"
	};
	private static final String[] SHADOW = {
			"zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni",
			"buty cieni", "tarcza cieni", "rękawice cieni", "pas cieni"
	};
	private static final String[] CHAOS = {
			"zbroja chaosu", "hełm chaosu", "płaszcz chaosu",
			"spodnie chaosu", "buty chaosu", "tarcza chaosu"
	};
	private static final String[] GOLDEN = {
			"złota zbroja", "złoty hełm", "złoty płaszcz", "złote spodnie",
			"złote buty", "złota tarcza", "złote rękawice", "złoty pas"
	};
	private static final String[] BLACK = {
			"czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie",
			"czarne buty", "czarna tarcza", "czarne rękawice", "czarny pas"
	};
	private static final String[] MITHRIL = {
			"zbroja z mithrilu", "hełm z mithrilu", "płaszcz z mithrilu",
			"spodnie z mithrilu", "buty z mithrilu", "tarcza z mithrilu",
			"rękawice z mithrilu", "pas z mithrilu"
	};
	private static final String[] MAINIO = {
			"zbroja mainiocyjska", "hełm mainiocyjski", "płaszcz mainiocyjski",
			"spodnie mainiocyjskie", "buty mainiocyjskie", "tarcza mainiocyjska",
			"rękawice mainiocyjskie"
	};
	private static final String[] XENO = {
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
	private static final String[] ROYAL = {
			"zbroja monarchistyczna", "hełm monarchistyczny", "płaszcz monarchistyczny",
			"spodnie monarchistyczne", "buty monarchistyczne", "tarcza monarchistyczna"
	};
	private static final String[] MAGIC = {
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
			"pas barbarzyńcy", "płaszcz barbarzyńcy"
	};

	private static final String[] DRAGON_CLOAKS = {
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
		List<Achievement> itemSetAchievements = new LinkedList<Achievement>();

		itemSetAchievements.add(createAchievement(
				ID_RED, "Niebezpieczna Amazonia", "Zdobył cały karmazynowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, RED)));

		itemSetAchievements.add(createAchievement(
				ID_BLUE, "Czuję Błękit", "Zdobył cały lazurowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLUE)));

		itemSetAchievements.add(createAchievement(
				ID_ELVISH, "Zmora Nalwor", "Zdobył cały elficki zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ELVISH)));

		itemSetAchievements.add(createAchievement(
				ID_SHADOW, "Mieszkaniec Cienia", "Zdobył cały zestaw cieni",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, SHADOW)));

		itemSetAchievements.add(createAchievement(
				ID_CHAOS, "Zdobywca Chaosu", "Zdobył cały zestaw chaosów",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, CHAOS)));

		itemSetAchievements.add(createAchievement(
				ID_GOLDEN, "Złote Dziecko", "Zdobył cały złoty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GOLDEN)));

		itemSetAchievements.add(createAchievement(
				ID_BLACK, "Przejdź na Ciemną Stronę", "Zdobył cały czarny zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BLACK)));

		itemSetAchievements.add(createAchievement(
				ID_MITHRIL, "Przejdź na Jasną Stronę", "Zdobył cały zestaw z mithrilu",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MITHRIL)));

		itemSetAchievements.add(createAchievement(
				ID_MAINIO, "Wspaniałe Rzeczy", "Zdobył cały mainiocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAINIO)));

		itemSetAchievements.add(createAchievement(
				ID_XENO, "Trochę Xenofobiczny?", "Zdobył cały xenocyjski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, XENO)));

		itemSetAchievements.add(createAchievement(
				ID_GORALSKIE, "Góralskie Dziecko", "Zdobył cały góralski zestaw",
				Achievement.HARD_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, GORALSKIE)));

		itemSetAchievements.add(createAchievement(
				ID_VAMPIRE, "Wampir", "Zdobył cały wampirzy zestaw",
				Achievement.LEGENDARY_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, VAMPIRE)));

		itemSetAchievements.add(createAchievement(
				ID_ROYAL, "Królewsko Obdarowany", "Zdobył cały monarchistyczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ROYAL)));

		itemSetAchievements.add(createAchievement(
				ID_MAGIC, "Magiczne Zaopatrzenie", "Zdobył cały magiczny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, MAGIC)));

		itemSetAchievements.add(createAchievement(
				ID_STONE, "Człowiek Skała", "Zdobył cały kamienny zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, STONE)));

		itemSetAchievements.add(createAchievement(
				ID_FIRE, "Zapałka", "Zdobył cały ognisty zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, FIRE)));

		itemSetAchievements.add(createAchievement(
				ID_ICE, "Skrawek Lodu", "Zdobył cały lodowy zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, ICE)));

		itemSetAchievements.add(createAchievement(
				ID_BARB, "Mały Barbarzyńca", "Zdobył cały barbarzyński zestaw",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BARB)));

		itemSetAchievements.add(createAchievement(
				ID_DRAGON_CLOAKS, "Pogromca Smoka", "Zdobył wszyskie smocze płaszcze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, DRAGON_CLOAKS)));

		itemSetAchievements.add(createAchievement(
				ID_WANDS, "Magiczny Zaklinacz", "Zdobył wszystkie różdżki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, WANDS)));

		itemSetAchievements.add(createAchievement(
				ID_BOWS, "Napięta Cięciwa", "Zdobył wszystkie łuki",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, BOWS)));

		itemSetAchievements.add(createAchievement(
				ID_CROSSBOW, "Kuszownik", "Zdobył wszystkie kusze",
				Achievement.MEDIUM_BASE_SCORE, true,
				new PlayerLootedNumberOfItemsCondition(1, CROSSBOW)));

		return itemSetAchievements;
	}

	@Override
	protected Category getCategory() {
		return Category.ITEMSETS;
	}
}
