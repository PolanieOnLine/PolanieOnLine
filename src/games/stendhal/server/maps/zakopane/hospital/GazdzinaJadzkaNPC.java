/* $Id: GazdzinaJadzkaNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
 //Na podstawie pliku HealerNPC.
 
package games.stendhal.server.maps.zakopane.hospital;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.FreeHealerAdder;

/**
 * Builds a Healer NPC for the magic city.
 *
 * @author kymara
 */
public class GazdzinaJadzkaNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Gaździna Jadźka") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// walks along the aqueduct path, roughly
				nodes.add(new Node(20, 10));
				nodes.add(new Node(34, 10));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Moja niezwykła moc pomaga mi uleczyć rany.");
				addHelp("Mogę Cię #uleczyć .");
				new FreeHealerAdder().addHealer(this, 0);
				addGoodbye("Dowidzenia");
			}
		};

		npc.setDescription("Oto Gaździna Jadźka.");
		npc.setEntityClass("npcgenowefa");
		npc.setPosition(20, 10);
		npc.initHP(100);
		zone.add(npc);
	}
}
