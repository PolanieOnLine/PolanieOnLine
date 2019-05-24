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
// Baseed on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
package games.stendhal.server.maps.koscielisko.zrc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class ZakonnikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Zakonnik") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(118, 3));
				nodes.add(new Node(118, 8));
				nodes.add(new Node(111, 8));
				nodes.add(new Node(111, 23));
				nodes.add(new Node(101, 23));
				nodes.add(new Node(101, 26));
				nodes.add(new Node(75, 26));
				nodes.add(new Node(75, 8));
				nodes.add(new Node(65, 8));
				nodes.add(new Node(65, 26));
				nodes.add(new Node(117, 26));
				nodes.add(new Node(117, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj na terenie zakonu cienia.");
				addJob("Nie ja nic nie mam dziś dla ciebie.");
				addHelp("Pomocy? też nie potrzebuje.");
				addGoodbye("Dowidzenia kolego.");
			}
		};

		npc.setDescription("Oto Zakonnik.");
		npc.setEntityClass("npczakonnik");
		npc.setPosition(118, 3);
		npc.initHP(1000);
		zone.add(npc);
	}
}
