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
package games.stendhal.server.maps.zakopane.bakery;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author Legolas (based on: ChefNPC in Semos bakery)
 */
public class ChefNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jaś") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(13, 8));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(3, 10));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(5, 3));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(15, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jam jest tutejszym piekarzem. Jedną z usług jakie prowadzę na tym terenie to produkcja placków z marchewką dla naszych drogich klientów, którzy chwalą sobie ich smak! Powiedz tylko #upiecz.");
				addHelp("Zajmuje się robieniem placków z marchewką. Powiedz #upiecz jeżeli zdecydujesz się na jeden z moich słynnych przepisów.");
				addReply("mąka",
						"Zapasy mąki znajdziesz w okolicy młynka w Krakowie.");
				addReply("marchew",
						"Niestety, ale w naszej zimowej krainie ciężko o rosnące marchewki. Najbliżej znajdziesz na działkach w Krakowie.");
				addReply("por",
						"Bardzo mi przykro, ale najbliżej por będziesz mógł zdobyć w Krakowie.");
				addGoodbye();
			}
		};

		npc.setEntityClass("chefnpc");
		npc.setGender("M");
		npc.setPosition(15, 3);
		zone.add(npc);
	}
}
