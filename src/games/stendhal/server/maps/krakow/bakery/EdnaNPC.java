/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.bakery;

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
public class EdnaNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Edna") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(24, 8));
				nodes.add(new Node(24, 6));
				nodes.add(new Node(27, 6));
				nodes.add(new Node(27, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Specjalizuje się w pieczeniu znakomitego chleba");
				addOffer("Mogę dla Ciebie przygotować #'chleb' z miejscowych świeżych składników. Powiedz tylko #'upiecz'.");
				addGoodbye("Żegnaj, mam nadzieję, że jeszcze wrócisz do naszej piekarni!");
			}
		};

		npc.setDescription("Oto Edna. Wraz ze swoim mężem prowadzą najlepszą piekarnię w całym Kraku.");
		npc.setEntityClass("housewifenpc");
		npc.setGender("F");
		npc.setPosition(27, 8);
		zone.add(npc);
	}
}
