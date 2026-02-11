/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import java.util.List;

import games.stendhal.server.entity.creature.Creature;

public interface MapEventSpawnStrategy {
	void spawnCreatures(String eventName, List<String> zones, String creatureName, int count,
			SpawnedCreatureHandler spawnedCreatureHandler);

	interface SpawnedCreatureHandler {
		void onSpawned(Creature creature);
	}
}
