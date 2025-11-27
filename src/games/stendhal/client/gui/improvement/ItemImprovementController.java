/***************************************************************************
 *                      (C) Copyright 2024 - PolanieOnLine                 *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.improvement;

import static games.stendhal.common.constants.Actions.IMPROVE_DO;
import static games.stendhal.common.constants.Actions.IMPROVE_LIST;
import static games.stendhal.common.constants.Actions.ITEM_ID;
import static games.stendhal.common.constants.Actions.NPC;
import static games.stendhal.common.constants.Actions.TYPE;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.InternalWindow;
import games.stendhal.client.gui.InternalWindow.CloseListener;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.NotificationType;
import games.stendhal.common.grammar.Grammar;
import marauroa.common.game.RPAction;

/**
 * Coordinates item improvement list/results with the internal improvement window.
 */
public final class ItemImprovementController {
	private static final Logger logger = Logger.getLogger(ItemImprovementController.class);
	private static final String BLACKSMITH = "Tworzymir";
	private static ItemImprovementWindow window;

	private ItemImprovementController() {
		// utility
	}

	public static void requestList(final String npcName) {
		if (!isTworzymir(npcName)) {
			return;
		}

		final RPAction action = new RPAction();
		action.put(TYPE, IMPROVE_LIST);
		action.put(NPC, npcName);
		StendhalClient.get().send(action);
	}

	public static void showList(final String npcName, final String itemsJson) {
		if (!isTworzymir(npcName)) {
			return;
		}

		final List<ItemImprovementEntry> entries = parseItems(itemsJson);
		if (entries.isEmpty()) {
			addMessage(new EventLine(npcName,
				"Nie masz przy sobie przedmiotów do ulepszania.", NotificationType.INFORMATION));
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (window == null || !window.isForNpc(npcName)) {
					final ItemImprovementWindow newWindow = new ItemImprovementWindow(npcName);
					newWindow.addCloseListener(new CloseListener() {
						@Override
						public void windowClosed(final InternalWindow closed) {
							window = null;
						}
					});
					j2DClient.get().addWindow(newWindow);
					window = newWindow;
				}

				window.updateItems(entries);
				window.setVisible(true);
				window.raise();
			}
		});
	}

	public static void handleResult(final String npcName, final boolean success, final String message) {
		if (!isTworzymir(npcName)) {
			return;
		}

		final NotificationType type = success ? NotificationType.POSITIVE : NotificationType.NEGATIVE;
		addMessage(new EventLine(npcName, message, type));

		if (window != null && window.isForNpc(npcName)) {
			requestList(npcName);
		}
	}

	public static void performUpgrade(final String npcName, final ItemImprovementEntry entry) {
		final RPAction action = new RPAction();
		action.put(TYPE, IMPROVE_DO);
		action.put(NPC, npcName);
		action.put(ITEM_ID, entry.getId());
		StendhalClient.get().send(action);
	}

	private static boolean isTworzymir(final String npcName) {
		return npcName != null && npcName.contains(BLACKSMITH);
	}

	private static List<ItemImprovementEntry> parseItems(final String itemsJson) {
		final List<ItemImprovementEntry> entries = new ArrayList<ItemImprovementEntry>();
		try {
			final JSONArray array = (JSONArray) JSONValue.parse(itemsJson);
			if (array == null) {
				return entries;
			}
			for (final Object obj : array) {
				if (!(obj instanceof JSONObject)) {
					continue;
				}
				final JSONObject item = (JSONObject) obj;
				final Number id = (Number) item.get("id");
				final String name = (String) item.get("name");
				final String icon = (String) item.get("icon");
				final Number improve = (Number) item.get("improve");
				final Number max = (Number) item.get("max");
				final Number cost = (Number) item.get("cost");
				final Object chanceObj = item.get("chance");
				@SuppressWarnings("unchecked")
				final Map<String, Number> requirements = (Map<String, Number>) item.get("requirements");

				if (id == null || name == null || improve == null || max == null || cost == null
					|| requirements == null || chanceObj == null) {
					continue;
				}

				final double chance = Double.parseDouble(chanceObj.toString());
				entries.add(new ItemImprovementEntry(id.intValue(), name, icon, improve.intValue(), max.intValue(),
					cost.intValue(), chance, buildRequirements(requirements), loadIcon(icon)));
			}
		} catch (final Exception e) {
			logger.warn("Failed to parse improvement list", e);
		}
		return entries;
	}

	private static String buildRequirements(final Map<String, Number> requirements) {
		final List<String> parts = new ArrayList<String>();
		for (final Map.Entry<String, Number> entry : requirements.entrySet()) {
			parts.add(entry.getKey() + " " + entry.getValue());
		}
		return Grammar.enumerateCollection(parts, "i");
	}

	private static Sprite loadIcon(final String iconName) {
		if (iconName == null || iconName.isEmpty()) {
			return SpriteStore.get().getColoredSprite("/data/gui/bag.png", Color.LIGHT_GRAY);
		}
		try {
			final SpriteStore store = SpriteStore.get();
			final String path = "/data/sprites/items/" + iconName + ".png";
			Sprite sprite = store.getSprite(path);
			if (sprite.getWidth() > sprite.getHeight()) {
				sprite = store.getAnimatedSprite(sprite, 100);
			}
			return sprite;
		} catch (final Exception e) {
			return SpriteStore.get().getColoredSprite("/data/gui/bag.png", Color.LIGHT_GRAY);
		}
	}

	private static void addMessage(final EventLine line) {
		ClientSingletonRepository.getUserInterface().addEventLine(line);
	}
}
