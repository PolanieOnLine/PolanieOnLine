/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: The Sad Scientist.
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Vasi Elos, a scientist in Kalavan</li>
 * <li>Mayor Sakhs, the mayor of semos</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * 		<li>Talk to Vasi Elos, a lonely scientist.</li>
 * 		<li>Give him all stuff he needs for a present for his honey.</li>
 * 		<li>Talk to semos mayor.</li>
 * 		<li>Bring Elos mayor's letter.</li>
 * 		<li>Kill the Imperial Scientist.</li>
 *		<li>Give him the flask with his brother's blood.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * 		<li>a pair of black legs</li>
 * 		<li>20 Karma</li>
 * 		<li>10000 XP</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * 		<li>None</li>
 * </ul>
 */
public class SadScientist extends AbstractQuest {

	private static Logger logger = Logger.getLogger(SadScientist.class);

	private static final String LETTER_DESCRIPTION = "Oto list dla Vasi Elos.";
	private static final String QUEST_SLOT = "sad_scientist";
	private static final int REQUIRED_MINUTES = 20;
	private static final String NEEDED_ITEMS = "szmaragd=1;obsydian=1;szafir=1;rubin=2;sztabka złota=20;sztabka mithrilu=1";

	@Override
	public String getName() {
		return "TheSadScientist";
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
		final String questState = player.getQuest(QUEST_SLOT);
		// it might have been rejected before Vasi even explained what he wanted.
		if ("rejected".equals(questState)) {
			res.add("Vasi Elos poprosił mnie o pomoc, ale nie jestem zainteresowany, aby pomóc naukowcowi.");
			return res;
		}
		res.add("Vasi Elos poprosił mnie, abym dostarczył mu złoto i mithril, aby mógł sprawić dla swej słodkiej Very spodnie wysadzane klejnotami jako prezent.");
		if (getConditionForBeingInCollectionPhase().fire(player,null,null)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Do zrobienia spodni potrzeba jeszcze: " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
			return res;
		}
		res.add("Vasi Elos potrzebuje spodni cienia. Muszę mu je dostarczyć.");
		if ("legs".equals(questState)) {
			return res;
		}
		res.add("Vasi Elos wysadza spodnie klejnotami, które mu dostarczyłem.");
		if (questState.startsWith("making")) {
			return res;
		}
		res.add("Vasi Elos wysłał mnie do Mayor Sakhs, abym dowiedział się gdzie jest Vera.");
		if ("find_vera".equals(questState) && !player.isEquippedWithInfostring("karteczka", QUEST_SLOT)) {
			return res;
		}
		res.add("Mam straszną wiadomość dla Vasi Elos od Mayora Sakhsa.");
		if ("find_vera".equals(questState) && player.isEquippedWithInfostring("karteczka", QUEST_SLOT)) {
			return res;
		}
		res.add("Vasi Elos jest taki smutny i zły, że Vera odeszła. Muszę zabić jego własnego brata i dać mu kielich pełen krwi jego brata.");
		if (questState.startsWith("kill_scientist") && !new KilledForQuestCondition(QUEST_SLOT, 1).fire(player, null, null)) {
			return res;
		}
		res.add("Zabiłem Imperial Scientist Sergej Elos i muszę jako dowód dostarczyć kielich z jego krwią.");
		if (questState.startsWith("kill_scientist") && new KilledForQuestCondition(QUEST_SLOT, 1).fire(player, null, null)) {
			return res;
		}
		res.add("Vasi Elos jest naprawdę zrozpaczony. Naznaczył spodnie krwią brata.");
		if (questState.startsWith("decorating")) {
			return res;
		}
		res.add("Spodnie naznaczone krwią są całe czarne. Należą teraz do mnie." +
				"Ale jakim kosztem.");
		if ("done".equals(questState)){
			return res;
		}
        // if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Smutny Naukowiec",
			"Vasi Elos samotny naukowiec chce abym przyniósł mu prezent dla jego dziewczyny.",
			false);
		prepareQuestSteps();
	}

	private void prepareQuestSteps() {
		prepareScientist();
	}

	private void prepareScientist() {
		final SpeakerNPC scientistNpc = npcs.get("Vasi Elos");
		final SpeakerNPC mayorNpc = npcs.get("Mayor Sakhs");
		startOfQuest(scientistNpc);
		bringItemsPhase(scientistNpc);
		playerReturnsAfterRequestForLegs(scientistNpc);
		playerReturnsAfterGivingTooEarly(scientistNpc);
		playerReturnsAfterGivingWhenFinished(scientistNpc);
		playerReturnsWithoutLetter(scientistNpc);
		playerVisitsMayorSakhs(mayorNpc);
		playerReturnsWithLetter(scientistNpc);
		playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(scientistNpc);
		playerReturnsAfterKillingTheImperialScientist(scientistNpc);
		playerReturnsToFetchReward(scientistNpc);
		playerReturnsAfterCompletingQuest(scientistNpc);
	}

	private void playerReturnsToFetchReward(SpeakerNPC npc) {
		// time has passed
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
						new TimePassedCondition(QUEST_SLOT, 1, 5)
					);
		final ChatAction action = new MultipleActions(
				new SetQuestAction(QUEST_SLOT,"done"),
				new IncreaseKarmaAction(20),
				new IncreaseXPAction(10000),
				// here, true = bind them to player
				new EquipItemAction("czarne spodnie", 1, true));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Oto gotowe czarne spodnie. Możesz je teraz nosić, jest na nich symbol mojego bólu. Bądź pozdrowiony",
				action);

		// time has not yet passed
		final ChatCondition notCondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT,"decorating"),
				new NotCondition( new TimePassedCondition(QUEST_SLOT, 1, 5)));

		ChatAction reply = new SayTimeRemainingAction(QUEST_SLOT, 1, 5, "Nie zakończyłem dekorowania spodni. " +
				"Proszę sprawdź za ");
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				notCondition,
				ConversationStates.IDLE,
				null,
				reply);
	}

	private void playerReturnsAfterKillingTheImperialScientist(SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
				new KilledForQuestCondition(QUEST_SLOT, 1),
				new PlayerHasInfostringItemWithHimCondition("czara", QUEST_SLOT)
			);
		ChatAction action = new MultipleActions(
				new SetQuestAction(QUEST_SLOT, "decorating;"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1),
				new DropInfostringItemAction("czara", 1, QUEST_SLOT));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.ATTENDING,
				"Ha, ha, ha! Skropię spodnie wysadzane kamieniami krwią, aż powstanie na nich " +
				"mój #symbol bólu.",
				null);

		npc.add(ConversationStates.ATTENDING, "symbol",
				condition, ConversationStates.IDLE,
				"Mam zamiar stworzyć parę czarnych spodni. Wróć za 5 minut.",
				action);
	}


	private void playerReturnsWithoutKillingTheImperialScientistOrWithoutGoblet(
			SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
		new GreetingMatchesNameCondition(npc.getName()),
			new QuestStateStartsWithCondition(QUEST_SLOT, "kill_scientist"),
			new NotCondition(
				new AndCondition(new KilledForQuestCondition(QUEST_SLOT, 1),
					new PlayerHasInfostringItemWithHimCondition("czara", QUEST_SLOT))));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition, ConversationStates.IDLE,
				"Czuję ból i cierpienie. Zabij mojego brata i przynieś mi jego krew. To wszystko co mi w tej chwili potrzeba.",
				null);
	}

	private void playerReturnsWithLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new PlayerHasInfostringItemWithHimCondition("karteczka", QUEST_SLOT));

		final ChatAction action = new MultipleActions(
				new SetQuestAction(QUEST_SLOT, 0, "kill_scientist"),
				new StartRecordingKillsAction(QUEST_SLOT, 1, "Sergej Elos", 0, 1),
				new DropInfostringItemAction("karteczka", QUEST_SLOT));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_2,
				"Witam! Czy masz coś dla mnie?",
				null);

		npc.add(ConversationStates.INFORMATION_2, Arrays.asList("letter", "yes", "note", "list", "karteczka", "tak"),
				condition,
				ConversationStates.ATTENDING,
				"Och nie! Nie widzisz, że cierpię. W tej chwili nie jestem w stanie wykonać tych przepięknych spodni nabijanych klejnotami. " +
				"Chcę aby te spodnie były symbolem bólu i cierpienia. Ty! Pójdziesz i zabijesz mojego brata, " +
				"to imperialny naukowiec Sergej Elos. Przynieś mi jego krew.",
				action);

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.GOODBYE_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, 0, "kill_scientist"),
				ConversationStates.INFORMATION_2,
				"Zrób to!",
				null);
	}

	private void playerReturnsWithoutLetter(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", QUEST_SLOT)));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Proszę spytaj Majora Sakhsa o moją żonę Verę.",
				null);
	}

	private void playerVisitsMayorSakhs(final SpeakerNPC npc) {
		final ChatAction action = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item item = SingletonRepository.getEntityManager().getItem("karteczka");
				item.setInfoString(QUEST_SLOT);
				item.setDescription(LETTER_DESCRIPTION);
				item.setBoundTo(player.getName());
				player.equipOrPutOnGround(item);
			}
		};

		final String mayor_response = "Zbierała arandulę dla Ilisy (były przyjaciółkami)," +
				" zapuściła się w czeluście katakumb. 3 miesiące później" +
				" młody wojownik ujrzał ją, była... wampirzycą.";

		// Player has not received note
		npc.add(ConversationStates.ATTENDING, Arrays.asList("Vera","Vere"),
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new NotCondition(new PlayerHasInfostringItemWithHimCondition("karteczka", QUEST_SLOT))),
			ConversationStates.ATTENDING,
			"Co? Skąd ją znasz? To smutna historia." +
			mayor_response + " Co za smutna historia. Trzymałem ten list dla jej męża. " +
			" Myślę, że spotkasz go gdzieś w Kalavan." ,
			action);

		// Player is already carrying note
		npc.add(ConversationStates.ATTENDING, "Vera",
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "find_vera"),
				new PlayerHasInfostringItemWithHimCondition("karteczka", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			mayor_response + " Proszę, zanieś ten list do jej męża." +
			" Myślę, że spotkasz go gdzieś w Kalavan.",
			null);
	}

	private void playerReturnsAfterGivingWhenFinished(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES));

		final ChatAction action = new SetQuestAction(QUEST_SLOT,"find_vera");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()), condition),
				ConversationStates.INFORMATION_1,
				"Skończyłem spodnie, ale nie mogę ci zaufać. Zanim oddam tobie te" +
				" spodnie to dostarczysz mi wiadomość od mojej ukochanej. Zapytaj " +
				" Mayora Sakhsa o Werę. Zrobisz to dla mnie?",
				null);

		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.YES_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Och, dziękuję. Będę czekać.",
				action);

		npc.add(ConversationStates.INFORMATION_1, ConversationPhrases.NO_MESSAGES,
				condition,
				ConversationStates.IDLE,
				"Pa! Dowidzenia!",
				null);
	}

	private void playerReturnsAfterGivingTooEarly(final SpeakerNPC npc) {
		final ChatCondition condition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestStateStartsWithCondition(QUEST_SLOT, "making;"),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))
			);
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				condition,
				ConversationStates.IDLE,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES,  "Czy myślisz, że mogę pracować tak szybko? Odejdź. " +
						"Wróć za"));
	}

	private void bringItemsPhase(final SpeakerNPC npc) {
		//condition for quest being active and in item collection phase
		ChatCondition itemPhaseCondition = getConditionForBeingInCollectionPhase();

		//player returns during item collection phase
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						itemPhaseCondition),
				ConversationStates.QUESTION_1,
				"Witaj. Czy posiadasz #przedmioty potrzebne do wykonania spodni nabijanych klejnotami?",
				null);

		//player asks for items
		npc.add(ConversationStates.QUESTION_1, Arrays.asList("items", "item", "przedmioty", "przedmiot"),
				itemPhaseCondition,
				ConversationStates.QUESTION_1,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Proszę wróć, gdy będziesz miał wszystko co jest potrzebne do zrobienia spodni nabijanych klejnotami. Potrzebuję [items]."));

		//player says no
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
				itemPhaseCondition,
				ConversationStates.IDLE,
				"Co za leniwe dziecko.",
				null);

		//player says yes
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES,
				itemPhaseCondition,
				ConversationStates.QUESTION_1,
				"Dobrze! Więc co masz dla mnie?",
				null);

		//add transition for each item
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_1, item.getKey(), null,
					ConversationStates.QUESTION_1, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"Dobrze masz coś jeszcze?",
							"To już od ciebie dostałem!",
							new MultipleActions(
									new SetQuestAction(QUEST_SLOT,"legs"),
									new SayTextAction("Ale jestem roztrzepany. Bezgraniczna miłość do mej żony Very kiedyś mnie zgubi. Potrzebuję jakieś spodnie aby te klejnoty do czegoś przymocować. " +
											"Proszę zdobądź spodnie cieni. Dowidzenia.")), ConversationStates.IDLE
							));
		}
	}


