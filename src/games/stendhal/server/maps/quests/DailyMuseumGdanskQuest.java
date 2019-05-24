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
//Na podstawie taska DailyItemQuest.
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DailyMuseumGdanskQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "daily_museum_gdansk_quest";

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK; 

	/** How often the quest may be repeated */
	private static final int delay = 2*MathHelper.MINUTES_IN_ONE_DAY; 

	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 * not to use yet, just getting it ready.
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();
		items.put("szmaragd",5);
		items.put("szafir",5);
		items.put("rubin",5);
		items.put("ametyst",5);
		items.put("szmaragd",10);
		items.put("szafir",10);
		items.put("rubin",10);
		items.put("ametyst",10);
		items.put("diament",3);
		items.put("bursztyn",5);
		items.put("sztabka złota",5);
		items.put("sztabka złota",10);
		items.put("żelazo",10);
		items.put("żelazo",15);
		items.put("sztabka mithrilu",3);
		items.put("bryłka złota",10);
		items.put("ruda srebra",10);
		items.put("sztabka srebra",5);
		items.put("obsydian",3);
		items.put("ruda żelaza",10);
		items.put("czarna perła",3);
		items.put("bryłka mithrilu",5);
		items.put("kryształ szmaragdu",10);
		items.put("kryształ szafiru",10);
		items.put("kryształ rubinu",10);
		items.put("kryształ ametystu",10);
		items.put("węgiel",25);
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
		final SpeakerNPC npc = npcs.get("Mieczysław");
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
		final SpeakerNPC npc = npcs.get("Mieczysław");

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
		actions.add(new IncreaseAtkXPDependentOnLevelAction(6, 70.0));
		actions.add(new IncreaseDefXPDependentOnLevelAction(6, 70.0));
		actions.add(new IncreaseKarmaAction(15.0));
		


		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING, 
				"Dobra robota! Pozwól sobie podziękować w imieniu obywateli Gdańska! W nagrodę dostałeś punkty umiejętności ataku i obrony.",
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
		final SpeakerNPC npc = npcs.get("Mieczysław");

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
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Mieczysława w Gdańsku");
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
				res.add(("Zostałem poproszony o przyniesienie "
						+ Grammar.quantityplnoun(amount, questItem, "") + ", aby pomóc miastu Gdańsk. Nie mam tego jeszcze."));
			} else {
				res.add(("Znalazłem "
						+ Grammar.quantityplnoun(amount, questItem, "") + " do pomocy miastu Gdańsk i muszę je dostarczyć."));
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("Pomogłem miastu Gdańsk z dostawami "
					+ Grammar.quantityplnoun(repetitions, "razy") + " do tej pory.");
		}
		if (isRepeatable(player)) {
			res.add("Dostarczyłem ostatni przedmiot do Mieczysława i teraz Zakopane znów potrzebuje zapasów.");
		} else if (isCompleted(player)){
			res.add("Dostarczyłem ostatni przedmiot do Mieczysław i odebrałem moją nagrodę w ciągu ostatnich 48 godzin.");
		}
		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
		"Dzienne Zadanie Mieczysława",
		"Mieczysław potrzebuje dostaw towaru do Gdańska.",
		true);

		buildItemsMap();

		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "DailyMuseumGdanskQuest";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}

	@Override
	public String getNPCName() {
		return "Mieczysław";
	}
}
