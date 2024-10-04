/***************************************************************************
 *                 (C) Copyright 2018-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.shop;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;

/**
 * @author KarajuSs
 */
public class StasekNPC implements ZoneConfigurator {
	private final ShopsList shops = SingletonRepository.getShopsList();

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
		final SpeakerNPC npc = new SpeakerNPC("Stasek") {
			@Override
			protected void createDialog() {
				addGreeting("Przyjacielu, w czym mogę #pomóc.");
				addHelp("Zapytaj mnie o #zadanie, a może będziesz w stanie mi pomóc...");
				addJob("Zarządzam tym sklepem z uzbrojeniem w Zakopcu.");
				addGoodbye("Miło było Cię poznać.");
				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour("wegiel_na_opal",
						"Musiałbyś wykonać moje #'zadanie', abym mógł tobie zaufać!",
						shops.get("stasek", ShopType.ITEM_SELL)), false);
			}
		};

		npc.setDescription("Oto Stasek. Prowadzi bardzo atraktycjny sklep.");
		npc.setEntityClass("barman3npc");
		npc.setGender("M");
		npc.setPosition(5, 6);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
