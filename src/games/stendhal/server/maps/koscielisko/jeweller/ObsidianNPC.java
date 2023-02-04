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

public class ObsidianNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSobek(zone);
	}

	private void buildSobek(final StendhalRPZone zone) {
		final SpeakerNPC sobek = new SpeakerNPC("czeladnik Sobek") {
			@Override
			protected void createDialog(){
				addHelp("Jeżeli chcesz nam pomóc to lepiej zapytaj czy chcesz abym #'oszlifował obsydian'. Za opłatą oczywiście.");
				addReply("mistrz",
						"A wysłał Wielmożność do mnie w sprawie obsydianu. Obrobię go wystarczy, że powiecie #'oszlifuj obsydian'.");
				addReply("money",
						"Mistrz ustala ceny.");
				addGoodbye();
			}
		};

		sobek.setDescription("Oto Sobek. Wyglądem przypomina czeladnika, ciekawe jakimi kamieniami się zajmuje.");
		sobek.setEntityClass("youngnpc");
		sobek.setGender("M");
		sobek.setPosition(4, 29);
		sobek.setDirection(Direction.RIGHT);
		zone.add(sobek);
	}
}
