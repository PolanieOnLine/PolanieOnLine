/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.city;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author KarajuSs
 */

public class MieszkaniecGdanska1NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGdanskMieszkancy(zone);
	}

	private void buildGdanskMieszkancy(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jagoda") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(113, 108));
				nodes.add(new Node(113, 111));
				nodes.add(new Node(110, 111));
				nodes.add(new Node(110, 110));
				nodes.add(new Node(107, 110));
				nodes.add(new Node(107, 113));
				nodes.add(new Node(110, 113));
				nodes.add(new Node(110, 115));
				nodes.add(new Node(115, 115));
				nodes.add(new Node(115, 113));
				nodes.add(new Node(118, 113));
				nodes.add(new Node(118, 110));
				nodes.add(new Node(119, 110));
				nodes.add(new Node(119, 106));
				nodes.add(new Node(117, 106));
				nodes.add(new Node(117, 104));
				nodes.add(new Node(113, 104));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć, moja mama nie kazała mi się zadawać z obcymi.");
				addJob("Praca? Ja lubię pracować! Uwielbiam pomagać mojej mamie w pracy.");
				addHelp("Moja mama nie kazała mi się zadawać z obcymi.");
				addGoodbye();
			}
		};

		npc.setEntityClass("npcdziewczynka");
		npc.setPosition(113, 108);
		npc.initHP(100);
		zone.add(npc);
	}
}
