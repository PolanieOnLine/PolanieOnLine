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
package games.stendhal.server.maps.tatry.mountain;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GoralskiKolekcjonerNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Gazda Bartek") {

			/**
			 * Creates a path around the table with the beers and to the furnance.
			 */
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(89, 33));
				nodes.add(new Node(85, 33));
				nodes.add(new Node(85, 25));
				nodes.add(new Node(93, 25));
				nodes.add(new Node(93, 33));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj młody bohaterze.");
				addJob("Moja praca? Uwielbiam kolekcjonować góralskie rzeczy!");
				addHelp("Tak możesz mi pomóc, wykonaj dla mnie #zadanie.");
				addGoodbye();
			}
		};

		npc.setEntityClass("noimagenpc");
		npc.setPosition(89, 33);
		npc.initHP(100);
		zone.add(npc);
	}
}
