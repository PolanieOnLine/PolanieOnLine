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
// Based on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
package games.stendhal.server.maps.koscielisko.zrc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class EdgardNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Edgard") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 42));
				nodes.add(new Node(14, 42));
				nodes.add(new Node(14, 44));
				nodes.add(new Node(30, 44));
				nodes.add(new Node(30, 58));
				nodes.add(new Node(26, 58));
				nodes.add(new Node(26, 62));
				nodes.add(new Node(8, 62));
				nodes.add(new Node(8, 69));
				nodes.add(new Node(37, 69));
				nodes.add(new Node(37, 66));
				nodes.add(new Node(51, 66));
				nodes.add(new Node(51, 42));
				nodes.add(new Node(2, 42));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Widzisz jak latam, pewno sam byś tak chciał ?.");
				addJob("Przenoszę wiadomości z dalekich pól bitew i rozkazy od  Samego Mistrza Zakonu Cieni.");
				addHelp("Latam tu i tam, nie potrzebuje od nikogo pomocy.");
				addGoodbye("No to hej, ja lecę.");
			}
		};

		npc.setDescription("Oto Edgard, jest szybki.");
		npc.setEntityClass("npcedgard");
		npc.setPosition(2, 42);
		npc.initHP(1000);
		zone.add(npc);
	}
}
