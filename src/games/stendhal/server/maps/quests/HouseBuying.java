/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.LinkedList;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.houses.HouseBuyingMain;
import games.stendhal.server.maps.quests.houses.HouseTax;

public class HouseBuying extends AbstractQuest {
	private static final String QUEST_SLOT = "house";
	private HouseBuyingMain quest;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		quest = new HouseBuyingMain();
		quest.addToWorld();

		fillQuestInfo(
				"Kupno Domu",
				"Mieszkania można kupić w całej Faiumoni i na ziemiach Polan.",
				false);
	}

	@Override
	public LinkedList<String> getHistory(final Player player) {
		return quest.getHistory(player);
	}

	@Override
	public String getName() {
		return "Kupno Domu";
	}

	@Override
	public int getMinLevel() {
		return 50;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return quest.isCompleted(player);
	}

	@Override
	public String getNPCName() {
		return "Barrett Holmes";
	}

	/**
	 * Retrieves house tax manager.
	 *
	 * @return
	 *   {@code HouseTax} instance.
	 */
	public HouseTax getHouseTax() {
		return quest.getHouseTax();
	}
}
