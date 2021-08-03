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
package games.pol.server.maps.zakopane.city;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class DorianNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Dorian") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(92, 7));
				nodes.add(new Node(94, 7));
				nodes.add(new Node(94, 8));
				nodes.add(new Node(96, 8));
				nodes.add(new Node(96, 10));
				nodes.add(new Node(98, 10));
				nodes.add(new Node(98, 12));
				// REVERT
				nodes.add(new Node(98, 10));
				nodes.add(new Node(96, 10));
				nodes.add(new Node(96, 8));
				nodes.add(new Node(94, 8));
				nodes.add(new Node(94, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("*płacz* Cze *pociągnięcie nosem* ść.");
				addJob("Chciałem pomóc swojemu ojczymowi w zabijaniu złych potworów. *płacz* *pociągnięcie nosem*");
				addOffer("Zgubiłem swoją maczugę. *płacz*");
				addGoodbye("Uważaj na siebie.");
			}
		};

		npc.setDescription("Oto Dorian. Jest strasznie smutny i wygląda jakby czegoś chyba szukał.");
		npc.setEntityClass("npcdorian");
		npc.setGender("M");
		npc.setPosition(92, 7);
		zone.add(npc);
	}
}
