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
package games.stendhal.server.maps.kalavan.cottage;

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
 * NPC who makes tea.
 *
 * @author kymara
 */
public class HouseKeeperNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Granny Graham") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(4, 4));
				nodes.add(new Node(12, 4));
				nodes.add(new Node(12, 6));
				nodes.add(new Node(4, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem gospodynią domową. Mogę zaparzyć filiżankę świeżej #herbaty o ile chcesz. Powiedz tylko #zaparz.");
				addHelp("Zawsze mi brakuje, ale znajdę jakąś piękną filiżankę #herbaty.");
				addOffer("Zaparzę Tobie filiżankę #herbaty o ile chcesz. Powiedz tylko #zaparz.");
				addQuest("Mam ból głowy i małą Annie, która za każdym razem jak schodzi to hałasuje. Może mógłbyś dać jej jakieś zajęcie? ... tak, aby się uciszyła ...");
				addGoodbye("Dowidzenia.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();	
				requiredResources.put("mleko", 1);
				requiredResources.put("miód", 1);

				final ProducerBehaviour behaviour = new ProducerBehaviour("granny_brew_tea",
						 Arrays.asList("brew", "zaparz"), "filiżanka herbaty", requiredResources, 3 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Cześć.");
				addReply("mleko",
		        		"Cóż spodziewam się, że zdobędziesz mleko z farmy.");
				addReply("miód",
				        "Nie znasz pszczelarza z lasu Fado?");
				addReply("herbaty",
				        "To najlepszy napój. Słodzę ją miodem. Powiedz #'zaparz filiżanka herbaty' o ile będziesz chciał.");
			}
		};
		npc.setDescription("Oto starsza Granny Graham krzątająca się po kuchni i mówiąca do siebie.");
		npc.setEntityClass("granmanpc");
		npc.setDirection(Direction.RIGHT);
		npc.setPosition(4, 4);
		npc.initHP(100);
		zone.add(npc);
	}
}
