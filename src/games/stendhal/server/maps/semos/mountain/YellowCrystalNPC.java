/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.mountain;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Map;

/**
 * A crystal NPC
 * 
 * @author AntumDeluge
 *
 */
public class YellowCrystalNPC implements ZoneConfigurator {
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
		
		// Create the NPC
		final SpeakerNPC crystal = new SpeakerNPC("Żółty Kryształ") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj mam nadzieję, że korzystasz z dóbr natury.");
				addHelp("W okolicach gór jest pięknie wyglądająca wieża. Jest wielka!");
				addJob("Jestem kryształem. Co mogę więcej powiedzieć?");
				addGoodbye("Żegnaj i wróć, gdy będziesz potrzebował mojej pomocy.");   
				
			}
		};

		crystal.setEntityClass("transparentnpc");
		crystal.setAlternativeImage("crystalyellownpc");
		crystal.setPosition(76, 16);
		crystal.initHP(100);
		crystal.setDescription("Oto żółty kryształ. Co za energetyzujący widok.");
		crystal.setResistance(0);
		
		zone.add(crystal);
	}
	
}
