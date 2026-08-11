/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;

/**
 * Controlled runtime changes for passive item spawn points originating from a
 * TMX {@code objects} layer.
 */
public final class PassiveEntityRespawnPointUpdater {
	private PassiveEntityRespawnPointUpdater() {
		// utility class
	}

	/**
	 * Validates a logic/item map object using the same factory as normal zone
	 * population. Map signs are intentionally ignored by the normal loader and
	 * therefore are also valid no-op objects for runtime reload.
	 *
	 * @param source TMX tileset source
	 * @param type local tile id
	 * @param x tile x
	 * @param y tile y
	 */
	public static void validateMapObject(final String source, final int type,
			final int x, final int y) {
		if (isIgnoredMapObject(source)) {
			return;
		}
		if (PassiveEntityRespawnPointFactory.create(source, type, null, x, y) == null) {
			throw new IllegalArgumentException("Unsupported passive item map object "
					+ source + ":" + type + " at " + x + "," + y);
		}
	}

	/**
	 * Removes map-generated passive item spawners matching the TMX source/type
	 * at the supplied position. The operation is idempotent so it is safe for a
	 * rollback after a partially applied event transition.
	 *
	 * @param zone active zone
	 * @param source TMX tileset source
	 * @param type local tile id
	 * @param x tile x
	 * @param y tile y
	 */
	public static void removeMapSpawner(final StendhalRPZone zone,
			final String source, final int type, final int x, final int y) {
		if (isIgnoredMapObject(source)) {
			return;
		}
		final PassiveEntityRespawnPoint expected = create(source, type, zone, x, y);
		final List<PassiveEntityRespawnPoint> matching = findMatching(
				zone, expected.getItemName(), x, y);
		for (final PassiveEntityRespawnPoint found : matching) {
			removeSpawner(zone, found, x, y);
		}
	}

	/**
	 * Adds a map-generated passive item spawner using the same factory and
	 * startup state as {@link StendhalRPZone#populate}. The operation is
	 * idempotent: when the desired spawner already exists, no duplicate is
	 * created.
	 *
	 * @param zone active zone
	 * @param source TMX tileset source
	 * @param type local tile id
	 * @param x tile x
	 * @param y tile y
	 */
	public static void addMapSpawner(final StendhalRPZone zone,
			final String source, final int type, final int x, final int y) {
		if (isIgnoredMapObject(source)) {
			return;
		}
		final PassiveEntityRespawnPoint point = create(source, type, zone, x, y);
		final List<PassiveEntityRespawnPoint> matching = findMatching(
				zone, point.getItemName(), x, y);
		if (!matching.isEmpty()) {
			for (int index = 1; index < matching.size(); index++) {
				removeSpawner(zone, matching.get(index), x, y);
			}
			return;
		}

		point.setPosition(x, y);
		zone.add(point);
		point.setStartState();
	}

	private static List<PassiveEntityRespawnPoint> findMatching(
			final StendhalRPZone zone, final String itemName,
			final int x, final int y) {
		final List<PassiveEntityRespawnPoint> result =
				new ArrayList<PassiveEntityRespawnPoint>();
		for (final Entity entity : new ArrayList<Entity>(zone.getEntitiesAt(x, y))) {
			if (entity instanceof PassiveEntityRespawnPoint) {
				final PassiveEntityRespawnPoint point = (PassiveEntityRespawnPoint) entity;
				if (itemName.equals(point.getItemName())) {
					result.add(point);
				}
			}
		}
		return result;
	}

	private static void removeSpawner(final StendhalRPZone zone,
			final PassiveEntityRespawnPoint found, final int x, final int y) {
		SingletonRepository.getTurnNotifier().dontNotify(found);
		final List<Entity> occupants = new ArrayList<Entity>(zone.getEntitiesAt(x, y));
		for (final Entity entity : occupants) {
			if (entity instanceof Item && ((Item) entity).getPlantGrower() == found) {
				zone.remove(entity);
			}
		}
		zone.remove(found);
	}

	private static PassiveEntityRespawnPoint create(final String source,
			final int type, final StendhalRPZone zone, final int x, final int y) {
		final PassiveEntityRespawnPoint point = PassiveEntityRespawnPointFactory.create(
				source, type, zone.getID(), x, y);
		if (point == null) {
			throw new IllegalArgumentException("Unsupported passive item map object "
					+ source + ":" + type + " at " + x + "," + y);
		}
		return point;
	}

	private static boolean isIgnoredMapObject(final String source) {
		return source != null && source.contains("sign");
	}
}
