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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class ForgeNewArms extends AbstractQuest {
	private static final String QUEST_SLOT = "forge_newarms";
	private static final String QUEST_COMPLETE = "grind_misty_gem";
	private static final int DELAY = 48 * 60; // 48 godzin

	private final SpeakerNPC npc = npcs.get("kowal Przemysław");

	private List<String> questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);

	@Override
	public List<String> getHistory(Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " z kowalem napotkanym w Tarnowie.");
		if ("start".equals(player.getQuest(QUEST_SLOT)) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
			res.add("Muszę przynieść surowce z tej listy:"
				+ "\n- 5 sztuk #'klejnotu ciemnolitu'"
				+ "\n- 14 sztuk #'sztabek platyny'"
				+ "\n- 24 sztuki #'bryłek mithrilu'. Oraz nie mogę zapomnieć o swojej tarczy z mithrilu...");
		}

		if (player.getQuest(QUEST_SLOT).startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT, 1, DELAY).fire(player, null, null)) {
				res.add("Według mojego czasu kowal musiał skończyć swoje prace.");
			} else {
				res.add("By sprawdzić efekt prac kowala muszę poczekać 48 godzin...");
			}
			return res;
		} 

		return res;
	}

	private ChatCondition PlayerHasItemsCondition() {
		return new AndCondition(
			new PlayerHasItemWithHimCondition("tarcza z mithrilu"),
			new PlayerHasItemWithHimCondition("klejnot ciemnolitu", 5),
			new PlayerHasItemWithHimCondition("sztabka platyny", 14),
			new PlayerHasItemWithHimCondition("bryłka mithrilu", 24)
		);
	}

	private ChatAction DropItemsAction() {
		return new MultipleActions(
			new DropItemAction("tarcza z mithrilu"),
			new DropItemAction("klejnot ciemnolitu", 5),
			new DropItemAction("sztabka platyny", 14),
			new DropItemAction("bryłka mithrilu", 24)
		);
	}

	private void step1() {
		questTrigger.addAll(Arrays.asList("forge", "create", "stwórz", "wytwórz", "ulepsz"));

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new QuestCompletedCondition(QUEST_COMPLETE),
				new NotCondition(new QuestStartedCondition(QUEST_SLOT))),
			ConversationStates.QUEST_OFFERED,
			"Ostatnio po całej krainie chodzą pogłoski na temat nowego surowca z dosłownie nie z tego świata..."
				+ " Jestem też ciekaw w jaki sposób można by wykorzystać ten nowy surowiec do udoskonalenia uzbrojenia."
				+ " Pomożesz mi w małym eksperymencie? Nie obiecuję, że od razu się uda zjednoczyć nowy materiał...",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			new AndCondition(
				new NotCondition(new QuestStartedCondition(QUEST_SLOT)),
				new NotCondition(PlayerHasItemsCondition())),
			ConversationStates.ATTENDING,
			"Oto lista potrzebnych mi surowców, abym mógł wziąć się za prace:"
				+ "\n- 5 sztuk #'klejnotu ciemnolitu'"
				+ "\n- 14 sztuk #'sztabek platyny'"
				+ "\n- 24 sztuki #'bryłek mithrilu'"
				+ "\nPamiętaj jeszcze, aby zostały surowce dostarczone wszystkie razem do mnie! Nie zapomnij również o tarczy z mithrilu, nada się do ulepszenia!",
			new SetQuestAction(QUEST_SLOT, 0, "start"));

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				new NotCondition(PlayerHasItemsCondition())),
			ConversationStates.ATTENDING,
			"Zgubiłeś swoją listę surowców? No dobra, napiszę dla ciebie jeszcze raz, nową listę. Tylko nie zapomnij też o tarczy z mithrilu! Proszę:"
				+ "\n- 5 sztuk #'klejnotu ciemnolitu'"
				+ "\n- 14 sztuk #'sztabek platyny'"
				+ "\n- 24 sztuki #'bryłek mithrilu'",
			null);

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				PlayerHasItemsCondition()),
			ConversationStates.QUEST_OFFERED,
			"Przychodzisz z takim entuzjazmem do mnie iż podejrzewam, że masz te surowce przy sobie. Możemy zaczynać w takim razie?",
			null);

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new QuestCompletedCondition(QUEST_COMPLETE),
				new QuestInStateCondition(QUEST_SLOT, 0, "rejected"),
				PlayerHasItemsCondition()),
			ConversationStates.QUEST_OFFERED,
			"Nazbierałeś nowe materiały do powtórzenia eksperymentu?",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			PlayerHasItemsCondition(),
			ConversationStates.ATTENDING,
			"Świetnie! Tak więc biorę się za prace.",
			new MultipleActions(
				DropItemsAction(),
				new SetQuestAction(QUEST_SLOT, 0, "forging"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new QuestCompletedCondition(QUEST_COMPLETE),
				new QuestInStateCondition(QUEST_SLOT, 0, "rejected"),
				new NotCondition(PlayerHasItemsCondition())),
			ConversationStates.ATTENDING,
			"Wróć proszę gdy nazbierasz nowe materiały do powtórzenia ulepszenia.",
			null);

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new QuestNotCompletedCondition(QUEST_COMPLETE),
			ConversationStates.ATTENDING,
			"Słucham? Nie rozumiem co dokładnie miałbym zrobić dla ciebie, czy też co miałbym zlecić do wykonania...",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Gdy się zastanowisz nad tym to wróć.",
			null);
	}

	private void step2() {
		questTrigger.addAll(Arrays.asList("forge", "create", "stwórz", "wytwórz", "ulepsz"));

		npc.add(ConversationStates.ATTENDING, 
			questTrigger,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, 0, "forging"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 2, DELAY, "Wciąż ciężko pracuje. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			questTrigger,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, 0, "forging"),
				new TimePassedCondition(QUEST_SLOT, 1, DELAY)),
			ConversationStates.IDLE,
			null,
			new ForgeItemAction("tarcza ciemnomithrilowa",
				"Kurczę, te klejnoty nie chciały się złączyć z tarczą... Nie martw się, zreperowałem tarcze uszkodzenia jakie wywołałem, bryłkami. Zwracam twoją własność, jak nazbierasz kolejne surowce możemy powtórzyć proces...",
				"Udało się! Ino spójrz jak pięknie wygląda! Myślę, że już wiem w jaki sposób obchodzić się z tym surowcem."));
	}

	private class ForgeItemAction implements ChatAction {
		private final String item;
		private String faultText;
		private String successText;

		public ForgeItemAction(final String item, String faultText, String successText) {
			this.item = checkNotNull(item);
			this.faultText = faultText;
			this.successText = successText;
		}

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			if (isSuccessful(player)) {
				if (successText != null) {
					npc.say(successText);
					player.addXP(1000000);
					player.setQuest(QUEST_SLOT, 0, "done");
					player.setQuest(QUEST_SLOT, 1, ""); // clean timestamp
					new EquipItemAction(item, 1, true).fire(player, null, null);
				}
			} else {
				if (faultText != null) {
					npc.say(faultText);
					player.setQuest(QUEST_SLOT, 0, "rejected");
					new EquipItemAction("tarcza z mithrilu", 1, true).fire(player, null, null);
				}
			}
		}

		protected boolean isSuccessful(final Player player) {
			// Player have 30% chance to successful
			return Rand.roll1D100() <= (0.3 * 100);
		}
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Próba Kowalstwa",
			"",
			false);
		step1();
		step2();
	}

	@Override
	public String getName() {
		return "Próba Kowalstwa";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.TARNOW_VILLAGE;
	}
}
