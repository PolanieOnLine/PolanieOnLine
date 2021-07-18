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
package games.stendhal.server.maps.deniran.cityinterior.castle;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

public class KingNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Król Edward") {
			@Override
			public void createDialog() {
				addGreeting("Witaj w zamku Deniran.");
				addJob("Jesteśmy królestwem!");
				addQuest("W tej chwili nie mam dla ciebie nic. Lecz... Krążą pogłoski o przekleństwach, które kopały jaskinie pod miastem. Prawdopodobnie będę potrzebować twojej pomocy w przyszłości.");
				addGoodbye("Żegnaj, nieznajomy!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto król Deniran, Edward.");
		npc.setEntityClass("deniran_king");
		npc.setGender("M");
		npc.setPosition(14,7);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
		
		buildShops(npc);
	}

	private void buildShops(final SpeakerNPC npc) {
		new SellerAdder().addSeller(npc, new SellerBehaviour(ShopList.get().get("denirankingsell")));
	}
}
