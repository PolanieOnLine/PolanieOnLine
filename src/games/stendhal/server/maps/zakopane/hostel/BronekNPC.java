/* $Id: BronekNPC.java,v 1.7 2012/09/21 02:28:01 Legolas Exp $ */
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
// Base on ../games/stendhal/server/maps/ados/barracks/BuyerNPC.java
package games.stendhal.server.maps.zakopane.hostel;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * Builds an NPC to buy previously un bought armor.
 *
 * @author kymara
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
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Skupuję smocze pazury.");
				addHelp("Chętnie od ciebie kupię pazury smoków, bo słyszałem, że gdzieś za siedmioma lasami można będzie z niego wykonać talizman! Jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Na tablicy masz napisane jakie przedmioty skupuję.");
				addQuest("O, dziękuję, ale niczego już nie potrzebuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buydragonitems")), false);
				addGoodbye("Dowidzenia kolego.");
			}
		};

		npc.setDescription("Oto Bronek, wygląda na porządnego górala.");
		npc.setEntityClass("npcbronek");
		npc.setPosition(16, 18);
		npc.initHP(100);
		zone.add(npc);

		final Sign tablica = new Sign();
		tablica.setPosition(16, 15);
		tablica.setText(" -- Możesz sprzedać te rzeczy Bronkowi -- \n pazury wilcze\t 5\n pazury niedźwiedzie\t 8\n pazury tygrysie\t 100\n pazur zielonego smoka\t 5000\n"+
						" pazur niebieskiego smoka\t 5000\n pazur czerwonego smoka\t 5000\n pazur czarnego smoka\t 10000\n pazur złotego smoka\t 15000");
		tablica.setEntityClass("blackboard");
		tablica.setResistance(10);
		zone.add(tablica);

	}
}
