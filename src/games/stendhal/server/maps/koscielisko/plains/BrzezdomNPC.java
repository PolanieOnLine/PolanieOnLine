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
// Based on ../games/stendhal/server/maps/amazon/hut/JailedBarbNPC.java
package games.stendhal.server.maps.koscielisko.plains;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the Brzezdom on plains on Koscielisko.
 *
 * @author Teiv
 */
public class BrzezdomNPC implements ZoneConfigurator {
	//
	// ZoneConfigurator
	//

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
		final SpeakerNPC BrzezdomNPC = new SpeakerNPC("Brzezdom") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(44, 86));
				nodes.add(new Node(49, 86));
				nodes.add(new Node(49, 89));
				nodes.add(new Node(79, 89));
				nodes.add(new Node(79, 84));
				nodes.add(new Node(44, 84));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Zagadka tu, zagadka tam, kto je rozwiąże będzie chłop na schwał!");
				addJob("Błąkam się po górach lasach, zwiedzam, podpatruję, wszystko w mym notesie zapisuję.");
				addHelp("Stawiam pytania, oczekuję odpowiedzi. Mam dla ciebie małe zadanie.");
				addOffer("Nie mam nic do zaoferowania!");
				addGoodbye("Dowidzenia, czytaj legendy.");
			}
		};

		BrzezdomNPC.setEntityClass("jailedbarbariannpc");
		BrzezdomNPC.setPosition(44, 86);
		BrzezdomNPC.initHP(100);
		zone.add(BrzezdomNPC);
	}
}
