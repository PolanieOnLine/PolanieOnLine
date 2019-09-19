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
package games.stendhal.server.maps.wieliczka.city;

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
 * @author zekkeq
 */
public class FishMarket implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Walery") {

			@Override
			public void createDialog() {
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyfishes")), false);
				addGreeting("Witaj rycerzu. W czym mogę #pomóc?");
				addHelp("Nie potrzebuję pomocy, ale możesz spojrzeć na tablicę, która znajduje się po mojej lewej.");
				addJob("Nie marnuj mego czasu, jak masz coś to sprzedaj mi to!");
				addOffer("Sprawdź na tablicy ile dublonów mogę dać.");
				addQuest("Nie masz towaru, którego ja potrzebuję.");
				addGoodbye("Arrgh, zostań pójdę z tobą!");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(113, 43));
				nodes.add(new Node(116, 43));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setPosition(116, 43);
		npc.setEntityClass("sailor1npc");
		npc.setDescription("Oto Walery. Skupuje ryby.");
		zone.add(npc);
	}
}
