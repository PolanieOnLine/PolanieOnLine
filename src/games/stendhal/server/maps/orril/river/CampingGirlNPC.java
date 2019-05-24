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
package games.stendhal.server.maps.orril.river;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * Configure Orril River South Campfire (Outside/Level 0).
 */
public class CampingGirlNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildCampfireArea(zone);
	}

	private void buildCampfireArea(final StendhalRPZone zone) {
		final SpeakerNPC sally = new SpeakerNPC("Sally") {

			@Override
			protected void createPath() {
				// NPC does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				//addGreeting();
				addJob("Praca? Jestem tylko małą dziewczynką! Skautem.");
				addHelp("Możesz znaleźć sporo użytecznych rzeczy w lesie na przykład drewno i grzyby. Uważaj, niektóre grzyby są trujące!");
				addGoodbye();
				// remaining behaviour is defined in maps.quests.Campfire.				
			}
		};

		sally.setEntityClass("littlegirlnpc");
		sally.setPosition(40, 61);
		sally.setDirection(Direction.RIGHT);
		sally.initHP(100);
		sally.setDescription("Widzisz Sally. Ona jest córką Leandera, piekarza Semos a obecnie obozuję nad rzeką.");
		zone.add(sally);
	}
}
