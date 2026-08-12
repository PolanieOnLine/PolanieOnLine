/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import games.stendhal.server.core.config.XMLUtil;

/**
 * Fully prepared world resources for one Mine Town Revival state.
 */
final class MineTownEventPlan extends PreparedSeasonalEventPlan {
	static final String PROPERTY = "stendhal.minetown";

	private final ChristmasZonePlan zones;

	private MineTownEventPlan(final boolean enabled, final ChristmasZonePlan zones) {
		super(enabled);
		this.zones = zones;
	}

	static MineTownEventPlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, enabled)) {
			return new MineTownEventPlan(enabled,
					ChristmasZonePlan.prepare(PROPERTY, enabled));
		}
	}

	@Override
	void apply() throws Exception {
		try {
			zones.apply();
		} catch (final Exception e) {
			throw new IllegalStateException("Aktualizacja stref Mine Town nie powiodła się: "
					+ message(e), e);
		}
	}

	private static String message(final Exception e) {
		final String value = e.getMessage();
		return value == null || value.trim().isEmpty()
				? e.getClass().getSimpleName() : value;
	}
}
