/***************************************************************************
 *                    (C) Copyright 2024 - PolanieOnLine                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

public class AlternativeCreatures implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		double factor = 0.1;
		if (attributes.containsKey("factor")) {
			factor = Double.parseDouble(attributes.get("factor"));
		}

		for (CreatureRespawnPoint spawner : zone.getRespawnPointList()) {
			Creature c = spawner.getPrototypeCreature();
			// Rare & abnormal & immortal creatures should not count.
			if (c.isAbnormal()) {
				continue;
			}
			c.setName("alt " + c.getName());
			c.setAtk(configureStats(c.getAtk(), factor));
			c.setDef(configureStats(c.getDef(), factor));
			c.setBaseHP(configureStats(c.getBaseHP(), factor));
			c.setHP(configureStats(c.getHP(), factor));
			c.setXP(configureStats(c.getXP(), factor));
			c.setLevel(configureStats(c.getLevel(), factor));
		}
	}

	private int configureStats(int value, double factor) {
		return (int) (value * (1 + factor));
	}
}
