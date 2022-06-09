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
package games.pol.server.maps.zakopane.home;

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
 * @author Legolas
 */
public class AdasNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZakopaneHome(zone);
	}

	private void buildZakopaneHome(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Adaś") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(6, 3));
				nodes.add(new Node(6, 7));
				nodes.add(new Node(4, 7));
				nodes.add(new Node(4, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody bohaterze.");
				addJob("Boje się zejść do piwnicy.");
				addHelp("Tak możesz mi pomóc, wykonaj dla mnie #zadanie.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto mały chłopiec o imieniu Adam. Jest nieco czymś przygnębiony.");
		npc.setEntityClass("npcdzieckogoralskie");
		npc.setGender("M");
		npc.setPosition(6, 3);
		npc.setSounds(Arrays.asList("hiccup-01", "sneeze-male-01"));
		zone.add(npc);
	}
}
