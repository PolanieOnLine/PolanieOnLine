/* $Id: AdasNPC.java,v 1.7 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.home;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AdasNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneHome(zone);
	}

	private void buildZakopaneHome(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Adaś") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(6, 3)); 
				nodes.add(new Node(6, 7));
				nodes.add(new Node(4, 7));
				nodes.add(new Node(4, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody bohaterze.");
				addJob("Boje się zejść do piwnicy.");
				addHelp("Tak możesz mi pomóc, wykonaj dla mnie #zadanie.");
				addGoodbye();
			}
		};

		npc.setEntityClass("npcdzieckogoralskie");
		npc.setPosition(6, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}

