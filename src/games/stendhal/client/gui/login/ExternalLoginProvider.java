package games.stendhal.client.gui.login;

/** Providers supported by the website's confirmed desktop login flow. */
enum ExternalLoginProvider {
	GOOGLE("google", "Google"), STEAM("steam", "Steam");

	final String id;
	final String label;

	ExternalLoginProvider(final String id, final String label) {
		this.id = id;
		this.label = label;
	}

	String apiPath() {
		// Keep the existing Steam endpoints compatible with deployed websites.
		return this == STEAM ? "/api/v1/auth/game-steam" : "/api/v1/auth/game/google";
	}
}
