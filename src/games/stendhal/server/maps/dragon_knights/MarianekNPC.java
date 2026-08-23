/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.dragon_knights;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.quests.socialstatusrings.MieszczaninFinale;

public class MarianekNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Marianek") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj. Jeśli przyszedłeś po robotę albo starą opowieść, mów śmiało.");
				addJob("Jestem kowalem. Naprawiam broń, obrabiam metal i pamiętam kilka zwyczajów starszych niż ta kuźnia. O jednym z nich opowiadam przy #zadaniu.");
				addHelp("Dobra stal lubi cierpliwość. Z ludźmi bywa podobnie.");
				addReply("ciupaga", "Złota ciupaga to porządna próba rzemiosła. Jeśli jeszcze jej nie ukończyłeś, porozmawiaj z Andrzejem i doprowadź jego pracę do końca, potem wróć do mnie.");
				addGoodbye("Niech ci droga służy.");
			}
		};

		npc.setDescription("Oto Marianek, doświadczony kowal zbrojowni Dragon Knights.");
		npc.setEntityClass("blacksmithnpc");
		npc.setGender("M");
		npc.setPosition(5, 4);
		zone.add(npc);
		MieszczaninFinale.attach(npc);
	}
}
