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
import games.stendhal.server.entity.mapstuff.sign.Sign;
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
			protected void createPath() {
				setPath(null);
			}

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

		npc.setDescription("Oto Przekupka");
		npc.setEntityClass("curatornpc");
		npc.setPosition(26, 11);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);

		final Sign book = new Sign();
		book.setPosition(24, 10);
		book.setText(" -- Kupię -- \n marchew\t\t 2\n sałata\t\t 2\n pieczarka\t\t 4\n pomidor\t\t 5\n kapusta\t\t 6\n opieńka miodowa\t 6\n borowik\t\t 6\n szpinak\t\t 6\n brokuł\t\t 7\n por\t\t 7\n kalafior\t\t 8\n cebula\t\t 8\n cukinia\t\t 10");
		book.setEntityClass("book_blue");
		book.setResistance(10);
		zone.add(book);
	}
}
