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
package games.stendhal.server.maps.krakow.saddler;

import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;

public class SaddlerNPC extends SpeakerNPCFactory {
	@Override
	public void createDialog(final SpeakerNPC npc) {
		npc.addJob("Zajmuję się wyprawianiem skór. Przynieś mi skórę zwierzęcą, a uszyję Tobie bukłak na wodę.");
		npc.addReply("skóra zwierzęca",
					"Polując na różne zwierzęta w końcu ją zdobędziesz.");
		npc.addHelp("Wyprawiam skóry i szyje bukłaki. Powiedz tylko #'uszyj pusty bukłak'.");
		npc.addGoodbye("Do widzenia.");

		npc.setGender("M");
	}
}
