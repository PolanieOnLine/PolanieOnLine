/*
 * @(#) src/games/stendhal/server/config/ZoneGroupsXMLLoader.java
 *
 * $Id$
 */

package games.stendhal.server.core.config;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Load and configure zones via an XML configuration file.
 */
public class ZoneGroupsXMLLoader extends DefaultHandler {


	private static final Logger LOGGER = Logger.getLogger(ZoneGroupsXMLLoader.class);

	/** The main zone configuration file. */
	protected URI uri;

	/**
	 * Create an xml based loader of zone groups.
	 *
	 * @param uri
	 *            The location of the configuration file.
	 */
	public ZoneGroupsXMLLoader(final URI uri) {
		this.uri = uri;
	}

	/**
	 * Returns zone group URIs without loading them into the world.
	 *
	 * Runtime configuration validation uses the same master list as normal
	 * server startup, so newly added zone groups are discovered automatically.
	 *
	 * @return zone group URIs
	 * @throws SAXException if the group list cannot be parsed
	 * @throws IOException if the group list cannot be read
	 */
	public List<URI> getZoneGroups() throws SAXException, IOException {
		return new GroupsXMLLoader(uri).load();
	}

	/**
	 * Load zones into a world.
	 *
	 * @throws SAXException
	 *             If a SAX error occurred.
	 * @throws IOException
	 *             If an I/O error occurred.
	 */
	public void load() throws SAXException, IOException {
		final List<URI> zoneGroups = getZoneGroups();

		// Load each group
		for (final URI tempUri : zoneGroups) {
			LOGGER.debug("Loading zone group [" + tempUri + "]");

			final ZonesXMLLoader loader = new ZonesXMLLoader(tempUri);

			try {
				loader.load();
			} catch (final SAXException ex) {
				LOGGER.error("Error loading zone group: " + tempUri, ex);
			} catch (final IOException ex) {
				LOGGER.error("Error loading zone group: " + tempUri, ex);
			}
		}
	}
}
