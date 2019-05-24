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
package games.stendhal.server.maps.ados.city;

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
 * Builds a npc in Ados (name:Marla) who is there on vacation
 * 
 * @author erdnuggel (idea), madmetzger and Vanessa Julius
 *
 */
public class ForeignWomanNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Marla") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(73, 7));
				nodes.add(new Node(73, 1));
                nodes.add(new Node(70, 1));
                nodes.add(new Node(69, 10));
                nodes.add(new Node(69, 12));
                nodes.add(new Node(70, 12));
                nodes.add(new Node(70, 34));  
                nodes.add(new Node(75, 33));
                nodes.add(new Node(75, 24)); 
                nodes.add(new Node(74, 24)); 
                nodes.add(new Node(74, 9));
                nodes.add(new Node(73, 9)); 
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Dzień dobry.");
				addHelp("Odwiedziłeś już człowieka o wielkiej mądrości? Znajdziesz go w bibliotece. Nazywa się Wikipedian. Jest popularny dzięki swojej wielkiej wiedzy.");
				addQuest("Od kiedy mieszkam tutaj, w Ados, nie potrzebuję pomocy, ale to miłe, że pytasz."); 
				addJob("Nie, jestem już zbyt stara, aby pracować!");
				addOffer("Mogę zaoferować Ci tylko to świeże powietrze, pachnące morską bryzą.");
				addGoodbye("Bywaj.");
				
			}
		};

		npc.setDescription("Oto Marla.  Odbyła długą podróż aż do swojego kurortu.");
		npc.setEntityClass("womanexoticdressnpc");
		npc.setPosition(73, 7);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
