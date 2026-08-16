/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class QuestTaskBuilderTest {
	@Test
	public void completedQuestCanBeRequiredBeforeStartingTask() {
		final Player player = PlayerTestHelper.createPlayer("quest_builder");
		final KillCreaturesTask task = new KillCreaturesTask();
		task.requireCompletedQuest("previous_quest");

		final ChatCondition condition = task.buildQuestPreCondition("next_quest");
		assertFalse(condition.fire(player, null, null));

		player.setQuest("previous_quest", "done");
		assertTrue(condition.fire(player, null, null));
	}
}
