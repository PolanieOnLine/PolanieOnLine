package games.stendhal.common.constants;

/**
 * Global switch controlling the Polish currency reform.
 *
 * <p>The reform is enabled when the JVM is started with the
 * {@code -Dpol.waloryzacja} system property. When disabled the game falls back
 * to the legacy single "money" currency. Both the server and the client use
 * this flag to decide how to display and manage money.</p>
 */
public final class CurrencyReform {
	/**
	 * {@code true} when {@code -Dpol.waloryzacja} has been provided.
	 */
	public static final boolean ENABLED = System.getProperty("pol.waloryzacja") != null;

	private CurrencyReform() {
		// utility class
	}

	/**
	 * Indicates whether the multi-currency system should be used.
	 *
	 * @return {@code true} if the reform is enabled
	 */
	public static boolean useReformedCurrency() {
		return ENABLED;
	}
}
