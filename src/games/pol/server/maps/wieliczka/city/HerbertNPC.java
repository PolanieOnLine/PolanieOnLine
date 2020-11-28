/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.pol.server.maps.wieliczka.city;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author zekkeq
 */
public class HerbertNPC implements ZoneConfigurator {
    @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Herbert") {

			@Override
			public void createDialog() {
				addGreeting();
				addHelp("Dzięki, że się pytasz. Właśnie potrzebuję czyjejś pomocy.");
				addJob("Aktualnie niczym się nie zajmuję.");
                addOffer("Nie mam nic do zaoferowania. Spytaj się mojego przyjaciela #Arthura o ofertę.");
				addGoodbye();
			}

			@Override
			protected void createPath() {
				setPath(null);
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setPosition(40, 100);
		npc.setEntityClass("holidaymakernpc");
		npc.setDescription("Oto Herbert. Wygląda jakby potrzebował pomocy.");
		npc.setDirection(Direction.RIGHT);
		zone.add(npc);
	}
}
