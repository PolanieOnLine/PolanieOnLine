/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.sewinghouse;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author ZEKKEQ
 */
public class SewingNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Falisława") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 8));
				nodes.add(new Node(7, 15));
				nodes.add(new Node(12, 15));
				nodes.add(new Node(12, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem szwaczką. Produkuję rękawice dla wojowników.");
				addHelp("Obecnie nie potrzebuje pomocy.");
				//addHelp("Yhm... Zapytaj mnie o #'zadanie'...");
				addOffer("Moja znajoma, która znajduje się piętro niżej skupuje stare rękawice oraz skóry.");
				addGoodbye("Do widzenia i dziękuję za wstąpienie.");
			}
		};

		npc.setEntityClass("woman_004_npc");
		npc.setPosition(7, 8);
		npc.initHP(100);
		npc.setDescription("Oto Falisława. Szwaczka, która szyje rękawice dla wojowników.");
		zone.add(npc);
	}
}
