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
// Based on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
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
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
 */
public class KacperNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Kacper") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 2));
				nodes.add(new Node(16, 2));
				nodes.add(new Node(16, 5));
				nodes.add(new Node(10, 5));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć, ładny dziś dzień mamy.");
				addJob("Zbieram budulec na zamek lodowy. Możesz mi coś #zaoferować.");
				addHelp("Skupuję wszystko co potrzebne do budowy zamku lodowego, jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Skupuję śnieżka 5, kamienie 6, bryła lodu 8.");
				addQuest("Poszukaj kamieni, brył lodu, śnieżek a dobrze cię wynagrodzę.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buykacper")), false);
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto chłopak budujący zamek lodowy.");
		npc.setEntityClass("kid7npc");
		npc.setPosition(10, 2);
		npc.initHP(100);
		zone.add(npc);
	}
}
