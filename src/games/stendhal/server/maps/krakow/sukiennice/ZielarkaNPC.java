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
public class ZielarkaNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Zielarka") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Witaj");
				addHelp("Skupuję zioła. Na stole leży książka, w niej są ceny i rodzaje ziół, które kupię.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyziola")), false);
				addOffer("Skupuję zioła, oferta moja jest w książce.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Zielarka. Wie wszystko o ziołach.");
		npc.setEntityClass("confectionerapplepienpc");
		npc.setPosition(15, 44);
		npc.setDirection(Direction.UP);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(16, 42);
		book.setText(" -- Skup ziół -- \n arandula\t 12\n kekik\t 28\n sclaria\t 28\n kokuda\t 250");
		book.setEntityClass("book_red");
		book.setResistance(10);
		zone.add(book);
	}
}
