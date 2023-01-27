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
package games.stendhal.server.maps.krakow.tavern;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */
public class KuglarzNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Jan Kuglarz") {
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Kiedyś chciałem zostać czarodziejem, ale okazało się, że nie mam do tego talentu... Dlatego też staram się wgłębiać tajniki magii!");
				addOffer("Skupię od Ciebie #'magię ziemi', #'magię płomieni', #'magię deszczu', #'magię lodu', #'magię światła' oraz #'magię mroku'.");
				addGoodbye("Nie zapomnij dostarczyć dla mnie magii!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto Jan Kuglarz. Wygląda na osobę, która interesuje się magią.");
		npc.setEntityClass("npcmagik");
		npc.setGender("M");
		npc.setPosition(12, 13);
		npc.setDirection(Direction.UP);
		zone.add(npc);
	}
}
