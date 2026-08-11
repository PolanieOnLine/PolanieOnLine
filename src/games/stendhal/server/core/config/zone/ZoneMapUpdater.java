/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.config.zone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.common.tiled.TileSetDefinition;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPointUpdater;

/**
 * Applies an already parsed TMX map to an existing zone instance.
 */
public final class ZoneMapUpdater {
	private static final String[] REQUIRED_LAYERS = {
		"0_floor", "1_terrain", "2_object", "objects", "collision", "protection"
	};
	private static final String[] REQUIRED_CLIENT_LAYERS = {
		"0_floor", "1_terrain", "2_object"
	};
	private static final String[] OPTIONAL_CLIENT_LAYERS = {
		"3_roof", "4_roof_add", "secret", "blend_ground", "blend_roof", "0_floor_parallax"
	};

	private ZoneMapUpdater() {
		// utility class
	}

	public static StendhalMapStructure prepare(final String relativePath) throws Exception {
		if (relativePath == null || relativePath.trim().isEmpty()) {
			throw new IllegalArgumentException("relativePath must not be empty");
		}
		final StendhalMapStructure map = TMXLoader.load(
				StendhalRPWorld.MAPS_FOLDER + relativePath);
		validate(map, relativePath);
		return map;
	}

	/**
	 * Prepares only the map parts whose serialized or runtime state differs.
	 * Comparison and layer compression happen on the prepare worker so the RP
	 * thread does not encode and replace unchanged layers during a transition.
	 */
	public static MapUpdatePlan prepareMapUpdate(
			final StendhalMapStructure reference,
			final StendhalMapStructure candidate,
			final String referencePath,
			final String candidatePath) throws IOException {
		if (reference == null || candidate == null) {
			throw new IllegalArgumentException("reference and candidate must not be null");
		}

		final ObjectLayerUpdatePlan objectLayerUpdate = prepareObjectLayerUpdate(
				reference, candidate, referencePath, candidatePath);
		final boolean tilesetsChanged = !reference.getTilesets().equals(candidate.getTilesets());
		final List<LayerDefinition> clientLayers = new ArrayList<LayerDefinition>();

		for (final String layerName : REQUIRED_CLIENT_LAYERS) {
			final LayerDefinition referenceLayer = reference.getLayer(layerName);
			final LayerDefinition candidateLayer = candidate.getLayer(layerName);
			if (!sameLayer(referenceLayer, candidateLayer)) {
				clientLayers.add(prepareLayerForApply(candidateLayer));
			}
		}

		for (final String layerName : OPTIONAL_CLIENT_LAYERS) {
			final LayerDefinition referenceLayer = reference.getLayer(layerName);
			final LayerDefinition candidateLayer = candidate.getLayer(layerName);
			if (candidateLayer != null && !sameLayer(referenceLayer, candidateLayer)) {
				clientLayers.add(prepareLayerForApply(candidateLayer));
			}
		}

		final LayerDefinition collision = sameLayer(reference.getLayer("collision"),
				candidate.getLayer("collision")) ? null
						: prepareLayerForApply(candidate.getLayer("collision"));
		final LayerDefinition protection = sameLayer(reference.getLayer("protection"),
				candidate.getLayer("protection")) ? null
						: prepareLayerForApply(candidate.getLayer("protection"));

		return new MapUpdatePlan(tilesetsChanged
				? new ArrayList<TileSetDefinition>(candidate.getTilesets()) : null,
				clientLayers, collision, protection, objectLayerUpdate);
	}

	/**
	 * Prepares server-side object changes and client layers which must be
	 * explicitly cleared when the target map no longer contains them.
	 */
	public static ObjectLayerUpdatePlan prepareObjectLayerUpdate(
			final StendhalMapStructure reference,
			final StendhalMapStructure candidate,
			final String referencePath,
			final String candidatePath) throws IOException {
		if (reference == null || candidate == null) {
			throw new IllegalArgumentException("reference and candidate must not be null");
		}

		final List<LayerDefinition> optionalLayersToClear = new ArrayList<LayerDefinition>();
		final LayerDefinition candidateFloor = candidate.getLayer("0_floor");
		for (final String layerName : OPTIONAL_CLIENT_LAYERS) {
			if (reference.hasLayer(layerName) && !candidate.hasLayer(layerName)) {
				optionalLayersToClear.add(prepareLayerForApply(createEmptyClientLayer(
						layerName, candidateFloor.getWidth(), candidateFloor.getHeight())));
			}
		}

		final LayerDefinition referenceObjects = reference.getLayer("objects");
		final LayerDefinition candidateObjects = candidate.getLayer("objects");
		referenceObjects.build();
		candidateObjects.build();

		if (referenceObjects.getWidth() != candidateObjects.getWidth()
				|| referenceObjects.getHeight() != candidateObjects.getHeight()) {
			throw new IOException("Runtime objects layer dimensions differ between "
					+ referencePath + " and " + candidatePath);
		}

		final List<ObjectTileChange> changes = new ArrayList<ObjectTileChange>();
		for (int y = 0; y < referenceObjects.getHeight(); y++) {
			for (int x = 0; x < referenceObjects.getWidth(); x++) {
				final ObjectTile referenceTile = objectTile(referenceObjects, x, y);
				final ObjectTile candidateTile = objectTile(candidateObjects, x, y);
				if (!same(referenceTile, candidateTile)) {
					validateSupportedObjectDifference(referenceTile, candidateTile,
							x, y, referencePath, candidatePath);
					changes.add(new ObjectTileChange(x, y, referenceTile, candidateTile));
				}
			}
		}
		return new ObjectLayerUpdatePlan(changes, optionalLayersToClear);
	}

