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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

public class AlternativeCreatures implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		double factor = 0.1;
		boolean spawnPoint = true;
		if (attributes.containsKey("factor")) {
			factor = Double.parseDouble(attributes.get("factor"));
		}
		if (isCreatureValid(attributes)) {
			spawnPoint = false;
			final Creature monster = SingletonRepository.getEntityManager().getCreature(attributes.get("creature"));
			int x = Integer.parseInt(attributes.get("spawnX"));
			int y = Integer.parseInt(attributes.get("spawnY"));

			creatureAttributes(monster, factor);
			dropSpecialItem(monster, attributes);

			final CreatureRespawnPoint spawnMonster = new CreatureRespawnPoint(zone, x, y, monster, 1);
			zone.add(spawnMonster);
		}

		if (spawnPoint) {
			for (CreatureRespawnPoint spawner : zone.getRespawnPointList()) {
				Creature c = spawner.getPrototypeCreature();
				// Rare & abnormal & immortal creatures should not count.
				if (c.isAbnormal()) {
					continue;
				}
				creatureAttributes(c, factor);
			}
		}
	}

	private void dropSpecialItem(final Creature monster, final Map<String, String> attr) {
		if (attr.containsKey("dropItem") && attr.containsKey("dropRate")) {
			double probability = Double.parseDouble(attr.get("dropRate"));

			int min = 1, max = 1;
			if (attr.containsKey("dropMinMax")) {
				String[] amount = attr.get("dropMinMax").split(",");
				min = Integer.parseInt(amount[0]);
				max = Integer.parseInt(amount[1]);
			}

			monster.addDropItem(attr.get("dropItem"), probability, min, max);
		}
	}

	private void creatureAttributes(Creature c, double factor) {
		c.setName("mgielny " + c.getName());
		c.setAtk(calculateStat(c.getAtk(), factor));
		c.setDef(calculateStat(c.getDef(), factor));
		c.setBaseHP(calculateStat(c.getBaseHP(), factor, true));
		c.setHP(calculateStat(c.getHP(), factor, true));
		c.setXP(calculateStat(c.getXP(), factor));
		c.setLevel(calculateStat(c.getLevel(), factor));

		c.addDropItem("kryształ ciemnolitu", 0.2, 1);
	}

	private int calculateStat(int value, double factor) {
		return calculateStat(value, factor, false);
	}

	private int calculateStat(int value, double factor, boolean checkMaxValue) {
		int result = (int) (value * (1 + factor));
		if (checkMaxValue && result > Short.MAX_VALUE) {
			return Short.MAX_VALUE;
		}
		return result;
	}

	private boolean isCreatureValid(final Map<String, String> attr) {
		return attr.containsKey("creature") && attr.containsKey("spawnX") && attr.containsKey("spawnY");
	}
}
