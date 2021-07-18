/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.river;

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
 * A port manager
 */
public class PortManagerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Fiete") {
			@Override
			public void createDialog() {
				addGreeting("Moin! Statki Ados #opóźnione. Brak dostępnych pakietów.");
				addJob("Jestem menedżerem portu. Zajęty ważną pracą! Statki Ados opóźniły się, więc zrobiłem sobie #przerwę.");
				addReply(Arrays.asList("delayed", "opóźnione", "break", "przerwę", "przerwa"),
						"Pracuję ciężko. Bardzo ciężko. Więc przerwa jest dobra, kiedy nie ma statków.");
				addHelp("Stolica Deniran leży na północ stąd.");
				addQuest("Dużo pracy, kiedy przybywają statki. Ale teraz nie ma statków, nie ma pracy. Wróć później.");
				addGoodbye("Wróć później. Dużo pracy, kiedy przybywają statki.");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(82, 52));
				nodes.add(new Node(100, 52));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setDescription("Oto silny mężczyzna Fiete, który leniwie kroczy.");
		npc.setEntityClass("beardmannpc");
		npc.setGender("M");
		npc.setPosition(82, 52);
		zone.add(npc);
	}
}
