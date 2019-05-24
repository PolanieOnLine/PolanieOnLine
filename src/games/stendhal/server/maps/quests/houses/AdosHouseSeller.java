/**
 * 
 */
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

final class AdosHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in ados. */
	private static final int COST_ADOS = 120000;
	private static final String ANNA_QUEST_SLOT = "toys_collector";
	private static final String KEYRING_QUEST_SLOT = "hungry_joshua";
	private static final String GHOSTS_QUEST_SLOT = "find_ghosts";
	private static final String DAILY_ITEM_QUEST_SLOT = "daily_item";
	private static final String FISHROD_QUEST_SLOT = "get_fishing_rod";
	private static final String ZARA_QUEST_SLOT = "suntan_cream_zara";

	AdosHouseSeller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		// Other than the condition that you must not already own a house, there are a number of conditions a player must satisfy. 
		// For definiteness we will check these conditions in a set order. 
		// So then the NPC doesn't have to choose which reason to reject the player for (appears as a WARN from engine if he has to choose)
		
		// player is not old enough
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
				 new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE)),
				 ConversationStates.ATTENDING,
				 "Cena za nowy dom w Ados to "
				 + getCost()
				 + " money. Ale obawiam się, że nie mogę ci jeszcze zaufać, wróć kiedy spędzisz przynajmniej " 
				 + Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " godzin w Faiumoni.",
					null);
		
		
		// player doesn't have a house and is old enough but has not done required quests
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT),
								  new NotCondition(
													  new AndCondition(
																	   new QuestCompletedCondition(AdosHouseSeller.DAILY_ITEM_QUEST_SLOT),
																	   new QuestCompletedCondition(AdosHouseSeller.ANNA_QUEST_SLOT),
																	   new QuestCompletedCondition(AdosHouseSeller.KEYRING_QUEST_SLOT),
																	   new QuestCompletedCondition(AdosHouseSeller.FISHROD_QUEST_SLOT),
																	   new QuestCompletedCondition(AdosHouseSeller.GHOSTS_QUEST_SLOT),
																	   new QuestCompletedCondition(AdosHouseSeller.ZARA_QUEST_SLOT)))),
				 ConversationStates.ATTENDING, 
				 "Koszt nowego domu w Ados wynosi "
				 + getCost()
				 + " money. Ale obawiam się, że nie mogę sprzedać Tobie domu, jeszcze trzeba udowodnić #obywatelstwo.",
				 null);
		
		// player is eligible to buy a house
		add(ConversationStates.ATTENDING, 
					Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), 
								  new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestCompletedCondition(AdosHouseSeller.DAILY_ITEM_QUEST_SLOT),
								  new QuestCompletedCondition(AdosHouseSeller.ANNA_QUEST_SLOT),
								  new QuestCompletedCondition(AdosHouseSeller.KEYRING_QUEST_SLOT),
								  new QuestCompletedCondition(AdosHouseSeller.FISHROD_QUEST_SLOT),
								  new QuestCompletedCondition(AdosHouseSeller.GHOSTS_QUEST_SLOT),
								  new QuestCompletedCondition(AdosHouseSeller.ZARA_QUEST_SLOT)),
				 ConversationStates.QUEST_OFFERED, 
				 "Nowy dom w Ados kosztuje "
				 + getCost()
				 + " money. Ponadto trzeba zapłacić podatek " + HouseTax.BASE_TAX
				 + " money, co miesiąc. Jeśli masz jakiś dom na oku powiedz jego numer, sprawdzę czy jest wolny. "
				 + "Domy w Ados mają numery od "
				 + getLowestHouseNumber() + " do " + getHighestHouseNumber() + ".",
				 null);
		
		// handle house numbers getLowestHouseNumber() - getHighestHouseNumber()
		addMatching(ConversationStates.QUEST_OFFERED,
				 // match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
				ConversationStates.ATTENDING, 
				null,
				new BuyHouseChatAction(getCost(), QUEST_SLOT));
		
		addJob("Jestem agentem nieruchomości, po prostu sprzedaje domy w Ados. Zapytaj o #cenę jeżeli jesteś zainteresowany. Nasz katalog domów znajduje się na  #http://www.polskagra.net/");
		addReply(Arrays.asList("citizen", "obywatelstwo"), "Przeprowadzam nieformalną ankietę wśród mieszkańców. A mówię o moim przyjacielu Joshua, burmistrzu Ados, małej dziewczynce Anna, rybaku Pequod, pięknej Zara, a nawet o duchu Carena. Wspólnie wydadzą wiarygodną opinie.");

		setDescription("Wygląda na inteligentnego.");
		setEntityClass("estateagent2npc");
		setPosition(37, 13);
		initHP(100);
		
	}

	@Override
	protected int getCost() {
		return AdosHouseSeller.COST_ADOS;
	}

	@Override
	protected void createPath() {
		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(37, 13));
		nodes.add(new Node(31, 13));
		nodes.add(new Node(31, 10));
		nodes.add(new Node(35, 10));
		nodes.add(new Node(35, 4));
		nodes.add(new Node(25, 4));
		nodes.add(new Node(25, 15));
		nodes.add(new Node(15, 15));
		nodes.add(new Node(15, 9));
		nodes.add(new Node(18, 9));
		nodes.add(new Node(18, 4));
		nodes.add(new Node(18, 10));
		nodes.add(new Node(15, 10));
		nodes.add(new Node(15, 16));
		nodes.add(new Node(25, 16));
		nodes.add(new Node(25, 3));
		nodes.add(new Node(35, 3));
		nodes.add(new Node(35, 10));
		nodes.add(new Node(37, 10));
		setPath(new FixedPath(nodes, true));
	}

	@Override
	protected int getHighestHouseNumber() {
		return 77;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 50;
	}

}
