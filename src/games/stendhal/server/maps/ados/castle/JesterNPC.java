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
package games.stendhal.server.maps.ados.castle;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a Jester NPC to inform entrants to the castle.
 *
 * @author kymara
 */
public class JesterNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Huckle Rohn") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 57));
				nodes.add(new Node(8, 45));
				nodes.add(new Node(20, 45));
				nodes.add(new Node(20, 35));
				nodes.add(new Node(10, 35));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(20, 10));
				nodes.add(new Node(20, 45));
				nodes.add(new Node(8, 45));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam!");
				addJob("Jestem nadwornym błaznem. Nie mogę przestać! W mojej umowie o pracę nie ma stania i rozmawiania.");
				addHelp("Ciii... Mógłbym Ci opowiedzieć coś o przestępcach..., gdy Król wyjechał to oni przejęli zamek. Siedzę teraz cicho. Ciii...");
				add(ConversationStates.ATTENDING, Arrays.asList("offer", "oferta"), null, ConversationStates.IDLE,
				        "Niczego nie potrzebuję! Muszę żonglować! Dowidzenia!", null);
				add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, null,
				        ConversationStates.IDLE,
				        "Niczego nie potrzebuję! Muszę żonglować! Dowidzenia!", null);
				addGoodbye("Dowidzenia!");
			}
		};
		npc.setDescription("Oto Huckle Rohn, nadworny błazen.");
		npc.setEntityClass("magic_jesternpc");
		npc.setPosition(8, 57);
		npc.initHP(100);
		zone.add(npc);
	}
}
