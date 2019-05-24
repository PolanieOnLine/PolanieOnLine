/* $Id: SmokNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
// based on ../games/stendhal/server/maps/ados/abandonedkeep/OrcKillGiantDwarfNPC.java
package games.stendhal.server.maps.zakopane.clouds;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the orc kill diant dwarf NPC.
 *
 * @author Teiv
 */
public class SmokNPC implements ZoneConfigurator {

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
		final SpeakerNPC smokNPC = new SpeakerNPC("Władca Smoków") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 6));
				nodes.add(new Node(20, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Jam jest Panem wszystkich smoków.");
				addJob("Może zrobisz dla mnie #zadanie?");								
				addHelp("W zamian za pomoc szepnę słówko do Krasnoluda, aby zrobił dla ciebie złotą ciupagę z dwoma wąsami.");
				addGoodbye("Trzymaj się dzielnie");
			}
		};

		smokNPC.setDescription("Oto Władca Smoków");
		smokNPC.setEntityClass("npcsmok");
		smokNPC.setPosition(8, 6);
		smokNPC.initHP(5000);
		zone.add(smokNPC);
	}
}
