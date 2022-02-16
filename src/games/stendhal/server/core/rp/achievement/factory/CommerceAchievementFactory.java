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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.Category;
import games.stendhal.server.core.rp.achievement.condition.BoughtNumberOfCondition;
import games.stendhal.server.core.rp.achievement.condition.SoldNumberOfCondition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Factory for buying & selling items.
 */
public class CommerceAchievementFactory extends AbstractAchievementFactory {
	private static final Logger logger = Logger.getLogger(CommerceAchievementFactory.class);
	private static final MerchantsRegister MR = MerchantsRegister.get();

	public static final String ID_HAPPY_HOUR = "buy.drink.alcohol";
	public static final String ID_HEALTH_IMPORTANT = "buy.drink.potions";
	public static final String ID_AID_KNOWLEDGE = "buy.drink.aidknowledge";
	public static final String ID_VANILLA_OR_CHOCOLATE = "buy.drink.shakes";
	public static final String ID_CHOCOLATE = "buy.food.chocolate";
	public static final String ID_LOVE_HOTDOGS = "buy.food.hotdogs";
	public static final String ID_SANDWICHES = "buy.food.sandwiches";
	public static final String ID_SCROLLS = "buy.scrolls";
	public static final String ID_HOUSE = "buy.house";
	public static final String ID_ICECREAM = "buy.drink.icecream";
	public static final String ID_STAYING_SANE = "buy.drink.stayingsane";

	public static final String ID_CHEESE_MERCHANT = "sell.food.cheese";
	public static final String ID_FISHSHOP = "sell.food.fishshop";
	public static final String ID_NAILS = "sell.item.nails";
	public static final String ID_SKINS = "sell.item.skins";
	public static final String ID_MUSHROOMS = "sell.item.mushrooms";
	public static final String ID_MAGICS = "sell.item.magics";
	public static final String ID_BARS = "sell.item.bars";
	public static final String ID_SNOWBALLS = "sell.item.snowballs";
	public static final String ID_SZCZERBIEC = "sell.item.szczerbiec";
	public static final String ID_MARKSMAN = "sell.item.marksman";
	public static final String ID_FURTRADER = "sell.item.furtrader";

