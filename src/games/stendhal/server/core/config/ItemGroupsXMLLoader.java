/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import marauroa.common.resource.ResourceProvider;

/**
 * Load and configure items via an XML configuration file.
 */
public class ItemGroupsXMLLoader extends DefaultHandler {


	private static final Logger LOGGER = Logger.getLogger(ItemGroupsXMLLoader.class);
	private static final String ITEM_INDEX_PATH = "data/conf/items.xml";
	private static final String ITEM_GROUP_PREFIX = "data/conf/items/";

	/** The main item configuration file. */
	protected URI uri;

	/**
	 * Create an xml based loader of item groups.
	 *
	 * @param uri
	 *            The location of the configuration file.
	 */
	public ItemGroupsXMLLoader(final URI uri) {
		this.uri = uri;
	}

	/**
	 * Load items.
	 *
	 * @return list of items
	 * @throws SAXException
	 *             If a SAX error occurred.
	 * @throws IOException
	 *             If an I/O error occurred.
	 */
	public List<DefaultItem> load() throws SAXException, IOException {
		final GroupsXMLLoader groupsLoader = new GroupsXMLLoader(uri);
		final List<URI> groups = groupsLoader.load();

		final ItemsXMLLoader loader = new ItemsXMLLoader();
		final List<DefaultItem> list = new LinkedList<DefaultItem>();
		for (final URI groupUri : groups) {
			LOGGER.debug("Loading item group [" + groupUri + "]");
			list.addAll(loader.load(groupUri));
		}

		return list;
	}

	/**
	 * Loads item definitions through an external resource provider.
	 *
	 * This path is used by safe runtime reloads. The provider receives relative
	 * paths so filesystem providers can resolve them below the configured
	 * server base directory while classpath providers use the same resource
	 * names. Runtime group references are restricted to the existing item
	 * configuration subtree and cannot escape to arbitrary server files.
	 *
	 * @param provider resource provider
	 * @return list of items
	 * @throws SAXException if XML parsing fails
	 * @throws IOException if a resource cannot be opened
	 */
	public List<DefaultItem> load(final ResourceProvider provider) throws SAXException, IOException {
		if (provider == null) {
			throw new IllegalArgumentException("resource provider must not be null");
		}

		final List<URI> groups;
		try (InputStream in = provider.open(toProviderPath(uri))) {
			groups = new GroupsXMLLoader(uri).load(in);
		}

		final ItemsXMLLoader loader = new ItemsXMLLoader();
		final List<DefaultItem> list = new LinkedList<DefaultItem>();
		for (final URI groupUri : groups) {
			LOGGER.debug("Loading item group through resource provider [" + groupUri + "]");
			try (InputStream in = provider.open(toProviderPath(groupUri))) {
				list.addAll(loader.load(in));
			}
		}
		return list;
	}

	private static String toProviderPath(final URI resource) {
		String path = resource.normalize().getPath();
		while (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (!ITEM_INDEX_PATH.equals(path) && !path.startsWith(ITEM_GROUP_PREFIX)) {
			throw new IllegalArgumentException("Runtime item reload cannot read resource outside "
					+ ITEM_GROUP_PREFIX + ": " + path);
		}
		return path;
	}
}
