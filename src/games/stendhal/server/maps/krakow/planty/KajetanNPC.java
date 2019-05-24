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
package games.stendhal.server.maps.krakow.planty;

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
public class KajetanNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Kajetan") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(111, 110));
				nodes.add(new Node(111, 111));
				nodes.add(new Node(112, 111));
				nodes.add(new Node(112, 112));
				nodes.add(new Node(114, 112));
				nodes.add(new Node(114, 113));
				nodes.add(new Node(117, 113));
				nodes.add(new Node(114, 113));
				nodes.add(new Node(114, 112));
				nodes.add(new Node(112, 112));
				nodes.add(new Node(112, 111));
				nodes.add(new Node(111, 111));
				nodes.add(new Node(111, 110));
				nodes.add(new Node(103, 110));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, co Cię do mnie sprowadza?");
				addJob("Zajmuję się wypasaniem kóz.");
				addOffer("Mogę Ci zaoferować kozę do wypasenia, gdy osiągnie ona wagę równą 100 to będziesz mógł ją sprzedać Targonowi.");
				// zakup kozy: 60 money, karmi się trawą
				addGoodbye();
			}
		};

		npc.setDescription("Oto Kajetan. Zajmuje się wypasaniem kóz.");
		npc.setEntityClass("noimagenpc"); // pasterz
		npc.setPosition(103, 110);
		npc.initHP(100);
		zone.add(npc);
	}
}
