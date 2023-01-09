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
package games.stendhal.server.maps.dragon;

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
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;

public class GuardianNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Racirad") {
			@Override
			protected void createDialog() {
				String greeting = "Może chcesz podjąć się #wyzwania i zostać jednym z nielicznych smokobójców?";
				
				final ChatCondition greaterThan =
					new AndCondition(new GreetingMatchesNameCondition(getName()),
						new LevelGreaterThanCondition(249),
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
								return !player.isGhost();
							}
						});
				final ChatCondition lessThan =
						new AndCondition(new GreetingMatchesNameCondition(getName()),
						new LevelLessThanCondition(250),
						new ChatCondition() {
							@Override
							public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
								return !player.isGhost();
							}
						});

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							greaterThan,
							new NotCondition(new QuestCompletedCondition("meet_racirad")),
							new NotCondition(new QuestStartedCondition("challenge_dragons"))),
						ConversationStates.ATTENDING,
						"Nieznajomy rycerzu! Zatrzymaj się na sekundkę... " + greeting,
						new SetQuestAction("meet_racirad", "done"));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							greaterThan,
							new QuestCompletedCondition("meet_racirad"),
							new NotCondition(new QuestStartedCondition("challenge_dragons"))),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Dobrze, że jesteś z powrotem [name]! " + greeting));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							greaterThan,
							new QuestCompletedCondition("meet_racirad"),
							new QuestActiveCondition("challenge_dragons")),
						ConversationStates.ATTENDING,
						"Witaj! Jeżeli chcesz abym sprawdził twoje #wyzwanie, to zapytaj.",
						null);

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							greaterThan,
							new QuestCompletedCondition("meet_racirad"),
							new QuestCompletedCondition("challenge_dragons")),
						ConversationStates.ATTENDING,
						null,
						new SayTextAction("Witaj z powrotem [name]! Moje wyzwanie zostało już ukończone, lecz wciąż możesz odwiedzić smocze siedliszcze."));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							lessThan,
							new NotCondition(new QuestCompletedCondition("meet_racirad"))),
						ConversationStates.ATTENDING,
						"Hej, ty nieznajomy! Kroczysz na ziemie podniebnych stworzeń! Na twoim miejscu uważałbym w tych rejonach...",
						new SetQuestAction("meet_racirad", "done"));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
							lessThan,
							new QuestCompletedCondition("meet_racirad")),
						ConversationStates.ATTENDING,
						"Hej, młody poszukiwaczu [name]! Kroczysz na ziemie podniebnych stworzeń! Proszę... uważaj w tym miejscu, nie chcę mieć ciebie na sumieniu.",
						null);

				addHelp("Strzegę tegoż przejścia przed smokami, nie czuję potrzeby wsparcia.");
				addOffer("Hmmm... Z ofertami możesz zajrzeć w sukienniach na rynku w pobliżu Krakowskiego grodu.");
				addGoodbye("Trzymaj się rycerzu i czuwaj przed smokami!");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.addInitChatMessage(null, new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				if (!player.hasQuest("RaciradFirstChat")) {
					player.setQuest("RaciradFirstChat", "done");
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				} else if (player.getLevel() < 250) {
					((SpeakerNPC) raiser.getEntity()).listenTo(player, "hi");
				}
			}
		});

		npc.setDescription("Oto Racirad. Wygląda jakby strzegł obok niego przejścia przed czymś.");
		npc.setEntityClass("royalguard2npc");
		npc.setGender("M");
		npc.setPosition(24, 123);
		npc.setDirection(Direction.DOWN);
		npc.setPerceptionRange(3);
		zone.add(npc);
	}
}
