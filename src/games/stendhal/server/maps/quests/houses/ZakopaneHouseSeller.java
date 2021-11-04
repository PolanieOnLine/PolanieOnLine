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
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

final class ZakopaneHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in zakopane. */
	private static final int PERCENTAGE = 10;
	private static final int COST_ZAKOPANE = 500000;

	private static final String WOJTEK_QUEST_SLOT = "gazda_wojtek_daily_item";
	private static final String JADZKA_QUEST_SLOT = "herbs_for_jadzka";
	private static final String ADAS_QUEST_SLOT = "pomoc_adasiowi";
	private static final String FRYDERYK_QUEST_SLOT = "scythe_fryderyk";
	private static final String BERCIK_QUEST_SLOT = "cech_gornika";
	private static final String ANDRZEJ_QUEST_SLOT = "andrzej_make_zlota_ciupaga";

	ZakopaneHouseSeller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		final List<String> costPhrases = Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena");

		// player is not old enough
		add(ConversationStates.ATTENDING, 
			costPhrases,
			new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE)),
			ConversationStates.ATTENDING,
			"Cena za nowy dom w Zakopanym to "
			+ getCost()
			+ " money. Ale obawiam się, że nie mogę ci jeszcze zaufać, wróć kiedy spędzisz przynajmniej " 
			+ Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " godzin w Zakopanym.",
			null);

		// player doesn't have a house and is old enough but has not done required quests
		add(ConversationStates.ATTENDING, 
			costPhrases,
			new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
					new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
					new NotCondition(
							new AndCondition(
									new QuestCompletedCondition(ZakopaneHouseSeller.WOJTEK_QUEST_SLOT),
									new QuestCompletedCondition(ZakopaneHouseSeller.JADZKA_QUEST_SLOT),
									new QuestCompletedCondition(ZakopaneHouseSeller.ADAS_QUEST_SLOT),
									new QuestCompletedCondition(ZakopaneHouseSeller.FRYDERYK_QUEST_SLOT),
									new QuestCompletedCondition(ZakopaneHouseSeller.BERCIK_QUEST_SLOT),
									new QuestCompletedCondition(ZakopaneHouseSeller.ANDRZEJ_QUEST_SLOT)))),
			ConversationStates.ATTENDING, 
			"Koszt nowego domu w Zakopanym wynosi "
			+ getCost()
			+ " money. Ale obawiam się, że nie mogę sprzedać Tobie domu, jeszcze trzeba udowodnić #obywatelstwo.",
			null);

		// player is eligible to buy a house
		add(ConversationStates.ATTENDING, 
			costPhrases,
			new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
					new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
					new QuestCompletedCondition(ZakopaneHouseSeller.WOJTEK_QUEST_SLOT),
					new QuestCompletedCondition(ZakopaneHouseSeller.JADZKA_QUEST_SLOT),
					new QuestCompletedCondition(ZakopaneHouseSeller.ADAS_QUEST_SLOT),
					new QuestCompletedCondition(ZakopaneHouseSeller.FRYDERYK_QUEST_SLOT),
					new QuestCompletedCondition(ZakopaneHouseSeller.BERCIK_QUEST_SLOT),
					new QuestCompletedCondition(ZakopaneHouseSeller.ANDRZEJ_QUEST_SLOT)),
			ConversationStates.QUEST_OFFERED,
			"Nowy dom w Zakopanym kosztuje "
			+ getCost()
			+ " money. Ponadto trzeba zapłacić podatek " + HouseTax.BASE_TAX
			+ " money, co miesiąc. Jeśli masz jakiś dom na oku powiedz jego numer, sprawdzę czy jest wolny. "
			+ "Domy w Zakopanym mają numery od "
			+ getLowestHouseNumber() + " do " + getHighestHouseNumber() + ".",
			null);

		// handle house numbers 201 to 215
		addMatching(ConversationStates.QUEST_OFFERED,
			// match for all numbers as trigger expression
			ExpressionType.NUMERAL, new JokerExprMatcher(),
			new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
			ConversationStates.ATTENDING, 
			null,
			new BuyHouseChatAction(getCost(), QUEST_SLOT));

		addJob("Jestem agentem nieruchomości, po prostu sprzedaję domy w Zakopanym. Zapytaj o #cenę jeżeli jesteś zainteresowany. Nasz katalog domów znajduje sie na  (tu adres strony internetowej).");
		addReply(Arrays.asList("citizen", "obywatelstwo"), "Przeprowadzam nieformalną ankietę wśród mieszkańców.\n"
			+ "A mówię o moim przyjacielu kowalu Andrzeju,\n"
			+ "małym chłopcu Adasiu, panu Fryderyku,\n"
			+ "naszym burmistrzu Wojtku i pani Jadzi, która pracuje w szpitalu.\n"
			+ "Wspólnie wydadzą wiarygodną opinie.");

		setDescription("Oto Domiesław, piewca ciepła domowego. Zapytaj czy ma dla ciebie ofertę.");
		setEntityClass("estateagent2npc");
		setPosition(24, 3);
		initHP(100);
	}

	@Override
	protected int getDeprecationPercentage() {
		return ZakopaneHouseSeller.PERCENTAGE;
	}

	@Override
	protected int getCost() {
		return ZakopaneHouseSeller.COST_ZAKOPANE;
	}

	@Override
	protected void createPath() {
		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(24, 3));
		nodes.add(new Node(24, 16));
		nodes.add(new Node(19, 16));
		nodes.add(new Node(19, 14));
		nodes.add(new Node(17, 14));
		nodes.add(new Node(17, 15));
		nodes.add(new Node(13, 15));
		nodes.add(new Node(13, 3));
		nodes.add(new Node(14, 3));
		nodes.add(new Node(14, 7));
		nodes.add(new Node(13, 7));
		nodes.add(new Node(13, 16));
		nodes.add(new Node(14, 16));
		nodes.add(new Node(14, 21));
		nodes.add(new Node(21, 21));
		nodes.add(new Node(21, 16));
		nodes.add(new Node(24, 16));
		nodes.add(new Node(24, 7));
		nodes.add(new Node(33, 7));
		nodes.add(new Node(33, 5));
		nodes.add(new Node(24, 5));
		setPath(new FixedPath(nodes, true));
	}

	@Override
	protected int getHighestHouseNumber() {
		return 215;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 201;
	}
}
