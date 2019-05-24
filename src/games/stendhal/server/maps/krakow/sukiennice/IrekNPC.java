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
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/*
 * Inside Semos Tavern - Level 0 (ground floor)
 */
public class IrekNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Irek") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jak każda inna, aby tylko byli klienci.");
				addHelp("Sprzedaję różne warzywa. Na stole leży książka, w niej jest moja oferta");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellwarzywairek")), false);
				addOffer("Sprzedaję warzywa, oferta moja jest w książce.");
				addQuest("Nie mam głowy do zadań.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto badylarz Irek");
		npc.setEntityClass("man_000_npc");
		npc.setPosition(26, 4);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(24, 4);
		book.setText(" -- Sprzedam -- \n marchew\t 5\n sałata\t 10\n pomidor\t 20\n kapusta\t 25\n cukinia\t 30\n borowik\t 35");
		book.setEntityClass("book_red");
		book.setResistance(10);
		zone.add(book);
	}
}
