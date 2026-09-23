package games.stendhal.client.gui;

import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.update.ClientGameConfiguration;

/** Connection used when no saved login profile has been selected. */
public final class StartupConnectionSettings {
	private static final String CUSTOM_PROPERTY = "ui.connection.custom";
	private static final String HOST_PROPERTY = "ui.connection.host";
	private static final String PORT_PROPERTY = "ui.connection.port";

	private StartupConnectionSettings() {
	}

	public static String getOfficialHost() {
		return ClientGameConfiguration.get("DEFAULT_SERVER");
	}

	public static int getOfficialPort() {
		return Integer.parseInt(ClientGameConfiguration.get("DEFAULT_PORT"));
	}

	public static boolean isCustom() {
		return Boolean.parseBoolean(WtWindowManager.getInstance().getProperty(CUSTOM_PROPERTY, "false"));
	}

	public static String getCustomHost() {
		String host = WtWindowManager.getInstance().getProperty(HOST_PROPERTY, getOfficialHost()).trim();
		return host.isEmpty() ? getOfficialHost() : host;
	}

	public static int getCustomPort() {
		try {
			return parsePort(WtWindowManager.getInstance().getProperty(PORT_PROPERTY,
					String.valueOf(getOfficialPort())));
		} catch (IllegalArgumentException exception) {
			return getOfficialPort();
		}
	}

	public static String getHost() {
		return isCustom() ? getCustomHost() : getOfficialHost();
	}

	public static int getPort() {
		return isCustom() ? getCustomPort() : getOfficialPort();
	}

	public static int parsePort(String value) {
		try {
			int port = Integer.parseInt(value.trim());
			if (port >= 1 && port <= 65535) {
				return port;
			}
		} catch (NumberFormatException exception) {
			// Report the same validation error for nonnumeric and out-of-range ports.
		}
		throw new IllegalArgumentException("Port musi być liczbą od 1 do 65535.");
	}

	public static void selectOfficial() {
		WtWindowManager manager = WtWindowManager.getInstance();
		manager.setProperty(CUSTOM_PROPERTY, "false");
		manager.save();
	}

	public static void selectCustom(String host, int port) {
		String trimmedHost = host.trim();
		if (trimmedHost.isEmpty() || trimmedHost.contains("/") || trimmedHost.matches(".*\\s+.*")) {
			throw new IllegalArgumentException("Podaj poprawną nazwę lub adres serwera.");
		}
		if (port < 1 || port > 65535) {
			throw new IllegalArgumentException("Port musi być liczbą od 1 do 65535.");
		}
		WtWindowManager manager = WtWindowManager.getInstance();
		manager.setProperty(HOST_PROPERTY, trimmedHost);
		manager.setProperty(PORT_PROPERTY, String.valueOf(port));
		manager.setProperty(CUSTOM_PROPERTY, "true");
		manager.save();
	}
}
