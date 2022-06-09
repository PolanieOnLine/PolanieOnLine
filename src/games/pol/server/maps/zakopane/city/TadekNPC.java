/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.zakopane.city;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class TadekNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Tadek") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj!");
				addJob("Pracę mogę Tobie załatwić, jeżeli jakieś szukasz.");
				addOffer("Mieszkańcy Zakopane się strasznie obawiają tego miejsca. Dlatego szukam odpowiedniego woja do #zadania.");
				addGoodbye("Uważaj na siebie.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Oto Tadek. Wygląda na wystraszonego z jakiegoś powodu.");
		npc.setEntityClass("npctadeusz");
		npc.setGender("M");
		npc.setPosition(106, 50);
		npc.setDirection(Direction.DOWN);
		npc.setSounds(Arrays.asList("hiccup-01", "sneeze-male-01"));
		zone.add(npc);
	}
}
