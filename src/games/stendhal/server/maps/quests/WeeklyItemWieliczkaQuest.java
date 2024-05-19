/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
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

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.constants.Testing;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
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

public class WeeklyItemWieliczkaQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "weekly_item_wieliczka";
	private final SpeakerNPC npc = npcs.get("Zbigniew");

	/** the logger instance */
	private static final Logger logger = Logger.getLogger(WeeklyItemWieliczkaQuest.class);

	private static WeeklyItemWieliczkaQuest instance;

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = TimeUtil.MINUTES_IN_WEEK * 3;

	/** How often the quest may be repeated */
	private static final int delay = TimeUtil.MINUTES_IN_WEEK;

	/**
	 * All items which are hard enough to find but not too hard and not in Daily quest. If you want to do
	 * it better, go ahead.
	 */
	private static final Map<String, Integer> items_easy = new HashMap<String, Integer>();
	private static final Map<String, Integer> items_med = new HashMap<String, Integer>();
	private static final Map<String, Integer> items_hard = new HashMap<String, Integer>();

	private static final int LEVEL_MED = 51;
	private static final int LEVEL_HARD = 151;

	/**
	 * Get the static instance.
	 *
	 * @return
	 * 		WeeklyItemQuest
	 */
	public static WeeklyItemWieliczkaQuest getInstance() {
		if (instance == null) {
			instance = new WeeklyItemWieliczkaQuest();
		}

		return instance;
	}

	private static void buildItemsMap() {
		/* Comments depict drop scoring (See: https://stendhalgame.org/wiki/StendhalItemsDropScoring)
		 * followed by lowest level creature that drops.
		 *
		 * Nothing below a certain score that is not obtainable by purchase
		 * or other repeatable means should be added.
		 *
		 * Current most rare not obtainable by other means: 0.097 (magic plate armor)
		 *
		 * Difficulty:
		 * - easy:   levels 0-50
		 * - medium: levels 51-150
		 * - hard:   levels 151+
		 */

		// armor (easy)
		addEasy("zbroja barbarzyńcy",1);
		addEasy("zbroja krasnoludzka",1);
		addEasy("zbroja lazurowa",1);
		addEasy("złota zbroja",1);
		// armor (medium)
		addMed("zbroja chaosu",1);
		addMed("cuha góralska",1);
		addMed("zbroja szamana barbarzyńców",1);

		// axe (easy)
		addEasy("ciupaga",1);
		// axe (medium)
		addMed("czarna halabarda",1);
		addMed("topór chaosu",1);
		// axe (hard)
		addHard("złota ciupaga",1);

		// boots (easy)
		addEasy("buty barbarzyńcy",1);
		addEasy("buty karmazynowe",1);
		addEasy("buty elfickie",1);
		// boots (medium)
		addMed("buty kamienne",1);
		addMed("buty z zielonego potwora",1);
		// boots (hard)
		addHard("buty cieni",1);
		addHard("buty wampirze",1);

		// cloak (easy)
		addEasy("złoty płaszcz",1);
		addEasy("płaszcz licha",1);
		// cloak (medium)
		addMed("szmaragdowy płaszcz smoczy",1);
		addMed("czarny płaszcz smoczy",1);
		addMed("lodowy płaszcz",1);
		addMed("płaszcz chaosu",1);
		// cloak (hard)
		addHard("płaszcz z otchłani",1);

		// club (medium)
		addMed("kij z czaszką",1);
		addMed("młot chaosu",1);
		addMed("kropacz",1);

		// drinks (medium)
		addMed("zabójcza trucizna",5);
		addMed("śmiertelna trucizna",5);
		addMed("gigantyczny eliksir",3);
		addMed("zupa rybna",3);

		// helmet (easy)
		addEasy("kapelusz elficki",1);
		addEasy("hełm nabijany ćwiekami",1);
		addEasy("hełm barbarzyńcy",1);
		addEasy("góralski kapelusz",1);
		addEasy("misiurka",1);
		// helmet (medium)
		addMed("hełm mainiocyjski",1);
		addMed("hełm cieni",1);

		// jewellery (easy)
		addEasy("rubin",3);
		addEasy("szmaragd",5);
		// jewellery (medium)
		addMed("ametyst",1);
		addMed("diament",1);
		// jewellery (hard)
		addHard("obsydian",2);

		// legs (easy)
		addEasy("góralska biała spódnica",1);
		addEasy("spodnie krasnoludzkie",1);
		// legs (medium)
		addMed("lodowe spodnie",1);
		addMed("spodnie pustynne",1);
		// legs (hard)
		addHard("spodnie wampirze",1);
		addHard("spodnie nabijane klejnotami",1);

		// shield (easy)
		addEasy("szmaragdowa tarcza smocza",1);
		addEasy("polska tarcza ciężka",1);
		addEasy("polska tarcza drewniana",1);
		addEasy("polska tarcza lekka",1);
		addEasy("polska tarcza kolcza",1);
		addEasy("tarcza piaskowa",1);
		addEasy("tarcza elficka",1);
		// shield (medium)
		addMed("tarcza jednorożca",1);
		addMed("ognista tarcza",1);
		addMed("lodowa tarcza",1);
		addMed("tarcza chaosu",1);
		addMed("lwia tarcza",1);
		// shield (hard)
		addHard("tarcza licha",1);

		// sword (easy)
		addEasy("nihonto",1);
		addEasy("klinga orków",1);
		addEasy("miecz elficki",1);
		addEasy("miecz zaczepny",1);
		// sword (medium)
		addMed("chopesz",1);
		addMed("miecz chaosu",1);
		addMed("miecz cieni",1);
		addMed("miecz ognisty",1);
		addMed("miecz lodowy",1);
		// sword (hard)
		addHard("miecz nieśmiertelnych",1);
		addHard("ognisty miecz demonów",1);
		
		// belt (easy)
		addEasy("wzmocniony pas skórzany",1);
		addEasy("pas krasnoludzki",1);
		addEasy("pas karmazynowy",1);
		addEasy("pas zbójnicki",1);
		addEasy("pas elficki",1);
		// belt (medium)
		addMed("złoty pas",1);
		addMed("pas olbrzymi",1);
		addMed("pas wampirzy",1);
		
		// glove (easy)
		addEasy("rękawice karmazynowe",1);
		addEasy("rękawice lazurowe",1);
		// glove (medium)
		addMed("lodowe rękawice",1);
		addMed("ogniste rękawice",1);
		addMed("rękawice cieni",1);
		addMed("rękawice płytowe",1);
		addMed("kamienne rękawice",1);
		
		// wand (medium)
		addMed("różdżka Strzyboga",1);
		addMed("różdżka Wołosa",1);

		// necklace (easy)
		addEasy("korale",1);
		addEasy("elficki naszyjnik",1);

		// ranged (medium)
		addMed("klejony łuk",1);
		addMed("kusza",1);
		addMed("kusza łowcy",1);
		addMed("lodowa kusza",1);

		// add "easy" items to "medium" list
		for (final String key: items_easy.keySet()) {
			items_med.put(key, items_easy.get(key));
		}

		// add "medium" items to "hard" list
		for (final String key: items_med.keySet()) {
			items_hard.put(key, items_med.get(key));
		}
	}

	private static void addEasy(final String item, final int quant) {
		if (DailyItemQuest.utilizes(item) && DailyItemZakopaneQuest.utilizes(item) && DailyItemGdanskQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest, DailyItemZakopaneQuest & DailyItemGdanskQuest: " + item);
			return;
		}

		items_easy.put(item, quant);
	}

	private static void addMed(final String item, final int quant) {
		if (DailyItemQuest.utilizes(item) && DailyItemZakopaneQuest.utilizes(item) && DailyItemGdanskQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest, DailyItemZakopaneQuest & DailyItemGdanskQuest: " + item);
			return;
		}

		items_med.put(item, quant);
	}

	private static void addHard(final String item, final int quant) {
		if (DailyItemQuest.utilizes(item) && DailyItemZakopaneQuest.utilizes(item) && DailyItemGdanskQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest, DailyItemZakopaneQuest & DailyItemGdanskQuest: " + item);
			return;
		}

		items_hard.put(item, quant);
	}

	private ChatAction startQuestAction(final String level) {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final Map<String, Integer> items;
		if (level.equals("easy")) {
			items = items_easy;
		} else if (level.equals("med") || level.equals("medium")) {
			items = items_med;
		} else {
			items = items_hard;
		}

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Dostarcz mi [item]"
				+ " i powiedz #'załatwione', gdy przyniesiesz."));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		return new MultipleActions(actions);
	}

	private void getQuest() {
		final ChatCondition startEasyCondition = new AndCondition(
				new LevelLessThanCondition(LEVEL_MED),
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT,1,delay))));

		final ChatCondition startMedCondition = new AndCondition(
				new LevelGreaterThanCondition(LEVEL_MED - 1),
				new LevelLessThanCondition(LEVEL_HARD),
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT,1,delay))));

		final ChatCondition startHardCondition = new AndCondition(
				new LevelGreaterThanCondition(LEVEL_HARD - 1),
				new OrCondition(
						new QuestNotStartedCondition(QUEST_SLOT),
						new AndCondition(
								new QuestCompletedCondition(QUEST_SLOT),
								new TimePassedCondition(QUEST_SLOT,1,delay))));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie, aby przynieść [item]"
						+ ". Powiedz #zakończone jeśli zdobędziesz już ten przedmiot."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie, aby przynieść [item]"
						+ ". Powiedz #zakończone jeśli zdobędziesz [item]. Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Mogę Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za "));

		// for players levels 50 & below
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				startEasyCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("easy"));

		// for players levels 51-150
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				startMedCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("med"));

		// for players levels 151+
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				startHardCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("hard"));
	}

	private void completeQuest() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Nie pamiętam, abym dawał Tobie #zadanie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już ukończyłeś ostatnie zadanie, które Ci dałem.",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(5.0/3.0, 290.0));
		if (!Occasion.SECOND_WORLD) {
			actions.add(new IncreaseAtkXPDependentOnLevelAction(5.0/3.0, 290.0));
			actions.add(new IncreaseDefXPDependentOnLevelAction(5.0/3.0, 290.0));
		}
		if (Testing.COMBAT) {
			actions.add(new IncreaseRatkXPDependentOnLevelAction(5.0/3.0, 290.0));
		}
		actions.add(new IncreaseKarmaAction(50.0));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int goldamount;
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 150 * Rand.roll1D20();
				money.setQuantity(goldamount);
				player.equipOrPutOnGround(money);
				raiser.say("Wspaniale! Oto " + Integer.toString(goldamount) + " pieniędzy na pokrycie strat.");
			}
		});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Nie masz ze sobą [item]"
						+ " Zdobądź i powiedz wtedy #zakończone."));
	}

	private void abortQuest() {
		final ChatCondition startEasyCondition = new AndCondition(
				new LevelLessThanCondition(LEVEL_MED),
				new QuestActiveCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT,1,expireDelay));

		final ChatCondition startMedCondition = new AndCondition(
				new LevelGreaterThanCondition(LEVEL_MED - 1),
				new LevelLessThanCondition(LEVEL_HARD),
				new QuestActiveCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT,1,expireDelay));

		final ChatCondition startHardCondition = new AndCondition(
				new LevelGreaterThanCondition(LEVEL_HARD - 1),
				new QuestActiveCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT,1,expireDelay));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				startEasyCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("easy"));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				startMedCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("med"));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				startHardCondition,
				ConversationStates.ATTENDING,
				null,
				startQuestAction("hard"));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				"Nie minęło tak dużo czasu od rozpoczęcia zadania. Nie powinieneś się tak szybko poddawać.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Obawiam się, że jeszcze nie dałem Tobie #zadania.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Wieliczka potrzebuje pomocy!",
				"Zbigniew, burmistrz Wieliczki, chce uzupełnić magazyn w ratuszu i potrzebuje mojej pomocy raz na tydzień.",
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
		res.add(player.getGenderVerb("Spotkałem") + " burmistrza Zbigniewa w ratuszu w Wieliczce.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomagać burmistrzowi w Wieliczce.");
			return res;
		}
		res.add("Chcę pomóc burmistrzowi.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(String.format(player.getGenderVerb("Zostałem") + " " + player.getGenderVerb("poproszony") + ", aby przynieść " + Grammar.quantityplnoun(amount, questItem) + " do ratusza w Wieliczce."));
			} else {
				res.add(String.format("Mam " + Grammar.quantityplnoun(amount, questItem) + " dla burmistrza Wieliczki. Muszę to im zanieść."));
			}
		}
		if (isRepeatable(player)) {
			res.add(player.getGenderVerb("Zaniosłem") + " wartościowy przedmiot do Zbigniewa i burmistrz zlecił mi znalezienie następnego.");
		} else if (isCompleted(player)) {
			res.add(player.getGenderVerb("Pomogłem") + " Wieliczce. Za 7 dni zajrze tam znów. Być może potrzebować będą mojej pomocy.");
		}
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(player.getGenderVerb("Przyniosłem") + " już " + repetitions + " wartościowych przedmiotów do ratusza w Wieliczce.");
		}

		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Tygodniowe Zadanie w Wieliczce";
	}

	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getRegion() {
		return Region.WIELICZKA;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
}
