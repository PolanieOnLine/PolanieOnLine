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
package games.stendhal.server.maps.nalwor.basement;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Inside Nalwor Inn basement .
 */
public class ArcheryDealerNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Merenwen") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10,5));
				nodes.add(new Node(16,5));
				nodes.add(new Node(16,6));
				nodes.add(new Node(10,6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Miłe spotkanie nieznajomy.");
				addJob("Skupuję łucznicze wyposażenie dla naszej wioski.");
				addHelp("Nie mogę zaoferować Tobie pomocy. Przykro mi.");
				addOffer("Spójrz na tablicę i sprawdź ceny.");
				addQuest("Nie mam dla Ciebie zadania.");
				addGoodbye("Bądź szczęśliwy. Do widzenia.");
			}
		};

		npc.setDescription("Oto piękna czarodziejka elfów Merenwen. Skupuje przedmioty związane z łucznictwem.");
		npc.setEntityClass("mageelfnpc");
		npc.setGender("F");
		npc.setPosition(10, 5);
		zone.add(npc);
   	}
}
