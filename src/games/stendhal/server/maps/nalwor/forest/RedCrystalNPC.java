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
package games.stendhal.server.maps.nalwor.forest;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.CrystalNPCBase;

/**
 * A crystal NPC
 *
 * @author AntumDeluge
 *
 */
public class RedCrystalNPC implements ZoneConfigurator {
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
		final CrystalNPCBase crystal = new CrystalNPCBase("Czerwony Kryształ") {
			@Override
			protected void createDialog() {
				addGreeting("Witaj nieznajomy. Miło cię poznać.");
				addHelp("Trzymaj się. Femme Fatale opowiedziała mi o szkole zabójców. Biegają tam dookoła i są nieznośni dla swoich nauczycieli!");
				addJob("Jestem kryształem. Cóż więcej mogę powiedzieć?");
				addGoodbye("Żegnaj. Wróć, gdy będziesz potrzebował mojej pomocy.");
			}
		};

		crystal.setDescription("Oto czerwony kryształ. Patrząc na niego czujesz się trochę poruszony.");
		crystal.setEntityClass("transparentnpc");
		crystal.setAlternativeImage("crystalrednpc");
		crystal.setPosition(44, 75);
		crystal.setResistance(0);
		zone.add(crystal);
	}
}
