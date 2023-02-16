/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.krakow.blacksmith;

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
public class BlacksmithAssistNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Inez") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 7));
				nodes.add(new Node(5, 11));
				nodes.add(new Node(14, 11));
				nodes.add(new Node(14, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, w czym mogę Tobie #'pomóc'?");
				addJob("Zajmuje się zamówieniami na wyposażenie, nie wiem czy wiesz, ale nasza kuźnia produkuje zbroje i nie tylko na skale światową.");
				addHelp("Możemy mieć dla ciebie #'zadanie', a jeśli masz przy sobie trochę żelaza i chcesz nam sprzedać to chętnie kupimy! Potrzebujemy na nowe wyposażenie.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Inez. Jest asystentem kowala.");
		npc.setEntityClass("naughtyteennpc");
		npc.setGender("M");
		npc.setPosition(5, 7);
		zone.add(npc);
	}
}
