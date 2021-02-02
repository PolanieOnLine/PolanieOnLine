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
 * A carbuncle source is a spot where a player can prospect for carbuncle. He
 * needs a kilof and lina, time, and luck.
 *
 * Prospecting takes 7-11 seconds; during this time, the player keep standing
 * next to the carbuncle source. In fact, the player only has to be there when the
 * prospecting action has finished. Therefore, make sure that two carbuncle sources
 * are always at least 5 sec of walking away from each other, so that the player
 * can't prospect for carbuncle at several sites simultaneously.
 *
 * @author daniel
 * @changes artur, KarajuSs
 */
public class SourceCarbuncle extends SourceEntity {
	private final static String sourceClass = "source_carbuncle";

	/**
	 * The name of the item to be found.
	 */
	private final String itemName;

	/**
	 * Create a carbuncle source.
	 */
	public SourceCarbuncle() {
		this("kryształ rubinu");
	}

	/**
	 * source name.
	 */
	@Override
	public String getName() {
		return("surowców");
	}

	/**
	 * Create a carbuncle source.
	 *
	 * @param itemName
	 *            The name of the item to be prospected.
	 */
	public SourceCarbuncle(final String itemName) {
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
		for (final String itemName : NEEDED_PICKS) {
			if (itemName != NEEDED_PICKS[0] && player.isEquipped(itemName)) {
				return true;
			}
		}

		player.sendPrivateText("Potrzebujesz kilofa do wydobywania kamieni.");
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
		setMiningXP(player, successful, itemName, 630);
	}
}
