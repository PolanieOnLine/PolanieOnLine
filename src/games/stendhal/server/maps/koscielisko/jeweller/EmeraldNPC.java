/***************************************************************************
 *                 (C) Copyright 2003-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.koscielisko.jeweller;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class EmeraldNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMieszek(zone);
	}

	private void buildMieszek(final StendhalRPZone zone) {
		final SpeakerNPC mieszek = new SpeakerNPC("czeladnik Mieszek") {
			@Override
			protected void createDialog() {
				addHelp("Nie potrzebuję pomocy, ale możesz ty pomóc nam... Mistrz planuje rozbudowe naszego placu więc mogę #'oszlifować' szmaragd za drobną opłatą.");
				addReply("mistrz",
						"Mistrz?? Tak, tak znam mistrza. Co potrzeba?");
				addReply("szmaragd",
						"Aaaa... mam obrobić kryształ szmaragdu. A może by tak ładnie poprosić #☺ coś w rodzaju #'oszlifuj szmaragd'.");    
				addReply("money",
						"☺ Drogo? Nie drogo, mistrz Drogosz ma zamiar podnieść i tak cenę. Więc nie ma co się zastanawiać.");
				addGoodbye();
			}
		};

		mieszek.setDescription("Oto Mieszek, który wygląda na czeladnika. Ciekawe jakimi kamieniami się zajmuje.");
		mieszek.setEntityClass("recruiter1npc");
		mieszek.setGender("M");
		mieszek.setPosition(27, 29);
		mieszek.setDirection(Direction.LEFT);
		zone.add(mieszek);
	}
}
