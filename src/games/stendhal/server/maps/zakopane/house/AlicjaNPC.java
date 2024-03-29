/***************************************************************************
 *                 (C) Copyright 2003-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.house;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author Legolas
 */
public class AlicjaNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Alicja") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 15));
				nodes.add(new Node(8, 15));
				nodes.add(new Node(8, 19));
				nodes.add(new Node(3, 19));
				nodes.add(new Node(3, 15));
				nodes.add(new Node(5, 15));
				nodes.add(new Node(5, 19));
				nodes.add(new Node(7, 19));
				nodes.add(new Node(7, 16));
				nodes.add(new Node(5, 16));
				nodes.add(new Node(5, 17));
				nodes.add(new Node(3, 17));
				nodes.add(new Node(3, 15));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("O, cześć.");
				addJob("Chodzę do przedszkola. A już za rok będę pasowana na #ucznia! ☺");
				addReply("ucznia", "Czy szkoła nie jest czymś wspaniałym? Mama kupi mi kolorowy plecak, piórnik i kredki."
						+ " Będę też miała własne zeszyty i książki, takie jak mój brat. Nie mogę się doczekać!");
				addOffer("Mogę ci najwyżej zaoferować zabawę w berka, hihi ^^");
				addHelp("Jestem tylko dzieckiem, moja pomoc na nic ci się nie przyda.");
				addGoodbye("Papa! Wróć jak najszybciej, pobawimy się razem!");
			}
		};

		npc.setDescription("Oto Alicja, córka Domiesława. Wygląda na strasznie zmęczoną.");
		npc.setEntityClass("npcdziecko3");
		npc.setGender("F");
		npc.setPosition(3, 15);
		zone.add(npc);
	}
}
