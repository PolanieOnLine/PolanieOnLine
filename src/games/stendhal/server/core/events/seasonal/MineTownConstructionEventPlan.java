/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import games.stendhal.server.core.config.XMLUtil;

/**
 * Fully prepared world resources for one Mine Town construction state.
 */
final class MineTownConstructionEventPlan extends PreparedSeasonalEventPlan {
	static final String PROPERTY = "stendhal.minetownconstruction";

	private final SeasonalZonePlan zones;

	private MineTownConstructionEventPlan(final boolean enabled,
			final SeasonalZonePlan zones) {
		super(enabled);
		this.zones = zones;
	}

	static MineTownConstructionEventPlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, enabled)) {
			return new MineTownConstructionEventPlan(enabled,
					SeasonalZonePlan.prepare(PROPERTY, enabled));
		}
	}

	@Override
	void apply() throws Exception {
		try {
			zones.apply();
		} catch (final Exception e) {
			throw new IllegalStateException(
					"Aktualizacja stref budowy Mine Town nie powiodła się: " + message(e), e);
		}
	}

	private static String message(final Exception e) {
		final String value = e.getMessage();
		return value == null || value.trim().isEmpty()
				? e.getClass().getSimpleName() : value;
	}
}
