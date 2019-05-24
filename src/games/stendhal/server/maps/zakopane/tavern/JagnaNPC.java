/* $Id: JagnaNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
package games.stendhal.server.maps.zakopane.tavern;

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
 * Food and drink seller,  Inside Semos Tavern - Level 0 (ground floor)
 * Sells the flask required for Tad's quest IntroducePlayers
 */
public class JagnaNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildJagna(zone);
	}

	private void buildJagna(final StendhalRPZone zone) {
		final SpeakerNPC jagna = new SpeakerNPC("Jagna") {

			@Override
			protected void createPath() {
				// NPC does not move
				setPath(null);
			}
			@Override
			protected void createDialog() {
				addGreeting("Witam miłego gościa. Czy coś podać?");
				addJob("Jestem kelnerką w tej karczmie. Sprzedajemy importowane i lokalne trunki oraz dobre jedzenie. Na deser też coś się znajdzie.");
				addHelp("Karczma ta jest znana w całym Zakopanem. Można tu odpocząć i dobrze zjeść. Jeżeli chcesz poznać naszą #ofertę, to powiedz mi o tym.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("urodziny")));
				addGoodbye("Smacznego i miłej zabawy do samego rana.");
			}
		};

		jagna.setEntityClass("hotelreceptionistnpc");
		jagna.setPosition(25, 3);
		jagna.setDirection(Direction.DOWN);
		jagna.initHP(100);
		zone.add(jagna);

		final Sign book = new Sign();
		book.setPosition(28, 1);
		book.setText(" -- Jagna sprzedaje -- \n sok z chmielu\t 8\n napój z winogron\t 10\n napój z oliwką\t 50\n shake waniliowy\t 100\n shake czekoladowy\t 100\n mięso\t\t 20\n szynka\t\t 30\n hotdog\t\t 120\n"+
					" hotdog z serem\t 140\n kanapka z tuńczykiem\t 110\n kanapka\t\t 110\n tabliczka czekolady\t 80\n lukrecja\t\t 80");
		book.setEntityClass("blackboard");
		book.setResistance(10);
		zone.add(book);
	}
}
