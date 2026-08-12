/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import games.stendhal.server.core.config.XMLUtil;

/**
 * Fully prepared world resources for one Easter state.
 *
 * The Easter Bunny remains a separate runtime event controlled by
 * {@code stendhal.easterbunny}; this plan only owns {@code stendhal.easter}.
 */
final class EasterEventPlan extends PreparedSeasonalEventPlan {
	static final String PROPERTY = "stendhal.easter";

	private final SeasonalZonePlan zones;

	private EasterEventPlan(final boolean enabled, final SeasonalZonePlan zones) {
		super(enabled);
		this.zones = zones;
	}

	static EasterEventPlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, enabled)) {
			return new EasterEventPlan(enabled,
					SeasonalZonePlan.prepare(PROPERTY, enabled));
		}
	}

	@Override
	void apply() throws Exception {
		try {
			zones.apply();
		} catch (final Exception e) {
			throw new IllegalStateException("Aktualizacja stref Easter nie powiodła się: "
					+ message(e), e);
		}
	}

	private static String message(final Exception e) {
		final String value = e.getMessage();
		return value == null || value.trim().isEmpty()
				? e.getClass().getSimpleName() : value;
	}
}
