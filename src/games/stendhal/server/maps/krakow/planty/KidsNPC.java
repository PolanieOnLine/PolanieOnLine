/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.planty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;

/**
 * Creates the NPCs.
 *
 * @author KarajuSs // based on KidsNPCs from Ados city
 */
public class KidsNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKids(zone);
	}

	private void buildKids(final StendhalRPZone zone) {
		final String[] names = { "Kasper", "Leo", "Balbina", "Nikodem", "Roch" };
		final String[] classes = { "kid3npc", "kid4npc", "kid5npc", "kid6npc", "kid7npc" };
		final String[] descriptions = {"Oto Kasper. Wydaje się być nieco znudzony.", "Oto Leo, który uwielbia się bawić!", "Oto Balbina. Jest uroczą dziewczynką.",
					"Oto Nikodem. Zgubił swoją zabawkę i nie może jej znaleźć.", "Oto Roch. Lubi opowiadać żarty."};
		final Node[] start = new Node[] {new Node(118, 71), new Node(118, 77), new Node(114, 77), new Node(114, 80), new Node(118, 74)};
		for (int i = 0; i < 5; i++) {
			final SpeakerNPC npc = new SpeakerNPC(names[i]) {
				@Override
				protected void createPath() {
					final List<Node> nodes = new LinkedList<Node>();
					nodes.add(new Node(118, 69));
					nodes.add(new Node(109, 69));
					nodes.add(new Node(109, 68));
					nodes.add(new Node(103, 68));
					nodes.add(new Node(103, 71));
					nodes.add(new Node(98, 71));
					nodes.add(new Node(98, 70));
					nodes.add(new Node(97, 70));
					nodes.add(new Node(97, 69));
					nodes.add(new Node(94, 69));
					nodes.add(new Node(94, 70));
					nodes.add(new Node(93, 70));
					nodes.add(new Node(93, 72));
					nodes.add(new Node(92, 72));
					nodes.add(new Node(92, 76));
					nodes.add(new Node(94, 76));
					nodes.add(new Node(94, 73));
					nodes.add(new Node(97, 73));
					nodes.add(new Node(97, 79));
					nodes.add(new Node(99, 79));
					nodes.add(new Node(99, 78));
					nodes.add(new Node(108, 78));
					nodes.add(new Node(108, 80));
					nodes.add(new Node(114, 80));
					nodes.add(new Node(114, 77));
					nodes.add(new Node(118, 77));
					setPath(new FixedPath(nodes, true));
				}

				@Override
				protected void createDialog() {
					if (!getName().equals("Nikodem") && !getName().equals("Balbina")
							&& !getName().equals("Leo")) {
						add(ConversationStates.IDLE,
						        ConversationPhrases.GREETING_MESSAGES,
					        new GreetingMatchesNameCondition(getName()), true,
						        ConversationStates.IDLE,
						        "Mamusia powiedziała, że nie powinniśmy rozmawiać z nieznajomymi! Dowidzenia.",
						        null);
					}
					addGoodbye("Dowidzenia, dowidzenia!");
				}
			};

			npc.setEntityClass(classes[i]);
			npc.setPosition(start[i].getX(), start[i].getY());
			npc.setDescription(descriptions[i]);
			npc.initHP(100);
			zone.add(npc);
		}
	}
}
