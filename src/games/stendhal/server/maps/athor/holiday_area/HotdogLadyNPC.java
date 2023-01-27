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
package games.stendhal.server.maps.athor.holiday_area;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Outside holiday area on Athor Island)
 */
public class HotdogLadyNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildhotdoglady(zone);
	}

	private void buildhotdoglady(final StendhalRPZone zone) {
		final SpeakerNPC hotdoglady = new SpeakerNPC("Sara Beth") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(33, 69));
				nodes.add(new Node(40, 69));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("O, Super! Cieszę się, że się zatrzymałeś!!! Naprawdę!");
				addJob("Jestem przygotowana psychicznie! Łapię promienie i sprzedaję rzeczy!");
				addHelp("Och! Myślisz, że mam wskazówkę?");
				addOffer("Sprawdź tablicę, która tam stoi, aby poznać ceny.");
				addQuest("Cokolwiek!");
				addGoodbye("Do zobaczenia. Chcesz z czymś frytki?");
			}
		};

		hotdoglady.setDescription("Oto Sara Beth. Jej punkt z hotdogami jest popularny na wyspie Athor.");
		hotdoglady.setEntityClass("woman_013_npc");
		hotdoglady.setGender("F");
		hotdoglady.setPosition(33,69);
		zone.add(hotdoglady);
	}
}
