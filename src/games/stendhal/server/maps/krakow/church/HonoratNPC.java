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
package games.stendhal.server.maps.krakow.church;

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
public class HonoratNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Ojciec Honorat") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 5));
				nodes.add(new Node(14, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Zajmuję się sporządzaniem cudownego #'eliksiru'!");
				addHelp("Mogę sporządzić dla Ciebie wyjątkowy #'eliksir'. Powiedz mi tylko #'sporządź duży eliksir'.");
				addOffer("Mógłbym dla Ciebie sporządzić ten wyśmienity #'eliksir'. Powiedz mi tylko #'sporządź duży eliksir'.");
				// 1x vampirette entrails + 2x sclaria = duży eliksir
				// koszt: 250; czas: 2 min na szt.
				addReply("eliskir", "Ten cudowny eliksir potrafi wyleczyć w okamgnieniu nawet bardzo rozległe rany!");
				addGoodbye("Niech Bóg Cię prowadzi!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("money", 250);
				requiredResources.put("truchło wampira", 1);
				requiredResources.put("sclaria", 2);

				final ProducerBehaviour behaviour = new ProducerBehaviour("honorat_concoct_potion",
						Arrays.asList("concoct", "sporządź"), "duży eliksir", requiredResources, 2 * 60);
				new ProducerAdder().addProducer(this, behaviour,
						"Pozdrawiam młodzieńcze. W czym mogę Tobie #'pomóc'?");
			}
		};

		npc.setDescription("Oto Ojciec Honorat. Jest mnichem i od lat zgłębia tajniki leczenia.");
		npc.setEntityClass("npcwikary");
		npc.setPosition(14, 5);
		npc.initHP(100);
		zone.add(npc);
	}
}
