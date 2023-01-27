/***************************************************************************
 *                 (C) Copyright 2023-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tatry.mountain;

import static games.stendhal.server.maps.quests.challenges.ChallengeGlutton.QUEST_SLOT;

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

public class HunterNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Wszebor") {
			@Override
			protected void createDialog() {
				String greeting = "Witaj z powrotem [name]!";

				final ChatCondition meetNPC = new QuestCompletedCondition("meet_wszebor");
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
					"Hej, zatrzymaj się na moment podróżniku! Podejdź do mnie nieco bliżej. Miałbym w sumie dla ciebie #wyzwanie.",
					new SetQuestAction("meet_wszebor", "done"));

				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(
						meetNPC,
						new QuestCompletedCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING,
					null,
					new SayTextAction(greeting));

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

				addHelp("Polować to najlepiej w pojedynkę...");
				addOffer("Nie mam nic do zaoferowania, być może w niedługim czasie...");
				addGoodbye("Uważaj, żebyś ty nie stał się zwierzyną dla łowcy.");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
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

		npc.setDescription("Oto " + npc.getName() + ". Wygląda jakby za moment miałby wyruszyć na polowanie.");
		npc.setEntityClass("npclesniczy2");
		npc.setGender("M");
		npc.setPosition(104, 108);
		npc.setDirection(Direction.UP);
		npc.setPerceptionRange(18);
		zone.add(npc);
	}
}