	public static void validateObjectLayerCompatibility(
			final StendhalMapStructure reference,
			final StendhalMapStructure candidate,
			final String referencePath,
			final String candidatePath) throws IOException {
		prepareObjectLayerUpdate(reference, candidate, referencePath, candidatePath);
	}

	public static void apply(final StendhalRPZone zone,
			final StendhalMapStructure map) throws IOException {
		if (zone == null || map == null) {
			throw new IllegalArgumentException("zone and map must not be null");
		}

		final String name = zone.getName();
		zone.addTilesets(name + ".tilesets", map.getTilesets());
		zone.addLayer(name + ".0_floor", map.getLayer("0_floor"));
		zone.addLayer(name + ".1_terrain", map.getLayer("1_terrain"));
		zone.addLayer(name + ".2_object", map.getLayer("2_object"));

		for (final String layer : OPTIONAL_CLIENT_LAYERS) {
			loadOptionalLayer(zone, map, layer);
		}

		zone.addCollisionLayer(name + ".collision", map.getLayer("collision"));
		zone.addProtectionLayer(name + ".protection", map.getLayer("protection"));
	}

	private static LayerDefinition prepareLayerForApply(final LayerDefinition source)
			throws IOException {
		return new PreparedLayerDefinition(source);
	}

	private static boolean sameLayer(final LayerDefinition first,
			final LayerDefinition second) {
		if (first == null || second == null) {
			return first == second;
		}
		return first.getWidth() == second.getWidth()
				&& first.getHeight() == second.getHeight()
				&& Arrays.equals(first.exposeRaw(), second.exposeRaw());
	}

	private static void validateSupportedObjectDifference(final ObjectTile reference,
			final ObjectTile candidate, final int x, final int y,
			final String referencePath, final String candidatePath) throws IOException {
		if (isSupportedPassiveItem(reference) && isSupportedPassiveItem(candidate)) {
			try {
				validatePassiveItem(reference, x, y);
				validatePassiveItem(candidate, x, y);
			} catch (final IllegalArgumentException e) {
				throw new IOException(e.getMessage(), e);
			}
			return;
		}
		throw new IOException("Unsupported runtime objects change at " + x + "," + y
				+ " between " + referencePath + " [" + token(reference) + "] and "
				+ candidatePath + " [" + token(candidate) + "]");
	}

	private static void validatePassiveItem(final ObjectTile tile,
			final int x, final int y) {
		if (tile != null) {
			PassiveEntityRespawnPointUpdater.validateMapObject(
					tile.source, tile.type, x, y);
		}
	}

	private static boolean isSupportedPassiveItem(final ObjectTile tile) {
		return tile == null || tile.source.contains("logic/item");
	}

	private static ObjectTile objectTile(final LayerDefinition layer,
			final int x, final int y) throws IOException {
		final int value = layer.getTileAt(x, y);
		if (value == 0) {
			return null;
		}

		final TileSetDefinition tileset = layer.getTilesetFor(value);
		if (tileset == null || tileset.getSource() == null) {
			throw new IOException("Unable to resolve objects tileset for gid " + value
					+ " at " + x + "," + y);
		}
		return new ObjectTile(tileset.getSource(), value - tileset.getFirstGid());
	}

	private static boolean same(final ObjectTile first, final ObjectTile second) {
		if (first == null || second == null) {
			return first == second;
		}
		return first.type == second.type && first.source.equals(second.source);
	}

	private static String token(final ObjectTile tile) {
		return tile == null ? "<empty>" : tile.source + ":" + tile.type;
	}

	private static void validate(final StendhalMapStructure map,
			final String relativePath) throws IOException {
		for (final String layer : REQUIRED_LAYERS) {
			if (!map.hasLayer(layer)) {
				throw new IOException("Required layer " + layer
						+ " missing in map " + relativePath);
			}
		}
	}

