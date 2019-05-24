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
package games.stendhal.server.maps.semos.city;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * An old man (original name: Monogenes) who stands around and gives directions
 * to newcomers. He's the brother of RetireeNPC (original name: Diogenes).
 *
 * @see games.stendhal.server.maps.quests.MeetMonogenes
 * @see games.stendhal.server.maps.quests.HatForMonogenes
 */
public class GreeterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Monogenes") {
			@Override
			public void createDialog() {
				addJob("Jam jest starszym bratem Diogenesa i już nie pamiętam czym kiedyś się zajmowałem... Jestem już za stary.");
				addOffer("Daję nowo przybyłym wskazówki, gdzie znajdują się #budynki w Semos. Jak mam zły humor podaję zły kierunek i od razu robi mi się weselej, he he he! A czasami na złość podaję dobry kierunek, ale przecież od tego nikt nie zginie! Ha ha!");
				// All further behaviour is defined in quest classes.
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.LEFT);
			}
			
			
			
		};
		npc.setPosition(27, 43);
		npc.setEntityClass("oldmannpc");
		npc.setDescription("Widzisz Monogenesa. Wygląda bardzo staro, możliwe, że ma dużą wiedzę...");
		npc.setDirection(Direction.LEFT);
		zone.add(npc);
	}

}