/***************************************************************************
 *                   Copyright (C) 2026 - PolanieOnLine                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.QuestMarker;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.Expression;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.QuestMarkerConditions;
import games.stendhal.server.entity.npc.fsm.Transition;
import games.stendhal.server.maps.quests.IQuest;

/** Publishes only the current player's quest indicators, never public NPC state. */
public final class QuestMarkerService {
	private static final Logger logger = Logger.getLogger(QuestMarkerService.class);

	private QuestMarkerService() {
	}

	/** Updates a small private map only when its contents actually change. */
	public static void update(final Player player) {
		final Map<String, String> next = new LinkedHashMap<>();
		final StendhalRPZone zone = player.getZone();
		if (zone != null) {
			next.put(QuestMarker.ZONE_KEY, zone.getID().getID());
			for (final NPC entity : zone.getNPCList()) {
				if (entity instanceof SpeakerNPC && entity.getID() != null) {
					final QuestMarker marker = getMarker(player, (SpeakerNPC) entity);
					if (marker != QuestMarker.NONE) {
						next.put(String.valueOf(entity.getID().getObjectID()), marker.getCode());
					}
				}
			}
		}
		final Map<String, String> previous = player.hasMap(QuestMarker.ATTRIBUTE)
				? player.getMap(QuestMarker.ATTRIBUTE) : Collections.emptyMap();
		if (next.equals(previous)) {
			return;
		}
		for (final String key : new ArrayList<>(previous.keySet())) {
			if (!next.containsKey(key)) {
				player.remove(QuestMarker.ATTRIBUTE, key);
			}
		}
		for (final Map.Entry<String, String> entry : next.entrySet()) {
			if (!entry.getValue().equals(previous.get(entry.getKey()))) {
				player.put(QuestMarker.ATTRIBUTE, entry.getKey(), entry.getValue());
			}
		}
		player.notifyWorldAboutChanges();
	}

	public static QuestMarker getMarker(final Player player, final SpeakerNPC npc) {
		final Map<IQuest, List<Transition>> bindings = new LinkedHashMap<>();
		for (final Transition transition : npc.getTransitions()) {
			final IQuest quest = transition.getMarkerQuest();
			if (quest != null) {
				bindings.computeIfAbsent(quest, key -> new ArrayList<>()).add(transition);
			}
		}
		QuestMarker result = QuestMarker.NONE;
		for (final Map.Entry<IQuest, List<Transition>> binding : bindings.entrySet()) {
			if (!SingletonRepository.getStendhalQuestSystem().isLoaded(binding.getKey())) {
				continue;
			}
			try {
				result = result.prefer(getMarkerForQuest(player, npc, binding.getKey(), binding.getValue()));
			} catch (final RuntimeException e) {
				// An optional indicator must not interrupt player logic or a broken quest.
				logger.debug("Cannot preview quest " + binding.getKey().getName(), e);
			}
		}
		return result;
	}

	/** Does not run actions, change conversation state, award items or start quests. */
	public static QuestMarker getMarkerForQuest(final Player player, final SpeakerNPC npc,
			final IQuest quest, final List<Transition> transitions) {
		if (!quest.isVisibleOnQuestStatus(player)) {
			return QuestMarker.NONE;
		}
		final QuestMarker explicit = quest.getNPCQuestMarker(player, npc);
		if (explicit != null) {
			return explicit;
		}
		final boolean completed = quest.isCompleted(player);
		final boolean rejected = "rejected".equals(player.getQuest(quest.getSlotName(), 0));
		final boolean active = quest.isStarted(player) && !completed && !rejected;
		if (completed && !quest.isRepeatable(player)) {
			return QuestMarker.NONE;
		}
		// getMinLevel is only a recommendation. Real level requirements are
		// enforced by the same offer/acceptance conditions as the conversation.
		for (final Transition transition : transitions) {
			if (!isEntryState(transition.getState())) {
				continue;
			}
			final boolean ready = active && (transition.getNextState() == ConversationStates.QUEST_ITEM_BROUGHT
					|| (transition.getCondition() != null && transition.getAction() != null
						&& transition.getAction().completesQuest(quest.getSlotName())));
			if (ready && isFulfilled(player, npc, transition)) {
				return QuestMarker.READY;
			}
		}
		if (active) {
			return QuestMarker.IN_PROGRESS;
		}
		for (final Transition transition : transitions) {
			final boolean offer = isOfferState(transition.getNextState())
					&& canAccept(player, npc, transition.getNextState(), transitions);
			final boolean direct = transition.getAction() != null
					&& transition.getAction().startsQuest(quest.getSlotName());
			if (isEntryState(transition.getState()) && (offer || direct)
					&& isFulfilled(player, npc, transition)) {
				return quest.getQuestInfo(player).getRepeatable() || completed
						? QuestMarker.REPEATABLE : QuestMarker.AVAILABLE;
			}
		}
		return QuestMarker.NONE;
	}

	private static boolean isEntryState(final ConversationStates state) {
		return state == ConversationStates.IDLE || state == ConversationStates.ATTENDING
				|| state == ConversationStates.ANY;
	}

	private static boolean isOfferState(final ConversationStates state) {
		return state == ConversationStates.QUEST_OFFERED
				|| state == ConversationStates.QUEST_2_OFFERED
				|| state == ConversationStates.QUEST_3_OFFERED;
	}

	private static boolean canAccept(final Player player, final SpeakerNPC npc,
			final ConversationStates offer, final List<Transition> transitions) {
		for (final Transition transition : transitions) {
			if (transition.getState() == offer) {
				for (final Expression trigger : transition.getTriggers()) {
					if (ConversationPhrases.YES_MESSAGES.contains(trigger.getOriginal())
							&& isFulfilled(player, npc, transition)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isFulfilled(final Player player, final SpeakerNPC npc,
			final Transition transition) {
		for (final Expression trigger : transition.getTriggers()) {
			final Sentence sentence = ConversationParser.parse(trigger.getOriginal());
			if (Boolean.TRUE.equals(QuestMarkerConditions.evaluate(
					transition.getCondition(), player, sentence, npc))) {
				return true;
			}
		}
		return false;
	}
}
