/* $Id: RatownikNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.plains;

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
public class RatownikNPC implements ZoneConfigurator {

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
		final SpeakerNPC ratownikNPC = new SpeakerNPC("Ratownik Mariusz") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(108, 11));
				nodes.add(new Node(125, 11));
				nodes.add(new Node(125, 67));
				nodes.add(new Node(108, 67));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addJob("Mam pełne ręce roboty. Właśnie zaginął następny turysta w górach.");
				addHelp("Oczywiście, że możesz mi pomóc. Mam dla ciebie #zadanie.");
				addGoodbye("Życzę powodzenia i szczęścia na wyprawach.");
			}
		};

		ratownikNPC.setEntityClass("npcratownik");
		ratownikNPC.setPosition(108, 11);
		ratownikNPC.initHP(1000);
		zone.add(ratownikNPC);
	}
}
