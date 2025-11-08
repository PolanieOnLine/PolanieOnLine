package games.stendhal.server.util;

import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.portal.Portal;

/**
 * Marks a portal so that missing destination validation warnings are ignored.
 */
public class IgnorePortalDestinationConfigurator implements ZoneConfigurator {
	private static final Logger LOGGER = Logger.getLogger(IgnorePortalDestinationConfigurator.class);

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String xValue = attributes.get("x");
		final String yValue = attributes.get("y");

		if ((xValue == null) || (yValue == null)) {
			LOGGER.warn("Missing coordinates for IgnorePortalDestinationConfigurator in zone " + zone.getName());
			return;
		}

		try {
			final int x = Integer.parseInt(xValue);
			final int y = Integer.parseInt(yValue);
			final Portal portal = zone.getPortal(x, y);
			if (portal == null) {
				LOGGER.warn("No portal at " + zone.getName() + "[" + x + "," + y + "] to ignore destination check");
				return;
			}
			portal.setIgnoreNoDestination(true);
		} catch (final NumberFormatException e) {
			LOGGER.warn("Invalid coordinates for IgnorePortalDestinationConfigurator: x=" + xValue + ", y=" + yValue, e);
		}
	}
}
