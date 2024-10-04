/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.fado.forest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedBuyerBehaviour;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;

/**
 * Builds an albino elf NPC .
 * He is a trader and takes part in a quest (maps/quests/ElvishArmor.java)
 *
 * @author kymara
 */
public class CraftsmanNPC implements ZoneConfigurator {
	private final ShopsList shops = SingletonRepository.getShopsList();

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
		final SpeakerNPC npc = new SpeakerNPC("Lupos") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(3, 11));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 2));
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 6));
				nodes.add(new Node(3, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
			    //addGreeting("Co cię sprowadza do lasu, na południe od rzeki Or'ril.");
				addJob("Jestem rzemieślnikiem. Pewnego dnia, mam nadzieję, uda mi się wytworzyć taki przedmiot jaki robią zielone elfy.");
				addHelp("Mój przyjaciel Orchiwald jest największym gadułą. Mógłby z tobą rozmawiać godzinami o albino elfach i jak tutaj się dostaliśmy.");
				new BuyerAdder().addBuyer(this, new QuestCompletedBuyerBehaviour("elvish_armor",
						"W tej chwili nie mogę nic od ciebie kupić.",
						shops.get("buyelvish", ShopType.ITEM_BUY)), false);
				addGoodbye("Do widzenia.");
			}
		};

		npc.setDescription("Oto Lupos. Jak widzisz jest albino-elfem.");
		npc.setEntityClass("albinoelfnpc");
		npc.setGender("M");
		npc.setPosition(3, 11);
		zone.add(npc);
	}
}
