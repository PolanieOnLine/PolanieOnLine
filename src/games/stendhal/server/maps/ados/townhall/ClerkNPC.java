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
package games.stendhal.server.maps.ados.townhall;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds ados townhall clerk NPC.
 * He can divorce people, see maps/quests/Marriage.java
 * 
 * @author kymara
 */
public class ClerkNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildClerk(zone);
	}

	private void buildClerk(final StendhalRPZone zone) {
		final SpeakerNPC clerk = new SpeakerNPC("Wilfred") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(23, 10));
				nodes.add(new Node(27, 10));	
				nodes.add(new Node(27, 8));	
				nodes.add(new Node(28, 8));
				nodes.add(new Node(28, 4));
				nodes.add(new Node(25, 4));
				nodes.add(new Node(25, 2));
				nodes.add(new Node(24, 2));
				nodes.add(new Node(24, 5));
			       	nodes.add(new Node(21, 5));
			       	nodes.add(new Node(21, 8));
			       	nodes.add(new Node(23, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam. Jeżeli mógłbym w czymś pomóc to wystarczy zapytać.");
				addJob("Jestem urzędnikiem w Ados. Mogę dać małżeństwu rozwód ( #divorce ).");
				addHelp("Mogę dać rozwód ( #divorce ) osobom, którym rozpada się związek.");
				addQuest("Nie mam nic dla Ciebie.");
				addOffer("Burmistrz, który urzęduje na wyższym piętrze posiada zwoje do tego miasta.");
				addGoodbye("Życzę miłego dnia.");
			}
		};

		clerk.setDescription("Oto ciężko pracujący urzędnik.");
		clerk.setEntityClass("executivenpc");
		clerk.setPosition(23, 10);
		clerk.initHP(100);
		zone.add(clerk);
	}
}
