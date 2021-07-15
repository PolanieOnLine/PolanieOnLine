/***************************************************************************
 *                   (C) Copyright 2003-2020 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.zakopane.hostel;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * v 1.8 2020/05/09 06:41:20
 *
 * @author Legolas
 * 		@edit by ZEKKEQ
 */
public class BronekNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Bronek") {

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Skupuję smocze pazury.");
				addHelp("Chętnie od ciebie kupię pazury smoków, bo słyszałem, że gdzieś za siedmioma lasami można będzie z niego wykonać talizman! Jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Na tablicy masz napisane jakie przedmioty skupuję.");
				addQuest("O, dziękuję, ale niczego już nie potrzebuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buydragonitems")), false);
				addGoodbye("Do widzenia kolego.");
			}
		};

		npc.setDescription("Oto Bronek, wygląda na porządnego górala.");
		npc.setEntityClass("npcbronek");
		npc.setGender("M");
		npc.setPosition(16, 18);
		zone.add(npc);
	}
}
