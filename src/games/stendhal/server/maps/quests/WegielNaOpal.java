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
			"Witaj przyjacielu. Mam dla Ciebie pewne '#zlecenie'.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zlecenie", "zadanie", "task", "quest", "order"),
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(ADAS_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Zanim zabierzesz się za moje zadanie udowodnij, że mogę Tobie ufać! Pomóż pierw małemu chłopcowi o imieniu Adaś.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("zlecenie", "zadanie", "task", "quest", "order"),
			new AndCondition(new QuestNotStartedCondition(QUEST_SLOT),
					 new OrCondition(new QuestNotCompletedCondition(FRYDERYK_QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Jeszcze nie tak prędko! Pomóż jeszcze Fryderykowi, a wtedy będę mógł Tobie zaufać.",
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
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, items, "Właśnie słyszałem od Fryderyka, że mu pomogłeś jak i Adasiowi, a więc mogę Tobie zafuać."
					+ " Proszę przynieś mi [item]. Potrzebuję opał na zimę. Straszne mrozy są ostatnio..."));
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
				new SayRequiredItemAction(QUEST_SLOT, "Hm, nie masz [item], nie próbuj mnie oszukać!"));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Dziękuję za węgiel! Teraz będę miał zapas na conajmniej kilka zim! Czas, być może złożyć #ofertę.",
				new MultipleActions(new DropRecordedItemAction(QUEST_SLOT),
									new SetQuestAction(QUEST_SLOT, "done"),
									new IncreaseXPAction(5500),
									new IncreaseKarmaAction(25)));

		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, "Bardzo dobrze, wróć kiedy będziesz mieć [the item] ze sobą."));
	}

	private void offerSteps() {
		// player returns after finishing the quest and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Spójrz na niebieską księgę! Oferuję to co na niej jest.",
				null);

		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.OFFER_MESSAGES,
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Powiem ci, że możesz u mnie kupić, jeżeli wykonasz dla mnie #'zlecenie'.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Węgiel na Opał",
				"Stasek, właściciel sklepu z uzbrojeniem w Zakopanem ma dla mnie zadanie.",
				true);

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
			res.add("Super! Teraz mogę kupować uzbrojenie w sklepie u Staska.");
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
