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
package games.stendhal.server.maps.krakow.tavern;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class BarmankaNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

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
		final SpeakerNPC npc = new SpeakerNPC("Idris") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(22, 7));
				nodes.add(new Node(26, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem barmanką w tej karczmie.");
				addOffer("Spójrz na tablice za mną, sprzedaję to co widnieje właśnie na niej.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellidris")), false);
				addGoodbye();
			}
		};

		final Sign book = new Sign();
		book.setPosition(27, 5);
		book.setText("-- Idris sprzedaje --\n sok z chmielu\t 10\n napój z winogron\t 15\n flaska\t\t 5\n ser\t\t 20\n marchew\t\t 10\n jabłko\t\t 10\n mięso\t\t 40\n szynka\t\t 80");
		book.setEntityClass("blackboard");
		book.setResistance(20);
		zone.add(book);

		npc.setDescription("Oto śliczna Idris. Jest barmanką w tej karczmie.");
		npc.setEntityClass("tavernbarmaidnpc");
		npc.setPosition(26, 7);
		zone.add(npc);
	}
}