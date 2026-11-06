/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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

import java.util.ArrayList;
import games.stendhal.server.core.rule.quest.StarterQuestLine;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * @author KarajuSs
 */
public class WegielNaOpal extends AbstractQuest {
	private static final String QUEST_SLOT = "wegiel_na_opal";
	private final SpeakerNPC npc = npcs.get("Stasek");

	private static final String ADAS_QUEST_SLOT = "pomoc_adasiowi"; // adas
	private static final String FRYDERYK_QUEST_SLOT = "scythe_fryderyk"; // fryderyk

	private void startQuests() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj. Rozejrzyj się spokojnie. Gdyby interesował cię zarobek, mam też pewne #'zlecenie'.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zlecenie", "zadanie", "task", "quest", "order"),
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(ADAS_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Z opałem jeszcze chwilę poczekam. Adaś z domku bohaterów szuka pomocy, bo szkodniki zalęgły im się w piwnicy. Zajrzyj najpierw do niego.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zlecenie", "zadanie", "task", "quest", "order"),
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(FRYDERYK_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Fryderykowi pilno zebrać plony, a nie ma czym pracować. Pomóż mu najpierw z kosą. Mój opał może jeszcze poczekać.",
			null);
	}

	private void requestItem() {
		final Map<String,Integer> items = new HashMap<String, Integer>();
		items.put("węgiel",50);

		// If all quests are completed, ask for an item
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zlecenie", "zadanie", "task", "quest", "order"),
				new AndCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new QuestCompletedCondition(ADAS_QUEST_SLOT),
						new QuestCompletedCondition(FRYDERYK_QUEST_SLOT)),
				ConversationStates.ATTENDING,
				null,
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Fryderyk chwalił twoją pomoc, a Adaś od rana o tobie opowiada. Dobrze mieć kogoś takiego w okolicy."
					+ " Brakuje mi jeszcze opału na zimę. Przynieś [item], a odwdzięczę się i otworzę dla ciebie skład z uzbrojeniem."));
	}

	private void collectItem() {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Przyniosłeś mi węgiel, o który cię prosiłem?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Chyba nie wszystko masz przy sobie. Potrzebuję [item]. Tyle wystarczy nam na zapas."));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Uff, będzie czym napalić! Dziękuję. Obiecane uzbrojenie jest do twojej dyspozycji, zerknij na #ofertę. Przy okazji... była tu ostatnio dziwna #'dostawa'. Coś mi w niej nie pasuje.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT),
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(5500),
									new IncreaseKarmaAction(25)));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Dobrze, poczekam. Pamiętaj tylko, żeby przynieść [item], zanim przyjdą mrozy."));
	}

	private void offerSteps() {
		// player returns after finishing the quest and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Obejrzyj spokojnie broń i pancerze. Może znajdziesz coś na dalszą drogę.",
				null);

		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Najpierw potrzebuję pomocy przy opale. Jeśli wykonasz moje #'zlecenie', udostępnię ci uzbrojenie ze składu.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Węgiel na Opał",
				"Stasek zbiera opał na nadchodzące mrozy. Pomoc mieszkańcom pozwoli zdobyć jego zaufanie i odblokować sklep z uzbrojeniem.",
				true);

        questInfo.setMainQuest(StarterQuestLine.CHAPTER, 4, StarterQuestLine.STASEK);
		questInfo.setRewards(new QuestRewards(5500, 0)
				.note("Dostęp do sklepu Staska z uzbrojeniem.").build());
		startQuests();
		requestItem();
		collectItem();
		offerSteps();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Stasek zaproponował mi wykonanie pewnego zadania.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę przynosić mu węgla.");
			return res;
		}
		res.add("Podejmę się wyzwania i przyniosę dla niego węgiel.");
		if (!isCompleted(player)) {
			res.add("Stasek poprosił mnie, abym mu przyniósł " + player.getRequiredItemName(QUEST_SLOT,0) + ".");
		}
		if (isCompleted(player)) {
			res.add("Stasek przyjął opał i podziękował za pomoc mieszkańcom w przygotowaniach do zimy.");
            res.add("Zdobyłem zaufanie Zakopanego. Mogę kupować uzbrojenie w sklepie u Staska. Wspomniał o podejrzanej dostawie; mogę zapytać go o dostawę.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Węgiel na Opał";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
