/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
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
import games.stendhal.server.entity.npc.SpeakerNPC;

public class RybakNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Rybak") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 94));
				nodes.add(new Node(19, 94));
				nodes.add(new Node(19, 96));
				nodes.add(new Node(19, 92));
				nodes.add(new Node(19, 94));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ciii, nie spłosz ryb.");
				addJob("Kupię chętnie ryby od ciebie, nie chcę wracać do domu bez ryb. Możesz mi coś #zaoferować.");
				addHelp("Kupię każdą rybę, jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Kupię okoń 8, makrela 8, płotka 7, palia alpejska 7, błazenek 10, pokolec 9, pstrąg 9, dorsz 5.");
				addQuest("Nie mogę nic złowić a do domu nie wrócę z pustymi rękoma, #pomóż mi.");
				addGoodbye("Do widzenia.");
			}
		};

		npc.setDescription("Oto rybak.");
		npc.setEntityClass("fishermannpc");
		npc.setGender("M");
		npc.setPosition(17, 94);
		zone.add(npc);
	}
}
