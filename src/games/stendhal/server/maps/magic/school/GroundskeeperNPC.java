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
package games.stendhal.server.maps.magic.school;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds the groundskeeper NPC.
 *
 * @author Teiv
 */
public class GroundskeeperNPC implements ZoneConfigurator {

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
		final SpeakerNPC groundskeeperNPC = new SpeakerNPC("Morgrin") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(35, 13));
				nodes.add(new Node(35, 7));
				nodes.add(new Node(34, 7));
				nodes.add(new Node(34, 4));
				nodes.add(new Node(30, 4));
				nodes.add(new Node(30, 14));
				nodes.add(new Node(32, 14));
				nodes.add(new Node(32, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć przyjacielu. Miły dzień na spacer nieprawdaż?");
				addReply(ConversationPhrases.NO_MESSAGES, "Och przepraszam. Może jutro będziesz miał lepszy dzień.");
				addReply(ConversationPhrases.YES_MESSAGES, "Dobrze, dobrze. Mam nadzieję, że cieszysz się dniem.");
				addJob("Moją pracą jest sprzątanie szkoły i naprawa zepsutych rzeczy! Mam dużo pracy!");
				addHelp("Nie mogę Tobie pomóc. Jestem zajęty przez cały dzień, ale ty mógłbyś mi pomóc w małym #zadaniu!");
				addGoodbye("Dowidzenia.");
			}
		};

		groundskeeperNPC.setEntityClass("groundskeepernpc");
		groundskeeperNPC.setPosition(35, 13);
		groundskeeperNPC.setCollisionAction(CollisionAction.STOP);
		groundskeeperNPC.initHP(1000);
		groundskeeperNPC.setDescription("Oto Morgrin, kierownik szkoły w Magic City. Jest zawsze zajęty i potrzebuje pomocnej dłoni.");
		zone.add(groundskeeperNPC);
	}
}
