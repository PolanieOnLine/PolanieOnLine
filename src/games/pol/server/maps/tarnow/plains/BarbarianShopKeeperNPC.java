/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.tarnow.plains;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * @author KarajuSs
 */
public class BarbarianShopKeeperNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Benio") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(86, 70));
				nodes.add(new Node(86, 74));
				nodes.add(new Node(80, 74));
				nodes.add(new Node(86, 74));
				nodes.add(new Node(86, 70));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Skupuję barbarzyńskie przedmioty. Spójrz proszę na tabliczkę z moimi proponowanymi cenami!");
				addOffer("Spójrz proszę na tabliczkę z moimi proponowanymi cenami!");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("barbarianshopkeeper")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Oto Benio. Lubi się przebierać.");
		npc.setEntityClass("barbarianshopkeepernpc");
		npc.setGender("M");
		npc.setPosition(86, 70);
		zone.add(npc);
	}
}
