/***************************************************************************
 *                   Copyright (C) 2026 - PolanieOnLine                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.QuestMarker;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.*;
import games.stendhal.server.entity.npc.fsm.QuestMarkerConditions;
import games.stendhal.server.entity.npc.fsm.TransitionContext;
import games.stendhal.server.maps.quests.AbstractQuest;
import marauroa.common.game.DetailLevel;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class QuestMarkerServiceTest {
	private Player player;
	private SpeakerNPC npc;
	private StendhalRPZone zone;
	private final List<TestQuest> loaded = new ArrayList<>();

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		player = PlayerTestHelper.createPlayer("marker_player");
		player.setLevel(30);
		zone = new StendhalRPZone("marker_zone", 30, 30);
		npc = SpeakerNPCTestHelper.createSpeakerNPC("Marker NPC");
		zone.add(npc);
		zone.add(player);
	}

	@After
	public void cleanup() {
		for (final TestQuest quest : loaded) {
			SingletonRepository.getStendhalQuestSystem().unloadQuest(quest);
		}
		SingletonRepository.getNPCList().remove(npc.getName());
		PlayerTestHelper.removeAllPlayers();
	}

	private TestQuest load(final String slot, final boolean repeatable) {
		final TestQuest quest = new TestQuest(slot, repeatable);
		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
		loaded.add(quest);
		return quest;
	}

	private void prerequisites() {
		player.setQuest("marker_prerequisite", "done");
	}

	private void cheese() {
		final StackableItem cheese = (StackableItem) SingletonRepository.getEntityManager().getItem("ser");
		cheese.setQuantity(2);
		assertTrue(player.equipToInventoryOnly(cheese));
	}

	@Test
	public void availabilityUsesPrerequisitesLevelAndAcceptanceConditions() {
		load("marker_offer", false);
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		prerequisites();
		player.setLevel(10);
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		player.setLevel(30);
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void acceptanceGuardCanHideAnOtherwiseUnconditionalOffer() {
		final TestQuest quest = load("marker_acceptance", false);
		TransitionContext.setMarkerQuest(quest);
		try {
			npc.add(ConversationStates.ATTENDING, "quest", null, ConversationStates.QUEST_OFFERED, null, null);
			npc.add(ConversationStates.QUEST_OFFERED, "yes", new LevelGreaterThanCondition(40),
					ConversationStates.ATTENDING, null, new SetQuestAction(quest.slot, "start"));
		} finally {
			TransitionContext.setMarkerQuest(null);
		}
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		player.setLevel(50);
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void suggestedLevelDoesNotHideAQuestThatActuallyAcceptsThePlayer() {
		final TestQuest quest = new TestQuest("marker_suggested", false) {
			@Override public int getMinLevel() { return 100; }
		};
		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
		loaded.add(quest);
		prerequisites();
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void transitionsFromProgressToRewardWithoutStartingOrCompletingQuest() {
		final TestQuest quest = load("marker_progress", false);
		prerequisites();
		player.setQuest(quest.slot, "start");
		npc.getEngine().setCurrentState(ConversationStates.IDLE);
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, npc));
		cheese();
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, npc));
		assertEquals("start", player.getQuest(quest.slot));
		assertEquals(2, player.getNumberOfEquipped("ser"));
		assertEquals(0, quest.actions);
		assertEquals(ConversationStates.IDLE, npc.getEngine().getCurrentState());
		player.drop("ser", 1);
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void completedQuestDisappearsAndRejectedQuestCanBeOfferedAgain() {
		final TestQuest quest = load("marker_finished", false);
		prerequisites();
		player.setQuest(quest.slot, "done");
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		player.setQuest(quest.slot, "rejected");
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void repeatableQuestRespectsCooldownWithoutMapChange() {
		final TestQuest quest = load("marker_repeat", true);
		prerequisites();
		assertEquals(QuestMarker.REPEATABLE, QuestMarkerService.getMarker(player, npc));
		player.setQuest(quest.slot, "done;" + System.currentTimeMillis());
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		player.setQuest(quest.slot, "done;0");
		assertEquals(QuestMarker.REPEATABLE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void eachPlayerSeesDifferentMarkerAtTheSameNpc() {
		final TestQuest quest = load("marker_private", false);
		prerequisites();
		player.setQuest(quest.slot, "start");
		cheese();
		final Player other = PlayerTestHelper.createPlayer("marker_other");
		other.setLevel(30);
		other.setQuest("marker_prerequisite", "done");
		zone.add(other);
		QuestMarkerService.update(player);
		QuestMarkerService.update(other);
		final String id = String.valueOf(npc.getID().getObjectID());
		assertEquals("ready", player.get(QuestMarker.ATTRIBUTE, id));
		assertEquals("available", other.get(QuestMarker.ATTRIBUTE, id));
		final StringBuilder publicJson = new StringBuilder();
		player.writeToJson(publicJson, DetailLevel.NORMAL);
		assertFalse(publicJson.toString().contains(QuestMarker.ATTRIBUTE));
		final StringBuilder privateJson = new StringBuilder();
		player.writeToJson(privateJson, DetailLevel.PRIVATE);
		assertTrue(privateJson.toString().contains(QuestMarker.ATTRIBUTE));
	}

	@Test
	public void rewardHasPriorityAndZoneChangesClearOldNpcIdentifiers() {
		final TestQuest reward = load("marker_priority_reward", true);
		load("marker_priority_offer", false);
		prerequisites();
		player.setQuest(reward.slot, "start");
		cheese();
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, npc));
		QuestMarkerService.update(player);
		final String id = String.valueOf(npc.getID().getObjectID());
		assertTrue(player.has(QuestMarker.ATTRIBUTE, id));
		zone.remove(player);
		new StendhalRPZone("marker_empty_zone", 30, 30).add(player);
		QuestMarkerService.update(player);
		assertFalse(player.has(QuestMarker.ATTRIBUTE, id));
		assertEquals("marker_empty_zone", player.get(QuestMarker.ATTRIBUTE, QuestMarker.ZONE_KEY));
	}

	@Test
	public void hiddenAndUnloadedQuestsDoNotLeaveMarkers() {
		final TestQuest quest = load("marker_hidden", false);
		prerequisites();
		quest.visible = false;
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		quest.visible = true;
		assertTrue(SingletonRepository.getStendhalQuestSystem().unloadQuest(quest));
		loaded.remove(quest);
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
	}

	@Test
	public void unknownAndMutatingConditionsAreNotExecutedEvenUnderNegation() {
		final ChatCondition dangerous = (p, sentence, entity) -> {
			p.setQuest("marker_accidental", "done");
			return false;
		};
		final ChatCondition nested = new AndCondition(new AlwaysTrueCondition(),
				new NotCondition(new OrCondition(new AlwaysFalseCondition(), dangerous)));
		assertNull(QuestMarkerConditions.evaluate(nested, player, ConversationParser.parse("quest"), npc));
		assertFalse(player.hasQuest("marker_accidental"));
		player.setQuest("marker_kills", "start;szczur,1,0,0,0");
		final String before = player.getQuest("marker_kills");
		assertNull(QuestMarkerConditions.evaluate(new KillsQuestSlotNeedUpdateCondition(
				"marker_kills", 1, java.util.Arrays.asList("szczur", "wilk"), true), player, null, npc));
		assertEquals(before, player.getQuest("marker_kills"));
	}

	@Test
	public void loadingAndFailedLoadingAlwaysRestoreTransitionContext() {
		final TestQuest quest = load("marker_context", false);
		assertNull(TransitionContext.getMarkerQuest());
		assertTrue(npc.getTransitions().stream().anyMatch(t -> t.getMarkerQuest() == quest));
		final TestQuest broken = new TestQuest("marker_broken", false) {
			@Override public void addToWorld() { throw new IllegalStateException("test"); }
		};
		SingletonRepository.getStendhalQuestSystem().loadQuest(broken);
		assertNull(TransitionContext.getMarkerQuest());
		assertFalse(SingletonRepository.getStendhalQuestSystem().isLoaded(broken));
	}

	private class TestQuest extends AbstractQuest {
		private final String slot;
		private final boolean repeatable;
		private boolean visible = true;
		private int actions;

		TestQuest(final String slot, final boolean repeatable) {
			this.slot = slot;
			this.repeatable = repeatable;
		}

		@Override public void addToWorld() {
			fillQuestInfo(slot, slot, repeatable);
			final ConversationStates offer = repeatable
					? ConversationStates.QUEST_2_OFFERED : ConversationStates.QUEST_OFFERED;
			final ChatCondition prerequisites = new AndCondition(
					new QuestCompletedCondition("marker_prerequisite"), new LevelGreaterThanCondition(20));
			npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(new QuestNotStartedCondition(slot), prerequisites),
					offer, null, null);
			if (repeatable) {
				npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
						new AndCondition(new QuestCompletedCondition(slot), new TimePassedCondition(slot, 1, 60),
								prerequisites), offer, null, null);
			}
			npc.add(offer, ConversationPhrases.YES_MESSAGES, prerequisites,
					ConversationStates.ATTENDING, null, new MultipleActions(
							new SetQuestAction(slot, "start"), (p, sentence, raiser) -> actions++));
			npc.add(ConversationStates.ATTENDING, ConversationPhrases.FINISH_MESSAGES,
					new AndCondition(new QuestActiveCondition(slot), new PlayerHasItemWithHimCondition("ser", 2)),
					ConversationStates.QUEST_ITEM_BROUGHT, null, null);
			npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.YES_MESSAGES,
					new QuestActiveCondition(slot),
					ConversationStates.ATTENDING, null, new MultipleActions(
							new SetQuestAction(slot, "done"), (p, sentence, raiser) -> actions++));
		}

		@Override public boolean isRepeatable(final Player p) {
			return repeatable && isCompleted(p) && new TimePassedCondition(slot, 1, 60).fire(p, null, null);
		}
		@Override public boolean isVisibleOnQuestStatus(final Player p) { return visible; }
		@Override public boolean removeFromWorld() { return true; }
		@Override public String getName() { return slot; }
		@Override public String getSlotName() { return slot; }
		@Override public int getMinLevel() { return 20; }
		@Override public List<String> getHistory(final Player p) { return Collections.emptyList(); }
	}
}
