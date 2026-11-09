/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.entity;

import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.player.Player;

/** Shared ownership rules for actors in a player's scene on a public map. */
public final class PlayerPrivateScene {

	private static final String PERCEPTION_KEY = "#perception_key";
	private static final String PERCEPTION_VALUE = "#perception_value";
	private static final String OWNER_COLLISION_ONLY = "owner_collision_only";

	private PlayerPrivateScene() {
	}

	/** Uses the existing server perception filter and client collision flag. */
	public static void assign(final Entity entity, final Player owner) {
		if (entity == null || owner == null) {
			throw new IllegalArgumentException("A private scene requires an entity and owner");
		}
		entity.put(PERCEPTION_KEY, "name");
		entity.put(PERCEPTION_VALUE, owner.getName());
		entity.put(OWNER_COLLISION_ONLY, "");
	}

	/** Returns the private scene owner, or null for an ordinary entity. */
	public static String ownerName(final Entity entity) {
		if (entity != null && entity.has(PERCEPTION_KEY) && "name".equals(entity.get(PERCEPTION_KEY))
				&& entity.has(PERCEPTION_VALUE)) {
			return entity.get(PERCEPTION_VALUE);
		}
		return null;
	}

	/** Preserve scene visibility when an actor leaves a corpse. */
	public static void copyOwner(final Entity target, final Entity source) {
		String owner = ownerName(source);
		if (owner != null) {
			target.put(PERCEPTION_KEY, "name");
			target.put(PERCEPTION_VALUE, owner);
			if (source.has(OWNER_COLLISION_ONLY)) {
				target.put(OWNER_COLLISION_ONLY, "");
			}
		}
	}

	public static boolean isOwnedBy(final Entity entity, final Player player) {
		return player != null && player.getName().equals(ownerName(entity));
	}

	/**
	 * Ordinary pairs keep their existing rules. A private actor can interact
	 * only with its owner, that owner's companions, or actors of the same scene.
	 * A null source represents environmental damage, not another scene actor.
	 */
	public static boolean canInteract(final Entity first, final Entity second) {
		if (first == null || second == null) {
			return true;
		}
		String firstOwner = ownerName(first);
		String secondOwner = ownerName(second);
		if (firstOwner == null && secondOwner == null) {
			return true;
		}
		return firstOwner != null ? firstOwner.equals(participantOwner(second))
				: secondOwner.equals(participantOwner(first));
	}

	/** Keep public map scenery solid while phasing unrelated scene actors. */
	public static boolean canCollide(final Entity first, final Entity second) {
		if (!first.has(OWNER_COLLISION_ONLY) && !second.has(OWNER_COLLISION_ONLY)) {
			return true;
		}
		if ((!(first instanceof RPEntity) && ownerName(first) == null)
				|| (!(second instanceof RPEntity) && ownerName(second) == null)) {
			return true;
		}
		return canInteract(first, second);
	}

	private static String participantOwner(final Entity entity) {
		String owner = ownerName(entity);
		if (owner != null) {
			return owner;
		}
		if (entity instanceof Player) {
			return entity.getName();
		}
		if (entity instanceof DomesticAnimal) {
			Player player = ((DomesticAnimal) entity).getOwner();
			return player == null ? null : player.getName();
		}
		return null;
	}
}
