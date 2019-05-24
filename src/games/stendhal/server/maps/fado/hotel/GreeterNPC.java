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
 * Builds the hotel greeter NPC.
 *
 * @author timothyb89
 */
public class GreeterNPC implements ZoneConfigurator {
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

	//
	// IL0_GreeterNPC
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC greeterNPC = new SpeakerNPC("Linda") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(16, 50));
				nodes.add(new Node(27, 50));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Witam w Hotelu w mieście Fado! Mogę w czymś #pomóc?");

				addJob("Jestem recepcjonistką w hotelu.");
				addHelp("Kiedy skończą się prace remontowe w hotelu to będzie można #zarezerwować sobie pokój.");
				//addHelp("You can #reserve a room if you'd like, or #explore the hotel.");
				addReply(Arrays.asList("zarezerwować", "reserve"),
				        "Przepraszam, ale hotel wciąż jest remontowany i nie można zarezerwować sobie pokoju. Możesz #pozwiedzać resztę hotelu.");
				addReply(Arrays.asList("pozwiedzać", "explore"), "Obawiam się, że nie ma zbyt wiele do zwiedzania. Hotel wciąż nie jest wykończony.");
				//addSeller(new SellerBehaviour(shops.get("food&drinks")));
				addGoodbye("Dowidzenia.");
			}
		};

		greeterNPC.setEntityClass("hotelreceptionistnpc");
		greeterNPC.setPosition(16, 50);
		greeterNPC.initHP(1000);
		greeterNPC.setDescription("Oto Linda. Należy do przyjaznego hotelowego personelu i jest odpowiedzialna za ustalanie cen pokoju.");
		zone.add(greeterNPC);
	}
}
