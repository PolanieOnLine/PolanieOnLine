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
package games.stendhal.server.maps.fado.house;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds Josephine NPC (Cloak Collector).
 *
 * @author kymara
 */
public class WomanNPC implements ZoneConfigurator {
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
	// IL0_womanNPC - Josephine, the Cloaks Collector
	//

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC woman = new SpeakerNPC("Josephine") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 4));
				nodes.add(new Node(16, 4));
				nodes.add(new Node(16, 7));
				nodes.add(new Node(3, 7));
				nodes.add(new Node(3, 6));
				nodes.add(new Node(5, 6));
				nodes.add(new Node(5, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				//addGreeting();
				addJob("Gdybym mogła to projektowałabym sukienki!");
				addHelp("Możesz otrzymać pomoc od Xhiphin Zohos. Zazwyczaj siedzi w sąsiednim domku. *hi hi hi* Ciekawa jestem dlaczego!");
				addGoodbye("Dowidzenia, dowidzenia!");
			}
		};

		woman.setDescription("Oto modnie ubrana młoda kobieta. Wygląda trochę na flirciarę.");
		woman.setEntityClass("youngwomannpc");
		woman.setPosition(3, 4);
		woman.initHP(100);
		zone.add(woman);
	}
}
