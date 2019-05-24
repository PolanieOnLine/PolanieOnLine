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
// Based on ../games/stendhal/server/maps/ados/goldsmith/GoldsmithNPC.java
package games.stendhal.server.maps.koscielisko.stones;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;


public class KowalJacekNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//


	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC KowalJacekNPC = new SpeakerNPC("Kowal Jacek") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 5));
				nodes.add(new Node(4, 5));
				nodes.add(new Node(4, 7));
				nodes.add(new Node(4, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć!");
				addJob("Jak widzisz kowala życie nie jest łatwe, masę roboty mam.");
				addHelp("Ja ci? Chyba raczej ty mi.");
				addOffer("Nie mam nic do zaoferowania!");
				addGoodbye("Dowidzenia.");
			}
		};

		KowalJacekNPC.setEntityClass("goldsmithnpc");
		KowalJacekNPC.setPosition(3, 5);
		KowalJacekNPC.initHP(100);
		zone.add(KowalJacekNPC);
	}
}
