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
// Based on ../games/stendhal/server/maps/ados/abandonedkeep/OrcKillGiantDwarfNPC.java
package games.stendhal.server.maps.desert.blackriver;

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
 * Builds the orc kill diant dwarf NPC.
 *
 * @author Teiv
 */
public class JozekNPC implements ZoneConfigurator {

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
		final SpeakerNPC jozekNPC = new SpeakerNPC("Józek") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(6, 2));
				nodes.add(new Node(6, 5));
				nodes.add(new Node(5, 5));
				nodes.add(new Node(5, 2));
				setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addJob("Może chcesz #ulepszyć #'złotą ciupagę'? Mogę to dla Ciebie wykonać.");
				addReply(Arrays.asList("ulepsz", "ulepszyć", "złotą ciupagę"), "Potrzebnych jest mi kilka rzeczy, więc musisz wykonać #zadanie.");
				addHelp("Zajmuję się ulepszaniem złotej ciupagi.");
				addGoodbye("Życzę powodzenia.");
			}
		};

		jozekNPC.setEntityClass("weaponsellernpc");
		jozekNPC.setPosition(6, 2);
		jozekNPC.initHP(1000);
		jozekNPC.setDescription("Oto Jóżek, który jest w stanie ulepszyć twoją złotą ciupagę.");
		zone.add(jozekNPC);
	}
}
