/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;

/** Builds the private wreck and tracking scenery used by the Mieszczanin story. */
final class MieszczaninRoadScene {

	static final String ZONE_NAME = "0_dragon_knights_e";
	static final int WITOMIR_START_X = 63;
	static final int WITOMIR_START_Y = 50;
	static final int WITOMIR_END_X = 84;
	static final int WITOMIR_END_Y = 66;
	static final int MEDICINE_CRATE_X = 44;
	static final int MEDICINE_CRATE_Y = 54;
	static final int TRACK_ENTRANCE_X = 28;
	static final int TRACK_ENTRANCE_Y = 94;
	static final String TRACK_TILESET = "ground/mieszczanin_tracks";
	static final int TRACK_TILESET_COLUMNS = 5;
	static final int TRACK_Z_ORDER = 50;

	private static final int[][] TRACK_MARKS = {
		{44, 54, 0},
		{39, 54, 0},
		{36, 56, 0},
		{35, 60, 1},
		{35, 65, 1},
		{35, 70, 1},
		{35, 72, 1},
		{31, 72, 2},
		{31, 77, 2},
		{31, 82, 2},
		{31, 87, 2},
		{31, 92, 2},
		{27, 92, 3},
		{27, 94, 3},
		{TRACK_ENTRANCE_X, TRACK_ENTRANCE_Y, 4}
	};

	private MieszczaninRoadScene() {
		// utility class
	}

