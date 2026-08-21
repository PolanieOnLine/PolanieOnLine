/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.quest;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.area.AreaEntity;
import games.stendhal.server.entity.player.Player;

/**
 * A small map prop that is only perceived by one player.
 *
 * By default solid props block only their owner, so players on a shared map
 * never collide with an invisible quest object. A private instance may opt in
 * to blocking every RPEntity because every actor in that instance participates
 * in the same private scene.
 */
public class PlayerPrivateQuestProp extends AreaEntity {

	public static final String PERCEPTION_KEY_ATTRIBUTE = "#perception_key";
	public static final String PERCEPTION_VALUE_ATTRIBUTE = "#perception_value";
	public static final String TILESET_ATTRIBUTE = "tileset";
	public static final String TILE_INDEX_ATTRIBUTE = "tile_index";
	public static final String TILESET_COLUMNS_ATTRIBUTE = "tileset_columns";

	private static final String PLAYER_NAME_ATTRIBUTE = "name";
	private static final String ENTITY_CLASS = "questprop";
	private static final int Z_ORDER = 8000;

	private final boolean solid;
	private final boolean blocksAllRPEntities;

	/**
	 * Creates a one tile private quest prop for use on a shared map.
	 *
	 * @param owner player who can perceive and collide with the prop
	 * @param tileset path relative to data/maps/tileset without .png
	 * @param tileIndex zero based tile index in the tileset
	 * @param tilesetColumns number of tile columns in the source image
	 * @param solid whether the prop blocks the owner
	 */
	public PlayerPrivateQuestProp(final Player owner, final String tileset,
			final int tileIndex, final int tilesetColumns, final boolean solid) {
		this(owner, tileset, tileIndex, tilesetColumns, solid, false);
	}

	/**
	 * Creates a one tile private quest prop.
	 *
	 * @param owner player who can perceive the prop
	 * @param tileset path relative to data/maps/tileset without .png
	 * @param tileIndex zero based tile index in the tileset
	 * @param tilesetColumns number of tile columns in the source image
	 * @param solid whether the prop is an obstacle
	 * @param blocksAllRPEntities whether every RPEntity in the private scene
	 *        should collide with a solid prop; keep false on shared maps
	 */
	public PlayerPrivateQuestProp(final Player owner, final String tileset,
			final int tileIndex, final int tilesetColumns, final boolean solid,
			final boolean blocksAllRPEntities) {
		super(1, 1);

		if (owner == null) {
			throw new IllegalArgumentException("owner must not be null");
		}
		if (tileset == null || tileset.isEmpty()) {
			throw new IllegalArgumentException("tileset must not be empty");
		}
		if (tileIndex < 0) {
			throw new IllegalArgumentException("tileIndex must not be negative");
		}
		if (tilesetColumns < 1) {
			throw new IllegalArgumentException("tilesetColumns must be positive");
		}

		this.solid = solid;
		this.blocksAllRPEntities = blocksAllRPEntities;

		setRPClass("block");
		put("type", "block");
		put("class", ENTITY_CLASS);
		put("name", ENTITY_CLASS);
		put("z", Z_ORDER);
		put(TILESET_ATTRIBUTE, tileset);
		put(TILE_INDEX_ATTRIBUTE, tileIndex);
		put(TILESET_COLUMNS_ATTRIBUTE, tilesetColumns);
		put(PERCEPTION_KEY_ATTRIBUTE, PLAYER_NAME_ATTRIBUTE);
		put(PERCEPTION_VALUE_ATTRIBUTE, owner.getName());
		setResistance(solid ? 100 : 0);
		setDescription(defaultDescription(tileset));
	}

	private static String defaultDescription(final String tileset) {
		if ("object/hay_cart".equals(tileset)) {
			return "Oto drewniany wóz.";
		}
		if ("item/pot/crate_small".equals(tileset)) {
			return "Oto niewielka drewniana skrzynia.";
		}
		if ("item/pot/barrels_1".equals(tileset)) {
			return "Oto drewniana beczka.";
		}
		if ("item/bazaar_produce".equals(tileset)) {
			return "Oto rozsypane zapasy.";
		}
		if ("item/sacks".equals(tileset)) {
			return "Oto worek z zapasami.";
		}
		if ("item/tools".equals(tileset)) {
			return "Oto zestaw narzędzi.";
		}
		if ("item/logs".equals(tileset)) {
			return "Oto stos drewnianych belek.";
		}
		if (tileset.endsWith("mieszczanin_tracks")) {
			return "Oto ślady pozostawione na ziemi.";
		}
		return "Oto przedmiot należący do tej sceny zadania.";
	}

	public String getOwnerName() {
		return get(PERCEPTION_VALUE_ATTRIBUTE);
	}

	public boolean isOwnedBy(final Entity entity) {
		return entity instanceof Player && getOwnerName().equals(entity.getName());
	}

	@Override
	public boolean isObstacle(final Entity entity) {
		if (!solid || !(entity instanceof RPEntity)) {
			return false;
		}
		return blocksAllRPEntities || isOwnedBy(entity);
	}

	@Override
	public String getDescriptionName() {
		return "przedmiot sceny zadania";
	}
}
