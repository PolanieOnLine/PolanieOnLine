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
package games.stendhal.server.maps.zakopane.mountain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Tworzy NPC o nazwie Yerena
 * 
 * @author KarajuSs 00:34:21 11-07-2018
 */

public class ResetLVLNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Yerena") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 18));
				nodes.add(new Node(16, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam Cię dzielny wojowniku.");
				addJob("Zajmuję się resetowaniem mocy u wojowników. Powiedz mi tylko #'zadanie'.");
				addOffer("Mogę ci zaoferować zadanie, które po wykonaniu udowodnisz, czy rzeczywiście chcesz zresetować swój poziom.");
				addGoodbye();
			}
		};

		npc.setEntityClass("npcsmok");
		npc.setDescription("Oto Yerena. Smok, który włada czasem.");
		npc.setPosition(16, 18);
		npc.initHP(100);
		zone.add(npc);
	}
}
