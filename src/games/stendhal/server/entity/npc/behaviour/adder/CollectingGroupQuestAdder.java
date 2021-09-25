/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.adder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.dbcommand.UpdateGroupQuestCommand;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.behaviour.impl.CollectingGroupQuestBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.command.DBCommandQueue;

public class CollectingGroupQuestAdder {

	private static final int REPEAT_DELAY_MINUTES = 20 * 60;
	private static final class IsRemainingItem implements ChatCondition {
		private final CollectingGroupQuestBehaviour behaviour;

		private IsRemainingItem(CollectingGroupQuestBehaviour behaviour) {
			this.behaviour = behaviour;
		}

		@Override
		public boolean fire(Player player, Sentence sentence, Entity npc) {
			Map<String, Integer> remaining = behaviour.calculateRemainingItems();
			String item = Grammar.singular(sentence.getNormalized());
			return remaining.get(item) != null;
		}
	}

	private static final class GroupQuestCompletedCondition implements ChatCondition {
		private final CollectingGroupQuestBehaviour behaviour;

		public GroupQuestCompletedCondition(CollectingGroupQuestBehaviour behaviour) {
			this.behaviour = behaviour;
		}

		@Override
		public boolean fire(Player player, Sentence sentence, Entity npc) {
			return behaviour.calculateRemainingItems().isEmpty();
		}
	}


	public void add(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		addGreeting(npc, behaviour);
		addProgress(npc, behaviour);
		addQuest(npc, behaviour);
		addCollectingItems(npc, behaviour);
	}
	
	private void addGreeting(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new QuestCompletedCondition(behaviour.getQuestSlot()),
				ConversationStates.ATTENDING,
				"Jeszcze raz dziękuję za pomoc. Robimy #progres. Miejmy nadzieję, że skończymy na czas.",
				null);
		
