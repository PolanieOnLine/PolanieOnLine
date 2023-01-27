/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.city;

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
 * @author KarajuSs
 */
public class HalloweenSellerNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Katia") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(49, 20));
				nodes.add(new Node(44, 20));
				nodes.add(new Node(49, 20));
				nodes.add(new Node(49, 16));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Oto dzielny rycerzyk! Witaj!");
				addJob("Przebrałam się za czarownice i sprzedaje różne smakołyki!");
				addHelp("Pomoc w #'zadaniu'? Cóż... Spytaj, a się dowiesz! Zapytaj mnie również o #'ofertę'!");
				addReply(Arrays.asList("straszna dynia", "straszne dynie", "dynie", "dynia"), "Możesz znaleźć u szczura, starca, wieśniaka, gajowego, wszystkich zbójników leśnych, w tym banici oraz zbójników górskich. Powodzenia w zbieraniu!");
				addGoodbye("Miłego zbierania cukierów!");
			}
		};

		npc.setEntityClass("witch2npc");
		npc.setGender("F");
		npc.setPosition(49, 16);
		zone.add(npc);
	}
}
