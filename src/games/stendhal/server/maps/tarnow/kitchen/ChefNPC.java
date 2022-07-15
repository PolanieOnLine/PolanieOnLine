/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tarnow.kitchen;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

public class ChefNPC implements ZoneConfigurator {
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
		final SpeakerNPC baker = new SpeakerNPC("Szef Piotruś") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(11, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj! W obecnej chwili wszyscy ludzie wraz z rycerzami są poza miastem. Podejrzewam, że niedługo oni wrócą!");
				addJob("Jestem lokalnym kucharzem. Mimo iż dostajemy większość zapasów z Krakowa to i tak jest sporo pracy do zrobienia.");
				addHelp("Mogę Tobie ugotować ziemniaki, powiedz mi tylko #'zrób gotowane ziemniaki'.");
				addGoodbye();

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("ziemniaki", Integer.valueOf(1));
				requiredResources.put("butelka wody", Integer.valueOf(2));

				final ProducerBehaviour behaviour = new ProducerBehaviour("piotrus_make_potatoes", Arrays.asList("make", "zrób"), "gotowane ziemniaki",
				        requiredResources, 5 * 60);

				new ProducerAdder().addProducer(this, behaviour,
						"Witaj! W obecnej chwili wszyscy ludzie wraz z rycerzami są poza miastem. Podejrzewam, że niedługo oni wrócą! Jeśli chciałbyś skosztować moje pyszne ziemniaczki powiedz mi tylko #'zrób gotowane ziemniaki'.");
			}
		};

		baker.setDescription("Oto Szef Piotruś. Przygotowuje dania głównie z ziemniaków dla podróżujących.");
		baker.setEntityClass("bakernpc");
		baker.setGender("M");
		baker.setDirection(Direction.DOWN);
		baker.setPosition(15, 3);
		zone.add(baker);
	}
}
