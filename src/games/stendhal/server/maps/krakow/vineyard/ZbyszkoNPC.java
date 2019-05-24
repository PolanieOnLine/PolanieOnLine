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
package games.stendhal.server.maps.krakow.vineyard;

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
public class ZbyszkoNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Zbyszko") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(26, 13));
				nodes.add(new Node(26, 19));
				nodes.add(new Node(27, 19));
				nodes.add(new Node(27, 28));
				nodes.add(new Node(18, 28));
				nodes.add(new Node(18, 25));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(17, 17));
				nodes.add(new Node(4, 17));
				nodes.add(new Node(4, 5));
				nodes.add(new Node(10, 5));
				nodes.add(new Node(4, 5));
				nodes.add(new Node(4, 17));
				nodes.add(new Node(17, 17));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(18, 25));
				nodes.add(new Node(18, 28));
				nodes.add(new Node(27, 28));
				nodes.add(new Node(27, 19));
				nodes.add(new Node(26, 19));
				nodes.add(new Node(26, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Zajmuje się tą winnicą oraz produkujemy tutaj najwyższej jakości wino!");
				addOffer("Mogę Tobie przygotować doskonałe wino jakie jeszcze nikt nie widział, jeżeli zdobędziesz dla mnie winogrona od #'brata'. Powiedz mi tylko #'zrób', a wykonam dla Ciebie to wino.");
				addReply("brat", "Mój brat ma na imię Winicjusz, który powinien się znajdować zaraz obok naszego domu.");
				addGoodbye();

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("kiść winogron", 2);
				final ProducerBehaviour behaviour = new ProducerBehaviour("zbyszko_make_vine",
						Arrays.asList("make", "zrób"), "napój z winogron", requiredResources, 1 * 60);
				new ProducerAdder().addProducer(this, behaviour,
						"Witajże w moim domu! Co potrzebujesz?");
			}
		};

		npc.setDescription("Oto Zbyszko. Jest bratem Winicjusza i może przygotować doskonałe wino dla Ciebie.");
		npc.setEntityClass("npczbyszko");
		npc.setPosition(26, 13);
		npc.initHP(100);
		zone.add(npc);
	}
}
