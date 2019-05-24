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
// Based on ../games/stendhal/server/maps/amazon/hut/JailedBarbNPC.java
package games.stendhal.server.maps.koscielisko.house;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the Gazdzina Maryska in House on Koscielisko.
 *
 * @author Teiv
 */
public class GazdzinaMaryskaNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC GazdzinaMaryskaNPC = new SpeakerNPC("Gaździna Maryśka") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(18, 4));
				nodes.add(new Node(17, 4));
				nodes.add(new Node(17, 3));
				nodes.add(new Node(17, 6));
				nodes.add(new Node(20, 6));
				nodes.add(new Node(20, 13));
				nodes.add(new Node(24, 13));
				nodes.add(new Node(24, 18));
				nodes.add(new Node(18, 18));
				nodes.add(new Node(18, 28));
				nodes.add(new Node(11, 28));
				nodes.add(new Node(11, 20));
				nodes.add(new Node(3, 20));
				nodes.add(new Node(3, 18));
				nodes.add(new Node(5, 18)); 
				nodes.add(new Node(5, 19));
				nodes.add(new Node(17, 19));
				nodes.add(new Node(17, 18)); 
				nodes.add(new Node(26, 18));
				nodes.add(new Node(26, 11)); 
				nodes.add(new Node(6, 11));
				nodes.add(new Node(6, 4));      
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze!");
				addJob("Mam masę roboty w domu: pranie, gotowanie.");
				addHelp("A w czym ja ci mogę pomóc?");
				addOffer("Nie mam nic do zaoferowania, ale spytaj mego męża bacę Zbyszka");
				addGoodbye("Dowidzenia.");
			}
		};

		GazdzinaMaryskaNPC.setEntityClass("curatornpc");
		GazdzinaMaryskaNPC.setPosition(18, 4);
		GazdzinaMaryskaNPC.initHP(100);
		zone.add(GazdzinaMaryskaNPC);
	}
}
