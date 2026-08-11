/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import games.stendhal.server.core.config.XMLUtil;

/**
 * Fully prepared set of resources for one Christmas state.
 */
final class ChristmasEventPlan {
	static final String PROPERTY = "stendhal.christmas";

	private final boolean enabled;
	private final ChristmasZonePlan zones;
	private final ChristmasCreaturePlan creatures;

	private ChristmasEventPlan(final boolean enabled,
			final ChristmasZonePlan zones,
			final ChristmasCreaturePlan creatures) {
		this.enabled = enabled;
		this.zones = zones;
		this.creatures = creatures;
	}

	/**
	 * Prepares every conditional Christmas resource without modifying the
	 * process wide system property or the active world.
	 *
	 * @param enabled target event state
	 * @return prepared plan
	 * @throws Exception if any dependent resource cannot be prepared
	 */
	static ChristmasEventPlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored =
				XMLUtil.overrideCondition(PROPERTY, enabled)) {
			return new ChristmasEventPlan(enabled,
					ChristmasZonePlan.prepare(enabled),
					ChristmasCreaturePlan.prepare(enabled));
		}
	}

	boolean isEnabled() {
		return enabled;
	}

	/**
	 * Applies only already prepared data. No TMX or XML parsing happens here.
	 *
	 * @throws Exception if applying a prepared resource fails
	 */
	void apply() throws Exception {
		try {
			creatures.apply();
		} catch (final Exception e) {
			throw new IllegalStateException("Aktualizacja definicji stworzeń nie powiodła się: "
					+ message(e), e);
		}
		try {
			zones.apply();
		} catch (final Exception e) {
			throw new IllegalStateException("Aktualizacja stref nie powiodła się: "
					+ message(e), e);
		}
	}

	private static String message(final Exception e) {
		final String value = e.getMessage();
		return value == null || value.trim().isEmpty()
				? e.getClass().getSimpleName() : value;
	}
}
