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
package games.stendhal.server.maps.semos.plains;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
//import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

/**
 * Provides a Hoeing Man, hoeing farm ground north of Semos,
 * near Jenny's Mill
 *
 * @author omero
 *
 */
public class HoeingManNPC implements ZoneConfigurator {

	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
			final SpeakerNPC npc = new SpeakerNPC("Jingo Radish") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(48, 62));
				nodes.add(new Node(43, 76));
				nodes.add(new Node(43, 62));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Co za spotkanie, drogi wędrowcze!");
				addJob("Oto? Ciągle pozbywam się chwastów przy pomocy #motyki, lecz one odrastają za każdym razem...");
				addHelp("Nie spiesz się! Przejdź się po okolicy. Na północy znajduje się młyn, na wschodzie zaś - tereny rolnicze. Przyjemna i bogata wieś, gdzie można zacząć polowanie na jedzenie!");
				addReply(Arrays.asList("hoe", "motyka"),
	                    "Oh cóż nie ma nic specjalnego w mojej motyce... Jeżeli potrzebujesz dobrego narzędzia rolniczego jak #kosa to odwiedź kowala w pobliskim miaście Semos!");
				addReply(Arrays.asList("scythe", "kosa"),
	                    "Ach cóż... Jeżeli potrzebujesz dobrego narzędzia rolniczego jak #kosa to odwiedź kowala w pobliskim miaście Semos!");
				addGoodbye("Żegnaj! Może Twoja droga będzie wolna od chwastów.");
			}
		};
		// Finalize Jingo Radish, the hoeing man near the Mill north of Semos
		npc.setEntityClass("hoeingmannpc");
		npc.setDescription("Oto człowiek z motyką, który zajęty jest odchwaszczaniem...");
		npc.setPosition(48,62);
		npc.initHP(100);
		zone.add(npc);

		return npc;
	}
}
