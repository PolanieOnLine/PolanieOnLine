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
package games.stendhal.server.maps.krakow.marketplace;

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
public class LajkonikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Lajkonik") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(88, 44));
				nodes.add(new Node(84, 44));
				nodes.add(new Node(84, 52));
				nodes.add(new Node(84, 52));
				nodes.add(new Node(88, 52));
				nodes.add(new Node(88, 48));
				nodes.add(new Node(101, 48));
				nodes.add(new Node(88, 48));
				nodes.add(new Node(88, 45));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jeżeli chcesz mogę ci opowiedzieć #'historię' z roku #'1287'.");
				addOffer("Mam do zaoferowania #'długi łuk' oraz #'wzmocnione drewniane strzały'.");
				addReply("wzmocnione drewniane strzały", "Te strzały są ulepszoną wersją zwykłych drewnianych strzał. Są zrobione z bardzo mocnego dębu wzmonionego odrobiną żeliwa. Powiedz mi tylko #buy <liczba> #'wzmocniona drewniana strzała'.");
				addReply("długi łuk", "Powiedz mi tylko #kup #'długi łuk'.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Lajkonik. Jest jednym z symboli miasta Krakowa. Jeżeli go spytasz opowie ci historię wydarzenia z 1287 roku.");
		npc.setEntityClass("lajkoniknpc");
		npc.setGender("M");
		npc.setPosition(88, 45);
		zone.add(npc);
	}
}
