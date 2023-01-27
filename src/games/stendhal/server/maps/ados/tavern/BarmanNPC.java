/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.tavern;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Ados Tavern (Inside / Level 0).
 *
 * @author kymara
 */
public class BarmanNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildTavern(zone, attributes);
	}

	private void buildTavern(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC barman = new SpeakerNPC("Dale") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(27, 2));
				nodes.add(new Node(23, 2));
				nodes.add(new Node(23, 5));
				nodes.add(new Node(27, 5));
				nodes.add(new Node(27, 8));
				nodes.add(new Node(24, 8));
				nodes.add(new Node(24, 12));
                nodes.add(new Node(28, 12));
                nodes.add(new Node(28, 6));
                nodes.add(new Node(23, 6));
                nodes.add(new Node(23, 2));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć, wyglądasz świetnie...");
				addJob("Uszczęśliwiam panie. Jak się masz?");
				addQuest("Usiądź, zrelaksuj się i zobacz przedstawienie.");
				addHelp("Ten pokój jest dla kobiet. Mężczyźni siedzą w innym barze. Coralia obsługuje panów.");
				addGoodbye("Na zobaczenia kociaczki.");
			}
		};

		barman.setDescription("Oto Dale, jest barmanem. Spytaj, może ma coś ciekawego do zaoferowania.");
		barman.setEntityClass("barman3npc");
		barman.setGender("M");
		barman.setPosition(27, 2);
		zone.add(barman);
	}
}
