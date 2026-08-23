/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import utilities.PlayerTestHelper;

public class CraftItemTaskTest {
	private static final String QUEST_SLOT = "legacy_craft";

	@Test
	public void legacyForgingStateKeepsTimestampAndMatchesExactSubstate() {
		final CraftItemTask task = new CraftItemTask()
				.waitingTime(60)
				.legacyForgingState("make");
		final ChatCondition completed = task.buildQuestCompletedCondition(QUEST_SLOT);
		final Player player = PlayerTestHelper.createPlayer("legacy_craft_player");

		player.setQuest(QUEST_SLOT, "make;" + System.currentTimeMillis());
		assertFalse(completed.fire(player, null, null));

		player.setQuest(QUEST_SLOT, "make;"
				+ (System.currentTimeMillis() - 61 * TimeUtil.MILLISECONDS_IN_MINUTE));
		assertTrue(completed.fire(player, null, null));

		player.setQuest(QUEST_SLOT, "makeup;1");
		assertFalse(completed.fire(player, null, null));
	}

	@Test
	public void malformedLegacyTimestampIsHandledAsExpiredInsteadOfThrowing() {
		final CraftItemTask task = new CraftItemTask()
				.waitingTime(60)
				.legacyForgingState("make");
		final ChatCondition completed = task.buildQuestCompletedCondition(QUEST_SLOT);
		final Player player = PlayerTestHelper.createPlayer("broken_legacy_craft_player");

		player.setQuest(QUEST_SLOT, "make");
		assertTrue(completed.fire(player, null, null));

		player.setQuest(QUEST_SLOT, "make;not_a_timestamp");
		assertTrue(completed.fire(player, null, null));
	}

	@Test
	public void soloKillRequirementDoesNotAcceptSharedKill() {
		final CraftItemTask task = new CraftItemTask().requestSoloKill("archanioł");
		final Player player = PlayerTestHelper.createPlayer("solo_kill_craft_player");

		player.setSharedKill("archanioł");
		assertFalse(task.requiredConditionsBeforeForge().fire(player, null, null));

		player.setSoloKill("archanioł");
		assertTrue(task.requiredConditionsBeforeForge().fire(player, null, null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void legacyForgingStateRejectsWholeSerializedState() {
		new CraftItemTask().legacyForgingState("make;123");
	}

	@Test
	public void craftedQuestItemUsesCommonWhenRarityIsNotSet() {
		final CraftItemTask task = new CraftItemTask().craftItem("złota ciupaga");
		final ChatAction action = task.buildQuestCompleteAction(QUEST_SLOT);

		assertTrue(action.toString().contains("questRarity=COMMON"));
	}

	@Test
	public void craftedQuestItemUsesExplicitEpicRarity() {
		final CraftItemTask task = new CraftItemTask()
				.craftItem("złota ciupaga")
				.rarity(ItemRarity.EPIC);
		final ChatAction action = task.buildQuestCompleteAction(QUEST_SLOT);

		assertTrue(action.toString().contains("questRarity=EPIC"));
		assertTrue(action.toString().contains("randomizeModifiers=false"));
		assertTrue(action.toString().contains("generateAffixes=true"));
	}
}
