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
 * Builds a sad NPC (name: Andy) who lost his wife
 * 
 * @author Erdnuggel (idea) and Vanessa Julius (implemented)
 * 
 */

public class ManWithHatNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Andy") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(41, 6));
				nodes.add(new Node(41, 11));
                nodes.add(new Node(64, 11));
                nodes.add(new Node(64, 6));  
                nodes.add(new Node(63, 6));
                nodes.add(new Node(63, 10)); 
                nodes.add(new Node(42, 10)); 
                nodes.add(new Node(42, 6));
                nodes.add(new Node(41, 6)); 
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addHelp("Gdy mieszkałem z moją ukochaną żoną to sporo podróżowaliśmy. Kochaliśmy plażę na Athor Island! *Westchnienie* Te wspomnienia sprawiają, że robię się smutny...Proszę zostam mnie teraz samego...");
				addJob("Przestałem pracować, gdy moja żona odeszła.");
				addOffer("Nie mam nic do zaoferowania.");
				addGoodbye("Dowidzenia i dziękuję za rozmowę.");
				
			}
		};

		npc.setDescription("Oto mężczyzna w kapeluszu. Nazywa się Andy i wygląda na bardzo przygnębionego.");
		npc.setEntityClass("manwithhatnpc");
		npc.setPosition(41, 6);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
