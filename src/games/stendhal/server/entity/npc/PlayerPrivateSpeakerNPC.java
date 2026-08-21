/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.npc;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/**
 * Speaker NPC that belongs to one player and is filtered from every other
 * player's perception.
 *
 * The internal name is unique per owner while the visible title remains the
 * normal character name. Collision is also private, so another player cannot
 * be blocked by an NPC that is invisible to them.
 */
public class PlayerPrivateSpeakerNPC extends SpeakerNPC {

	public static final String PERCEPTION_KEY_ATTRIBUTE = "#perception_key";
	public static final String PERCEPTION_VALUE_ATTRIBUTE = "#perception_value";
	public static final String OWNER_COLLISION_ONLY_ATTRIBUTE = "owner_collision_only";
	private static final String PLAYER_NAME_ATTRIBUTE = "name";

	private final String ownerName;

	public PlayerPrivateSpeakerNPC(final Player owner, final String visibleName) {
		super(createInternalName(owner, visibleName));
		if (owner == null) {
			throw new IllegalArgumentException("owner must not be null");
		}
		if (visibleName == null || visibleName.isEmpty()) {
			throw new IllegalArgumentException("visibleName must not be empty");
		}

		ownerName = owner.getName();
		setTitle(visibleName);
		put(PERCEPTION_KEY_ATTRIBUTE, PLAYER_NAME_ATTRIBUTE);
		put(PERCEPTION_VALUE_ATTRIBUTE, ownerName);
		put(OWNER_COLLISION_ONLY_ATTRIBUTE, "");
		hideLocation();
	}

	private static String createInternalName(final Player owner, final String visibleName) {
		if (owner == null) {
			throw new IllegalArgumentException("owner must not be null");
		}
		if (visibleName == null || visibleName.isEmpty()) {
			throw new IllegalArgumentException("visibleName must not be empty");
		}
		return visibleName + " prywatny " + owner.getName();
	}

	public String getOwnerName() {
		return ownerName;
	}

	public boolean isOwnedBy(final Entity entity) {
		return entity instanceof Player && ownerName.equals(entity.getName());
	}

	/**
	 * If a private quest NPC receives a route during a conversation, let the
	 * current conversation finish before it starts travelling. Once it is on
	 * the road, new greetings are ignored until FixedPath reaches its end.
	 */
	@Override
	public void preLogic() {
		if (!hasPath() || isTalking()) {
			super.preLogic();
			return;
		}

		if (getAttending() != null) {
			setAttending(null);
		}
		if (has("text")) {
			remove("text");
		}

		setSpeed(getBaseSpeed());
		applyMovement();
		notifyWorldAboutChanges();
	}

	@Override
	public boolean isObstacle(final Entity entity) {
		return entity instanceof RPEntity && isOwnedBy(entity);
	}
}
