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
 * QUEST: Daily Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Ados
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Ados to get a quest to fetch an item
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
public class DailyItemQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "daily_item";
	private final SpeakerNPC npc = npcs.get("Mayor Chalmers");

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = TimeUtil.MINUTES_IN_WEEK;

	/** How often the quest may be repeated */
	private static final int delay = TimeUtil.MINUTES_IN_DAY;

	private static DailyItemQuest instance;

	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	/**
	 * Get the static instance.
	 *
	 * @return
	 * 		DailyItemQuest
	 */
	public static DailyItemQuest getInstance() {
		if (instance == null) {
			instance = new DailyItemQuest();
		}

		return instance;
	}

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// ammunition
		items.put("strzała złota",5);
		items.put("strzała żaru",5);
		items.put("strzała żelazna",7);
		items.put("strzała",10);

		// armor
		items.put("zbroja lazurowa",1);
		items.put("kolczuga",1);
		items.put("koszula",1);
		items.put("kolczuga wzmocniona",1);
		items.put("złota kolczuga",1);
		items.put("żelazna zbroja łuskowa",1);
		items.put("skórzana zbroja",1);
		items.put("skórzany kirys",1);
		items.put("skórzana zbroja łuskowa",1);
		items.put("żelazny kirys z naramiennikami",1);
		items.put("skórzany kirys z naramiennikami",1);
		items.put("zbroja płytowa",1);
		items.put("zbroja łuskowa",1);
		items.put("zbroja ćwiekowa",1);

		// axe
		items.put("topór",1);
		items.put("berdysz",1);
		items.put("topór bojowy",1);
		items.put("halabarda",1);
		items.put("topór jednoręczny",1);
		items.put("pordzewiała kosa",1);
		items.put("kosa",1);
		items.put("sierp",1);
		items.put("toporek",1);
		items.put("topór obosieczny",1);

		// boots
		items.put("buty lazurowe",1);
		items.put("buty kolcze",1);
		items.put("buty skórzane",1);
		items.put("buty nabijane ćwiekami",1);

		// cloaks
		items.put("lazurowy płaszcz elficki",1);
		items.put("peleryna",1);
		items.put("płaszcz krasnoludzki",1);
		items.put("peleryna elficka",1);
		items.put("szmaragdowy płaszcz smoczy",1);

		// club
		items.put("maczuga",1);
		items.put("piernacz",1);
		items.put("kiścień",1);
		items.put("złoty pyrlik",1);
		items.put("złoty buzdygan",1);
		items.put("pyrlik",1);
		items.put("buzdygan",1);
		items.put("złoty kiścień",1);
		items.put("kij",1);
		items.put("młot bojowy",1);

		// container
		items.put("butla czwórniaczka",3);
		items.put("butelka",5);
		items.put("wąska butelka",5);

		// drinks
		items.put("antidotum",5);
		items.put("sok z chmielu",10);
		items.put("mocne antidotum",5);
		items.put("duży eliksir",5);
		items.put("mleko",5);
		items.put("mały eliksir",5);
		items.put("trucizna",5);
		items.put("eliksir",5);
		items.put("filiżanka herbaty",3);
		items.put("butelka wody",5);
		items.put("napój z winogron",10);

		// flower
		items.put("stokrotki",5);
		items.put("lilia",5);
		items.put("róża",10);

		// food
		items.put("jabłko",5);
		items.put("jabłecznik",2);
		items.put("karczoch",10);
		items.put("chleb",5);
		items.put("brokuł",5);
		items.put("pieczarka",10);
		items.put("marchew",10);
		items.put("kalafior",5);
		items.put("palia alpejska",5);
		items.put("ser",10);
		items.put("wisienka",10);
		items.put("ciasto z wiśniami",2);
		items.put("udko",10);
		items.put("tabliczka czekolady",5);
		items.put("błazenek",5);
		items.put("kapusta",5);
		items.put("cukinia",5);
		items.put("jajo",1);
		items.put("mufinka", 5);
		items.put("tarta z rybnym nadzieniem",1);
		items.put("czosnek",5);
		items.put("winogrona",5);
		items.put("grillowany stek",1);
		items.put("szynka",10);
		items.put("miód",2);
		items.put("por",5);
		items.put("mięso",10);
		items.put("cebula",5);
		items.put("gruszka",5);
		items.put("okoń",5);
		items.put("tarta",5);
		items.put("granat",5);
		items.put("borowik",10);
		items.put("skrzydlica",5);
		items.put("płotka",5);
		items.put("sałata",10);
		items.put("kanapka",5);
		items.put("szpinak",5);
		items.put("pokolec",5);
		items.put("muchomor",15);
		items.put("pomidor",5);
		items.put("pstrąg",5);
		items.put("oliwka",5);

		// helmet
		items.put("lazurowy hełm",1);
		items.put("misiurka",1);
		items.put("hełm kolczy",1);
		items.put("skórzany hełm",1);
		items.put("kapelusz leśniczego",1);
		items.put("hełm nabijany ćwiekami",1);
		items.put("hełm wikingów",1);

		// herb
		items.put("arandula",5);
		items.put("sclaria",5);
		items.put("mandragora",3);
		items.put("kekik",5);
		
		// legs
		items.put("spodnie lazurowe",1);
		items.put("spodnie kolcze",1);
		items.put("skórzane spodnie",1);
		items.put("spodnie nabijane ćwiekami",1);

		// misc
		items.put("kości do gry",1);
		items.put("skórzana nić", 20);
		items.put("kolorowe kulki", 2);
		items.put("pułapka na gryzonie",5);
		items.put("pluszowy miś",1);

		// money
		items.put("money",100);

		// ranged
		items.put("klejony łuk",1);
		items.put("długi łuk",1);
		items.put("drewniany łuk",1);

		// resource
		items.put("węgiel drzewny",5);
		items.put("węgiel",10);
		items.put("mąka",5);
		items.put("zboże",20);
		items.put("sztabka złota",5);
		items.put("bryłka złota",10);
		items.put("sztabka żelaza",5);
		items.put("ruda żelaza",10);
		items.put("polano",10);

		// shield
		items.put("tarcza lazurowa",1);
		items.put("puklerz",1);
		items.put("tarcza królewska",1);
		items.put("wzmocniona lwia tarcza",1);
		items.put("lwia tarcza",1);
		items.put("tarcza płytowa",1);
		items.put("tarcza z czaszką",1);
		items.put("tarcza ćwiekowa",1);
		items.put("tarcza jednorożca",1);
		items.put("drewniana tarcza",1);

		// sword
		items.put("miecz zaczepny",1);
		items.put("pałasz",1);
		items.put("miecz dwuręczny",1);
		items.put("sztylecik",1);
		items.put("miecz elficki",1);
		items.put("katana",1);
		items.put("mieczyk",1);
		items.put("bułat",1);
		items.put("krótki miecz",1);
		items.put("miecz",1);

		// tool
		items.put("igła do skór",1);
		items.put("kilof",1);
		items.put("misa do płukania złota",1);
		items.put("lina",3);
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
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Ados potrzebuje zapasów. Zdobądź [item]"
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
						+ ", powiedz #załatwione, jak przyniesiesz. Być może nie ma tych przedmiotów! Możesz przynieść #inny przedmiot jeżeli chcesz lub wróć z tym, o który cię na początku prosiłem."));
	
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
		actions.add(new IncreaseXPDependentOnLevelAction(8, 90.0));
		if (!Occasion.SECOND_WORLD) {
			actions.add(new IncreaseAtkXPDependentOnLevelAction(8, 90.0));
			actions.add(new IncreaseDefXPDependentOnLevelAction(8, 90.0));
		}
		actions.add(new IncreaseKarmaAction(10.0));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING, 
				"Dobra robota! Pozwól sobie podziękować w imieniu obywateli Ados!",
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
				"Dzienne Zadanie na Przedmiot",
				"Mayor Chalmers potrzebuje zapasów dla miasta Ados.",
				true, 2);

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
		res.add(player.getGenderVerb("Spotkałem") + " burmistrza Mayor Chalmers w Ratuszu Ados");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chę pomóc Ados.");
			return res;
		}

		res.add("Chcę pomóc Ados.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(player.getGenderVerb("Zostałem") + " " + player.getGenderVerb("poproszony") + " o przyniesienie "
						+ Grammar.quantityplnoun(amount, questItem) + ", aby pomóc Ados. Nie mam tego jeszcze.");
			} else {
				res.add(player.getGenderVerb("Znalazłem") + " "
						+ Grammar.quantityplnoun(amount, questItem) + " do pomocy Ados i muszę je dostarczyć.");
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(player.getGenderVerb("Pomogłem") + " Ados z dostawami "
					+ Grammar.quantityplnoun(repetitions, "raz") + " do tej pory.");
		}
		if (isRepeatable(player)) {
            res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do burmistrza i teraz Ados znów potrzebuje zapasów.");
		} else if (isCompleted(player)){
			res.add(player.getGenderVerb("Dostarczyłem") + " ostatni przedmiot do burmistrza i odebrałem moją nagrodę w ciągu 24 godzin.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "Dzienne Zadanie w Ados";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
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
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
}
