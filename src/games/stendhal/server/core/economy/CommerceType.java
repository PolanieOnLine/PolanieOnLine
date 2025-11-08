package games.stendhal.server.core.economy;

/**
 * Type of commerce interaction for dynamic pricing calculations.
 */
public enum CommerceType {
	/** Merchant sells goods to the player. */
	NPC_SELLING,
	/** Merchant buys goods from the player. */
	NPC_BUYING;
}
