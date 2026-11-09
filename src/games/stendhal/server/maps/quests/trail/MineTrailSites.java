/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Passable investigation objects on the existing first Semos mine map. */
public final class MineTrailSites implements ZoneConfigurator {
	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		for (MineTrailEvidence.Kind kind : MineTrailEvidence.Kind.values()) {
			TrailSceneRegistry.prop(zone, "mine-evidence-" + kind.name(),
					player -> MineTrailEvidence.needed(player, kind),
					() -> new MineTrailEvidence(kind));
		}
		TrailSceneRegistry.encounter(zone, "mine-courier",
				MineTrailProgress.COURIER_X, MineTrailProgress.COURIER_Y,
				player -> MineTrailProgress.active(player, Step.MINE_DISPATCH)
						&& player.isQuestInState(Step.MINE_DISPATCH.slot, "guard"),
				() -> {
					Creature courier = TrailCreatureVariants.create(
							MineTrailProgress.COURIER);
					if (courier == null) {
						throw new IllegalStateException("Missing duergar courier");
					}
					return courier;
				});
	}
}
