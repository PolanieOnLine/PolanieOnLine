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
package games.stendhal.server.maps.gdansk.blacksmith;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class BlackSmithNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Polirois") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(17, 2));
				nodes.add(new Node(23, 2));
				nodes.add(new Node(23, 7));
				nodes.add(new Node(28, 7));
				nodes.add(new Node(28, 9));
				nodes.add(new Node(22, 9));
				nodes.add(new Node(22, 10));
				nodes.add(new Node(12, 10));
				nodes.add(new Node(12, 2));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Dzień dobry! Co sprowadza Ciebie do mnie?");
				addOffer("Nie mam nic do zaoferowania. Chyba, że #Wojtek Ciebie zainteresuje.");
				addReply("wojtek",
						"Pracuje u mnie jako czeladnik. Można powiedzieć, że jest moją prawą ręką oraz zajmuje się cześcią mojej pracy. Potrafi oszlifować kryształ #ametystu, wystarczy do niego podejść, gdyż ja mam zbyt dużo pracy na głowie.");
				addJob("Jestem jedynym kowalem w tym miasteczku.");
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setEntityClass("blacksmithnpc");
		npc.setPosition(17, 2);
		npc.initHP(100);
		npc.setDescription("Oto Polirois. Wygląda na zapracowanego.");
		zone.add(npc);
	}
}
