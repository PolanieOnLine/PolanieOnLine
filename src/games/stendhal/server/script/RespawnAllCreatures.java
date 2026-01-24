/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import marauroa.common.game.IRPZone;

/**
 * Script to force respawn for all creature spawn points in all zones.
 *
 * Usage: /script RespawnAllCreatures.class
 */
public class RespawnAllCreatures extends AbstractAdminScript {
    @Override
    protected void run(final List<String> args) {
        checkNotNull(admin);

        int zonesVisited = 0;
        int spawnPoints = 0;
        for (final IRPZone irpzone : SingletonRepository.getRPWorld()) {
            final StendhalRPZone zone = (StendhalRPZone) irpzone;
            zonesVisited++;
            for (final CreatureRespawnPoint point : zone.getRespawnPointList()) {
                point.spawnNow();
                spawnPoints++;
            }
        }

        sendText("Wymuszono respawn na " + spawnPoints + " punktach w " + zonesVisited + " strefach.");
    }

    @Override
    protected int getMaxParams() {
        return 0;
    }

    @Override
    protected List<String> getParamStrings() {
        return Collections.emptyList();
    }
}
