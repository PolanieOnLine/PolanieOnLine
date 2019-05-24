/* $Id$ */
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
package games.stendhal.server.maps.kotoch;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

public class SmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKotochSmitherArea(zone);
	}

	private void buildKotochSmitherArea(final StendhalRPZone zone) {
		final SpeakerNPC smith = new SpeakerNPC("Vulcanus") {

			@Override
			// he doesn't move.
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Szacum wodzu! Jestem Vulcanus kowal.");
				addGoodbye("Na razie");
				addHelp("Mógłbym Ci pomóc w zdobyciu #specjalnego przedmiotu...");
				addJob("Używałem do wykuwania broni dla Króla Faiumoni, ale było to dawno temu. Teraz droga jest zablokowana.");
				
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("special", "specjalnego"),
				        null,
				        ConversationStates.ATTENDING,
				        "Kto Ci to powiedział!?! *kaszlnięcie* W każdym razie tak. Mogę wykuć dla Ciebie specjalny przedmiot, ale będziesz musiał wykonać #zadanie",
				        null);
			}
		};

		smith.setDescription("Oto Vulcanus. Czujesz się dziwnie stojąc w jego pobliżu.");
		smith.setEntityClass("transparentnpc");
		smith.setAlternativeImage("vulcanus");
		smith.setPosition(62, 115);
		smith.setDirection(Direction.DOWN);
		smith.initHP(100);
		zone.add(smith);
	}
}
