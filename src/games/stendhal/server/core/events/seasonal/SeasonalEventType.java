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
	MINE_TOWN_CONSTRUCTION("minetownconstruction", "Budowa Mine Town Revival Weeks",
			MineTownConstructionEventPlan.PROPERTY, "mine-town-construction", "construction"),
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

	/**
	 * Reads the canonical runtime state directly from the property owned by this
	 * registry entry. This keeps status reporting independent from event-specific
	 * service methods while preserving those methods as compatibility wrappers.
	 */
	public boolean isEnabled() {
		return System.getProperty(property) != null;
	}

	public boolean request(final SeasonalEventService service, final boolean enabled,
			final SeasonalEventService.ResultListener listener) {
		switch (this) {
		case CHRISTMAS:
			return service.requestChristmas(enabled, listener);
		case MINE_TOWN:
			return service.requestMineTown(enabled, listener);
		case MINE_TOWN_CONSTRUCTION:
			return service.requestMineTownConstruction(enabled, listener);
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
