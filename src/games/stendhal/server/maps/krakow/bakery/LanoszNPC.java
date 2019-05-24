/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.bakery;

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
 * Build a NPC
 *
 * @author KarajuSs
 */
public class LanoszNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Lanosz") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 5));
				nodes.add(new Node(4, 11));
				nodes.add(new Node(14, 11));
				nodes.add(new Node(14, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w naszej piekarni wojowniku!");
				addJob("Jestem właścicielem jedynej piekarni w całym Krakowie.");
				addOffer("Mogę dla Ciebie przygotować #'kanapkę z tuńczykiem' z miejscowych świeżych składników. Powiedz mi tylko #'zrób'.");
				addGoodbye("Żegnaj, mam nadzieję, że jeszcze wrócisz do naszej piekarni!");

				addReply(Arrays.asList("kanapki", "kanapka", "sandwiches", "sandwich"), "Mogę wykonać dla ciebie specjalne kanapki z tuńczykiem. Powiedz mi tylko #'zrób kanapka z tuńczykiem'.");
			}
		};
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("chleb", 1);
		requiredResources.put("tuńczyk", 2);
		requiredResources.put("pomidor", 1);
		requiredResources.put("sałata", 1);

		final ProducerBehaviour behaviour = new ProducerBehaviour("lanosz_make_sandwich",
				Arrays.asList("make", "zrób"), "kanapka z tuńczykiem", requiredResources, 2 * 60);
		new ProducerAdder().addProducer(npc, behaviour,
				"Witaj! Jakże miło, że zawitałeś do naszej piekarni, gdzie robię #kanapki.");

		npc.setDescription("Oto Lanosz. Mąż Edny, jest doskonałym piekarzem, a jego specjalnością są kanapki rybne, przygotowywane z miejscowych świeżych składników.");
		npc.setEntityClass("chefnpc");
		npc.setPosition(14, 5);
		zone.add(npc);
	}
}
