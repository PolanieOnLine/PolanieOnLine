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
package games.stendhal.server.maps.koscielisko.plains;

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
 * @author ?
 */
public class WielkoludNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Wielkolud") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(1, 75));
				nodes.add(new Node(1, 85));
				nodes.add(new Node(15, 85));
				nodes.add(new Node(15, 90));
				nodes.add(new Node(23, 90));
				nodes.add(new Node(23, 100));
				nodes.add(new Node(46, 100));
				nodes.add(new Node(46, 90));
				nodes.add(new Node(36, 90));
				nodes.add(new Node(36, 81));
				nodes.add(new Node(15, 81));
				nodes.add(new Node(15, 76));
				nodes.add(new Node(8, 76));
				nodes.add(new Node(8, 73));
				nodes.add(new Node(1, 73));
				nodes.add(new Node(1, 75));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Poszukuję kogoś kto rozprawi się z tutejszymi #potworami.");
				addReply("potworami", "Biega tu masa pokutników, a i lawiny kamienne nieraz przygniotły palce u mych stóp. Jeżeli chcesz mi pomóc to przyjmij moje #zadanie.");
				addHelp("Skupuję różne przedmioty. Jeżeli masz coś to #zaoferuj mi to, poza tym mam dla ciebie małe #zadanie.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrareitems")), true);
				addGoodbye("Do widzenia kolego.");
			}
		};

		npc.setDescription("Oto Wielkolud, strzeże dzikich ostępów Kościeliska.");
		npc.setEntityClass("npcwielkolud");
		npc.setGender("M");
		npc.setPosition(1, 75);
		zone.add(npc);
	}
}
