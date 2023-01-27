/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.gdansk.city.market;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author zekkeq
 */
public class ArmoryNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Dariusz") {
			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Zajmuje się handlem różnego wyposażenia.");
				addHelp("Spójrz na tablice po mojej lewej, aby zobaczyć co mógłbyś mi sprzedać.");
				addOffer("Spójrz na tablicę, aby zobaczyć moje ceny i co skupuję.");
				addQuest("Zapytaj się Mieczysława, znajduje się on w muzeum.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Dariusz. Zajmuje się handlem wyposażenia.");
		npc.setEntityClass("man_002_npc");
		npc.setGender("M");
		npc.setDirection(Direction.DOWN);
		npc.setPosition(30, 78);
		zone.add(npc);
	}
}
