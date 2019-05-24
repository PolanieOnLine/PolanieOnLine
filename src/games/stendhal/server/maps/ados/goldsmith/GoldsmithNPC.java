/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.goldsmith;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Ados Goldsmith (Inside / Level 0).
 *
 * @author dine
 */
public class GoldsmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGoldsmith(zone);
	}

	private void buildGoldsmith(final StendhalRPZone zone) {
		final SpeakerNPC goldsmith = new SpeakerNPC("Joshua") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				// by the sink
				nodes.add(new Node(5, 7));
				// to the left wall
				nodes.add(new Node(2, 7));
				nodes.add(new Node(11, 7));
				// up one by the armor
				nodes.add(new Node(11, 6));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(12, 5));
				// to the right wall
				nodes.add(new Node(18, 5));
				nodes.add(new Node(18, 3));
				// to the starting point
				nodes.add(new Node(5, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć!");
				addJob("Jestem złotnikiem w tym mieście.");
				addHelp("Mój brat Xoderos jest kowalem w Semos. Teraz sprzedaje narzędzia. Może zrobić dla Ciebie #'misę do pukania złota'.");
				addGoodbye("Żegnaj");

				// Joshua makes gold if you bring him gold nugget and wood
				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 3);
				requiredResources.put("bryłka złota", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("joshua_cast_gold",
						 Arrays.asList("cast", "odlej"), "sztabka złota", requiredResources, 15 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Cześć! Jestem tutejszym złotnikiem. Jeżeli będziesz chciał, abym odlał dla Ciebie #sztabkę #złota to daj znać! Wystarczy, że powiesz #odlej");
				addReply(Arrays.asList("wood", "polano"),
						"Potrzebuję trochę polan, aby podtrzymywać ogień. Możesz je znaleźć w lesie.");
				addReply(Arrays.asList("ore", "gold ore", "gold nugget", "bryłka złota"),
						"Aby otrzymać sztabki złota potrzebne są bryłki tego kruszcu.");
				addReply(Arrays.asList("gold bar", "gold", "bar", "sztabkę złota"),
				        "Odlałem  już złoto dla Ciebie, ale trzymaj je w bezpiecznym miejscu. Słyszałem pogłoski, że niedługo znowu będzie można bezpiecznie podróżować do Fado. Tam będziesz mógł wymienić lub sprzedać.");
				addReply(Arrays.asList("gold pan", "misę do pukania złota"),
				        "Jeżeli już masz misę do pukania złota to możesz wydobywać złoto w pewnych miejscach.");
				addReply(Arrays.asList("oil","can of oil", "olejek"),"Rybacy nas w to zaopatrują.");
			}
		};

		goldsmith.setEntityClass("goldsmithnpc");
		goldsmith.setDirection(Direction.DOWN);
		goldsmith.setPosition(18, 3);
		goldsmith.initHP(100);
		goldsmith.setDescription("Oto Joshua. Jego rodzina jest znana z wykuwania różnych materjałów. Czy znasz jego brata Xoderosa?");
		zone.add(goldsmith);
	}
}
