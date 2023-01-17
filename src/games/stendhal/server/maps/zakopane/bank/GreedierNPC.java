/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.bank;

import static games.stendhal.server.maps.quests.challenges.ChallengeGreedy.QUEST_SLOT;

import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;

public class GreedierNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Chciwurak") {
			@Override
			protected void createDialog() {
				String greeting = "Witaj z powrotem na piętrze [name].";

				final ChatCondition meetNPC = new QuestCompletedCondition("meet_chciwurak");
				final ChatCondition playerIsNotGhost =
					new ChatCondition() {
						@Override
						public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
							return !player.isGhost();
						}
					};

				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						new NotCondition(meetNPC),
						playerIsNotGhost),
					ConversationStates.ATTENDING,
					"Hej, ty nieznajomy! Podejdź no na moment. Wyglądasz mi na osobę, która lubi #wyzwania.",
					new SetQuestAction("meet_chciwurak", "done"));

				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						meetNPC,
						new QuestCompletedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new SayTextAction(greeting + " Korzystaj ze skrzynek bez obaw, nie będę *kaszlnięcie* próbował nic podbierać."));

				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						meetNPC,
						new NotCondition(new QuestStartedCondition(QUEST_SLOT))),
					ConversationStates.ATTENDING,
					null,
					new SayTextAction(greeting + " Wyglądasz mi na osobę, która lubi #wyzwania."));

				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						meetNPC,
						new QuestActiveCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new SayTextAction(greeting + " Jeśli chcesz, abym sprawdził proces twojego #wyzwania to wystarczy się zapytać..."));

				addHelp("He... he... Rzuć groszem w takim razie.");
				addOffer("Daj nieco grosza biedakowi...");
				addGoodbye("Dam ci radę. Uważaj w tym banku na innych ludzi...");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest(npc.getName()+"FirstChat")) {
					player.setQuest(npc.getName()+"FirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setDescription("Oto Chciwurak. Wygląda jakby rozglądał się za czymś.");
		npc.setEntityClass("manwithhatnpc");
		npc.setGender("M");
		npc.setPosition(43, 3);
		npc.setDirection(Direction.DOWN);
		npc.setPerceptionRange(11);
		zone.add(npc);
	}
}
