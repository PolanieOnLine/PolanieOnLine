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
package games.stendhal.server.maps.gdansk.cottage;

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

public class AligernNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Aligern") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 3));
				nodes.add(new Node(3, 3));
				nodes.add(new Node(3, 9));
				nodes.add(new Node(5, 9));
				nodes.add(new Node(5, 12));
				nodes.add(new Node(14, 12));
				nodes.add(new Node(14, 14));
				nodes.add(new Node(18, 14));
				nodes.add(new Node(18, 17));
				nodes.add(new Node(29, 17));
				nodes.add(new Node(29, 18));
				nodes.add(new Node(38, 18));
				nodes.add(new Node(38, 14));
				nodes.add(new Node(28, 14));
				nodes.add(new Node(28, 13));
				nodes.add(new Node(26, 13));
				nodes.add(new Node(26, 10));
				nodes.add(new Node(19, 10));
				nodes.add(new Node(19, 9));
				nodes.add(new Node(17, 9));
				nodes.add(new Node(17, 7));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(13, 6));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(12, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody wędrowcze.");
				addJob("Pracują dla pewnego naukowca, który zadomowił się u mnie w domu.");
				addHelp("Szukam pomocy już kilka lat, lecz kto się tutaj zjawił od razu rezygnowali. Powiedz tylko #'zadanie', a się dowiesz więcej na ten temat.");
				addOffer("Wykonaj dla mnie specjalnie #'zadanie', a cię wynagrodzę.");
				addGoodbye();
			}
		};

		npc.setEntityClass("drugsdealernpc");
		npc.setPosition(9, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
