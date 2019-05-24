
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.condition.AgeGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;

import java.util.Arrays;

final class KirdnehHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in kirdneh. */
	private static final int COST_KIRDNEH = 120000;
	private static final String KIRDNEH_QUEST_SLOT = "weekly_item";

	KirdnehHouseSeller(final String name, final String location, final HouseTax houseTax) {
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
				 "Koszt nowego domu w Kirdneh wynosi "
						 + getCost()
				 + " money, ale obawiam się, że nie mogę Ci jeszcze zaufać w kwestii kupna domu. Wróć gdy spędzisz tutaj ponad " 
				 + Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " godzinę" + " w Faiumoni.",
				 null);
		
		// player is old enough and hasn't got a house but has not done required quest
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestNotCompletedCondition(KirdnehHouseSeller.KIRDNEH_QUEST_SLOT), 
									 new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT)),
				 ConversationStates.ATTENDING, 
				 "Koszt nowego domu w Kirdneh wynosi "
				 + getCost()
				 + " money, ale moja główna zasada nie pozwala na sprzedaż domu bez ustalenia #reputacji potencjalnego kupca. ",
				 null);
		
		// player is eligible to buy a house
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), 
								  new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestCompletedCondition(KirdnehHouseSeller.KIRDNEH_QUEST_SLOT)),
					ConversationStates.QUEST_OFFERED, 
				 "Koszt nowego domu w Kirdneh wynosi "
				 + getCost()
				 + " Prócz tego musisz zapłacić podatek od nieruchomości w wysokości " + HouseTax.BASE_TAX
				 + " money, miesięcznie. Jeżeli masz jakiś dom na oku to podaj mi jego numer. Sprawdzę czy jest wolny. "
				 + "Domy w Kirdneh są numerowane od "
				 + getLowestHouseNumber() + " do " + getHighestHouseNumber() + ".",
				 null);
		
		// handle house numbers 26 to 49
		addMatching(ConversationStates.QUEST_OFFERED,
				// match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
				ConversationStates.ATTENDING, 
				null,
				new BuyHouseChatAction(getCost(), QUEST_SLOT));

		addJob("Jestem agentem nieruchomości z prostymi zasadami. Sprzedaję domy dla miasta Kirdneh. Zapytaj mnie o #cenę jeżeli jesteś zainteresowany. Nasza broszura znajduje się na #http://www.polskagra.net/");
		addReply(Arrays.asList("reputation", "reputacji", "reputację"), "Zapytam Hazel o Ciebie. Jeżeli wykonałeś każde jej zadanie, o które Cię poprosiła i nie zostawiłeś niedokończonego to nie będę widział problemu, aby sprzedać Tobie dom w Kirdneh.");
		addReply("Amber", "Oh Amber... Tęsknie za nią. Mieliśmy kłótnię po której #odeszła. Mam nadzieje, że u niej wszystko w porządku.");
		addReply(Arrays.asList("left", "odeszła"), "Osobiście to nie wiem gdzie teraz jest. Jej syn Jef czeka na nią w mieście, ale słyszałem, że jest gdzieś w południowych lasach Fado.");
		setDescription("Oto mężczyzna wyglądający na bystrą osobę.");
		setEntityClass("man_004_npc");
		setPosition(31, 4);
		initHP(100);
		
	}

	@Override
	protected int getCost() {
		return KirdnehHouseSeller.COST_KIRDNEH;
	}

	@Override
	protected void createPath() {
		setPath(null);
	}

	@Override
	protected int getHighestHouseNumber() {
		return 49;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 26;
	}
}
