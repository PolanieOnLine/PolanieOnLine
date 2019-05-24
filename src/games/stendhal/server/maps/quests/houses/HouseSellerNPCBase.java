/* $Id$ */
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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;

/**
 * Base class for dialogue shared by all houseseller NPCs.
 * 
 */
abstract class HouseSellerNPCBase extends SpeakerNPC {

	static final String QUEST_SLOT = "house";
	/**
	 * age required to buy a house. Note, age is in minutes, not seconds! So
	 * this is 300 hours.
	 */
	static final int REQUIRED_AGE = 300 * 60;
	/** percentage of initial cost refunded when you resell a house.*/
	private static final int DEPRECIATION_PERCENTAGE = 40;

	private final String location;
	
	private final HouseTax houseTax;
	/**	
	 *	Creates NPC dialog for house sellers.
	 * @param name
	 *            the name of the NPC
	 * @param location
	 *            where are the houses?
	 * @param houseTax 
	 * 		      class which controls house tax, and confiscation of houses
	*/
	HouseSellerNPCBase(final String name, final String location, final HouseTax houseTax) {
		super(name);
		this.location = location;
		this.houseTax =  houseTax;
		createDialogNowWeKnowLocation();
	}
	
	@Override
	protected abstract void createPath();
	
	private void createDialogNowWeKnowLocation() {
		addGreeting(null, new HouseSellerGreetingAction(QUEST_SLOT));
		
			// quest slot 'house' is started so player owns a house
		add(ConversationStates.ATTENDING, 
			Arrays.asList("cost", "house", "buy", "purchase", "koszt", "dom", "kupić", "cenę"),
			new PlayerOwnsHouseCondition(),
			ConversationStates.ATTENDING, 
			"Jak wiesz koszt nowego domu wynosi "
				+ getCost()
			+ " money, ale nie możesz kupić więcej niż jednego domu, ponieważ na rynku jest duży popyt na takie domki! Nie możesz nabyć następnego domku dopóki go nie #odsprzedaż.",
			null);
		
		// we need to warn people who buy spare keys about the house
		// being accessible to other players with a key
		add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.QUESTION_2,
			"Przedtem muszę cię ostrzec, że każdy kto posiada klucz do Twojego domu będzie miał dostęp do przedmiotów w skrzyni znajdującej się w nim. Czy nadal chcesz kupić zapasowy klucz?",
			null);

		// player wants spare keys and is OK with house being accessible
		// to other person.
		add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES, 
			null,
			ConversationStates.ATTENDING, 
			null,
			new BuySpareKeyChatAction(QUEST_SLOT));
			
		// refused offer to buy spare key for security reasons
		add(ConversationStates.QUESTION_2,
			ConversationPhrases.NO_MESSAGES,
			null,
				ConversationStates.ATTENDING,
			"Mądra decyzja. Najlepiej ograniczyć korzystanie z domu do osób, którym naprawdę ufasz.",
			null);
		
		// refused offer to buy spare key 
		add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Żaden problem! Jeżeli potrzebujesz #wymienić zamki to mogę to zrobić, a także możesz mi #sprzedać swój dom o ile chcesz.",
			null);

		// player is eligible to resell a house
		add(ConversationStates.ATTENDING, 
			Arrays.asList("resell", "sell", "odsprzedać", "sprzedać"),
			new PlayerOwnsHouseCondition(),
				ConversationStates.QUESTION_3, 
			"Miasto płaci "
			+ Integer.toString(DEPRECIATION_PERCENTAGE)
			+ " procent ceny, którą zapłaciłeś za swój dom minus podatki. Powinieneś zapamiętać, aby przed sprzedażą zabrać z domu wszelkie przedmioty. Czy nadal chcesz sprzedać dom pośrednikowi?",
			null);
		
		// player is not eligible to resell a house
		add(ConversationStates.ATTENDING, 
			Arrays.asList("resell", "sell", "odsprzedać", "sprzedać"),
			new NotCondition(new PlayerOwnsHouseCondition()),
			ConversationStates.ATTENDING, 
			"W tym momencie nie posiadasz żadnego domu. Jeżeli chcesz kupić to zapytaj o jego #koszt.",
			null);
		
		add(ConversationStates.QUESTION_3,
			ConversationPhrases.YES_MESSAGES,
			null,
				ConversationStates.ATTENDING,
			null,
			new ResellHouseAction(getCost(), QUEST_SLOT, DEPRECIATION_PERCENTAGE, houseTax));
		
		// refused offer to resell a house
		add(ConversationStates.QUESTION_3,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ciesze się, że zmieniłeś zdanie.",
			null);
		
		// player is eligible to change locks
		add(ConversationStates.ATTENDING, 
			Arrays.asList("change", "zmieniłeś", "zmień", "wymienić"),
			new PlayerOwnsHouseCondition(),
			ConversationStates.SERVICE_OFFERED, 
			"Jeżeli boisz się o bezpieczeństwo domu lub nie ufasz osobom którym dałeś zapasowy klucz, "
			+ "to mądrze jest zmienić zamki. Czy chcesz, abym wymienił zamki w Twoim domu i dał Tobie nowy klucz?",
			null);

		// player is not eligible to change locks
		add(ConversationStates.ATTENDING, 
			Arrays.asList("change", "zmieniłeś", "zmień", "wymienić"),
			new NotCondition(new PlayerOwnsHouseCondition()),
			ConversationStates.ATTENDING, 
			"W tym momencie nie posiadasz żadnego domu. Jeżeli chcesz kupić to zapytaj o jego #koszt.",
			null);

		// accepted offer to change locks
		add(ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new ChangeLockAction(QUEST_SLOT));

		// refused offer to change locks 
		add(ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dobrze jeżeli jesteś pewien. Daj znać jeżeli mógłbym pomóc w czymś jeszcze.",
			null);

		add(ConversationStates.ANY,
			Arrays.asList("available", "unbought", "unsold", "dostępny", "niekupiony", "niesprzedany"),
			null, 
			ConversationStates.ATTENDING,
			null,
			new ListUnboughtHousesAction(location));

		addReply(
				 Arrays.asList("buy", "kupię", "kupić"),
				 "Powinieneś się dowiedzieć o #cenę przed kupnem i sprawdzić naszą broszurę #http://www.polskagra.net/");
		addReply(Arrays.asList("really", "naprawdę"),
				 "Tak jest naprawdę, naprawdę, naprawdę. Naprawdę.");
		addOffer("Sprzedaję domy, aby zobaczyć jak wyglądają wejdź na stronę #http://www.polskagra.net/ i przekonaj się. Później zapytaj mnie o #cenę, gdy będziesz gotowy.");
		addHelp("Możesz kupić dom o ile będzie dostępny. Jeżeli będziesz mógł zapłacić #cenę to dam Tobie klucz. Jako właściciel domu będziesz mógł kupować dodatkowe klucze do niego i dawać przyjaciołom. Wejdź na #http://www.polskagra.net/ i zobacz wnętrza domów oraz więcej szczegółów.");
		addQuest("Możesz kupić u mnie domy. Zapytaj mnie o #cenę jeżeli jesteś zainteresowany. Może chcesz najpierw zobaczyć naszą broszurę http://www.polskagra.net/");
		addGoodbye("Dowidzenia.");
	}

	protected abstract int getCost();

	protected abstract int getLowestHouseNumber();
	protected abstract int getHighestHouseNumber();
}
final class PlayerOwnsHouseCondition implements ChatCondition {
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
		return HouseUtilities.playerOwnsHouse(player);
	}
}
