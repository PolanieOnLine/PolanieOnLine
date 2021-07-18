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
package games.stendhal.server.maps.deniran.river;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * A soldier guarding the bridge
 */
public class BridgePostNPC implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Marcellus") {
			@Override
			public void createDialog() {
				addGreeting("Hej! Trzymaj się z daleka! Zostaniesz zabity.");
				addJob("Strzegę mostu. Upewniając się, że żaden bezmyślny cywil nie przejdzie i nie zostanie zabity na polu bitwy.");
				addHelp("Stolica Deniran leży na północ stąd.");
//				addGoodbye("/me shakes his head and mutters to himself: Thoughtless civilians.");
				addGoodbye("Bezmyślni cywile...");
			}
			@Override
			protected void onGoodbye(RPEntity player) {
				super.onGoodbye(player);
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto żołnierz strzegącego mostu.");
		npc.setEntityClass("deniran_stormtrooper");
		npc.setGender("M");
		npc.setDirection(Direction.UP);
		npc.setPosition(65, 25);
		zone.add(npc);
	}
}
