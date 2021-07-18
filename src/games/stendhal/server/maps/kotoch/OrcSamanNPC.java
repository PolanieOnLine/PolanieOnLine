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
package games.stendhal.server.maps.kotoch;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author kymara
 */
public class OrcSamanNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Szaman Orków") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 113));
				nodes.add(new Node(16, 113));
				nodes.add(new Node(16, 115));
				nodes.add(new Node(22, 115));
				nodes.add(new Node(22, 119));
				nodes.add(new Node(8, 119));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Oof.");
				addJob("Ja szaman orków.");
				addHelp("Szaman orków potrzebuje pomocy! Zrób #zadanie.");
				addOffer("Żadnego handlu.");
				addGoodbye("Żegnaj.");
			}
		};

		npc.setDescription("Oto Szaman Orków. Ciekawe jakie rytuały wyprawia.");
		npc.setEntityClass("orcsamannpc");
		npc.setGender("M");
		npc.setPosition(8, 113);
		zone.add(npc);
	}
}
