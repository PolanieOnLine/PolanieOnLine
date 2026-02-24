package games.stendhal.server.entity.player;

/**
 * Centralna konfiguracja progresji postaci.
 */
public final class ProgressionConfig {

	private ProgressionConfig() {
		// utility class
	}

	/** Minimalna liczba resetów potrzebna do aktywacji ścieżki mastery. */
	public static final int MASTERY_MIN_RESETS = 5;

	/** Maksymalny poziom mastery. Po osiągnięciu limitu dalszy mastery XP nie jest naliczany. */
	public static final int MASTERY_MAX_LEVEL = 2000;

	/** Ilość mastery XP potrzebna do zdobycia 1 poziomu mastery. */
	public static final int MASTERY_XP_PER_LEVEL = 100_000;
}
