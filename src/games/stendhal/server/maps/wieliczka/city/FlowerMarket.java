/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.wieliczka.city;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author zekkeq
 */
public class FlowerMarket implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Lisa") {
			@Override
			public void createDialog() {
				addGreeting("Cześć! Przyszedłeś tutaj #pohandlować?");
				addReply(ConversationPhrases.YES_MESSAGES, "Dobrze! Mogę sprzedać Tobie piękną czerwoną różę. Nie chodzi mi o rzadki orchideę. Tylko Róża Kwiaciarka wie, gdzie one rosną i nikt nie wie gdzie jest Róża Kwiaciarka!");
				addReply(ConversationPhrases.NO_MESSAGES, "Bardzo dobrze. Jeżeli będę mogła pomóc to daj znać.");
				addJob("Sprzedaję tutaj róże.");
				addHelp("Porozmawiaj z naszym burmistrzem! Na pewno znajdziesz u niego jakieś zlecenie.");
				addGoodbye("Do widzenia i zapraszam ponownie!");
			}
		};

		npc.setDescription("Oto Lisa. Sprzedaje kwiatki dla młodych par.");
		npc.setEntityClass("woman_001_npc");
		npc.setGender("F");
		npc.setPosition(111, 49);
		zone.add(npc);
	}
}
