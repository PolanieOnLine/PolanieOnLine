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
package games.pol.server.maps.zakopane.city;

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
 * Zakopane Nosiwoda Gerwazy (Outside / Level 0).
 *
 * @author edi18028
 */
public class BuklakNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Nosiwoda Gerwazy") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(31, 107));
				nodes.add(new Node(36, 107));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj!");
				addJob("Napełniam, pusty bukłak wodą, powiedz mi tylko #nalej.");
				addHelp("Nie, dziękuję. Do pomocy mam swoich pomocników.");
				addGoodbye("Trzymaj się.");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("pusty bukłak", 1);
				requiredResources.put("money", 140);

				final ProducerBehaviour behaviour = new ProducerBehaviour("nosiwoda_fill_buklak",
						Arrays.asList("fill", "nalej"), "bukłak z wodą", requiredResources, 3 * 5);

				new ProducerAdder().addProducer(this, behaviour,
					"Pozdrawiam! Jeżeli przyniesiesz mi #'pusty bukłak' to naleję do niego wody ze źródełka. Powiedz tylko #nalej.");
				addReply("pusty bukłak",
						"Koło Wisły w krainie Kraka mieszka rymarz, on ci może wykonać pusty bukłak.");
			}
		};

		npc.setDescription("Oto nosiwoda Gerwazy. Być może napełni trochę źródlanej wody.");
		npc.setEntityClass("npcgazda2");
		npc.setGender("M");
		npc.setPosition(31, 107);
		zone.add(npc);
	}
}
