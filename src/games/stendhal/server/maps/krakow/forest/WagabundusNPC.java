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
package games.stendhal.server.maps.krakow.forest;

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
public class WagabundusNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Wagabundus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 87));
				nodes.add(new Node(8, 86));
				nodes.add(new Node(9, 86));
				nodes.add(new Node(9, 85));
				nodes.add(new Node(12, 85));
				nodes.add(new Node(12, 84));
				nodes.add(new Node(13, 84));
				nodes.add(new Node(13, 83));
				nodes.add(new Node(16, 83));
				nodes.add(new Node(16, 82));
				nodes.add(new Node(17, 82));
				nodes.add(new Node(17, 81));
				nodes.add(new Node(20, 81));
				nodes.add(new Node(20, 80));
				nodes.add(new Node(23, 80));
				nodes.add(new Node(23, 79));
				nodes.add(new Node(24, 79));
				nodes.add(new Node(24, 78));
				nodes.add(new Node(27, 78));
				nodes.add(new Node(27, 77));
				nodes.add(new Node(30, 77));
				nodes.add(new Node(30, 76));
				nodes.add(new Node(31, 76));
				nodes.add(new Node(31, 75));
				nodes.add(new Node(34, 75));
				nodes.add(new Node(34, 74));
				nodes.add(new Node(37, 74));
				nodes.add(new Node(37, 73));
				nodes.add(new Node(40, 73));
				nodes.add(new Node(40, 72));
				nodes.add(new Node(41, 72));
				nodes.add(new Node(41, 71));
				nodes.add(new Node(44, 71));
				nodes.add(new Node(44, 70));
				nodes.add(new Node(47, 70));
				nodes.add(new Node(47, 69));
				nodes.add(new Node(50, 69));
				nodes.add(new Node(50, 68));
				nodes.add(new Node(53, 68));
				nodes.add(new Node(53, 67));
				nodes.add(new Node(56, 67));
				nodes.add(new Node(56, 66));
				nodes.add(new Node(124, 30));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ohooo.. Kto do mnie zawitał!");
				addJob("Podróżuję po rónych krainach prasłowiańskich i właśnie nie dawno wróciłem z kolejnej dalekiej podróży.");
				addOffer("Mogę Ci opowiedzieć #'legende' o trzech siostrach, które są czarownicami. Opierając się na tej legendzie to znam ich położenie. Ale za darmo Ci nie opowiem!");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Wagabundus. Lubi wesołą kompanię i jest zawsze chętny do rozmowy pod warunkiem, że przyniesiesz mu coś do zwilżenia gardła.");
		npc.setEntityClass("npcwagabundus");
		npc.setPosition(6, 87);
		npc.initHP(100);
		zone.add(npc);
	}
}
