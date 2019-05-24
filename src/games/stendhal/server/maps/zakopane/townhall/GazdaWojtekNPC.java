/* $Id: GazdaWojtekNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.townhall;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class GazdaWojtekNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneRatuszArea(zone);
	}

	private void buildZakopaneRatuszArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gazda Wojtek") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(12, 4));
				nodes.add(new Node(20, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj.");
				addJob("Jestem gazda Wojtek. Opiekuje się miastem Zakopane!");
				addHelp("Nasze miasto potrzebuje różnych rzeczy. Jeżeli potrzebujesz banku to znajdziesz go na południe od ratusza, a jeżeli leczenia to udaj się na wschód do Gaździny Jadźki.");
				addOffer("Już nie sprzedaję zwojów. Tym zajmuje się teraz Juhas. Może chciałbyś się zasłużyć dla Zakopanego podejmując się #zadania. Nagroda Cię nie minie.");
				addGoodbye("Życzę powodzenia!");
			}
		};

		npc.setEntityClass("npcgazda");
		npc.setPosition(12, 4);
		npc.initHP(100);
		zone.add(npc);
	}
}
