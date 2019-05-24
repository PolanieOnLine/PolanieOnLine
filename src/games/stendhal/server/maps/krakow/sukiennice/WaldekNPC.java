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
public class WaldekNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Waldek") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Dzień dobry");
				addHelp("Skupuję różne owoce. Na stole leży książka, w niej jest moja oferta");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyowoce")), false);
				addOffer("Skupuję owoce, oferta moja jest w książce.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Waldek");
		npc.setEntityClass("man_007_npc");
		npc.setPosition(26, 17);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(24, 15);
		book.setText(" -- Skup -- \n wisienka\t\t 8\n jabłko\t\t 5\n jabłko niezgody\t 15\n poziomka\t\t 5\n truskawka\t\t 8\n gruszka\t\t 6\n kokos\t\t 7\n ananas\t\t 8");
		book.setEntityClass("book_red");
		book.setResistance(10);
		zone.add(book);
	}
}
