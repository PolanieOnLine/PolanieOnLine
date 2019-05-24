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
package games.stendhal.server.maps.krakow.planty;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;

/*
 * Food and drink seller,  Inside Semos Tavern - Level 0 (ground floor)
 * Sells the flask required for Tad's quest IntroducePlayers
 */
public class EdragonNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("eDragon") {

			@Override
			protected void createPath() {
				// NPC does not move
				setPath(null);
			}
			@Override
			protected void createDialog() {
				addGreeting(null, new SayTextAction("Witaj [name]! Mam nadzieję że nie jesteś szpiegiem w takim wypadku musiałbym cię pożreć."));
				addJob("Nie mam żadnej pracy dla ciebie... Ale możesz zrobić #zadanie na pierścień barona");
				addHelp("Mi pomóc? Ha Ha .. Czekaj możesz zrobić #zadanie na pierścień barona.");
				addGoodbye("Powodzenia!");
			}
		};
		
    npc.setDescription("Oto smok eDragon, uciekł z siedziby przed jej zburzeniem.");
		npc.setEntityClass("npc_eDragon");
		npc.setPosition(123, 71);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
