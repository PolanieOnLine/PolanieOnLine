/***************************************************************************
 *                   (C) Copyright 2020-2021 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.revivalweeks.BuilderNPC;
import games.stendhal.server.maps.quests.revivalweeks.LoadableContent;

/**
 * Sets up the construction of Mine Town Revival Weeks.
 *
 * The runtime property is owned by SeasonalEventService. This quest only
 * manages its world content so loading or unloading it cannot race with the
 * event controller or independently change the canonical event state.
 */
public class MineTownRevivalWeeksConstruction extends AbstractQuest {
	private static final String QUEST_SLOT = "semos_mine_town_revival_construction";
	public static final String QUEST_NAME = "Budowa Festiwalu Odrodzenia Miasta Kopalni";

	private final List<LoadableContent> content = new LinkedList<LoadableContent>();

	@Override
	public void addToWorld() {
		content.add(new BuilderNPC());

		for (final LoadableContent loadableContent : content) {
			loadableContent.addToWorld();
		}
	}

	/**
	 * Removes a quest from the world.
	 *
	 * @return true, if the quest could be removed; false otherwise.
	 */
	@Override
	public boolean removeFromWorld() {
		for (final LoadableContent loadableContent : content) {
			loadableContent.removeFromWorld();
		}
		content.clear();
		return true;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return QUEST_NAME;
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}
