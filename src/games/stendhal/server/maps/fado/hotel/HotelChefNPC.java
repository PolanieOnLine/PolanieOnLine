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
package games.stendhal.server.maps.fado.hotel;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a NPC in a house on Ados market (name:Stefan) who is the daughter of fisherman Fritz
 * 
 * @author Vanessa Julius 
 *
 */
public class HotelChefNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Stefan") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(92, 9));
				nodes.add(new Node(98, 9));
	            nodes.add(new Node(98, 2));
	            nodes.add(new Node(93, 2));  
	            nodes.add(new Node(93, 4));
	            nodes.add(new Node(91, 4)); 
	            nodes.add(new Node(91, 3)); 
	            nodes.add(new Node(90, 3));
	            nodes.add(new Node(90, 11));
	            nodes.add(new Node(98, 11));
	            nodes.add(new Node(98, 9));
	           	setPath(new FixedPath(nodes, true));		
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w kuchni hotelu w Fado, nieznajomy!");
				addHelp("Jestem bardzo #zestresowany moją pracą tu. Przykro mi, ale żadnej rady nie mogę Ci teraz udzielić.");
				addReply(Arrays.asList("stressed", "zestresowany"), "To szczyt sezonu! Mamy wiele rezerwacji, co oznacza więcej #gości, czyli jeszcze więcej pracy.");
				addReply(Arrays.asList("guests", "gości"), "Wielu z nich odwiedza Fado, by #'pobrać się'. Rozumiem ich wybór. Fado to piękne miasto.");
				addReply(Arrays.asList("getting married", "pobrać się"), "Nie wiesz, że Fado słynie z największej ilości ślubów w całej Faiumoni? Musisz odwiedzić kościół, jest prześliczny!");
				addQuest("Jestem teraz tak zajęty myśleniem o tym, by ktoś pomógł mi #tutaj... "); 
				addReply(Arrays.asList("somewhere", "tutaj"), "Tak, tutaj... Wątpię, że problem może być rozwiązany, kuchnia... Jest zbyt mała!");
				addJob("Kilka tygodni temu, dostałem propozycję pracy tutaj, w hotelowej kuchni. Wtedy tego nie wiedziałem, ale... jestem tutaj jedynym #'kucharzem'!");
				addReply(Arrays.asList("cook", "kucharzem"), "Bycie kucharzem jest wspaniałe! Kocham wszystkie rodzaje jedzenia i przypraw i uwielbiam eksperymentować z różnymi potrawami! Sprawia mi to wielką przyjemność.");
				addOffer("Kuchnia jest teraz zamknięta ale wkrótce znów będę mógł ją otworzyć. Muszę pomyśleć nad rozwiązaniem mojego #problemu...");
				addReply(Arrays.asList("problem", "problemu"), "Pracuję w tej hotelowej kuchni sam, całymi dniami! To strasznie męczące.");
				addGoodbye("Dowidzenia! Miłego pobytu w Fado!");
			}

		};

		npc.setDescription("Widzisz Stefana, młodego szefa kuchni z hotelu w Fado.");
		npc.setEntityClass("hotelchefnpc");
		npc.setPosition(92, 9);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
