/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class GrindMistyGem extends AbstractQuest {
	private static final String QUEST_SLOT = "grind_misty_gem";
	private static final int DELAY = 30;

	private final SpeakerNPC npc = npcs.get("czeladnik Jaromir");

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " z czeladnikiem napotkanym w Wieliczce.");

		return res;
	}

	private List<String> questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);

	private void step1() {
		questTrigger.add("oszlifuj");
		questTrigger.add("obrób");

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(
				new PlayerHasItemWithHimCondition("kryształ ciemnolitu"),
				new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "grinding")),
				new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "done"))),
			ConversationStates.QUEST_OFFERED,
			"Zdobyłeś nowy materiał, który potrzebuje szlifu?",
			null);

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(
				new NotCondition(new PlayerHasItemWithHimCondition("kryształ ciemnolitu")),
				new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "grinding")),
				new NotCondition(new QuestInStateCondition(QUEST_SLOT, 0, "done"))),
			ConversationStates.ATTENDING,
			"Ciężko wziąć się za coś czego nie masz.",
			null);
		
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("kryształ ciemnolitu"),
			ConversationStates.ATTENDING,
			"Dobrze, także biorę się za pracę.",
			new MultipleActions(
				new DropItemAction("kryształ ciemnolitu", 1),
				new SetQuestAction(QUEST_SLOT, 0, "grinding"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 2)));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Gdy się zastanowisz nad tym to wróć.",
			null);
	}

	private void step2() {
		questTrigger.add("oszlifuj");
		questTrigger.add("obrób");

		npc.add(ConversationStates.ATTENDING, 
			questTrigger,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "grinding;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 2, DELAY))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 2, DELAY, "Wciąż ciężko pracuje. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "grinding;"),
				new TimePassedCondition(QUEST_SLOT, 2, DELAY)),
			ConversationStates.IDLE,
			null,
			new GrindItemAction("klejnot ciemnolitu",
				"Niestety, nie udało mi się tej sztuki wyszlifować... Popękał podczas obrabiania go.",
				"Udało się, spójrz jak pięknie wygląda!"));
	}

	private class GrindItemAction implements ChatAction {
		private final String item;
		private String faultText;
		private String successText;

		public GrindItemAction(final String item, String faultText, String successText) {
			this.item = checkNotNull(item);
			this.faultText = faultText;
			this.successText = successText;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			if (isSuccessful(player)) {
				if (successText != null) {
					String questState = "start";
					if (player.isQuestInState(QUEST_SLOT, 1, "4")) {
						questState = "done";
						successText += " Sądzę, że nabyłem nieco techniki do obróbki tego materiału. Następnym razem mogę spróbować w większych ilościach tego szlifować.";
					}

					npc.say(successText);
					player.addXP(1);
					player.setQuest(QUEST_SLOT, 0, questState);
					player.setQuest(QUEST_SLOT, 2, "");
					new EquipItemAction(item, 1, false).fire(player, null, null);
					new IncrementQuestAction(QUEST_SLOT, 1, 1).fire(player, null, null);;
				}
			} else {
				if (faultText != null) {
					npc.say(faultText);
					player.setQuest(QUEST_SLOT, 0, "rejected");
				}
			}
		}

		protected boolean isSuccessful(final Player player) {
			final int random = Rand.roll1D100();
			double probability = 0.3;
			String questSlot = player.getQuest(QUEST_SLOT, 1);
			int countCompletedQuest = Integer.parseInt(questSlot);
			if (countCompletedQuest > 0 && !questSlot.isEmpty()) {
				double temp = 0.05 * countCompletedQuest;
				probability = Math.min(1.0, probability + temp);
			}

			return random <= (probability * 100);
		}

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Obróbka Tajemniczego Surowca",
			"",
			false);
		step1();
		step2();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Obróbka Tajemniczego Surowca";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.WIELICZKA;
	}
}