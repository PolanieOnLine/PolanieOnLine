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
final class EasterEventPlan {
	static final String PROPERTY = "stendhal.easter";

	private final boolean enabled;
	private final ChristmasZonePlan zones;

	private EasterEventPlan(final boolean enabled, final ChristmasZonePlan zones) {
		this.enabled = enabled;
		this.zones = zones;
	}

	static EasterEventPlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, enabled)) {
			return new EasterEventPlan(enabled,
					ChristmasZonePlan.prepare(PROPERTY, enabled));
		}
	}

	boolean isEnabled() {
		return enabled;
	}

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