	static void ensureWreckProps(final StendhalRPZone zone, final Player owner) {
		ensureWreckProp(zone, owner, "object/hay_cart", 0, 1, true, 62, 47,
				"Oto rozbity wóz Witomira. Jedno koło jest przekrzywione, a przy osi widać ślady gwałtownego szarpnięcia.");
		ensureWreckProp(zone, owner, "item/pot/crate_small", 0, 1, true, 60, 48,
				"Oto rozbita skrzynia z dostawy Witomira. Deski zostały wyłamane, a środek jest pusty.");
		ensureWreckProp(zone, owner, "item/pot/barrels_1", 5, 4, true, 61, 50,
				"Oto przewrócona beczka z dostawy. Ktoś zepchnął ją z wozu podczas napadu.");
		ensureWreckProp(zone, owner, "item/pot/barrels_1", 1, 4, true, 64, 47,
				"Oto beczka odsunięta od wozu. Wieko jest naruszone, jakby ktoś w pośpiechu sprawdzał zawartość.");
		ensureWreckProp(zone, owner, "item/bazaar_produce", 2, 2, false, 62, 49,
				"Oto rozsypane zapasy z wozu Witomira. Napastnicy zabrali to, co było najłatwiejsze do uniesienia.");
		ensureWreckProp(zone, owner, "item/sacks", 0, 2, true, 63, 46,
				"Oto rozcięty worek z dostawy. Zawartość wysypała się na ziemię.");
		ensureWreckProp(zone, owner, "item/sacks", 1, 2, true, 64, 46,
				"Oto porzucony worek. Wygląda na zbyt ciężki albo zbyt mało wartościowy, by napastnicy zabrali go ze sobą.");

		if (owner.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE)) {
			ensureMedicineCrate(zone, owner);
		} else {
			removeMedicineCrate(zone, owner);
		}
	}

	static void removeWreckProps(final StendhalRPZone zone, final Player owner) {
		final List<Entity> toRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (prop.isOwnedBy(owner) && isWreckCoordinate(prop.getX(), prop.getY())) {
				toRemove.add(prop);
			}
		}
		for (final Entity entity : toRemove) {
			zone.remove(entity.getID());
		}
		removeMedicineCrate(zone, owner);
	}

	static void ensureTrackProps(final StendhalRPZone zone, final Player owner) {
		for (final int[] mark : TRACK_MARKS) {
			if (mark[0] == TRACK_ENTRANCE_X && mark[1] == TRACK_ENTRANCE_Y) {
				ensureHideoutEntrance(zone, owner);
			} else if (!hasTrackPropAt(zone, owner, mark[0], mark[1])) {
				addTrackProp(zone, owner, mark[2], mark[0], mark[1], trackDescription(mark[2]));
			}
		}
	}

	static void removeTrackProps(final StendhalRPZone zone, final Player owner) {
		final List<Entity> toRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (isTrackPropFor(prop, owner)) {
				toRemove.add(prop);
			}
		}
		for (final Entity entity : toRemove) {
			zone.remove(entity.getID());
		}
	}

	static List<games.stendhal.server.core.pathfinder.Node> createWitomirPath() {
		final List<games.stendhal.server.core.pathfinder.Node> nodes =
				new ArrayList<games.stendhal.server.core.pathfinder.Node>();
		add(nodes, 63, 50); add(nodes, 63, 48); add(nodes, 73, 48); add(nodes, 73, 47);
		add(nodes, 79, 47); add(nodes, 79, 46); add(nodes, 85, 46); add(nodes, 85, 45);
		add(nodes, 99, 45); add(nodes, 99, 46); add(nodes, 103, 46); add(nodes, 103, 47);
		add(nodes, 106, 47); add(nodes, 106, 48); add(nodes, 109, 48); add(nodes, 109, 49);
		add(nodes, 112, 49); add(nodes, 112, 50); add(nodes, 115, 50); add(nodes, 115, 51);
		add(nodes, 119, 51); add(nodes, 119, 57); add(nodes, 118, 57); add(nodes, 118, 58);
		add(nodes, 117, 58); add(nodes, 117, 59); add(nodes, 112, 59); add(nodes, 112, 58);
		add(nodes, 102, 58); add(nodes, 102, 59); add(nodes, 97, 59); add(nodes, 97, 60);
		add(nodes, 94, 60); add(nodes, 94, 61); add(nodes, 87, 61); add(nodes, 87, 64);
		add(nodes, 86, 64); add(nodes, 86, 65); add(nodes, 84, 65); add(nodes, 84, 66);
		return nodes;
	}

	static Direction finalDirection() {
		return Direction.DOWN;
	}

	private static void ensureWreckProp(final StendhalRPZone zone, final Player owner,
			final String tileset, final int tileIndex, final int columns,
			final boolean solid, final int x, final int y, final String description) {
		if (hasOwnedPropAt(zone, owner, x, y, tileset)) {
			return;
		}
		final PlayerPrivateQuestProp prop =
				new PlayerPrivateQuestProp(owner, tileset, tileIndex, columns, solid);
		prop.setDescription(description);
		prop.setPosition(x, y);
		zone.add(prop);
	}

	private static boolean hasOwnedPropAt(final StendhalRPZone zone, final Player owner,
			final int x, final int y, final String tileset) {
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (prop.isOwnedBy(owner)
					&& prop.getX() == x && prop.getY() == y
					&& tileset.equals(prop.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE))) {
				return true;
			}
		}
		return false;
	}

	private static void ensureMedicineCrate(final StendhalRPZone zone, final Player owner) {
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninMedicineCrate.class)) {
			final MieszczaninMedicineCrate crate = (MieszczaninMedicineCrate) entity;
			if (crate.isOwnedBy(owner)) {
				return;
			}
		}

		final MieszczaninMedicineCrate crate = new MieszczaninMedicineCrate(owner);
		crate.setPosition(MEDICINE_CRATE_X, MEDICINE_CRATE_Y);
		zone.add(crate);
	}

	private static void removeMedicineCrate(final StendhalRPZone zone, final Player owner) {
		final List<Entity> toRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninMedicineCrate.class)) {
			final MieszczaninMedicineCrate crate = (MieszczaninMedicineCrate) entity;
			if (crate.isOwnedBy(owner)) {
				toRemove.add(crate);
			}
		}
		for (final Entity entity : toRemove) {
			zone.remove(entity.getID());
		}
	}

	private static boolean hasTrackPropAt(final StendhalRPZone zone, final Player owner,
			final int x, final int y) {
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (prop.isOwnedBy(owner)
					&& TRACK_TILESET.equals(prop.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE))
					&& prop.getX() == x && prop.getY() == y) {
				return true;
			}
		}
		return false;
	}

	private static void ensureHideoutEntrance(final StendhalRPZone zone, final Player owner) {
		final List<Entity> stale = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateQuestProp.class)) {
			final PlayerPrivateQuestProp prop = (PlayerPrivateQuestProp) entity;
			if (!prop.isOwnedBy(owner)
					|| prop.getX() != TRACK_ENTRANCE_X || prop.getY() != TRACK_ENTRANCE_Y) {
				continue;
			}
			if (prop instanceof MieszczaninHideoutEntrance) {
				return;
			}
			stale.add(prop);
		}

		for (final Entity entity : stale) {
			zone.remove(entity.getID());
		}

		final MieszczaninHideoutEntrance entrance = new MieszczaninHideoutEntrance(owner);
		entrance.setPosition(TRACK_ENTRANCE_X, TRACK_ENTRANCE_Y);
		zone.add(entrance);
	}

	private static String trackDescription(final int tileIndex) {
		switch (tileIndex) {
		case 0:
			return "Oto świeże rysy w ziemi po czymś ciężkim ciągniętym na zachód. "
					+ "Obok widać wyraźne odciski butów.";
		case 1:
			return "Oto rozdeptane błoto. Rysa po ciągniętym ładunku i ślady kilku butów "
					+ "prowadzą dalej ku lasowi.";
		case 2:
			return "Oto zgnieciona trawa i ślad ciężkiej skrzynki. Między źdźbłami widać "
					+ "kolejne odciski butów.";
		case 3:
			return "Oto mocno rozdeptany skraj lasu. Ślady butów i głęboka rysa po ciężkim "
					+ "ładunku zbiegają się przy gęstwinie.";
		default:
			return "Oto ślady prowadzące ku lasowi.";
		}
	}

	private static void addTrackProp(final StendhalRPZone zone, final Player owner,
			final int tileIndex, final int x, final int y, final String description) {
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, TRACK_TILESET, tileIndex, TRACK_TILESET_COLUMNS, false);
		prop.put("z", TRACK_Z_ORDER);
		prop.setDescription(description);
		prop.setPosition(x, y);
		zone.add(prop);
	}

	private static boolean isTrackPropFor(final PlayerPrivateQuestProp prop, final Player owner) {
		return prop.isOwnedBy(owner)
				&& TRACK_TILESET.equals(prop.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE))
				&& isTrackCoordinate(prop.getX(), prop.getY());
	}

	private static boolean isWreckCoordinate(final int x, final int y) {
		return (x == 62 && y == 47)
				|| (x == 60 && y == 48)
				|| (x == 61 && y == 50)
				|| (x == 64 && y == 47)
				|| (x == 62 && y == 49)
				|| (x == 63 && y == 46)
				|| (x == 64 && y == 46);
	}

	private static boolean isTrackCoordinate(final int x, final int y) {
		for (final int[] mark : TRACK_MARKS) {
			if (mark[0] == x && mark[1] == y) {
				return true;
			}
		}
		return false;
	}

	private static void add(final List<games.stendhal.server.core.pathfinder.Node> nodes,
			final int x, final int y) {
		nodes.add(new games.stendhal.server.core.pathfinder.Node(x, y));
	}
}
