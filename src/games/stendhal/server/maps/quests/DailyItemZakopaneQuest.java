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

import games.stendhal.common.constants.Occasion;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseAtkXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseDefXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
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

/**
 * QUEST: Daily Gazda Wojtek Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Zakopane
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Zakopane to get a quest to fetch an item
 * <li> bring the item to the mayor
 * <li> if you cannot bring it in one week he offers you the chance to fetch
 * another instead
 * <p>
 * REWARD:
 * <li> xp 
 * <li> 10 Karma
 * <p>
 * REPETITIONS:
 * <li> once a day
 */
public class DailyItemZakopaneQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "gazda_wojtek_daily_item";
	private final SpeakerNPC npc = npcs.get("Gazda Wojtek");

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = TimeUtil.MINUTES_IN_WEEK; 

	/** How often the quest may be repeated */
	private static final int delay = TimeUtil.MINUTES_IN_DAY;

	private static DailyItemZakopaneQuest instance;

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
	 * 		DailyItemZakopaneQuest
	 */
	public static DailyItemZakopaneQuest getInstance() {
		if (instance == null) {
			instance = new DailyItemZakopaneQuest();
		}

		return instance;
	}

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// ammunition
		items.put("bełt",10);
		items.put("bełt stalowy",5);
		items.put("bełt złoty",25);
		items.put("bełt płonący",10);

		// armor
		items.put("cuha góralska",1);
		items.put("góralski gorset",1);
		items.put("zardzewiała zbroja płytowa",1);

		// axe
		items.put("ciupaga",1);
		items.put("siekierka",1);

		// boots
		items.put("kierpce",1);

		// cloaks
		items.put("chusta góralska",1);

		// containers
		items.put("pusty bukłak",5);

		// drinks
		items.put("buteleczka wody",20);
		items.put("butelka wody",15);
		items.put("bukłak z wodą",10);
		items.put("miód pitny",5);

		// food
		items.put("kiełbasa wiejska",10);
		items.put("opieńka miodowa",10);
		items.put("ziemniaki",10);
		items.put("oscypek",5);
		
		// helmet
		items.put("hełm barbarzyńcy",1);
		items.put("góralski kapelusz",1);

		// legs
		items.put("góralska biała spódnica",1);
		items.put("góralska spódnica",1);
		items.put("portki bukowe",1);

		// misc
		items.put("pióro herszta hordy zbójeckiej",5);

		// resource
		items.put("bryła lodu",30);
		items.put("skóra lwa",2);
		items.put("skóra tygrysa",3);
		items.put("skóra zwierzęca",10);
		items.put("skóra białego tygrysa",10);
		items.put("skóra xenocium",2);
		items.put("kość dla psa",20);
		items.put("ruda srebra",10);
		items.put("sztabka srebra",1);
		items.put("sól",10);
		items.put("siarka",10);
		items.put("węgiel drzewny",5);
		items.put("węgiel",10);
		items.put("kryształ ametystu",10);
		items.put("kryształ szmaragdu",10);
		items.put("kryształ szafiru",10);
		items.put("kryształ rubinu",10);
		items.put("kryształ obsydianu",10);
		items.put("kieł wilczy",20);
		items.put("kieł tygrysi",10);
		items.put("kieł niedźwiedzi",15);
		items.put("pazury wilcze",10);
		items.put("pazury tygrysie",10);
		items.put("niedźwiedzie pazury",10);
		items.put("piórko",30);

		// special
		items.put("piłka",1);

		// shield
		items.put("polska tarcza drewniana",1);
		items.put("polska tarcza kolcza",1);
		items.put("polska tarcza lekka",1);
		items.put("polska płytowa tarcza",1);
		items.put("tarcza królewska",1);

		// gloves
		items.put("skórzane rękawice",1);
		items.put("skórzane wzmocnione rękawice",1);

		// magic
		items.put("magia płomieni",10);
		items.put("magia ziemi",10);
		items.put("magia deszczu",10);
		items.put("magia mroku",5);
		items.put("magia światła",5);

		// wand
		items.put("różdżka",1);
		items.put("różdżka Strzyboga",1);
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
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Zakopane potrzebuje zapasów. Zdobądź [item]"
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
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie by dostarczyć [item]"
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
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Możesz dostać tylko jedno zadanie dziennie. Proszę wróć za"));

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
		actions.add(new IncreaseXPDependentOnLevelAction(6, 97.0));
		if (!Occasion.SECOND_WORLD) {
			actions.add(new IncreaseAtkXPDependentOnLevelAction(6, 97.0));
			actions.add(new IncreaseDefXPDependentOnLevelAction(6, 97.0));
		}
		actions.add(new IncreaseKarmaAction(20.0));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING, 
				"Dobra robota! Pozwól sobie podziękować w imieniu obywateli Zakopanego!",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING, 
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Jeszcze nie masz [item]"
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
				"Dzienne Zadanie Gazdy Wojtka",
				"Gazda Wojtek potrzebuje dostaw towaru do Zakopanego.",
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
		res.add(player.getGenderVerb("Napotkałem") + " się na Gazdę Wojtka w ratuszu Zakopane.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie pomogę gaździe oraz mieszkańcom Zakopane.");
			return res;
		}

		res.add("Bardzo chętnie poświęcę swój drogocenny czas, aby pomóc mieszkańcom Zakopanego.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(player.getGenderVerb("Zostałem") + " " + player.getGenderVerb("poproszony") + " o przyniesienie "
						+ Grammar.quantityplnoun(amount, questItem) + ", aby pomóc Zakopanemu. Nie mam tego jeszcze.");
			} else {
				res.add(player.getGenderVerb("Znalazłem") + " "
						+ Grammar.quantityplnoun(amount, questItem) + " do pomocy Zakopanemu i muszę je dostarczyć.");
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(player.getGenderVerb("Pomogłem") + " Zakopanemu z dostawami "
					+ Grammar.quantityplnoun(repetitions, "raz") + " do tej pory.");
		}
		if (isRepeatable(player)) {
			res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do Gazdy Wojtka i teraz Zakopane znów potrzebuje zapasów.");
		} else if (isCompleted(player)){
			res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do Gazdy Wojtka i odebrałem moją nagrodę w ciągu ostatnich 24 godzin.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Dzienne Zadanie w Zakopane";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
				new QuestCompletedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
}
