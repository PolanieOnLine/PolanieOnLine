/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.blacksmith;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author Legolas
 */
public class KrasnoludNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Krasnolud") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 3));
				nodes.add(new Node(7, 3));
				nodes.add(new Node(7, 7));
				nodes.add(new Node(8, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Produkuję strzały do kuszy.");
				addReply("polano",
						"Potrzebuję drewna na promień do strzały. Porozmawiaj z drwalem on ci powie gdzie można ścinać drzewa.");
				addReply(Arrays.asList("ore", "iron", "iron ore", "ruda żelaza"),
						"Rudę żelaza znajdziesz koło źródeł na wschód od domku poniżej jaskini zbójników. Potrzebuję ją na groty.");
				addReply("piórko",
						"Potrzebuję je na lotki. Zabij kilka gołębi.");
				addReply("kilof",
						"Przydatny przy wydobyciu siarki i węgla.");
				addReply("łopata",
						"No cóż, czymś trzeba kopać.");
				addReply("lina",
						"Przydatna gdy zechcesz zejść na niższe poziomy.");
				addHelp("Jeśli przyniesiesz mi #polano, #'ruda żelaza' i #piórko, mogę zrobić dla ciebie strzały. Powiedz tylko #zrób.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Krasnolud. Wygląda na bardzo zapracowanego oraz zmęczonego.");
		npc.setEntityClass("dwarfnpc");
		npc.setGender("M");
		npc.setPosition(8, 3);
		zone.add(npc);
	}
}
