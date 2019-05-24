/**
 * 
 */
package games.stendhal.server.maps.quests.houses;

import games.stendhal.common.Direction;
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

final class AthorHouseSeller extends HouseSellerNPCBase {
	/** Cost to buy house in athor. */
	private static final int COST_ATHOR = 100000;
	private static final String FISHLICENSE2_QUEST_SLOT = "fishermans_license2";

	AthorHouseSeller(final String name, final String location, final HouseTax houseTax) {
		super(name, location, houseTax);
		init();
	}

	private void init() {
		// Other than the condition that you must not already own a house, there are a number of conditions a player must satisfy. 
		// For definiteness we will check these conditions in a set order. 
		// So then the NPC doesn't have to choose which reason to reject the player for (appears as a WARN from engine if he has to choose)
		
		// player is not old enough
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "apartment", "koszt", "dom", "kupić", "cenę", "cena"),
				 new NotCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE)),
				 ConversationStates.ATTENDING, 
				 "Koszt apartamentu na Athor wynosi "
						 + getCost()
				 + " money. Ale musisz wrócić gdy spędzisz co najmniej " 
				 + Integer.toString((HouseSellerNPCBase.REQUIRED_AGE / 60)) + " godzin w Faiumoni. A do tej pory zażyj kąpieli słonecznych.",
				 null);
		
		// player is old enough and hasn't got a house but has not done required quest
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "apartment", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestNotCompletedCondition(AthorHouseSeller.FISHLICENSE2_QUEST_SLOT), 
								  new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT)),
				 ConversationStates.ATTENDING, 
				 "Co ci z domu na Athor, jeżeli nie jesteś prawdziwym #rybakiem. Staramy się przyciągnąć właścicieli, którzy spędzają dużo czasu na wyspie. Wróć kiedy zostaniesz wykwalifikowanym rybakiem.",
				 null);
		
		// player is eligible to buy a apartment
		add(ConversationStates.ATTENDING, 
				 Arrays.asList("cost", "house", "buy", "purchase", "apartment", "koszt", "dom", "kupić", "cenę", "cena"),
				 new AndCondition(new QuestNotStartedCondition(HouseSellerNPCBase.QUEST_SLOT), 
								  new AgeGreaterThanCondition(HouseSellerNPCBase.REQUIRED_AGE), 
								  new QuestCompletedCondition(AthorHouseSeller.FISHLICENSE2_QUEST_SLOT)),
					ConversationStates.QUEST_OFFERED, 
				 "Nowy apartament kosztuje "
				 + getCost()
				 + " money. Ponadto musisz zapłacić podatek w wysokości " + HouseTax.BASE_TAX
				 + " money. Jeżeli wpadł ci w oko jakiś dom to podaj jego numer, a wtedy powiem Tobie czy jest dostępny. "
				 + "Apartamenty w Athor są ponumerowane od "
				 + getLowestHouseNumber() + " do " + getHighestHouseNumber() + ".",
				 null);
		
		// handle house numbers 101 to 108
		addMatching(ConversationStates.QUEST_OFFERED,
				 // match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(getLowestHouseNumber(), getHighestHouseNumber()),
				ConversationStates.ATTENDING, 
				null,
				new BuyHouseChatAction(getCost(), QUEST_SLOT));
		

		addJob("Faktycznie jestem tutaj dla słońca. Ale zajmuje się też sprzedażą apartamentów na wyspie. Możesz je oglądnąć w katalogu na #http://www.polskagra.net.");
		addReply(Arrays.asList("fisherman", "rybakiem", "rybak"), "Staniesz się nim, gdy zdobędziesz licencje u Santiago w Ados. Po zdaniu 2 egzaminów zostaniesz prawdziwym rybakiem.");
		setDirection(Direction.DOWN);
		setDescription("Widzisz opalającego się mężczyznę.");
		setEntityClass("swimmer1npc");
		setPosition(44, 40);
		initHP(100);
		
	}

	@Override
	protected int getCost() {
		return AthorHouseSeller.COST_ATHOR;
	}

	@Override
	protected void createPath() {
		setPath(null);
	}
	
	@Override
	public void say(final String text) {
		// He doesn't move around because he's "lying" on his towel.
		say(text, false);
	}
	
	@Override
	protected int getHighestHouseNumber() {
		return 108;
	}

	@Override
	protected int getLowestHouseNumber() {
		return 101;
	}
}
