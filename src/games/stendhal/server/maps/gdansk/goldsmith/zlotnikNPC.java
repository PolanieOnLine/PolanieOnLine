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
package games.stendhal.server.maps.gdansk.goldsmith;

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
public class zlotnikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Jerzy") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(6, 3));
				nodes.add(new Node(14, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem złotnikiem. Zbieram wszystko co jest związane ze złotem.");
				addHelp("Byłbym wdzięczny, gdybyś sprzedał mi parę cennych minerałów.");
				addOffer("Byłbym wdzięczny, gdybyś sprzedał mi parę cennych minerałów.");
				addQuest("Nie posiadam dla Ciebie zadań.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto złotnik, który zwie się Jerzy. Być może skupi trochę kamieni szlachetnych.");
		npc.setEntityClass("goldsmithnpc");
		npc.setGender("M");
		npc.setPosition(10, 3);
		zone.add(npc);
	}
}
