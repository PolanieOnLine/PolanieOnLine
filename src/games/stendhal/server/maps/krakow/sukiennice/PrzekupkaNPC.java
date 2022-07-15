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
// Based on ../games/stendhal/server/maps/kirdneh/museum/CuratorNPC.java
package games.stendhal.server.maps.krakow.sukiennice;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class PrzekupkaNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Przekupka") {
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Dzień dobry, co potrzeba?");
				addHelp("Kupuję różne warzywa.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buywarzywa")), false);
				addOffer("W książce na stole jest napisane co skupuję.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Przekupka, która skupuje warzywa.");
		npc.setEntityClass("curatornpc");
		npc.setGender("F");
		npc.setPosition(26, 11);
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}
}
