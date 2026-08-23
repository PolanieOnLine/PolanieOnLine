/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.dragon.cave;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/** Creates eFuR in the dragon caves. */
public class eFuRNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("eFuR") {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj, wojowniku. Pilnuję tych podziemi, ale znam też magię, która potrafi całkowicie usunąć zapis niektórych zadań.");
				addJob("Pilnuję koboltów w podziemiach. Za odpowiednią opłatą świadczę też usługę #anulowania wybranych zadań.");
				addHelp("Jeśli naprawdę chcesz rozpocząć wybrane zadanie od początku, powiedz #zadanie. Najpierw wybierzesz zadanie i poznasz cenę. Niczego nie usunę, dopóki osobno nie powiesz #potwierdzam.");
				addGoodbye("Do widzenia. Pamiętaj, że anulowanie zadania oznacza utratę całego zapisanego postępu.");
			}
		};

		npc.setDescription("Oto eFuR, smok pilnujący podziemi i oferujący odpłatne anulowanie wybranych zadań.");
		npc.setEntityClass("dragon3npc");
		npc.setGender("M");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(8, 141);
		zone.add(npc);
	}
}
