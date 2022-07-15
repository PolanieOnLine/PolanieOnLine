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
package games.stendhal.server.maps.gdansk.city;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author KarajuSs
 */

public class MieszkaniecGdanska2NPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildGdanskMieszkancy(zone);
	}

	private void buildGdanskMieszkancy(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Adela") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam, nie jesteś stąd prawda?");
				addJob("Nie mam dla Ciebie żadnego zadania.");
				addHelp("Nie potrzebuję Twojej pomocy.");
				addOffer("Nie mam nic Tobie do zaoferowania... Możesz się przejść do naszej karczmy w Gdańsku, która się znajduje w centrum naszego miasta, tam może znajdziesz coś dla siebie.");
				addReply("Jagoda",
						"Jagoda jest moją małą córeczką, nie pozwalam jej się zadawać z obcymi...");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Adela, mama Jagody. Nie wygląda na taką osobę aby chciała chwilę porozmawiać.");
		npc.setEntityClass("woman_003_npc");
		npc.setGender("F");
		npc.setPosition(111, 103);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
