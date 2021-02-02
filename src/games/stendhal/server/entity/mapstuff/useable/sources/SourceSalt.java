/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.useable.sources;

import games.stendhal.server.entity.mapstuff.useable.SourceEntity;
import games.stendhal.server.entity.player.Player;

/**
 * A obsidian source is a spot where a player can prospect for obsidian. He
 * needs a kilof and lina, time, and luck.
 *
 * Prospecting takes 7-11 seconds; during this time, the player keep standing
 * next to the obsidian source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two obsidian sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect for obsidian at several sites simultaneously.
 *
 * @author daniel
 * @changes artur, KarajuSs
 */
public class SourceSalt extends SourceEntity {
	private final static String sourceClass = "source_salt";

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a salt source.
	 */
	public SourceSalt() {
		this("sól");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a salt source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceSalt(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", sourceClass);
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");

		setResistance(100);
	}

	/**
	 * Called when the activity has finished.
	 *
	 * @param player
	 *            The player that did the activity.
	 * @param successful
	 *            If the activity was successful.
	 */
	@Override
	protected void onFinished(final Player player, final boolean successful) {
		setMiningXP(player, successful, itemName, 70);
	}

	/**
	 * Called when the activity has started.
	 *
	 * @param player
	 *            The player starting the activity.
	 */
	@Override
	protected void onStarted(final Player player) {
		sendMessage(player, "Rozpocząłeś wydobywanie surowca.");
	}
}
