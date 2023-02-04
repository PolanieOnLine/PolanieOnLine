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

public class SapphireNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKrzesim(zone);
	}

	private void buildKrzesim(final StendhalRPZone zone) {
		final SpeakerNPC krzesim = new SpeakerNPC("czeladnik Krzesim") {
			@Override
			protected void createDialog() {
				addHelp("Pomożesz, jeśli za opłatą będziesz chciał #'oszlifować szafir'.");
				addReply("mistrz",
						"Mistrz już mi mówił. Mam obrobić kryształ #szafiru.");
				addReply("szafiru",
						"Zrobię to bez problemu. Proszę powiedzieć tylko #'oszlifuj szafir'.");
				addReply("money",
						"Z tym pytaniem proszę zwrócić się do mistrza Drogosza.");
				addReply("kryształ szafiru",
						"Mistrza trzeba zapytać. On wie co, gdzie i jak.");
				addGoodbye();
			}
		};

		krzesim.setDescription("Oto Krzesim, wygląda na czeladnika. Ciekawe jakimi kamieniami się zajmuje.");
		krzesim.setEntityClass("man_001_npc");
		krzesim.setGender("M");
		krzesim.setPosition(27, 17);
		krzesim.setDirection(Direction.LEFT);
		zone.add(krzesim);
	}
}
