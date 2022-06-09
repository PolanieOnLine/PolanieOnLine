/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

import games.stendhal.common.MathHelper;
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

/**
 * QUEST: Weekly Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <ul><li> Hazel, Museum Curator of Kirdneh
 * <li> some items
 * </ul>
 * STEPS:<ul>
 * <li> talk to Museum Curator to get a quest to fetch a rare item
 * <li> bring the item to the Museum Curator
 * <li> if you cannot bring it in 6 weeks she offers you the chance to fetch
 *
 * another instead </ul>
 *
 * REWARD:
 * <ul><li> xp
 * <li> between 100 and 600 money
 * <li> can buy kirdneh house if other eligibilities met
 * <li> 10 Karma
 * </ul>
 * REPETITIONS:
 * <ul><li> once a week</ul>
 */
public class WeeklyItemQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "weekly_item";
	private final SpeakerNPC npc = npcs.get("Hazel");

	/** the logger instance */
	private static final Logger logger = Logger.getLogger(WeeklyItemQuest.class);

	private static WeeklyItemQuest instance;

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK * 6;

	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;

	/**
	 * All items which are hard enough to find but not tooo hard and not in Daily quest. If you want to do
	 * it better, go ahead. *
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
	public static WeeklyItemQuest getInstance() {
		if (instance == null) {
			instance = new WeeklyItemQuest();
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
		addEasy("zbroja cieni",1); // 450.59, 35 (purchasable)
		addEasy("kamienna zbroja",1); // 53.63, 41
		// armor (medium)
		addMed("zbroja barbarzyńcy",1); // 400.95, 37
		addMed("zbroja krasnoludzka",1); // 163.15, 15
		addMed("złota zbroja",1); // 536.32, 30
		addMed("magiczna zbroja płytowa",1); // 0.097, 90 (maybe too rare, remove?)
		// armor (hard)
		addHard("zbroja chaosu",1); // 4.45, 70
		addHard("lodowa zbroja",1); // 3.38, 45
		addHard("zbroja mainiocyjska",1); // 1.33, 250
		addHard("zbroja xenocyjska",1); // 2.0, 170

		// axe (medium)
		addMed("złoty topór obosieczny",1); // 19.21, 60

		// boots (easy)
		addEasy("złote buty",1); // 89.09, 25
		addEasy("buty żelazne",1); // 315.17, 15
		addEasy("buty kamienne",1); // 374.07, 32
		// boots (medium)
		addMed("buty chaosu",1); // 347.15, 62
		addMed("buty mainiocyjskie",1); // 6.99, 28
		addMed("buty cieni",1); // 248.78, 41 (purchasable)
		// boots (hard)
		addHard("buty xenocyjskie",1); // 3.33, 170

		// cloak (easy)
		addEasy("prążkowany płaszcz lazurowy",1); // 39.38, 26 (purchasable)
		// cloak (medium)
		addMed("lazurowy płaszcz smoczy",1); // 42.19, 70
		addMed("płaszcz chaosu",1); // 200.67, 74
		addMed("złoty płaszcz",1); // 71.0, 70
		addMed("magiczny płaszcz",1); // 20.76, 114
		addMed("karmazynowy płaszcz smoczy",1); // 13.86, 125
		addMed("płaszcz cieni",1); // 93.14, 61 (purchasable)
		// cloak (hard)
		addHard("płaszcz mainiocyjski",1); // 5.16, 44
		addHard("płaszcz xenocyjski",1); // 2.0, 170

		// club (easy)
		addEasy("kij z czaszką",1); // 81.22, 15
		addEasy("kropacz",1); // 86.57, 16

		// drink (easy)
		addEasy("wielki eliksir",5); // 1357.34, 51 (purchasable)
		addEasy("zupa rybna",3); // n/a, n/a (producable)

		// helmet (medium)
		addMed("złoty hełm",1); // 80.9, 30
		addMed("złoty hełm wikingów",1); // 25.98, 58
		addMed("hełm mainiocyjski",1); // 3.47, 37
		addMed("hełm cieni",1); // 27.26, 52 (purchasable)
		// helmet (hard)
		addHard("hełm chaosu",1); // 44.16, 75

		// jewellery (medium)
		addMed("diament",1); // 20.13, 48
		// jewellery (hard)
		addHard("obsydian",1); // 4.17, 200

		// legs (medium)
		addMed("spodnie chaosu",1); // 123.2, 85
		addMed("spodnie krasnoludzkie",1); // 6.13, 67
		addMed("złote spodnie",1); // 53.80, 25
		addMed("spodnie mainiocyjskie",1); // 13.97, 20
		addMed("spodnie cieni",1); // 49.19, 55
		// legs (hard)
		addHard("spodnie xenocyjskie",1); // 2.0, 170

		// misc (medium)
		addMed("serce olbrzyma",5); // 2409.75, 110
		addMed("gruczoł jadowy",1); // 55.56, 120
		// misc (hard)
		addHard("róg jednorożca", 5); // 398.33, 250

		// resource (medium)
		addMed("sztabka mithrilu",1); // 0.0006, 90 (producable)
		addMed("bryłka mithrilu",1); // 0.16, 90 (harvestable)
		addMed("gruczoł przędzy",7); // 522.79, 110

		// ring (medium difficulty)
		addMed("pierścień leczniczy", 1); // 78.26, 73

		// special (easy)
		addEasy("czterolistna koniczyna",1); // 76.37, 9
		// special (medium)
		addMed("mityczne jajo",1); // n/a, n/a (quest reward)

		// shield (easy)
		addEasy("szmaragdowa tarcza smocza",1); // 11.3, 50
		addEasy("tarcza cieni",1); // 113.19, 36 (purchasable)
		// shield (medium)
		addMed("tarcza chaosu",1); // 165.18, 75
		addMed("złota tarcza",1); // 21.2, 48
		addMed("magiczna tarcza płytowa",1); // 23.31, 41
		addMed("tarcza mainiocyjska",1); // 9.33, 102
		// shield (hard)
		addHard("tarcza xenocyjska",1); // 1.33, 170

		// sword (easy)
		addEasy("sztylet mroku",1); // 6.95, 50 (acquired from well)
		addEasy("miecz demonów",1); // 33.73, 42
		// sword (medium)
		addMed("sztylet mordercy",1); // 235.12, 40
		addMed("pogromca",1); // 2.26, 69
		addMed("miecz chaosu",1); // 120.58, 43
		addMed("miecz elfów ciemności",1); // 8.04, 58
		addMed("miecz ognisty",1); // 78.51, 50
		addMed("półtorak",1); // 29.99, 52
		addMed("piekielny sztylet",1); // 1.46, 42 (purchasable)
		addMed("miecz lodowy",1); // 3.28, 45
		addMed("czarny sztylet",1); // 30.9, 39
		// sword (hard)
		addHard("miecz nieśmiertelnych",1); // 0.17, 230

		// tool (medium)
		addMed("zwój czyszczący", 1); // 18.05, 55

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
		if (DailyItemQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest: " + item);
			return;
		}

		items_easy.put(item, quant);
	}

	private static void addMed(final String item, final int quant) {
		if (DailyItemQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest: " + item);
			return;
		}

		items_med.put(item, quant);
	}

	private static void addHard(final String item, final int quant) {
		if (DailyItemQuest.utilizes(item)) {
			logger.warn("Not adding item already utilized in DailyItemQuest: " + item);
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
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 0, items, "Chcę, aby muzeum w Kirdneh było największe w krainie! Dostarcz mi [item]"
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
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie przyniesienia do muzeum [item]"
						+ ". Powiedz #zakończone jeżeli będziesz miał ze sobą."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie przyniesienia do muzeum [item]"
						+ ". Powiedz #zakończone jeżeli będziesz miał ze sobą. Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Muzeum może Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za "));

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
				"Nie pamiętam, abym dawała Tobie #zadanie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już ukończyłeś ostatnie zadanie, które Ci dałam.",
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
		actions.add(new IncreaseKarmaAction(40.0));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int goldamount;
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 100 * Rand.roll1D20();
				money.setQuantity(goldamount);
				player.equipOrPutOnGround(money);
				raiser.say("Wspaniale! Oto " + Integer.toString(goldamount) + " money na pokrycie wydatków.");
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
				"Muzeum Kirdneh potrzebuje pomocy!",
				"Hazel, kuratorka Muzeum Kirdneh, chce aby było one największym w kraju i potrzebuje mojej pomocy raz na tydzień.",
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
		res.add(Grammar.genderVerb(player.getGender(), "Spotkałem") + " kuratorkę muzeum Kirdneh, Hazel.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomagać muzeum w Kirdneh i stać się największym w kraju.");
			return res;
		}
		res.add("Chcę pomóc muzeum w Kirdneh i stać się największymw kraju.");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add(String.format(Grammar.genderVerb(player.getGender(), "Zostałem") + " " + Grammar.genderVerb(player.getGender(), "poproszony") + ", aby przynieść " +Grammar.quantityplnoun(amount, questItem) + " do muzeum w Kirdneh."));
			} else {
				res.add(String.format("Mam " + Grammar.quantityplnoun(amount, questItem) + " dla muzeum w Kirdneh. Muszę to im zanieść."));
			}
		}
		if (isRepeatable(player)) {
			res.add(Grammar.genderVerb(player.getGender(), "Zaniosłem") + " wartościowy przedmiot do Hazela i muzeum zleciło mi znalezienie następnego.");
		} else if (isCompleted(player)) {
			res.add(Grammar.genderVerb(player.getGender(), "Pomogłem") + " muzeum. Za 7 dni zajrze tam znów. Byś może potrzebować będą mojej pomocy.");
		}
		// add to history how often player helped Hazel so far
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(Grammar.genderVerb(player.getGender(), "Przyniosłem") + " już " + repetitions + " eksponatów do muzeum.");
		}

		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Tygodniowe Zadanie w Kirdneh";
	}

	// the items requested are pretty hard to get, so it's not worth prompting player to go till they are higher level.
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getRegion() {
		return Region.KIRDNEH;
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
