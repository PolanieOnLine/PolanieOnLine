/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Take gold for Grafindle
 * 
 * PARTICIPANTS: <ul>
 * <li> Grafindle
 * <li> Lorithien </ul>
 * 
 * STEPS:<ul>
 * <li> Talk with Grafindle to activate the quest.
 * <li> Talk with Lorithien for the money.
 * <li> Return the gold bars to Grafindle</ul>
 * 
 * REWARD:<ul>
 * <li> 2000 XP
 * <li> some karma (10)
 * <li> key to nalwor bank customer room
 * </ul>
 * REPETITIONS: <ul><li> None.</ul>
 */
public class TakeGoldforGrafindle extends AbstractQuest {
	
	private static final int GOLD_AMOUNT = 25;

	private static final String QUEST_SLOT = "grafindle_gold";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
				res.add("Poszłem do banku w Nalwor i spotkałem Grafindle.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Odpowiedzialność za sztabki była dla mnie zbyt wysoka, więc byłem zmuszony odrzucić prośbę Grafindle. ");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "lorithien", "done")) {
			res.add("Ponieważ jestem osobą godną zaufania, obiecałem przynieść Grafindle złoto od Lorithien.");
		}
		if (questState.equals("lorithien") && player.isEquipped("sztabka złota",
				GOLD_AMOUNT)
				|| questState.equals("done")) {
			res.add("Udało się! Zebrałem sztabki, których potrzebuje Grafindle.");
		}
		if (questState.equals("lorithien")
				&& !player.isEquipped("sztabka złota", GOLD_AMOUNT)) {
			res.add("O nie! Zgubiłem złote sztabki, które miałem przynieść Grafindle!");
		}
		if (questState.equals("done")) {
			res.add("Dałem złote sztabki Grafindle, a on wynagrodził mnie - dostałem klucz do pokoju klienta banku Nalwor.");
		}
		return res;
	}

	private void step_1() {

		final SpeakerNPC npc = npcs.get("Grafindle");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, "Potrzebuję kogoś, komu mógłbym zaufać w przypadku #złota.",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Pytam się Ciebie, bo wyglądasz na uczciwego.",
				null);

		/** In case quest is completed */
		npc.add(ConversationStates.ATTENDING, Arrays.asList("gold", "złota", "złoto"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Złoto w banku jest bezpieczne. Dziękuję!", null);

		/** If quest is not started yet, start it. */
		npc.add(
			ConversationStates.ATTENDING,
			"złota",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Jeden z naszych klientów potrzebuje złożyć sztabki złota w banku. Chodzi o #Lorithien, która nie może zamknąć poczty i dlatego nie ma czasu.",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Dziękuję. Mam nadzieję, że wkrótce zobaczę Cię z sztabkami złota... dopóki nie pokusisz się do ich zatrzymania.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT,"start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Cóż jesteś uczciwy, ponieważ od razu mi powiedziałeś.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			"Lorithien",
			null,
			ConversationStates.QUEST_OFFERED,
			"Pracuje na poczcie w Nalwor. To duża odpowiedzialność, zwłaszcza z tymi złotymi sztabkami, które są warte dużo pieniędzy. Czy mogę Ci zaufać?",
			null);

		/** Remind player about the quest */
		npc.add(
			ConversationStates.ATTENDING,
			"złoto",
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"#Lorithien będzie się martwiła tym, że jej złoto nie jest bezpieczne! Proszę przynieś je!",
			null);

		npc.add(ConversationStates.ATTENDING, "lorithien", null,
			ConversationStates.ATTENDING,
			"Pracuję na poczcie w Nalwor.", null);
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Lorithien");

		/**
		 * If player has quest and is in the correct state, just give him the
		 * gold bars.
		 */
		final List<ChatAction> givegold = new LinkedList<ChatAction>();
		givegold.add(new EquipItemAction("sztabka złota",GOLD_AMOUNT, true));
		givegold.add(new SetQuestAction(QUEST_SLOT, "lorithien"));	
		
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
					new QuestInStateCondition(QUEST_SLOT, "start")),
			ConversationStates.ATTENDING,
			"Jestem wdzięczna, że przybyłeś! Byłabym szczęśliwa, gdyby to złoto było bezpieczne w banku.",
			new MultipleActions(givegold));

		/** If player keep asking for book, just tell him to hurry up */
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
					new QuestInStateCondition(QUEST_SLOT, "lorithien")),
			ConversationStates.ATTENDING,
			"Proszę zanieś złoto z powrotem do #Grafindle zanim zgubię!",
			null);

		npc.add(ConversationStates.ATTENDING, "grafindle", null,
			ConversationStates.ATTENDING,
			"Grafindle jest starszym bankierem w Nalwor.",
			null);

		/** Finally if player didn't start the quest, just ignore him/her */
		npc.add(
			ConversationStates.ATTENDING,
			"złoto",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Przepraszam, mam tyle spraw na głowie... Nie zrozumiałem Ciebie.",
			null);
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Grafindle");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sztabka złota", GOLD_AMOUNT));
		reward.add(new EquipItemAction("klucz do banku Nalwor", 1, true));
		reward.add(new IncreaseXPAction(2000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(10));
		
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT)),
				ConversationStates.ATTENDING,
			"Przyniosłeś złoto! Wspaniale! Wiedziałam, że mogę na Tobie polegać. Proszę weź ten klucz do pokoju dla klientów.",
			new MultipleActions(reward));
		
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(getName()),
						new QuestInStateCondition(QUEST_SLOT, "lorithien"),
						new NotCondition(new PlayerHasItemWithHimCondition("sztabka złota", GOLD_AMOUNT))),
				ConversationStates.ATTENDING,
				"Nie masz jeszcze sztabek złota od #Lorithien ? Szybko idź do niej!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabierz Złoto dla Grafindla",
				"Grafindle w banku w Nalwor szuka kogoś komu może zaufać.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "TakeGoldforGrafindle";
	}
	
	// it is not easy to get to Nalwor
	@Override
	public int getMinLevel() {
		return 50;
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public String getNPCName() {
		return "Grafindle";
	}
}
