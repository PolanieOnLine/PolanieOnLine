/***************************************************************************
 *                   (C) Copyright 2023-2024 - Stendhal                    *
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;

public class ProducerGroupsXMLLoader extends DefaultHandler {
	private static final Logger logger = Logger.getLogger(ShopGroupsXMLLoader.class);

	private final static ProducerRegister producers = ProducerRegister.get();

	private static boolean loaded = false;
	protected final URI uri;

	public ProducerGroupsXMLLoader(final URI uri) {
		this.uri = uri;
	}

	public ProducerGroupsXMLLoader(final String path) {
		this(URI.create(path));
	}

	/**
	 * Loads shops from XML and configures NPC merchants.
	 */
	public void load() {
		if (loaded) {
			logger.warn("Tried to re-load producers from XML");
			return;
		}
		loaded = true;
		loadInternal(null);
	}

	/**
	 * Loads producers from XML and configures specific NPC producer (used for testing).
	 *
	 * @param npcNames
	 *   List of NPC names.
	 */
	public void load(final String... npcNames) {
		loadInternal(Arrays.asList(npcNames));
	}

	/**
	 * Loads producers from XML and configures NPC producer.
	 *
	 * @param npcNames
	 *   List of NPC names (if `null` will configure all parsed from XML).
	 */
	private void loadInternal(final List<String> npcNames) {
		try {
			final List<URI> groups = new GroupsXMLLoader(uri).load();
			final ProducersXMLLoader producersLoader = new ProducersXMLLoader();
			final List<ProducerConfigurator> configurators = new ArrayList<>();
			for (final URI groupUri: groups) {
				producersLoader.load(groupUri);
				for (final ProducerConfigurator pc: producersLoader.getConfigurators()) {
					if (npcNames != null) {
						if (npcNames.contains(pc.npc)) {
							pc.configure();
						}
						// loading was called for specific NPCs only
						continue;
					}
				}
			}

			if (configurators.size() == 0) {
				// don't cache runner if no merchants are to be configured at startup
				return;
			}

			SingletonRepository.getCachedActionManager().register(new Runnable() {
				private final List<ProducerConfigurator> _configurators = configurators;
				@Override
				public void run() {
					for (final ProducerConfigurator mc: _configurators) {
						mc.configure();
					}
				}
			});
		} catch (final SAXException e) {
			logger.error(e);
		} catch (final IOException e) {
			logger.error(e);
		}
	}

	public static class ProducerConfigurator {
		// behaviour
		public String questSlot;
		public String produceItem;
		public Map<String, Integer> produceResources;
		public List<String> produceActivity;
		public int producePerUnit;
		public int produceTime;
		public boolean itemBound;

		// producer
		public String npc;
		public String questComplete;
		public String welcomeMessage;
		public int unitsPerTime;
		public int waitingTime;
		public boolean remind;

		public void configure() {
			final ProducerBehaviour behaviour = new ProducerBehaviour(questSlot, produceActivity, produceItem, producePerUnit, produceResources, produceTime, itemBound);

			producers.configureNPC(npc, behaviour, questComplete, welcomeMessage, unitsPerTime, waitingTime, remind);
		}
	}
}