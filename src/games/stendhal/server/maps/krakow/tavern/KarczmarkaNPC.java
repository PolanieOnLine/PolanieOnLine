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
package games.stendhal.server.maps.krakow.tavern;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build a NPC
 *
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
		final SpeakerNPC npc = new SpeakerNPC("Granny Alina") {

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
				addJob("Jestem karczmarką w swej tawernie.");
				addHelp("Dobrze, że się pytasz. Aktualnie eksperymentuje, aby zrobić magiczną grzybową zupę. Powiedz mi tylko #'zadanie', a Ci powiem co mógłbyś dla mnie zrobić.");
				addOffer("Jeżeli wykonasz dla mnie #'zadanie' to będę mogła dla Ciebie robić me specjalne danie!");
				// borowik, pieczarka, opieńka, pieprznik jadalny, cebula, marchew, por
				// powtórka co 7 min.
				addGoodbye();
			}
		};

		npc.setDescription("Oto babcia Alina, jest znana ze swojej wspaniałej kuchni domowej.");
		npc.setEntityClass("granmanpc");
		npc.setPosition(35, 20);
		zone.add(npc);
	}
}