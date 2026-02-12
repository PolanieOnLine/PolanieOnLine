package games.stendhal.server.skilltree;

/**
 * Possible outcomes when attempting to unlock a skill tree node.
 */
public enum SkillTreeUnlockResult {
    SUCCESS,
    UNKNOWN_NODE,
    ALREADY_UNLOCKED,
    PREREQUISITES_MISSING,
    NOT_ENOUGH_POINTS
}

