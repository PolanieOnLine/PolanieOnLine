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
public class RybakMiloradNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Rybak Miłorad") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Uprzejmie witam wielmożność.");
				addHelp("Skupuję ryby. W książce przede mną są ceny i rodzaje ryb, które kupuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyryby")), false);
				addOffer("Kupię ryby, oferta moja jest w książce.");
				addGoodbye("Dowidzenia. Miłego dnia życzę.");
			}
		};

		npc.setDescription("Oto Rybak Miłorad. Jest bardzo uprzejmy.");
		npc.setEntityClass("man_008_npc");
		npc.setPosition(26, 36);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(24, 38);
		book.setText(" -- Skup -- \n dorsz\t\t 6\n palia alpejska\t 7\n roach\t\t 7\n makrela\t\t 8\n okoń\t\t 9\n pokolec\t\t 9\n pstrąg\t\t 9\n błazenek\t\t 11");
		book.setEntityClass("book_blue");
		book.setResistance(10);
		zone.add(book);
	}
}
