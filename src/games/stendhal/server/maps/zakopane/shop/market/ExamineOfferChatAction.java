/* $Id: ExamineOfferChatAction.java,v 1.7 2011/05/01 19:50:07 martinfuchs Exp $ */
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
package games.stendhal.server.maps.zakopane.shop.market;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.trade.Offer;
import games.stendhal.server.events.ExamineEvent;

import java.util.Map;

public class ExamineOfferChatAction extends KnownOffersChatAction {
	@Override
	public void fire(Player player, Sentence sentence, EventRaiser npc) {
		if (sentence.hasError()) {
			npc.say("Wybacz, ale nie rozumiem Ciebie. "
					+ sentence.getErrorString());
		} else if ((sentence.getExpressions().iterator().next().toString().equals("examine"))
			|| (sentence.getExpressions().iterator().next().toString().equals("sprawdź"))
			|| (sentence.getExpressions().iterator().next().toString().equals("sprawdzam"))){
			handleSentence(player,sentence,npc);
		}
	}

	private void handleSentence(Player player, Sentence sentence, EventRaiser npc) {
		MarketManagerNPC manager = (MarketManagerNPC) npc.getEntity();
		try {
			String offerNumber = getOfferNumberFromSentence(sentence).toString();
			Map<String,Offer> offerMap = manager.getOfferMap();
			if (offerMap == null) {
				npc.say("Proszę, najpierw rzuć okiem na listę ofert.");
				return;
			}
			if(offerMap.containsKey(offerNumber)) {
				Offer o = offerMap.get(offerNumber);
				if (o.hasItem()) {
					player.sendPrivateText(o.getItem().describe());
					showImage(player, o.getItem());
					return;
				}
			}
			npc.say("Wybacz, ale musisz wybrać liczbę z spośród tych, które ci podałem.");
		} catch (NumberFormatException e) {
			npc.say("Wybacz, ale musisz powiedzieć #akceptuję #numer");
		}
	}

	private void showImage(Player player, Item item) {
		String caption = item.getName();
		String image = "items/" + item.getItemClass() + "/" + item.getItemSubclass() + ".png";
		ExamineEvent event = new ExamineEvent(image, caption, "");
		player.addEvent(event);
	}
}
