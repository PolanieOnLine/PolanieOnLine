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
package games.stendhal.server.maps.fado.hut;

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
public class BlueCrystalNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 * 
	 * @author AntumDeluge
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}
	
	private void buildNPC(final StendhalRPZone zone) {
		
		// Create the NPC
		final SpeakerNPC crystal = new SpeakerNPC("Niebieski Kryształ") {

			@Override
			protected void createPath() {
				// NPC doesn't move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Miło cię poznać! Ta chatka jest wspaniała.");
				addHelp("Lupos zawsze szuka ręcznie robionego elfickiego ekwipunku.");
				addJob("Jestem kryształem. Cóż więcej mogę powiedzieć?");
				addGoodbye("Żegan. Wróć, gdy będziesz potrzebował mojej pomocy.");     
			}
		};

		crystal.setEntityClass("transparentnpc");
		crystal.setAlternativeImage("crystalbluenpc");
		crystal.setPosition(9, 8);
		crystal.initHP(100);
		crystal.setDescription("Oto niebieski kryształ. Jakoś czujesz, że twoje ramiona są lekkie.");
		crystal.setResistance(0);
		
		zone.add(crystal);
	}
	
}
