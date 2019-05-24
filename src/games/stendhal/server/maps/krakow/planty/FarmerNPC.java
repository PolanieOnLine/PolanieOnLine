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
package games.stendhal.server.maps.krakow.planty;

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
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class FarmerNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Farmer Bruno") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(76, 23));
				nodes.add(new Node(79, 23));
				nodes.add(new Node(79, 22));
				nodes.add(new Node(82, 22));
				nodes.add(new Node(82, 21));
				nodes.add(new Node(89, 21));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witojże w mej farmie!");
				addJob("Opiekuje się tym polem zboża!");
				addHelp("Mógłbym trochę sprzedać mego zboża bo mam nadmiar tego. Aaaa... jeszcze jedno, będę potrzebował twej pomocy, więc jak szukasz jakiegoś #'zadania' to czekam.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellbruno")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Widzisz pewnego farmera o imieniu Bruno, który zajmuje się uprawą zboża.");
		npc.setEntityClass("hoeingmannpc");
		npc.setPosition(76, 23);
		npc.initHP(100);
		zone.add(npc);
	}
}
