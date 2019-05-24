/* $Id: StableNPC.java,v 1.0 2018/07/01 21:30:51 KarajuSs Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.stable;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Build the StableNPC
 *
 * @author KarajuSs
 */
public class StableNPC implements ZoneConfigurator {

	 @Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Stajenny Marcel") {

			@Override
			protected void createDialog() {
				addGreeting("Oooh... Witoj bohaterze w mej stajni!");
				addJob("Mo praca? Jestem stajennym i zajmuje się końmi.");
				addHelp("Możesz pomóc w uzdrowieniu mych koni.. Są bardzo chore. Przidź do weterynarza ino wyżej i zapytoj co potrzebuje! Bo jo na tym się nie znom..");
				addGoodbye();
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.RIGHT);
			}
		};

		npc.setEntityClass("hanknpc");
		npc.setPosition(26, 21);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
