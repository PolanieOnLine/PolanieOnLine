/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

/**
 * Registry of seasonal events which can be switched at runtime.
 *
 * The registry owns canonical command names, aliases, display names and
 * system properties so administrative entry points do not maintain a second
 * independent list of supported events.
 */
public enum SeasonalEventType {
	CHRISTMAS("christmas", "Christmas", ChristmasEventPlan.PROPERTY, "xmas"),
	MINE_TOWN("minetown", "Mine Town Revival Weeks", MineTownEventPlan.PROPERTY,
			"mine-town", "revival"),
	EASTER("easter", "Easter", EasterEventPlan.PROPERTY);

	private final String commandName;
	private final String displayName;
	private final String property;
	private final String[] aliases;

	SeasonalEventType(final String commandName, final String displayName,
			final String property, final String... aliases) {
		this.commandName = commandName;
		this.displayName = displayName;
		this.property = property;
		this.aliases = aliases;
	}

	public String getCommandName() {
		return commandName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getProperty() {
		return property;
	}

	public boolean isEnabled(final SeasonalEventService service) {
		switch (this) {
		case CHRISTMAS:
			return service.isChristmasEnabled();
		case MINE_TOWN:
			return service.isMineTownEnabled();
		case EASTER:
			return service.isEasterEnabled();
		default:
			throw new IllegalStateException("Nieobsługiwany event sezonowy: " + this);
		}
	}

	public boolean request(final SeasonalEventService service, final boolean enabled,
			final SeasonalEventService.ResultListener listener) {
		switch (this) {
		case CHRISTMAS:
			return service.requestChristmas(enabled, listener);
		case MINE_TOWN:
			return service.requestMineTown(enabled, listener);
		case EASTER:
			return service.requestEaster(enabled, listener);
		default:
			throw new IllegalStateException("Nieobsługiwany event sezonowy: " + this);
		}
	}

	public static SeasonalEventType parse(final String value) {
		if (value == null) {
			return null;
		}
		final String normalized = value.trim().toLowerCase();
		for (final SeasonalEventType event : values()) {
			if (event.commandName.equals(normalized)) {
				return event;
			}
			for (final String alias : event.aliases) {
				if (alias.equals(normalized)) {
					return event;
				}
			}
		}
		return null;
	}
}
