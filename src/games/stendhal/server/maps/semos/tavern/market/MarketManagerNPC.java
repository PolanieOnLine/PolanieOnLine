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
package games.stendhal.server.maps.semos.tavern.market;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TextHasParameterCondition;
import games.stendhal.server.entity.trade.Offer;

public final class MarketManagerNPC extends SpeakerNPC {
	
	private Map<String,Offer> offers = new HashMap<String, Offer>();
	
	MarketManagerNPC(String name) {
		super(name);
		// Use smaller than normal range to not interfere players trying to talk
		// to the other NPCs in the tavern.
		setPerceptionRange(3);
	}

	@Override
	protected void createPath() {
		// npc is lazy and does not move
	}

	@Override
	protected void onGoodbye(RPEntity player) {
		//clean the offer map on leaving of a player
		offers.clear();
		setDirection(Direction.DOWN);
	}

	@Override
	protected void createDialog() {
		addGreeting("Witam w centrum handlu w Semos. W czym mogę #pomóc?");
		addJob("Jestem tutaj, aby #pomóc Tobie w sprzedaży przedmiotów.");
		addOffer("Aby wystawić ofertę na rynku powiedz #sprzedam #przedmiot #cena - wtedy każdy będzie mógł to kupić " +
                "nawet, gdy Ciebie nie ma. W celu uzyskania szczegółów zapytaj o #pomoc.");
		addHelp("Czy chciałbyś uzyskać pomoc w #kupowaniu lub #sprzedaży?");
		addReply(Arrays.asList("buying", "kupowaniu", "kupić"),"Jeżeli chcesz coś kupić to powiedz #pokaż, a wtedy pojawią się aktualne oferty z " +
				 "liczbą ofert. Jeżeli będziesz chciał wybrać jedną z ofert to powiedz #akceptuję #numer, aby kupić " +
				 "oferowany towar z tym numerem. Ciesze się, że mogę przefiltrować listę dla ciebie. Powiedz tylko " +
				 "na przykład #pokaż #meat, aby zobaczyć tylko oferty z mięsem.");
		addReply(Arrays.asList("selling", "sprzedaży"),"Powiedz #sprzedam #przedmiot #cena, aby wystawić przedmiot na rynku. Jeżeli chcesz zdjąć " +
				 "ofertę z rynku to powiedz mi #'pokaż mine', a wtedy zobaczysz swoje oferty. Powiedz #usuń " +
				 "#numer, a wtedy usunę ten przedmiot z ofert. Jeżeli masz wygaśnięte oferty to możesz o nie zapytać " +
				 "mówiąc #pokaż #wygaśnięte. Możesz przedłużyć ważność wygaśniętych ofert mówiąc #przedłuż #numer. Jeżeli sprzedałeś jakieś przedmioty " +
				 "to możesz powiedzieć mi #wypłata, a wtedy wypłacę Tobie twoje zarobki.");
		new PrepareOfferHandler().add(this);
		add(ConversationStates.ATTENDING, Arrays.asList("show", "pokaż"), new NotCondition(new TextHasParameterCondition()),
				ConversationStates.ATTENDING, null, new ShowOfferItemsChatAction());
		add(ConversationStates.ATTENDING, Arrays.asList("show", "pokaż"), new TextHasParameterCondition(), ConversationStates.ATTENDING, null, new ShowOffersChatAction());
		// fetch earnings when starting to talk to the market manager
		add(ConversationStates.ATTENDING, Arrays.asList("fetch", "wypłata", "wypłać"), null, ConversationStates.ATTENDING, null, new FetchEarningsChatAction());
		new AcceptOfferHandler().add(this);
		new RemoveOfferHandler().add(this);
		new ProlongOfferHandler().add(this);
		add(ConversationStates.ATTENDING, Arrays.asList("examine", "sprawdź", "sprawdzam"), null, ConversationStates.ATTENDING, null, new ExamineOfferChatAction());
		addGoodbye("Odwiedź mnie ponownie, aby sprawdzić najnowsze oferty lub w celu wystawienia nowych ofert, albo wypłaty zarobków.");
	}

	public Map<String, Offer> getOfferMap() {
		return offers;
	}
}
