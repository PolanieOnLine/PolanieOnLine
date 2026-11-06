/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/** Passable investigation objects on the existing first Semos mine map. */
public final class MineTrailSites implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		for (MineTrailEvidence.Kind kind : MineTrailEvidence.Kind.values())
			zone.add(new MineTrailEvidence(kind));
		Creature courier = SingletonRepository.getEntityManager().getCreature(
				MineTrailProgress.COURIER);
		if (courier == null)
			throw new IllegalStateException("Missing duergar courier");
		zone.add(new CreatureRespawnPoint(zone, MineTrailProgress.COURIER_X,
				MineTrailProgress.COURIER_Y, courier, 1));
	}
}
