/* $Id$ */
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
package games.stendhal.server.maps.semos.kanmararn;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DwarfGuardNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

		/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildPrisonArea(zone);
	}

	private void buildPrisonArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Hunel") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 23));
				nodes.add(new Node(12, 23));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			    addQuest("Boję się stąd wychodzić... Czy możesz mi #zaoferować naprawdę dobry sprzęt?");
				addJob("Byłem strażnikiem w więzieniu. Dopóki... cóż pewnie znasz resztę.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buychaos")), true);

				addGoodbye("Dowidzenia ... bądź ostrożny ...");
			}
			// remaining behaviour is defined in maps.quests.JailedDwarf.
		};

		npc.setEntityClass("dwarfguardnpc");
		npc.setDescription("Hunel jest przyjaznym krasnoludem. Jak on tu wszedł i dlaczego on jest przestraszony?");
		npc.setPosition(10, 23);
		npc.setPerceptionRange(7);
		npc.initHP(100);
		zone.add(npc);
	}
}
