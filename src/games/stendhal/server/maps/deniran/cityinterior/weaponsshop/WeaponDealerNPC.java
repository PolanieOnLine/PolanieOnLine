/***************************************************************************
 *                      (C) Copyright 2019 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.weaponsshop;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.mapstuff.sign.ShopSign;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

public class WeaponDealerNPC implements ZoneConfigurator  {
	private final ShopList shops = ShopList.get();

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
		buildSigns(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("D J Smith") {
			@Override
			public void createDialog() {
				addGreeting("Witaj w miejscowej zbrojowni.");
				addJob("Zajmuje się sprzedażą broni.");
				addOffer("Sprawdź moje ceny na tablicach.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("deniranequipbuy")), false);
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("deniranequipsell")), false);
				addGoodbye();
			}
	
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(29, 5));
				nodes.add(new Node(11, 5));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.setDescription("Oto D J Smith, sprzedawca broni.");
		npc.setEntityClass("wellroundedguynpc");
		npc.setGender("M");
		npc.setPosition(11, 5);
		zone.add(npc);
	}

	private void buildSigns(final StendhalRPZone zone) {
		final ShopSign buys = new ShopSign("deniranequipbuy", "Sklep D J Smith (skupuje)", "Możesz sprzedać te rzeczy u D J Smith.", false);
		buys.setEntityClass("blackboard");
		buys.setPosition(20, 1);

		final ShopSign sells = new ShopSign("deniranequipsell", "Sklep D J Smith (sprzedaje)", "Możesz kupić te rzeczy od D J Smith.", false);
		sells.setEntityClass("blackboard");
		sells.setPosition(21, 1);

		zone.add(buys);
		zone.add(sells);
	}
}
