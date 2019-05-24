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
package games.stendhal.server.maps.ados.wall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Creates a man NPC to help populate Ados
 *
 */
public class HolidayingManNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Martin Farmer") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(70, 52));
				nodes.add(new Node(76, 52));
				nodes.add(new Node(76, 17));
				nodes.add(new Node(70, 17));
				nodes.add(new Node(70, 14));
				nodes.add(new Node(79, 14));
				nodes.add(new Node(79, 52));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Hi hi.");
				addHelp("Burmistrz tego miasta jest naprawdę w porządku. Odwiedziłem go wraz z żoną wczoraj aby uzyskać pomoc.");
				addOffer("Co takiego? Ja jestem na wczasach.");
				addQuest("Ehm ... I nie potrzebuję pomocy w tej chwili, ale dzięki."); 
				addJob("Nie nie, ja tu jestem z żoną Alice na wakacjach.");
				addGoodbye("Do zobaczenia i zajmij się lwami za murem.");

				}
		};

		npc.setEntityClass("man_008_npc");
		npc.setPosition(70, 52);
		npc.initHP(100);
		npc.setDescription("Oto Martin Farmer. Jest na wakacjach z żoną Alice.");
		zone.add(npc);
	}
}
