/***************************************************************************
 *                   (C) Copyright 2022 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tatry.mountain;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

public class ColdNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ogi") {
			@Override
			protected void createDialog() {
				addGreeting("Cz...cz...eść.");
				addJob("M...mm...uszę się r...r...ozg...rzać.");
				addHelp("M...mm...uszę się r...r...ozg...rzać.");
				addGoodbye("By...by...waj.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setEntityClass("npcstarygoral");
		npc.setGender("M");
		npc.setPosition(91, 5);
		npc.setDirection(Direction.RIGHT);
		zone.add(npc);
	}
}
