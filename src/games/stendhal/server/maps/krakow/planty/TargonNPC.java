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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class TargonNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Targon") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(95, 32));
				nodes.add(new Node(95, 31));
				nodes.add(new Node(92, 31));
				nodes.add(new Node(92, 25));
				nodes.add(new Node(100, 25));
				nodes.add(new Node(100, 31));
				nodes.add(new Node(99, 31));
				nodes.add(new Node(99, 32));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuję się handlem kóz.");
				addOffer("Jeżeli przyniesiesz mi kozę, która została przez Ciebie wypasiona to zostaniesz odpowiednio wynagrodzony.");
				// 150 money za kozę ważącą 100 kg
				addGoodbye();
			}
		};

		npc.setDescription("Oto Targon. Kupuje tylko w pełni wypasione owce i płaci za nie całkiem dobrze.");
		npc.setEntityClass("noimagenpc"); // npctargon
		npc.setPosition(98, 32);
		npc.initHP(100);
		zone.add(npc);
	}
}
