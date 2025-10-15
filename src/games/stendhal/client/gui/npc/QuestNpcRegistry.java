/***************************************************************************
 *                   (C) Copyright 2024 - Polanie OnLine                   *
 ***************************************************************************/
package games.stendhal.client.gui.npc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.client.entity.NPC;
import games.stendhal.client.sprite.DataLoader;
import marauroa.common.game.RPObject;

/**
 * Loads quest giver metadata extracted from the server quest definitions.
 */
public final class QuestNpcRegistry {
	private static final Logger LOGGER = Logger.getLogger(QuestNpcRegistry.class);
	private static final String RESOURCE = "data/quest_npc_names.txt";
	private static final QuestNpcRegistry INSTANCE = new QuestNpcRegistry();

	private final Set<String> questNpcNames;

	private QuestNpcRegistry() {
		questNpcNames = loadQuestNpcNames();
	}

	/**
	 * Retrieves registry singleton.
	 *
	 * @return registry instance
	 */
	public static QuestNpcRegistry get() {
		return INSTANCE;
	}

	/**
	 * Checks whether the provided npc can offer a quest to the player at any point.
	 *
	 * @param npc target npc
	 * @return {@code true} when the npc is known to deliver quests
	 */
	public boolean hasQuestFor(final NPC npc) {
		final String name = resolveNpcName(npc);
		return (name != null) && questNpcNames.contains(normalize(name));
	}

	private Set<String> loadQuestNpcNames() {
		final InputStream stream = DataLoader.getResourceAsStream(RESOURCE);
		if (stream == null) {
			LOGGER.warn("Quest NPC metadata not found: " + RESOURCE);
			return Collections.emptySet();
		}

		final Set<String> names = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					names.add(normalize(line));
				}
			}
		} catch (final IOException e) {
			LOGGER.error("Failed to load quest NPC metadata", e);
		}
		return names;
	}

	private String resolveNpcName(final NPC npc) {
		if (npc == null) {
			return null;
		}
		final RPObject object = npc.getRPObject();
		if ((object != null) && object.has("cloned")) {
			return object.get("cloned");
		}
		final String title = npc.getTitle();
		if ((title != null) && !title.isEmpty()) {
			return title;
		}
		return npc.getName();
	}

	private String normalize(final String name) {
		return name.toLowerCase(Locale.ROOT);
	}
}
