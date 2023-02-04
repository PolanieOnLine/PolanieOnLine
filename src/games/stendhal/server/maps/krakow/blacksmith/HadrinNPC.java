/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.blacksmith;

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
 * @author zekkeq
 */
public class HadrinNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Hadrin") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// walks along the aqueduct path, roughly
				nodes.add(new Node(6, 12));
				nodes.add(new Node(10, 12));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Produkuję strzały do łuków.");

				addReply("polano",
						"Potrzebuję drewna na promień do strzały. Porozmawiaj z drwalem on ci powie gdzie można ścinać drzewa.");
				addReply(Arrays.asList("gold nugget", "nugget", "gold", "złota", "bryłka", "bryłka złota"),
						"Bryłki złota znajdziesz w Zakopane koło źródeł na wschód od domku poniżej jaskini zbójników. Potrzebuję je na groty.");
				addReply("piórko",
						"Potrzebuję je na lotki. Zabij kilka gołębi.");
				addReply("kilof",
						"Przydatny przy wydobyciu siarki i węgla.");

				addReply("łopata", "No cóż... czymś trzeba kopać.");
				addReply("lina", "Przydatna gdy zechcesz zejść na niższe poziomy.");
				addHelp("Jeśli przyniesiesz mi #'polano', #'bryłka złota' i #'piórko', mogę zrobić dla ciebie złote strzały. Powiedz tylko #'zrób strzała złota'.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto krasnolud zwany Hadrinem. Jeśli masz dobre zamiary to ulepszy twą złotą ciupagę.");
		npc.setEntityClass("dwarfnpc");
		npc.setGender("M");
		npc.setPosition(6, 12);
		zone.add(npc);
	}
}
