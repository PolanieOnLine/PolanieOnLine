/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.warszawa.castle;

import java.util.LinkedList;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SilentNPC;

public class WarsawArmy implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPCs(zone);
	}

	private void buildNPCs(StendhalRPZone zone) {
		final LinkedList<SilentNPC> npclist = new LinkedList<SilentNPC>();
		for(int i=0; i<6; i++) {
			for(int j=0; j<3; j++) {
				final SilentNPC npc = new SilentNPC();
				//npc.setIdea("defence");
				npc.setEntityClass("youngsoldiernpc");
				npc.setDescription("Oto armia rycerzy.");
				npc.setGender("M");
				npc.setPosition(89+(i+i), 87+(j+j));
				npc.setDirection(Direction.UP);
				npc.setName("Rycerze");
				zone.add(npc);
				npclist.add(npc);
			}
		}
	};
}