	private static void loadOptionalLayer(final StendhalRPZone zone,
			final StendhalMapStructure map, final String layerName) throws IOException {
		final LayerDefinition layer = map.getLayer(layerName);
		if (layer != null) {
			zone.addLayer(zone.getName() + "." + layerName, layer);
		}
	}

	static LayerDefinition createEmptyClientLayer(final String layerName,
			final int width, final int height) {
		if (layerName == null || layerName.trim().isEmpty()) {
			throw new IllegalArgumentException("layerName must not be empty");
		}
		final LayerDefinition layer = new LayerDefinition(width, height);
		layer.setName(layerName);
		return layer;
	}

	public static final class MapUpdatePlan {
		private final List<TileSetDefinition> tilesets;
		private final List<LayerDefinition> clientLayers;
		private final LayerDefinition collision;
		private final LayerDefinition protection;
		private final ObjectLayerUpdatePlan objectLayerUpdate;

		private MapUpdatePlan(final List<TileSetDefinition> tilesets,
				final List<LayerDefinition> clientLayers,
				final LayerDefinition collision,
				final LayerDefinition protection,
				final ObjectLayerUpdatePlan objectLayerUpdate) {
			this.tilesets = tilesets;
			this.clientLayers = clientLayers;
			this.collision = collision;
			this.protection = protection;
			this.objectLayerUpdate = objectLayerUpdate;
		}

		public void apply(final StendhalRPZone zone) throws IOException {
			if (zone == null) {
				throw new IllegalArgumentException("zone must not be null");
			}
			objectLayerUpdate.apply(zone);
			final String name = zone.getName();
			if (tilesets != null) {
				zone.addTilesets(name + ".tilesets", tilesets);
			}
			for (final LayerDefinition layer : clientLayers) {
				zone.addLayer(name + "." + layer.getName(), layer);
			}
			if (collision != null) {
				zone.addCollisionLayer(name + ".collision", collision);
			}
			if (protection != null) {
				zone.addProtectionLayer(name + ".protection", protection);
			}
		}

		public boolean isEmpty() {
			return tilesets == null && clientLayers.isEmpty()
					&& collision == null && protection == null
					&& objectLayerUpdate.isEmpty();
		}
	}

	public static final class ObjectLayerUpdatePlan {
		private final List<ObjectTileChange> changes;
		private final List<LayerDefinition> optionalLayersToClear;

		private ObjectLayerUpdatePlan(final List<ObjectTileChange> changes,
				final List<LayerDefinition> optionalLayersToClear) {
			this.changes = changes;
			this.optionalLayersToClear = optionalLayersToClear;
		}

		/**
		 * Removes old server-side objects before additions. Optional visual layers
		 * absent from the target are replaced with a zero-filled layer under the
		 * same content name so connected clients explicitly discard old pixels.
		 */
		public void apply(final StendhalRPZone zone) {
			for (final ObjectTileChange change : changes) {
				change.removeOld(zone);
			}
			for (final ObjectTileChange change : changes) {
				change.addNew(zone);
			}
			for (final LayerDefinition layer : optionalLayersToClear) {
				try {
					zone.addLayer(zone.getName() + "." + layer.getName(), layer);
				} catch (final IOException e) {
					throw new IllegalStateException("Unable to clear optional layer "
							+ layer.getName() + " in zone " + zone.getName(), e);
				}
			}
		}

		public boolean isEmpty() {
			return changes.isEmpty() && optionalLayersToClear.isEmpty();
		}
	}

	private static final class PreparedLayerDefinition extends LayerDefinition {
		private final byte[] encoded;

		private PreparedLayerDefinition(final LayerDefinition source) throws IOException {
			super(source.getWidth(), source.getHeight());
			setName(source.getName());
			System.arraycopy(source.exposeRaw(), 0, exposeRaw(), 0, source.exposeRaw().length);
			build();
			encoded = source.encode();
		}

		@Override
		public byte[] encode() {
			return encoded;
		}
	}

	private static final class ObjectTileChange {
		private final int x;
		private final int y;
		private final ObjectTile oldTile;
		private final ObjectTile newTile;

		private ObjectTileChange(final int x, final int y,
				final ObjectTile oldTile, final ObjectTile newTile) {
			this.x = x;
			this.y = y;
			this.oldTile = oldTile;
			this.newTile = newTile;
		}

		private void removeOld(final StendhalRPZone zone) {
			if (oldTile != null) {
				PassiveEntityRespawnPointUpdater.removeMapSpawner(
						zone, oldTile.source, oldTile.type, x, y);
			}
		}

		private void addNew(final StendhalRPZone zone) {
			if (newTile != null) {
				PassiveEntityRespawnPointUpdater.addMapSpawner(
						zone, newTile.source, newTile.type, x, y);
			}
		}
	}

	private static final class ObjectTile {
		private final String source;
		private final int type;

		private ObjectTile(final String source, final int type) {
			this.source = source;
			this.type = type;
		}
	}
}
