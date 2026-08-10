/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/** Handles the attack-locking stunned status. */
public class StunnedStatusHandler implements StatusHandler<StunnedStatus> {
	/**
	 * Player attack targets paused for the duration of a stun. Creature attack
	 * strategies already suppress attacks while preserving their target, so only
	 * players need this small bridge between the client focus and the internal
	 * attack loop.
	 */
	private final Map<Player, RPEntity> pausedPlayerTargets =
			new WeakHashMap<Player, RPEntity>();

	@Override
	public void inflict(final StunnedStatus status, final StatusList statusList,
			final Entity attacker) {
		// Never stack or refresh an active stun. This is the first anti-chain-stun
		// guard and keeps the original expiry authoritative.
		if (statusList.hasStatus(StatusType.STUNNED)) {
			return;
		}

		final RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		// Stun pauses attacks without dropping focus. Creature strategies already
		// implement that directly. For players, detach only the internal attack
		// target while leaving the public target attribute unchanged for the client.
		pausePlayerAttackKeepingFocus(entity);
		if (attacker == null) {
			entity.sendPrivateText(NotificationType.SCENE_SETTING,
					"Zostałeś ogłuszony.");
		} else {
			entity.sendPrivateText(NotificationType.SCENE_SETTING,
					"Zostałeś ogłuszony przez " + attacker.getName() + ".");
		}

		statusList.addInternal(status);
		statusList.activateStatusAttribute("status_" + status.getName());
		TurnNotifier.get().notifyInSeconds(status.getDurationSeconds(entity),
				new StatusRemover(statusList, status));
	}

	@Override
	public void remove(final StunnedStatus status, final StatusList statusList) {
		statusList.removeInternal(status);

		final RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}
		if (!statusList.hasStatus(StatusType.STUNNED)) {
			entity.remove("status_" + status.getName());
			resumePlayerAttackIfStillFocused(entity);
			entity.sendPrivateText(NotificationType.SCENE_SETTING,
					"Nie jesteś już ogłuszony.");
			entity.notifyWorldAboutChanges();
		}
	}

	/**
	 * Pause a player's attack loop while keeping the same target id visible to the
	 * client. Package-private for regression tests.
	 */
	void pausePlayerAttackKeepingFocus(final RPEntity entity) {
		if (!(entity instanceof Player)) {
			return;
		}

		final Player player = (Player) entity;
		final RPEntity target = player.getAttackTarget();
		if (target == null) {
			return;
		}

		pausedPlayerTargets.put(player, target);
		final int targetId = target.getID().getObjectID();
		player.stopAttack();
		// stopAttack() clears the public target attribute together with the
		// internal attack target. Put only the public id back so the client keeps
		// its focus while Player.logic() sees no active attack to execute.
		player.put("target", targetId);
	}

	/**
	 * Restore the internal attack target after stun if the player did not cancel
	 * focus and the original target is still a valid living entity in the zone.
	 * Package-private for regression tests.
	 */
	void resumePlayerAttackIfStillFocused(final RPEntity entity) {
		if (!(entity instanceof Player)) {
			return;
		}

		final Player player = (Player) entity;
		final RPEntity target = pausedPlayerTargets.remove(player);
		if (target == null) {
			return;
		}

		final int targetId = target.getID().getObjectID();
		if (!player.has("target") || player.getInt("target") != targetId) {
			// The player explicitly changed/cancelled focus while stunned.
			return;
		}

		if (player.getHP() <= 0 || target.getHP() <= 0
				|| player.getZone() == null || player.getZone() != target.getZone()
				|| !player.getZone().has(target.getID())) {
			// Do not leave a stale client focus behind if the remembered target died
			// or disappeared while the attack was paused.
			player.remove("target");
			return;
		}

		player.setTarget(target);
	}
}
