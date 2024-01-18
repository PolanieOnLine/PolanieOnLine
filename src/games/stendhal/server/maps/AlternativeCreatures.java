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
	/**
	 * Configures the zone with alternative creatures based on the provided attributes.
	 *
	 * @param zone       The StendhalRPZone to be configured.
	 * @param attributes A map containing attributes for configuring the zone.
	 */
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

	/**
	 * Adds a special item drop to the given creature based on the provided attributes.
	 *
	 * @param monster The creature to receive the special item drop.
	 * @param attr    A map containing attributes for configuring the special item drop.
	 */
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

	/**
	 * Adjusts the attributes of the given creature based on the provided factor.
	 *
	 * @param c      The creature to be modified.
	 * @param factor The factor by which to adjust the creature's attributes.
	 */
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

	/**
	 * Calculates a new stat value based on the original value and a given factor.
	 *
	 * @param value         The original stat value.
	 * @param factor        The factor by which to adjust the stat.
	 * @return              The adjusted stat value.
	 */
	private int calculateStat(int value, double factor) {
		return calculateStat(value, factor, false);
	}

	/**
	 * Calculates a new stat value based on the original value, a given factor, and an option to check for the maximum value.
	 *
	 * @param value          The original stat value.
	 * @param factor         The factor by which to adjust the stat.
	 * @param checkMaxValue  A flag indicating whether to check for the maximum value.
	 * @return               The adjusted stat value.
	 */
	private int calculateStat(int value, double factor, boolean checkMaxValue) {
		int result = (int) (value * (1 + factor));
		if (checkMaxValue && result > Short.MAX_VALUE) {
			return Short.MAX_VALUE;
		}
		return result;
	}

	/**
	 * Checks if the provided attributes contain valid creature information for spawning.
	 *
	 * @param attr A map containing attributes to be validated.
	 * @return     True if the attributes are valid for creature spawning, otherwise false.
	 */
	private boolean isCreatureValid(final Map<String, String> attr) {
		return attr.containsKey("creature") && attr.containsKey("spawnX") && attr.containsKey("spawnY");
	}
}
