/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.krakow.tavern;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class KarczmarkaNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("babcia Alina") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 20));
				nodes.add(new Node(33, 5));
				nodes.add(new Node(35, 5));
				nodes.add(new Node(35, 20));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w starej karczmie #'U Babci Aliny'! Co potrzebujesz?");
				addJob("Jestem karczmarką w swej tawernie. Mogę wykonać dla ciebie specjalną zupę, która pomoże zregenerować siły na dalszą podróż.");
				addHelp("Rozgoście się w mojej tawernie i zamawiajcie jedzenie lub specjalne napoje z różnych krain u barmanki.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto babcia Alina, jest znana ze swojej wspaniałej kuchni domowej.");
		npc.setEntityClass("granmanpc");
		npc.setGender("F");
		npc.setPosition(35, 20);
		npc.setCollisionAction(CollisionAction.STOP);
		zone.add(npc);
	}
}