		npc.addReply(
				Arrays.asList("inexperienced", "inexperience", "experience", "niedoświadczony", "niedoświadczenie", "doświadczenie"), 
				"Bardzo mi przykro, nie mam teraz czasu, aby cię uczyć. Warto zwiedzić świat i zdobyć trochę doświadczenia.");
	}
	
	private void addProgress(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.addReply(Arrays.asList("status", "progress", "progres"),
				null,
				new ChatAction() {
			
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				if (behaviour.calculateRemainingItems().isEmpty()) {
					npc.say("Dzięki Bogu! W końcu dostaliśmy wszystko. Bądźcie czujni. Budowa zakończy się w mgnieniu oka.");
					return;
				}
				int percent = behaviour.getProgressPercent();
				if (percent < 10) {
					npc.say("Jest jeszcze wiele do zrobienia, zanim zacznie się " + behaviour.getProjectName()  + ". Ledwo zaczęliśmy. Może mógłbyś pomóc w niektórych #zadaniach.");
				} else if (percent < 50) {
					npc.say("Jest jeszcze wiele do zrobienia, zanim zacznie się " + behaviour.getProjectName() + ". Nie dotarliśmy nawet do półmetka. Może mógłbyś pomóc w niektórych #zadaniach.");
				} else if (percent < 75) {
					npc.say("Jest jeszcze wiele do zrobienia, zanim zacznie się " + behaviour.getProjectName() + ". Ledwo co dotarliśmy do półmetka. Może mógłbyś pomóc w niektórych #zadaniach.");
				} else if (percent < 90) {
					npc.say("Jesteśmy prawie na końcu. Ale nadal pozostaje kilka #zadań przed rozpoczęciem " + behaviour.getProjectName() + ".");
				}
			}
		});
	}

	private void addQuest(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new GroupQuestCompletedCondition(behaviour)),
						new QuestNotCompletedCondition(behaviour.getQuestSlot()),
						new LevelLessThanCondition(5)
				),
				ConversationStates.ATTENDING,
				"Przepraszam, jestem w tej chwili bardzo zajęty, próbując zakończyć ten #projekt na czas. Prosiłbym cię o pomoc, ale wydajesz się być bardzo #niedoświadczony.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new GroupQuestCompletedCondition(behaviour)),
						new TimePassedCondition(behaviour.getQuestSlot(), 1, REPEAT_DELAY_MINUTES),
						new LevelGreaterThanCondition(4)
				),
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						npc.say("Czy możesz podać którykolwiek z wymienionych przedmiotów, aby pomóc w budowie? ");
						Map<String, Integer> remaining = behaviour.calculateRemainingItems();
						Set<String> entries = new HashSet<>();
						for (Map.Entry<String, Integer> entry : remaining.entrySet()) {
							int chunkSize = behaviour.getChunkSize(entry.getKey()).intValue();
							String entr = Grammar.numberString(chunkSize) + " #'" + Grammar.plnoun(chunkSize, entry.getKey()) + "'";
							if (chunkSize < entry.getValue().intValue()) {
								entr = entr + " tego ogólnie jeszcze potrzebuję " + Grammar.numberString(entry.getValue().intValue());
							}
							entries.add(entr);
						}
						npc.say(Grammar.enumerateCollection(entries, "lub") + ".");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(behaviour.getQuestSlot()),
						new NotCondition(new TimePassedCondition(behaviour.getQuestSlot(), 1, REPEAT_DELAY_MINUTES)),
						new NotCondition(new GroupQuestCompletedCondition(behaviour))
				),
				ConversationStates.ATTENDING,
				"Dziękuję za pomoc! W tej chwili już nie będę ci przeszkadzać. Sprawdź ponownie jutro.",
				null);
		

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new GroupQuestCompletedCondition(behaviour),
				ConversationStates.ATTENDING,
				"Dzięki twojej pomocy otrzymaliśmy cały potrzebny materiał. W krótkim czasie zakończymy budowę. Sprawdź ponownie wkrótce.",
				null);
	}


	private void addCollectingItems(SpeakerNPC npc, CollectingGroupQuestBehaviour behaviour) {

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				"Och dziękuje. Jakie przedmioty możesz mi dostarczyć?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Och, bardzo źle.",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"",
				new NotCondition(new IsRemainingItem(behaviour)),
				ConversationStates.QUEST_OFFERED,
				"Dziękuję za pomoc. Ale nie potrzebujemy tego przedmiotu. Czy jest jeszcze coś z listy potrzebnych pozycji, które chciałbyś przynieść?",
				null);

		npc.add(ConversationStates.QUEST_OFFERED,
				"",
				new IsRemainingItem(behaviour),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						String item = Grammar.singular(sentence.getNormalized());
						String hint = behaviour.getHint(item);
						if (!player.isEquipped(item, 1)) {
							npc.say("Przepraszam, ale chyba żadnego nie masz " + Grammar.plural(item) + ".");
							if (hint != null) {
								npc.say(hint);
							}
							return;
						}
						Integer stackSize = behaviour.getChunkSize(item);
						if (!player.isEquipped(item, stackSize)) {
							npc.say("Przepraszam, ale chyba nie posiadasz " + Grammar.numberString(stackSize) + " " + Grammar.plural(item) + ".");
							if (hint != null) {
								npc.say(hint);
							}
							return;
						}
						player.drop(item, stackSize);

						UpdateGroupQuestCommand command = new UpdateGroupQuestCommand(behaviour.getQuestSlot(), item, player.getName(), stackSize);
						DBCommandQueue.get().enqueue(command);
						behaviour.addProgress(item, stackSize);

						MultipleActions action = new MultipleActions(
							new SetQuestAction(behaviour.getQuestSlot(), 0, "done"),
							new SetQuestToTimeStampAction(behaviour.getQuestSlot(), 1),
							new IncrementQuestAction(behaviour.getQuestSlot(), 2, 1),
							new EquipItemAction("kupon"),
							new IncreaseKarmaAction(10),
							new IncreaseXPAction(5000),
							new SayTextAction("Dziękuję za pomoc!")
						);
						action.fire(player, sentence, npc);
					}
				});
	}
}