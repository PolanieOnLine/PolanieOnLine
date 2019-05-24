/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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

import games.stendhal.common.grammar.Grammar;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author KarajuSs
 */

public class WegielNaOpal extends AbstractQuest {

	/** Quest slot for this quest, the Ultimate Collector */
	private static final String QUEST_SLOT = "wegiel_na_opal";

	private static final String ADAS_QUEST_SLOT = "pomoc_adasiowi"; // adas

	private static final String FRYDERYK_QUEST_SLOT = "scythe_fryderyk"; // fryderyk

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
		res.add("Stasek zaproponował mi zadanie.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę przynosić mu węgla.");
			return res;
		}
		res.add("Przyjąłem jego zadanie i przyniose mu węgiel.");
		if (!isCompleted(player)) {
			res.add("Stasek poprosił mnie, aby mu przyniósł " + Grammar.a_noun(player.getRequiredItemName(QUEST_SLOT,0)) + ".");
		}
		if (isCompleted(player)) {
			res.add("Super! Teraz mogę kupować uzbrojenie w sklepie u Staska.");
		}
		return res;
	}

	private void startQuests() {
		final SpeakerNPC npc = npcs.get("Stasek");

		npc.add(
			ConversationStates.IDLE,
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
		final SpeakerNPC npc = npcs.get("Stasek");
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
		final SpeakerNPC npc = npcs.get("Stasek");

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
  		final SpeakerNPC npc = npcs.get("Stasek");

		// player returns after finishing the quest and says offer
		npc.add(
				ConversationStates.ATTENDING,
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
				"Węgiel na opał",
				"Stasek, właściciel sklepu z uzbrojeniem w Zakopanem ma dla mnie zadanie.",
				true);

		startQuests();
		requestItem();
		collectItem();
		offerSteps();
	}

	@Override
	public String getName() {
		return "WegielNaOpal";
	}

	@Override
	public String getNPCName() {
		return "Stasek";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
