/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/** Permanent, passable investigation sites built from existing map graphics. */
public final class TrailSites implements ZoneConfigurator {
    @Override public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
        switch (zone.getName()) {
        case "int_zakopane_shop":
            evidence(zone, TrailEvidence.Kind.SHIPMENT, 7, 10);
            break;
        case "int_zakopane_townhall":
            evidence(zone, TrailEvidence.Kind.REGISTRY, 25, 34);
            break;
        case TrailQuestLine.FOREST:
            evidence(zone, TrailEvidence.Kind.TRACKS, 82, 62);
            evidence(zone, TrailEvidence.Kind.DOCUMENTS, 60, 61);
            supplies(zone, 60, 60, 0);
            supplies(zone, 61, 60, 1);
            final Creature guard = SingletonRepository.getEntityManager().getCreature(TrailQuestLine.GUARD);
            if (guard == null) throw new IllegalStateException("Missing stockpile guard definition");
            zone.add(new CreatureRespawnPoint(zone, TrailQuestLine.STOCK_X, TrailQuestLine.STOCK_Y, guard, 1));
            break;
        default:
            throw new IllegalArgumentException("No trail investigation site in " + zone.getName());
        }
    }
    private void evidence(StendhalRPZone zone, TrailEvidence.Kind kind, int x, int y) {
        final TrailEvidence evidence = new TrailEvidence(kind);
        evidence.setPosition(x, y); zone.add(evidence);
    }
    private void supplies(StendhalRPZone zone, int x, int y, int tile) {
        final AreaEntity prop = new AreaEntity(1, 1);
        prop.setRPClass("block"); prop.put("type", "block"); prop.put("class", "questprop");
        prop.put("name", "zapasy mieszkańców"); prop.put("z", 8000);
        prop.put("tileset", "item/sacks"); prop.put("tile_index", tile); prop.put("tileset_columns", 2);
        prop.setDescription("Worki z zapasami. Na każdym wisi karteczka z nazwiskiem kogoś z Zakopanego.");
        prop.setPosition(x, y); zone.add(prop);
    }
}
