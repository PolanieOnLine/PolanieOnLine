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
package games.stendhal.server.maps.zakopane.city;

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
 * Zakopane Nosiwoda Jakub (Outside / Level 0).
 *
 * @author edi18028
 */
public class ButelkaNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Nosiwoda Jakub") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(19, 95));
				nodes.add(new Node(23, 95));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj!");
				addJob("Napełniam butelkę wodą, powiedz mi tylko #nalej.");
				addHelp("Pomagamy Gerwazemu w napełnianiu butelek wodą.");
				addGoodbye("Żegnaj i życzę zdrowia!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("butelka", 1);
				requiredResources.put("money", 100);

				final ProducerBehaviour behaviour = new ProducerBehaviour("nosiwoda_fill_butelke",
					Arrays.asList("fill", "nalej"), "butelka wody", requiredResources, 1 * 10);

				new ProducerAdder().addProducer(this, behaviour,
					"Pozdrawiam! Jeżeli przyniesiesz mi butelkę to naleję Tobie do niej wody ze źródełka. Powiedz tylko #nalej.");
				addReply("butelka",
					"Zaraz przy centrum w szpitalu pracuje Boguś, u którego to zakupisz lub możesz odwiedzić tawernę w Semos.");
			}
		};

		npc.setDescription("Oto nosiwoda Jakub. Być może napełni trochę orzeźwiającej wody do butelki.");
		npc.setEntityClass("oldcowboynpc");
		npc.setGender("M");
		npc.setPosition(19, 95);
		zone.add(npc);
	}
}
