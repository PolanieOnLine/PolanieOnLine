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
package games.stendhal.server.maps.ados.snake_pit;

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
public class PurpleCrystalNPC implements ZoneConfigurator {
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
		final CrystalNPCBase crystal = new CrystalNPCBase("Purpurowy Kryształ") {
			@Override
			protected void createDialog() {
				addGreeting("Atmosfera w tych poziemiach przyprawia mnie o ciarki...");
				addHelp("Te węże poniżej są straszne! Uważaj na królewską kobrę!");
				addJob("Jestem kryształem. Cóż więcej mogę powiedzieć?");
				addGoodbye("Żegnaj. Wróć o mnie jeżeli będziesz potrzebował mojej pomocy.");
			}
		};

		crystal.setDescription("Oto purpurowy kryształ. Jest w tym coś niesamowitego.");
		crystal.setEntityClass("transparentnpc");
		crystal.setAlternativeImage("crystalpurplenpc");
		crystal.setPosition(47, 64);
		crystal.setResistance(0);
		zone.add(crystal);
	}
}
