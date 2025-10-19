/***************************************************************************
 *                 (C) Copyright 2003-2018 - Faiumoni e.V.                 *
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

/**
 * Replaces a map's data with that loaded from a .tmx.
 */
public class ChangeMap extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		if (args.size() != 2) {
			sandbox.privateText(admin, "Użyj: /script ChangeMap.class <nazwa_mapy> <ścieżka_do_zmodyfikowanej_mapy_tmx>");
			return;
		}
		IRPZone zn = world.getRPZone(args.get(0));
		if (!(zn instanceof StendhalRPZone)) {
			sandbox.privateText(admin, "Obaszer nie znaleziony: '" + args.get(1) + "'");
			return;
		}
		StendhalRPZone zone = (StendhalRPZone) zn;
		StendhalMapStructure map;
		try {
		map = TMXLoader.load(args.get(1));
		} catch (Exception e) {
			sandbox.privateText(admin, "Nie powiodło się ładowanie mapy: " + e);
			return;
		}
		if (!validateMapCompatibility(admin, zone, map)) {
			return;
		}
		try {
			updateZone(zone, map);
			zone.notifyOnlinePlayers();
		} catch (IOException e) {
			sandbox.privateText(admin, "Nie powiodła się aktualizacja mapy: " + e);
		}
	}

	/**
	 * Update zone from StendhalMapStructure.
	 *
	 * @param zone zone to be updated
	 * @param map new map data
	 * @throws IOException When encoding the layer data fails
	 */
	private void updateZone(StendhalRPZone zone, StendhalMapStructure map) throws IOException {
		String name = zone.getName();
		zone.addTilesets(name + ".tilesets", map.getTilesets());
		zone.addLayer(name + ".0_floor", map.getLayer("0_floor"));
		zone.addLayer(name + ".1_terrain", map.getLayer("1_terrain"));
		zone.addLayer(name + ".2_object", map.getLayer("2_object"));

		// Roof layers are optional
		loadOptionalLayer(zone, map, "3_roof");
		loadOptionalLayer(zone, map, "4_roof_add");
		// Effect layers are optional too
		loadOptionalLayer(zone, map, "blend_ground");
		loadOptionalLayer(zone, map, "blend_roof");
		// Secret layer is too optional to load
		loadOptionalLayer(zone, map, "secret");

		zone.addCollisionLayer(name + ".collision", map.getLayer("collision"));
		zone.addProtectionLayer(name + ".protection", map.getLayer("protection"));
		// Spawn points are intentionally left untouched so existing coordinates remain valid.
	}

	private boolean validateMapCompatibility(Player admin, StendhalRPZone zone, StendhalMapStructure map) {
		List<String> errors = new ArrayList<String>();
		if ((map.getWidth() != zone.getWidth()) || (map.getHeight() != zone.getHeight())) {
			errors.add("Nowa mapa ma wymiary " + map.getWidth() + "x" + map.getHeight()
				+ " a strefa oczekuje " + zone.getWidth() + "x" + zone.getHeight() + ".");
		}
		checkLayer(zone, map, "0_floor", true, errors);
		checkLayer(zone, map, "1_terrain", true, errors);
		checkLayer(zone, map, "2_object", true, errors);
		checkLayer(zone, map, "collision", true, errors);
		checkLayer(zone, map, "protection", true, errors);
		if (!errors.isEmpty()) {
			for (String message : errors) {
				sandbox.privateText(admin, message);
			}
			sandbox.privateText(admin, "Przerwano zmianę mapy z powodu niezgodności.");
			return false;
		}
		return true;
	}

	private void checkLayer(StendhalRPZone zone, StendhalMapStructure map,
			String layerName, boolean required, List<String> errors) {
		LayerDefinition layer = map.getLayer(layerName);
		if (layer == null) {
			if (required) {
				errors.add("Brak warstwy "" + layerName + "" w pliku TMX.");
			}
			return;
		}
		if ((layer.getWidth() != zone.getWidth()) || (layer.getHeight() != zone.getHeight())) {
			errors.add("Warstwa "" + layerName + "" ma rozmiar " + layer.getWidth() + "x" + layer.getHeight()
				+ " zamiast oczekiwanego " + zone.getWidth() + "x" + zone.getHeight() + ".");
		}
	}

	/**
	 * Load an optional layer, if present, to a zone.
	 *
	 * @param zone zone to be updated
	 * @param zonedata new map data
	 * @param layerName name of the layer
	 * @throws IOException When encoding the layer data fails
	 */
	private void loadOptionalLayer(StendhalRPZone zone,
			StendhalMapStructure zonedata, String layerName) throws IOException {
		LayerDefinition layer = zonedata.getLayer(layerName);
		if (layer != null) {
			zone.addLayer(zone.getName() + "." + layerName, layer);
		}
	}
	}
