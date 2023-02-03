/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.bakery;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * The bakery chef. Father of the camping girl.
 * He makes sandwiches for players.
 * He buys cheese.
 *
 * @author daniel
 * @see games.stendhal.server.maps.orril.river.CampingGirlNPC
 * @see games.stendhal.server.maps.quests.PizzaDelivery
 */
public class ChefNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Patrick") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(13, 3));
				nodes.add(new Node(26, 3));
				nodes.add(new Node(26, 4));
				nodes.add(new Node(26, 4));
				nodes.add(new Node(26, 4));
				nodes.add(new Node(26, 3));
				nodes.add(new Node(13, 3));
				nodes.add(new Node(13, 7));
				nodes.add(new Node( 5, 7));
				nodes.add(new Node( 5, 2));
				nodes.add(new Node( 5, 2));
				nodes.add(new Node( 5, 2));
				nodes.add(new Node( 5, 7));
				nodes.add(new Node(14, 7));
				nodes.add(new Node(14, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Witamy w Denirańskiej piekarni.");
				addJob("Otworzyłem piekarnie w Deniran.");
				addHelp("Uważaj. Nie daj się wciągnąć w wojnę.");
				addOffer("Christina zajmuje się sprzedażą, po prostu z nią porozmawiaj.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Patrick. Nosi uroczy kapelusz szefa kuchni.");
		npc.setEntityClass("chefnpc");
		npc.setGender("M");
		npc.setPosition(13, 3);
		zone.add(npc);
	}
}
