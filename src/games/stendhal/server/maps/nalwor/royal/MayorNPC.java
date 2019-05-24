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
package games.stendhal.server.maps.nalwor.royal;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds the elf mayor NPC.
 *
 * @author kymara
 */
public class MayorNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Maerion") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 23));
				nodes.add(new Node(13, 23));
				nodes.add(new Node(13, 25));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(17, 23));
				nodes.add(new Node(21, 23));
				nodes.add(new Node(21, 27));
				nodes.add(new Node(17, 27));
				nodes.add(new Node(17, 25));
				nodes.add(new Node(13, 25));
				nodes.add(new Node(13, 23));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć. Musisz być odważny, aby stać przede mną.");
				addJob("Masz czelność pytać, mały człowieczku?!");
				addHelp("Cóż, może mógłbyś mi pomóc rozwiązać #problem.");
				add(ConversationStates.ATTENDING, "problem", null, ConversationStates.ATTENDING,
				        "Tutaj nie ma mrocznych elfów. Uwierz mi! Ja?! nie, nie, nie, Jestem dobrze opalony...", null);
				addGoodbye("Żegnaj człowieczku.");
			}
		};

		npc.setDescription("Oto królewski elf. Coś w nim wzbudza twój niepokój.");
		npc.setEntityClass("elfmayornpc");
		npc.setPosition(9, 23);
		npc.initHP(100);
		zone.add(npc);
	}
}
