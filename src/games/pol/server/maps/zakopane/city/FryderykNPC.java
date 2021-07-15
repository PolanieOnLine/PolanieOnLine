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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Npc bierze udział w tasku:
 * 		MeetFryderyk
 * 		ScytheForFryderyk
 * 
 * @author Legolas
 */
public class FryderykNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Fryderyk") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(21, 8));
				nodes.add(new Node(32, 8));
				nodes.add(new Node(32, 9));
				nodes.add(new Node(21, 9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem tutaj bo jestem.");
				addOffer("Udzielam nowo przybyłym wskazówek gdzie znajdują się #budynki w Zakopanem.");
			}
		};

		npc.setDescription("Oto Fryderyk, który wygląda jakby znał te miejsce od podszewki.");
		npc.setEntityClass("paulnpc");
		npc.setGender("M");
		npc.setPosition(21, 8);
		npc.setSounds(Arrays.asList("hiccup-1", "sneeze-male-01"));
		zone.add(npc);
	}
}
