/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

/**
 * Already prepared runtime state for one seasonal event.
 *
 * Implementations may parse XML/TMX and validate resources while they are
 * prepared, but {@link #apply()} must only mutate the live world using data
 * which has already been prepared.
 */
abstract class PreparedSeasonalEventPlan {
	private final boolean enabled;

	PreparedSeasonalEventPlan(final boolean enabled) {
		this.enabled = enabled;
	}

	final boolean isEnabled() {
		return enabled;
	}

	abstract void apply() throws Exception;
}
