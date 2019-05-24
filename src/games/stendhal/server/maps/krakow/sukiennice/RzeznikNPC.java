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
// Based on ../games/stendhal/server/maps/amazon/hut/JailedBarbNPC.java
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
public class RzeznikNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Rzeźnik") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Dobry dzień");
				addHelp("Tak możesz pomóc sprzedając mi jakiekolwiek mięsiwo.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buymieso")), false);
				addOffer("Oferta moja jest w książce przede mną.");
				addGoodbye("Żegnam");
			}
		};

		npc.setDescription("Oto Rzeźnik jakiś taki gburowaty.");
		npc.setEntityClass("jailedbarbariannpc");
		npc.setPosition(26, 43);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(24, 43);
		book.setText(" -- Skup mięsa -- \n udko\t 12\n mięso\t 15\n szynka\t 25\n stek\t 30");
		book.setEntityClass("book_blue");
		book.setResistance(10);
		zone.add(book);
	}
}