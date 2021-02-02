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
public class SourceObsidian extends SourceEntity {
	private final static String sourceClass = "source_obsidian";

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a obsidian source.
	 */
	public SourceObsidian() {
		this("kryształ obsydianu");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a obsidian source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceObsidian(final String itemName) {
		this.itemName = itemName;

		setRPClass("useable_entity");
		put("type", "useable_entity");
		put("class", "source");
		put("name", sourceClass);
		setMenu("Wydobądź|Użyj");
		setDescription("Wszystko wskazuje na to, że tutaj coś jest.");

		setResistance(100);
	}

	@Override
	protected boolean isPrepared(final Player player) {
		if (player.isEquipped(NEEDED_PICKS[2])
			|| player.isEquipped(NEEDED_PICKS[3])
			|| player.isEquipped(NEEDED_PICKS[4])) { 
			return true;
		}

		player.sendPrivateText("Potrzebujesz mocniejszego kilofa do wydobywania takiego kamienia.");
		return false;
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
		setMiningXP(player, successful, itemName, 750);
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