//									new SetQuestAction(QUEST_SLOT,"making;"), new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	/**
	 * Creates a condition for quest being active and in item collection phase
	 * @return the condition
	 */
	private AndCondition getConditionForBeingInCollectionPhase() {
		return new AndCondition(new QuestActiveCondition(QUEST_SLOT),
			new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"making;")),
			new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"decorating;")),
			new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"find_vera")),
			new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"kill_scientist")),
			new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"legs")));
	}

	private void playerReturnsAfterRequestForLegs(final SpeakerNPC npc) {
	//player returns without legs
	final AndCondition nolegscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestInStateCondition(QUEST_SLOT, "legs"),
			new NotCondition(new PlayerHasItemWithHimCondition("spodnie cieni")));

	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			nolegscondition,
			ConversationStates.IDLE,
			"Witaj ponownie. Proszę wróć gdy zdobędziesz spodnie cieni, jako podstawę do wykonania spodni nabijanych klejnotami dla mojej żony Very.",
			null);

	//player returns with legs
	final AndCondition legscondition = new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestInStateCondition(QUEST_SLOT, "legs"),
			new PlayerHasItemWithHimCondition("spodnie cieni"));

	final ChatAction action = new MultipleActions(
	new SetQuestAction(QUEST_SLOT,"making;"),
	new SetQuestToTimeStampAction(QUEST_SLOT, 1),
	new DropItemAction("spodnie cieni"));
	npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			legscondition,
			ConversationStates.IDLE,
			"Masz spodnie cieni! Wspaniale! Niezwłocznie przystąpię do pracy. Zrobię je w bardzo krótkim czasie dzięki nowej technologi! " +
			"Proszę wróci za 20 minut.",
			action);
	}

	private void startOfQuest(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Odejdź!",null);

		//offer the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Hm.... Wyglądasz jak byś chciał mi pomóc?",null);

		//accept the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"Moja żona mieszka w Semos. Kocha klejnoty. Czy możesz mi dostarczyć kilka cennych #klejnotów. Potrzebuję ich do zrobienia " +
				"pary drogocennych #spodni." ,
				null);

		// #gems
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("gem", "gems", "klejnotów", "klejnoty"),
				null,
				ConversationStates.QUEST_STARTED,
				"Moja żona mieszka w Semos. Kocha klejnoty. Czy możesz mi dostarczyć kilka cennych #klejnotów. Potrzebuję ich do zrobienia " +
				"pary drogocennych  #spodni." ,
				null);

		// #legs
		npc.add(ConversationStates.QUEST_STARTED,
				Arrays.asList("leg", "legs", "spodni", "spodnie"),
				null,
				ConversationStates.QUEST_STARTED,
				"Do spodni wysadzanych klejnotami potrzebny jest szmaragd, obsydian, szafir, 2 rubiny, 20 sztabek złota i jedna sztabka mithrilu." +
				" Możesz zrobić to dla mej żony i dostarczyć mi wszystko co potrzebuję?" ,
				null);

		//yes, no after start of quest
		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Będę czekać cudzoziemcze." ,
				new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS));

		npc.add(ConversationStates.QUEST_STARTED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.QUEST_STARTED,
				"Odejdź, nim cię zabiję!" ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		//reject the quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Jeżeli zmienisz zdanie to możemy porozmawiać ponownie." ,
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void playerReturnsAfterCompletingQuest(final SpeakerNPC npc) {
		// after finishing the quest, just tell them to go away, and mean it.
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.IDLE,
				"Odejdź!",null);
	}

	// The items and surviving in the basement mean we shouldn't direct them till level 100 or so
	@Override
	public int getMinLevel() {
		return 100;
	}

	@Override
	public String getRegion() {
		return Region.KALAVAN;
	}

	@Override
	public String getNPCName() {
		return "Vasi Elos";
	}
}
