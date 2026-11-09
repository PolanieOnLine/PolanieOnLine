/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Private investigation sites built from existing map graphics. */
public final class TrailSites implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		switch (zone.getName()) {
		case "int_zakopane_shop":
			evidence(zone, TrailEvidence.Kind.SHIPMENT, 7, 10);
			break;
		case "int_zakopane_townhall":
			TrailSceneRegistry.prop(zone, "registry-table",
					player -> TrailEvidence.needed(player,
							TrailEvidence.Kind.REGISTRY),
					() -> {
						final AreaEntity table = new AreaEntity(1, 1);
						table.setRPClass("block");
						table.put("type", "block");
						table.put("class", "questprop");
						table.put("name", "stół Gościrada");
						table.put("z", 2000);
						table.put("tileset", "furniture/table/square");
						table.put("tile_index", 0);
						table.put("tileset_columns", 1);
						table.setDescription(
								"Oto stół Gościrada. Leży na nim rejestr zamówień.");
						table.setResistance(100);
						table.setPosition(17, 33);
						return table;
					});
			evidence(zone, TrailEvidence.Kind.REGISTRY, 17, 33);
			break;
		case TrailQuestLine.FOREST:
			evidence(zone, TrailEvidence.Kind.TRACKS, 82, 62);
			evidence(zone, TrailEvidence.Kind.DOCUMENTS, 60, 61);
			supplies(zone, 60, 60, 0);
			supplies(zone, 61, 60, 1);
			TrailSceneRegistry.encounter(zone, "stockpile-guard",
					TrailQuestLine.STOCK_X, TrailQuestLine.STOCK_Y,
					player -> Step.SEAL.available(player)
							&& player.isQuestInState(Step.SEAL.slot, "guard"),
					() -> {
						Creature guard = TrailCreatureVariants.create(
								TrailQuestLine.GUARD);
						if (guard == null) {
							throw new IllegalStateException(
									"Missing stockpile guard definition");
						}
						return guard;
					});
			break;
		default:
			throw new IllegalArgumentException(
					"No trail investigation site in " + zone.getName());
		}
	}

	private void evidence(StendhalRPZone zone, TrailEvidence.Kind kind, int x,
			int y) {
		TrailSceneRegistry.prop(zone, "trail-evidence-" + kind.name(),
				player -> TrailEvidence.needed(player, kind), () -> {
					TrailEvidence evidence = new TrailEvidence(kind);
					evidence.setPosition(x, y);
					return evidence;
				});
	}

	private void supplies(StendhalRPZone zone, int x, int y, int tile) {
		TrailSceneRegistry.prop(zone, "stockpile-supplies-" + tile,
				player -> Step.SEAL.available(player)
						&& player.hasQuest(Step.SEAL.slot)
						&& !player.isQuestCompleted(Step.SEAL.slot), () -> {
					final AreaEntity prop = new AreaEntity(1, 1);
					prop.setRPClass("block");
					prop.put("type", "block");
					prop.put("class", "questprop");
					prop.put("name", "zapasy mieszkańców");
					prop.put("z", 8000);
					prop.put("tileset", "item/sacks");
					prop.put("tile_index", tile);
					prop.put("tileset_columns", 2);
					prop.setDescription(
							"Worki z zapasami. Na każdym wisi karteczka z nazwiskiem kogoś z Zakopanego.");
					prop.setPosition(x, y);
					return prop;
				});
	}
}
