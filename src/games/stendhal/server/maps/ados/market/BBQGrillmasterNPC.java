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
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a npc in Ados (name:Haunchy Meatoch) who is a grillmaster on the market
 * 
 * @author storyteller (idea) and Vanessa Julius (implemented)
 *
 */
public class BBQGrillmasterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Haunchy Meatoch") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(14, 25));
				nodes.add(new Node(15, 25));
                nodes.add(new Node(12, 25));
                nodes.add(new Node(16, 25));  
                nodes.add(new Node(16, 24));
                nodes.add(new Node(16, 25)); 
                nodes.add(new Node(15, 25)); 
                nodes.add(new Node(12, 25));
                nodes.add(new Node(17, 25));
                nodes.add(new Node(17, 24));
                nodes.add(new Node(17, 25));
                nodes.add(new Node(13, 25));
                nodes.add(new Node(14, 25));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Hej! Wspaniały dzień na grilla?");
				addHelp("Niestety steki nie są jeszcze gotowe... Jeżeli jesteś głodny i nie możesz czekać to może przejrzysz oferty przy wyjściu jak na przykład oferty Blacksheep koło chatek rybackich w Ados lub możesz popłynąć promem do Athor, aby zdobyć trochę przekąsek..."); 
				addJob("Jestem mistrzem grilla jak widzisz. Kocham zapach świeżo zgrilowanego mięsa!");
				addOffer("Mam nadzieje, że moje steki będą wkrótce gotowe. Bądź cierpliwy lub spróbuj przedtem innych przysmaków.");
				addGoodbye("Życzę miłego dnia! Zawsze podtrzymuj ogień!");
				
			}
		};

		npc.setDescription("Oto Haunchy Meatoch. Otoczony jest miłym zapachem świeżo zgrilowanego mięsa.");
		npc.setEntityClass("bbqgrillmasternpc");
		npc.setPosition(14, 25);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
