/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/**
 * Short crowd-control status which prevents attacks without preventing movement.
 *
 * <p>Stun intentionally does not stack or refresh. The handler ignores another
 * stun while one is already active so repeated procs cannot extend one control
 * window indefinitely.</p>
 */
public class StunnedStatus extends Status {
	/** Default duration when the stunned target is a player. */
	public static final int PLAYER_DURATION_SECONDS = 4;
	/** Default duration when the stunned target is a creature or other entity. */
	public static final int CREATURE_DURATION_SECONDS = 3;

	/** Zero means that the duration should be selected from the target type. */
	private final int explicitDurationSeconds;

	/** Creates a stun whose duration is selected from the stunned target type. */
	public StunnedStatus() {
		super("stunned");
		explicitDurationSeconds = 0;
	}

	/**
	 * Creates a stun with an explicit deterministic duration.
	 *
	 * @param durationSeconds duration in seconds, must be positive
	 */
	public StunnedStatus(final int durationSeconds) {
		super("stunned");
		if (durationSeconds <= 0) {
			throw new IllegalArgumentException("Stun duration must be positive");
		}
		explicitDurationSeconds = durationSeconds;
	}

	/**
	 * Resolves the effective duration for the entity receiving this stun.
	 * Explicit durations always win over the standard target-based defaults.
	 *
	 * @param target stunned entity
	 * @return duration in seconds
	 */
	public int getDurationSeconds(final RPEntity target) {
		if (explicitDurationSeconds > 0) {
			return explicitDurationSeconds;
		}
		return target instanceof Player
				? PLAYER_DURATION_SECONDS : CREATURE_DURATION_SECONDS;
	}

	@Override
	public StatusType getStatusType() {
		return StatusType.STUNNED;
	}
}
