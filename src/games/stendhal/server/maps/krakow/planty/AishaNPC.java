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
package games.stendhal.server.maps.krakow.planty;

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
public class AishaNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Aisha") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(100, 30));
				nodes.add(new Node(105, 30));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuje się tym młynem!");
				addHelp("Farmer #Bruno poszukuje odpowiedniej osoby do pomocy.");
				addReply("Bruno", "Bruno jest farmerem, który aktualnie zajmuje się farmą zboża.");
				addGoodbye();
			}
		};
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("zboże", 5);

		final ProducerBehaviour behaviour = new ProducerBehaviour("aisha_mill_flour",
				 Arrays.asList("mill", "zmiel"), "mąka", requiredResources, 2 * 60);
		new ProducerAdder().addProducer(npc, behaviour,"Pozdrawiam! Nazywam się Aisha i jestem szefową tutejszego młyna. Jeżeli przyniesiesz mi #kłosy zboża to zmielę je dla Ciebie na mąkę. Powiedz mi tylko #zmiel ilość #mąka.");

		npc.setDescription("Oto Aisha. Wygląda na sympatyczną osobę.");
		npc.setEntityClass("woman_003_npc");
		npc.setPosition(100, 30);
		zone.add(npc);
	}
}