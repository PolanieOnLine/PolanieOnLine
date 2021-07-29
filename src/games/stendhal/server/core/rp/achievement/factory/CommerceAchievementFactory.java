/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.BoughtNumberOfCondition;
import games.stendhal.server.core.rp.achievement.condition.SoldNumberOfCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Factory for buying & selling items.
 */
public class CommerceAchievementFactory extends AbstractAchievementFactory {

	public static final String ID_HAPPY_HOUR = "buy.drink.alcohol";
	public static final String ID_HEALTH_IMPORTANT = "buy.drink.potions";
	public static final String ID_VANILLA_OR_CHOCOLATE = "buy.drink.shakes";
	public static final String ID_CHOCOLATE = "buy.food.chocolate";
	public static final String ID_LOVE_HOTDOGS = "buy.food.hotdogs";
	public static final String ID_SANDWICHES = "buy.food.sandwiches";
	public static final String ID_SCROLLS = "buy.scrolls";

	public static final String ID_CHEESE_MERCHANT = "sell.food.cheese";
	public static final String ID_FISHSHOP = "sell.food.fishshop";
	public static final String ID_NAILS = "sell.item.nails";
	public static final String ID_SKINS = "sell.item.skins";

	public static final int COUNT_HAPPY_HOUR = 100;

	public static final String[] ITEMS_HAPPY_HOUR = { "sok z chmielu", "napój z winogron" };
	public static final String[] ITEMS_HEALTH_IMPORTANT = { "mały eliksir", "eliksir", "duży eliksir", "wielki eliksir" };
	public static final String[] ITEMS_VANILLA_OR_CHOCOLATE = { "shake waniliowy", "shake czekoladowy" };
	public static final String[] ITEMS_LOVE_HOTDOGS = { "hotdog", "hotdog z serem" };
	public static final String[] ITEMS_SANDWICHES = { "kanapka", "kanapka z tuńczykiem" };
	public static final String[] ITEMS_SCROLLS = {
			"zwój ados", "zwój deniran", "zwój fado", "zwój gdański",
			"zwój kalavan", "zwój kirdneh", "zwój krakowski", "zwój nalwor",
			"zwój semos", "zwój tatrzański", "zwój wieliczka",
			"bilet na mecz", "bilet turystyczny", "niezapisany zwój"
	};

	public static final String[] ITEMS_CHEESE_MERCHANT = { "ser" };
	public static final String[] ITEMS_FISHSHOP = {
			"okoń", "makrela", "płotka", "palia alpejska", "błazenek", "pokolec", "pstrąg",
			"dorsz", "skrzydlica", "tuńczyk", "leszcz", "szczupak", "karp", "karp lustrzeń"
	};
	public static final String[] ITEMS_NAILS = { "pazury wilcze", "niedźwiedzie pazury", "pazury tygrysie" };
	public static final String[] ITEMS_SKINS = {
			"skóra arktycznego smoka", "skóra czarnego smoka", "skóra czerwonego smoka",
			"skóra niebieskiego smoka", "skóra zielonego smoka", "skóra złotego smoka",
			"skóra tygrysa", "skóra lwa", "skóra białego tygrysa", "skóra zwierzęca"
	};

	@Override
	protected Category getCategory() {
		return Category.COMMERCE;
	}

	@Override
	public Collection<Achievement> createAchievements() {
		final List<Achievement> achievements = new LinkedList<Achievement>();

		achievements.add(createAchievement(
				ID_HAPPY_HOUR, "Gdzieś jest Szczęśliwa Godzina", "Zakupił po 100 butelek soku z chmielu oraz kieliszków napoju z winogron",
				Achievement.EASY_BASE_SCORE, true,
				new BoughtNumberOfCondition(COUNT_HAPPY_HOUR, ITEMS_HAPPY_HOUR)));

		achievements.add(createAchievement(
				ID_HEALTH_IMPORTANT, "Zdrowie Najważniejsze", "Zakupił razem 500 różnych eliksirów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String potions: ITEMS_HEALTH_IMPORTANT) {
							items += player.getQuantityOfBoughtItems(potions);
						}
						return items >= 500;
					}
				}));

		achievements.add(createAchievement(
				ID_VANILLA_OR_CHOCOLATE, "Wanilia czy Czekolada", "Zakupił po 200 shake'ów waniliowych i czekoladowych",
				Achievement.EASY_BASE_SCORE, true,
				new BoughtNumberOfCondition(200, ITEMS_VANILLA_OR_CHOCOLATE)));

		achievements.add(createAchievement(
				ID_CHOCOLATE, "Czekoladowy Raj", "Zakupił 200 tabliczek czekolady i 50 lukrecji",
				Achievement.EASY_BASE_SCORE, true,
				new AndCondition(
						new BoughtNumberOfCondition(200, "tabliczka czekolady"),
						new BoughtNumberOfCondition(50, "lukrecja"))));

		achievements.add(createAchievement(
				ID_LOVE_HOTDOGS, "Miłośnik Hotdogów", "Zakupił razem 500 różnych hotdogów",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String hotdogs: ITEMS_LOVE_HOTDOGS) {
							items += player.getQuantityOfBoughtItems(hotdogs);
						}
						return items >= 500;
					}
				}));

		achievements.add(createAchievement(
				ID_SANDWICHES, "Kanapkowicz", "Zakupił razem 1,000 różnych kanapek",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String sandwiches: ITEMS_SANDWICHES) {
							items += player.getQuantityOfBoughtItems(sandwiches);
						}
						return items >= 1000;
					}
				}));

		achievements.add(createAchievement(
				ID_SCROLLS, "Wygodny Podróżnik", "Zakupił po 100 różnych zwojów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new SoldNumberOfCondition(100, ITEMS_SCROLLS)));

		achievements.add(createAchievement(
				ID_CHEESE_MERCHANT, "Serowy Handlarz", "Sprzedał 1,000 kawałków sera",
				Achievement.MEDIUM_BASE_SCORE, true,
				new SoldNumberOfCondition(1000, ITEMS_CHEESE_MERCHANT)));

		achievements.add(createAchievement(
				ID_NAILS, "Zwierzęce Paznokietki", "Sprzedał po 100 różnych zwierzęcych pazurów",
				Achievement.HARD_BASE_SCORE, true,
				new SoldNumberOfCondition(100, ITEMS_NAILS)));

		achievements.add(createAchievement(
				ID_SKINS, "Skórnik", "Sprzedał po 50 różnych smoczych i zwierzęcych skór",
				Achievement.HARD_BASE_SCORE, true,
				new SoldNumberOfCondition(50, ITEMS_SKINS)));

		achievements.add(createAchievement(
				ID_FISHSHOP, "Działalność Rybacka", "Sprzedał razem 1,000 różnych ryb",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String fishes: ITEMS_FISHSHOP) {
							items += player.getQuantityOfSoldItems(fishes);
						}
						return items >= 1000;
					}
				}));

		return achievements;
	}
}