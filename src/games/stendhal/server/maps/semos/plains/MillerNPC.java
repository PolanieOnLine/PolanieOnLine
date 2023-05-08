/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.plains;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SeedSellerBehaviour;

/**
 * The miller (original name: Jenny). She mills flour for players who bring
 * grain.
 */
public class MillerNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Jenny") {
			@Override
			public void createDialog() {
				addJob("Prowadzę młyn, w którym mielę #kłosy na mąkę. Zaopatruję też piekarnię w Semos. Powiedz tylko #zmiel.");
				addReply("kłosy",
				        "Niedaleko jest farma. Zazwyczaj pozwalają ludziom tam zbierać kłosy zboża. Oczywiście potrzebujesz #kosy do ich ścięcia.");
				addReply("kosy",
					    "Kuźnia jest miejscem gdzie możesz ją dostać");
				addHelp("Czy znasz piekarnię w Semos? Z dumą mogę powiedzieć, że używają mojej mąki. Ale ostatnio wilki znowu zjadły mojego dostawcę... albo może uciekł... hmm.");
				addGoodbye();
				addOffer("Możesz #zasadzić moje nasiona, aby wyrosły z nich piękne kwiatki.");
				addReply(Arrays.asList("plant", "zasadzić"),
						"Twoje nasiona powinny zostać zasiane na żyznym gruncie. Szukaj brązowej ziemi nie daleko ścieżki koło której rośnie arandula na równinach semos. Nasiona będą tam kwitnąć. Możesz codziennie doglądać jak rośnie twój kwiatek. Gdy urośnie to będziesz mógł go zerwać. Obszar jest dostępny dla każdego i istniej prawdopodobieństwo, że ktoś inny zerwie twój kwiatek, ale na szczęście nasiona są tanie!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		new SellerAdder().addSeller(npc, new SeedSellerBehaviour());

		npc.setDescription("Oto Jenny. Pracuje w młynie.");
		npc.setEntityClass("woman_003_npc");
		npc.setGender("F");
		npc.setPosition(19, 39);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
