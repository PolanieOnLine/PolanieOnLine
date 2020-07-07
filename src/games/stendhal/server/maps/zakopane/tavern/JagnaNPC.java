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

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
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
		jagna.setSounds(Arrays.asList("hiccup-1", "hiccup-2", "hiccup-3"));
		zone.add(jagna);
	}
}
