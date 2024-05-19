/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.constants.Testing;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseRatkXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.TimeUtil;

public class DailyItemGdanskQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "daily_museum_gdansk_quest";
	private final SpeakerNPC npc = npcs.get("Mieczysław");

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = TimeUtil.MINUTES_IN_WEEK; 

	/** How often the quest may be repeated */
	private static final int delay = TimeUtil.MINUTES_IN_DAY;

	private static DailyItemGdanskQuest instance;

	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 * not to use yet, just getting it ready.
	 */
	private static Map<String,Integer> items;

	/**
	 * Get the static instance.
	 *
	 * @return
	 * 		DailyItemGdanskQuest
	 */
	public static DailyItemGdanskQuest getInstance() {
		if (instance == null) {
			instance = new DailyItemGdanskQuest();
		}

		return instance;
	}

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// resource
		items.put("szmaragd", Rand.roll1D10());
		items.put("szafir", Rand.roll1D10());
		items.put("rubin", Rand.roll1D10());
		items.put("ametyst", Rand.roll1D10());
		items.put("diament", Rand.roll1D3());
		items.put("obsydian", Rand.roll1D3());
		items.put("bursztyn", Rand.roll1D10());
		items.put("sztabka mithrilu", Rand.roll1D6());
		items.put("sztabka platyny", Rand.roll1D6());
		items.put("sztabka srebra", Rand.roll1D10());
		items.put("sztabka złota", Rand.roll1D10());
		items.put("sztabka miedzi", Rand.roll1D10());
		items.put("sztabka cieni", Rand.roll1D10());
		items.put("bryłka mithrilu", Rand.roll1D10());
		items.put("bryłka złota", Rand.roll1D10());
		items.put("ruda srebra", Rand.roll1D10());
		items.put("ruda żelaza", Rand.roll1D20());
		items.put("ruda miedzi", Rand.roll1D20());
		items.put("ruda platyny", Rand.roll1D10());
		items.put("ruda cieni", Rand.roll1D10());
		items.put("sztabka żelaza", Rand.roll1D20());
		items.put("siarka", Rand.roll1D20());
		items.put("sól", Rand.roll1D20());
		items.put("czarna perła", Rand.roll1D3());
		items.put("kryształ szmaragdu", Rand.roll1D20());
		items.put("kryształ szafiru", Rand.roll1D20());
		items.put("kryształ rubinu", Rand.roll1D20());
		items.put("kryształ ametystu", Rand.roll1D20());
		items.put("węgiel", 25);
	}

	/**
	 * For other quests to check if an item is already utilized in this one.
	 *
	 * @param item
	 * 		<code>String</code> name of the item.
	 * @return
	 * 		<code>true</code> if the item is found in the list, <code>false</code>
	 */
	public static boolean utilizes(final String item) {
		return items.containsKey(item);
	}

	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Gdańsk potrzebuje zapasów. Zdobądź [item]"
				+ " i powiedz #załatwione, gdy przyniesiesz."));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		return new MultipleActions(actions);
	}
	
	private void getQuest() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już dostałeś zadanie by przynieść [item]"
						+ ". Powiedz #załatwione jeżeli przyniesiesz!"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Jesteś na etapie poszukiwania [item]"
						+ " powiedz #załatwione, jak przyniesiesz. Być może nie ma tych przedmiotów! Możesz przynieść #inny przedmiot jeżeli chcesz lub wróć z tym, o który cię na początku prosiłem."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Możesz dostać tylko jedno zadanie na 2 dni. Proszę wróć za"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}

	private void completeQuest() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Obawiam się, że jeszcze nie dałem tobie #zadania.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Już ukończyłeś ostatnie zadanie, które ci dałem.",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(6, 70.0));
		if (!Occasion.SECOND_WORLD) {
			actions.add(new IncreaseAtkXPDependentOnLevelAction(6, 70.0));
			actions.add(new IncreaseDefXPDependentOnLevelAction(6, 70.0));
		}
		if (Testing.COMBAT) {
			actions.add(new IncreaseRatkXPDependentOnLevelAction(6, 70.0));
		}
		actions.add(new IncreaseKarmaAction(15.0));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING, 
				"Dobra robota! Pozwól sobie podziękować w imieniu obywateli Gdańska!",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Jeszcze nie przyniosłeś [item]"
						+ ". Idź i zdobądź, a wtedy wróć i powiedz #załatwione jak skończysz."));

	}

	private void abortQuest() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING, 
				null, 
				// start quest again immediately
				startQuestAction());

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))), 
				ConversationStates.ATTENDING, 
				"Nie minęło zbyt wiele czasu, od rozpoczęcia zadania. Nie pozwolę Tobie poddać się tak szybko.", 
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Obawiam się, że jeszcze nie dałem tobie #zadania.", 
				null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
		"Dzienne Zadanie w Gdańsku",
		"Mieczysław potrzebuje dostaw towaru do Gdańska.",
		true);

		buildItemsMap();

		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Spotkałem") + " Mieczysława w Gdańsku");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie pomogę miastu Gdańsk.");
			return res;
		}

		res.add("Chcę pomóc mieszkańcom Gdańska.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add((player.getGenderVerb("Zostałem") + " " + player.getGenderVerb("poproszony") + " o przyniesienie "
						+ Grammar.quantityplnoun(amount, questItem) + ", aby pomóc miastu Gdańsk. Nie mam tego jeszcze."));
			} else {
				res.add((player.getGenderVerb("Znalazłem") + " "
						+ Grammar.quantityplnoun(amount, questItem) + " do pomocy miastu Gdańsk i muszę je dostarczyć."));
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(player.getGenderVerb("Pomogłem") + " miastu Gdańsk z dostawami "
					+ Grammar.quantityplnoun(repetitions, "raz") + " do tej pory.");
		}
		if (isRepeatable(player)) {
			res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do Mieczysława i teraz Gdańsk znów potrzebuje zapasów.");
		} else if (isCompleted(player)){
			res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do Mieczysław i odebrałem moją nagrodę w ciągu ostatnich 48 godzin.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "Dzienne Zadanie w Gdańsku";
	}

	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
}
