/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.magic.shrine;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds a priestess NPC.
 * She is a
 *
 * @author kymara
 */
public class PriestessNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Kendra Mattori") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 10));
				nodes.add(new Node(14, 10));
				nodes.add(new Node(14, 13));
				nodes.add(new Node(9, 13));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			    addGreeting(null, new SayTextAction("Witaj [name]."));
			    addJob("Jako kapłanka mogę #zaoferować Ci mikstury.");
				addHelp("Moja siostra Salva ma dar uzdrawiania. Wyszła na spacer koło akweduktu. Powinieneś ją znaleźć o ile jej potrzebujesz.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("superhealing")), true);
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto piękna kobieta, ukrywająca się pod owinięta tkaniną.");
		npc.setEntityClass("cloakedwoman2npc");
		npc.setPosition(9, 10);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.initHP(100);
		zone.add(npc);
	}
}
