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
package games.stendhal.server.maps.krakow.vineyard;

import java.util.Arrays;
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
public class WinicjuszNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Winicjusz") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(14, 118));
				nodes.add(new Node(9, 118));
				nodes.add(new Node(9, 120));
				nodes.add(new Node(14, 120));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuje się tą winnicą oraz produkujemy tutaj najwyższej jakości wino!");
				addOffer("Możesz ode mnie kupić winogrona, które możesz zanieść do mojego #'brata'. Zrobi on dla Ciebie najlepsze wino jakie jeszcze nikt nie widział!");
				addReply(Arrays.asList("brat", "brata", "brother"), "Mój brat ma na imię Zbyszko, który powinien się znajdować w środku domu.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellwinicjusz")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Oto Winicjusz. Jest plantatorem i na pobliskim wzgórzu uprawia soczyste winogrona, z których potem produkuje się doskonałe wino.");
		npc.setEntityClass("npcwinicjusz");
		npc.setPosition(14, 120);
		npc.initHP(100);
		zone.add(npc);
	}
}