	public static final String[] ITEMS_HAPPY_HOUR = { "sok z chmielu", "napój z winogron" };
	public static final String[] ITEMS_HEALTH_IMPORTANT = { "mały eliksir", "eliksir", "duży eliksir", "wielki eliksir" };
	public static final String[] ITEMS_VANILLA_OR_CHOCOLATE = { "shake waniliowy", "shake czekoladowy" };
	public static final String[] ITEMS_LOVE_HOTDOGS = { "hotdog", "hotdog z serem" };
	public static final String[] ITEMS_SANDWICHES = { "kanapka", "kanapka z tuńczykiem" };
	public static final String[] ITEMS_SCROLLS = {
			"zwój ados", "zwój deniran", "zwój fado", "zwój gdański",
			"zwój kalavan", "zwój kirdneh", "zwój krakowski", "zwój nalwor",
			"zwój semos", "zwój tatrzański", "zwój wieliczka",
			"bilet na mecz", "niezapisany zwój", "zwój tarnów"
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
	public static final String[] ITEMS_MUSHROOMS = { "borowik", "pieczarka", "muchomor" };
	public static final String[] ITEMS_MAGICS = {
			"magia ziemi", "magia płomieni", "magia deszczu",
			"magia lodu", "magia mroku", "magia światła"
	};
	public static final String[] ITEMS_BARS = { "sztabka złota", "sztabka mithrilu" };

	// default required amount to spend at a seller for "Community Supporter"
	private static final int default_req_purchase = 1000;

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
				new BoughtNumberOfCondition(100, ITEMS_HAPPY_HOUR)));

		achievements.add(createAchievement(
				ID_AID_KNOWLEDGE, "Pierwsza Pomoc", "Zakupił eliksir",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String potions: ITEMS_HEALTH_IMPORTANT) {
							items += player.getQuantityOfBoughtItems(potions);
						}
						return items >= 1;
					}
				}));

		achievements.add(createAchievement(
				ID_HEALTH_IMPORTANT, "Zdrowie Najważniejsze", "Zakupił łącznie 500 różnych eliksirów",
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
						new BoughtNumberOfCondition("tabliczka czekolady", 200),
						new BoughtNumberOfCondition("lukrecja", 50))));

		achievements.add(createAchievement(
				ID_LOVE_HOTDOGS, "Miłośnik Hotdogów", "Zakupił łącznie 500 różnych hotdogów",
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
				ID_SANDWICHES, "Kanapkowicz", "Zakupił łącznie 1,000 różnych kanapek",
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
				ID_SCROLLS, "Wygodny Podróżnik", "Zakupił po 100 każdego rodzaju zwojów oraz co najmniej 20 biletów turystycznych",
				Achievement.MEDIUM_BASE_SCORE, true,
				new AndCondition(
						new BoughtNumberOfCondition(100, ITEMS_SCROLLS),
						new BoughtNumberOfCondition("bilet turystyczny", 20))));

		achievements.add(createAchievement(
				ID_HOUSE, "Nie ma to jak w Domu", "Zakupił pierwszy domek",
				Achievement.EASY_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
						return player.hasQuest("house");
					}
				}
		));

		achievements.add(createAchievement(
				ID_ICECREAM, "Schłodzenie", "Zakupił 100 lodów",
				Achievement.EASY_BASE_SCORE, true,
				new BoughtNumberOfCondition("lody", 100)));

		achievements.add(createAchievement(
				ID_STAYING_SANE, "Przy Zdrowych Zmysłach", "Zakupił 1,000 eliksirów, 750 dużych eliksirów oraz 500 wielkich eliksirów",
				Achievement.EASY_BASE_SCORE, true,
				new AndCondition(
						new BoughtNumberOfCondition("eliksir", 1000),
						new BoughtNumberOfCondition("duży eliksir", 750),
						new BoughtNumberOfCondition("wielki eliksir", 500))));

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
				ID_FISHSHOP, "Działalność Rybacka", "Sprzedał łącznie 1,000 różnych ryb",
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

		achievements.add(createAchievement(
				ID_MUSHROOMS, "Hodowca Kapeluszników", "Sprzedał łącznie 2,000 różnych grzybów",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String mushrooms: ITEMS_MUSHROOMS) {
							items += player.getQuantityOfSoldItems(mushrooms);
						}
						return items >= 2000;
					}
				}));

		achievements.add(createAchievement(
				ID_MAGICS, "Zbędne Czary", "Sprzedał po 500 każdego rodzaju magii",
				Achievement.MEDIUM_BASE_SCORE, true,
				new SoldNumberOfCondition(500, ITEMS_MAGICS)));

		achievements.add(createAchievement(
				ID_BARS, "Wartość Złota", "Sprzedał łącznie 777 sztabek złota lub mithrilu",
				Achievement.MEDIUM_BASE_SCORE, true,
				new ChatCondition() {
					@Override
					public boolean fire(Player player, Sentence sentence, Entity npc) {
						int items = 0;
						for (final String bars: ITEMS_BARS) {
							items += player.getQuantityOfSoldItems(bars);
						}
						return items >= 777;
					}
				}));

		achievements.add(createAchievement(
				ID_SNOWBALLS, "Zrzut Śnieżek", "Sprzedał 1,000 śnieżek",
				Achievement.EASY_BASE_SCORE, true,
				new SoldNumberOfCondition("śnieżka", 1000)));

		achievements.add(createAchievement(
				ID_SZCZERBIEC, "Życiowy Interes", "Sprzedał szczerbiec",
				Achievement.EASY_BASE_SCORE, true,
				new SoldNumberOfCondition("szczerbiec", 1)));

		achievements.add(createAchievement(
				ID_MARKSMAN, "Koniec Strzelca", "Sprzedał łuk z mithrilu",
				Achievement.EASY_BASE_SCORE, true,
				new SoldNumberOfCondition("łuk z mithrilu", 1)));

		achievements.add(createAchievement(
				ID_FURTRADER, "Handlarz Futer", "Sprzedał 300 futer",
				Achievement.EASY_BASE_SCORE, true,
				new SoldNumberOfCondition("futro", 300)));

		achievements.add(createAchievement(
				"commerce.buy.all", "Wspierający Społeczność", "Wydał swoje pieniądze u różnych handlarzy na świecie",
				Achievement.MEDIUM_BASE_SCORE, true,
				new HasSpentAmountAtSellers()));

		SingletonRepository.getCachedActionManager().register(new Runnable() {
			public void run() {
				logger.debug("Registering seller responses for Community Supporter achievement...");
				String csSellers = "";

				final NPCList npcs = NPCList.get();
				for (final String name: MR.getSellersNames()) {
					final SpeakerNPC seller = npcs.get(name);
					if (seller != null) {
						Integer req_purchase = default_req_purchase;
						if (HasSpentAmountAtSellers.TRADE_ALL_AMOUNTS.containsKey(name)) {
							req_purchase = HasSpentAmountAtSellers.TRADE_ALL_AMOUNTS.get(name);
							if (req_purchase.equals(0)) {
								req_purchase = null;
							}
						}

						if (req_purchase != null) {
							seller.add(
								ConversationStates.ATTENDING,
								Arrays.asList("patron", "patronage", "patronat"),
								null,
								ConversationStates.ATTENDING,
								null,
								new RespondToPurchaseAmountInquiry(req_purchase));

							// add some info to "help" response
							final String sHelp = seller.getReply("help");
							if (sHelp != null && !sHelp.equals("")) {
								seller.addHelp(sHelp + " Możesz też zapytać o #patronat.");
							} else {
								seller.addHelp("Możesz mnie zapytać o #patronat.");
							}

							// logger output
							if (csSellers.length() > 0) {
								csSellers = csSellers + ", ";
							}
							csSellers = csSellers + name;
						}
					}
				}

				logger.debug("Community Supporter sellers: " + csSellers);
			}
		});

		return achievements;
	}

	private static class HasSpentAmountAtSellers implements ChatCondition {
		// default value for sellers not included in this list is 1000
		// NOTE: maybe use an explicit list instead of setting default to 1000
		//   as more NPCs will likely be added in the future.
		protected static final Map<String, Integer> TRADE_ALL_AMOUNTS = new HashMap<String, Integer>() {{
			put("Margaret", 1000);
			put("Ilisa", 4000);
			put("Adena", 500);
			put("Coralia", 500);
			put("Dale", 500);
			put("Mayor Chalmers", 10000);
			put("Mia", 2000);
			put("Mrotho", 2500);
			put("Karl", 50);
			put("Philomena", 200);
			put("Dr. Feelgood", 8000);
			put("Haizen", 10000);
			put("Mirielle", 20000);
			put("D J Smith", 4000);
			put("Wanda", 20000);
			put("Sarzina", 17000);
			put("Xhiphin Zohos", 12000);
			put("Orchiwald", 9000);
			put("Sam", 600);
			put("Hazel", 16000);
			put("Erodel Bmud", 20000);
			put("Kendra Mattori", 16000);
			put("Diehelm Brui", 1000);
			put("Lorithien", 10000);
			put("Jynath", 16000);
			put("Ouchit", 400);
			put("Xin Blanca", 190); // 1 of each item
			put("Xoderos", 570); // 1 of each item
			put("Barbarus", 400); // 1 pick
			put("Jenny", 1000);
			//put("Nishiya", 60); // 2 sheep (need to update so buying animals is supported)
			put("Wrviliza", 200);
			put("Sara Beth", 2500);
			put("Laura", 2000);
			put("Jimbo", 2000);
			put("Old Mother Helena", 2500);
			put("Aldrin", 2000);
			put("Ruarhi", 2000);
			put("Trillium", 2500);
			put("Carmen", 2000);

			// excluded
			put("Gulimo", 0);
			put("Hagnurk", 0);
			put("Ognir", 0);
			put("Mrs. Yeti", 0);
			put("Wrvil", 0);
			put("Zekiel", 0);
			put("Mizuno", 0);
			put("Rengard", 0);
			put("Felina", 0);
			put("Caroline", 0); // only sells during Minetown weeks
			put("Edward", 0);
		}};

		public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
			for (final String seller: MR.getSellersNames()) {
				if (!player.has("npc_purchases", seller)) {
					return false;
				} else {
					final int amount = Integer.parseInt(player.get("npc_purchases", seller));
					if (TRADE_ALL_AMOUNTS.containsKey(seller)) {
						if (amount < TRADE_ALL_AMOUNTS.get(seller)) {
							return false;
						}
					} else if (amount < default_req_purchase) {
						return false;
					}
				}
			}

			return true;
		}
	}

	private static class RespondToPurchaseAmountInquiry implements ChatAction {
		private int req_purchase = 0;

		protected RespondToPurchaseAmountInquiry(final int amount) {
			req_purchase = amount;
		}

		public void fire(final Player player, final Sentence sentence, final EventRaiser seller) {
			int spent = 0;
			final String sellerName = seller.getName();
			if (player.has("npc_purchases", sellerName)) {
				spent = player.getInt("npc_purchases", sellerName);
			}

			if (spent == 0) {
				seller.say("Nawet niczego ode mnie jeszcze nie " + Grammar.genderVerb(player.getGender(), "kupiłeś") + ".");
			} else if (spent >= req_purchase) {
				seller.say("Dziękuję za wsparcie mojego sklepu! Poszukiwacze przygód, tacy jak ty, utrzymują ten świat na powierzchni.");
			} else {
				final double per = (Double.valueOf(spent) / req_purchase) * 100;

				if (per < 25) {
					seller.say("Nie odwiedzasz tu zbyt często.");
				} else if (per < 50) {
					seller.say("Widzę, że od czasu do czasu przychodzisz.");
				} else if (per < 75) {
					seller.say("Stajesz się tutaj regularnym klientem, prawda?");
				} else {
					seller.say("Oczywiście, że cię pamiętam. Jak mogę zapomnieć o moim ulubionym kliencie?");
				}
			}
		}
	}
}