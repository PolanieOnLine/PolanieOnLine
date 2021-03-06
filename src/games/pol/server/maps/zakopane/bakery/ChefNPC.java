/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.zakopane.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

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
				addReply("mąka", "Dostajemy zapasy mąki z Krakowa.");
				addReply("marchew",
						"Niestety, ale w naszej zimowej krainie ciężko o rosnące marchewki. Najbliżej znajdziesz na działkach w Krakowie.");
				addReply("por",
						"Bardzo mi przykro, ale najbliżej por będziesz mógł zdobyć w Krakowie.");
				addGoodbye();
			}
		};

		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("mąka", 1);
		requiredResources.put("marchew", 2);
		requiredResources.put("por", 1);

		final ProducerBehaviour behaviour = new ProducerBehaviour(
				"jas_make_carrot_pie", Arrays.asList("make", "upiecz"), "tarta z marchewką",
				requiredResources, 4 * 60);

		new ProducerAdder().addProducer(npc, behaviour,
				"Witaj! Jakże miło, że zawitałeś do mojej piekarni. Przyszedłeś spróbować moich placków z marchewką? Mogę upiec jeden dla Ciebie.");

		npc.setEntityClass("chefnpc");
		npc.setPosition(15, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
