package games.stendhal.server.maps.quests;
 
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipRandomAmountOfItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Fruits for Coralia
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Coralia (Bar-maid of Ado tavern)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Coralia introduces herself and asks for a variety of fresh fruits for her hat.</li>
 * <li>You collect the items.</li>
 * <li>Coralia sees your items, asks for them then thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>XP: 300</li>
 * <li><1-5> Crepes Suzettes</li>
 * <li><2-8> Minor Potions</li>
 * <li>Karma: 5</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>After 1 week, fit with the withering of the fruits</li>
 * </ul>
 * 
 * @author pinchanzee
 */
public class FruitsForCoralia extends AbstractQuest {

	
	
	/**
	 * NOTE: Reward has not been set, nor has the XP.
	 * left them default here, but in the JUnit test
	 * called reward item "REWARD" temporarily
	 */
	
	public static final String QUEST_SLOT = "fruits_coralia";

	/** 
	 * The delay between repeating quests.
	 * 1 week
	 */
	private static final int REQUIRED_MINUTES = 1440;

	/**
	 * Required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "jabłko=4;banan=5;wisienka=9;winogrona=2;gruszka=4;arbuz=1;granat=2";
 
	@Override
	public void addToWorld() {
		fillQuestInfo("Owoce dla Coralii",
				"Coralia kelnerka w Tawernie Ados poszukuje świeżych owoców do swojego egzotycznego kapelusza.",
				true);
		prepareQuestStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "FruitsForCoralia";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;"),
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Coralia poprosiła mnie o świeże owoce do swojego kapelusza.");
		final String questState = player.getQuest(QUEST_SLOT);
		
		if ("rejected".equals(questState)) {
			// quest rejected
			res.add("Zdecydowałem, że nie pomogę Coralii w poszukiwaniach owoców ponieważ mam lepsze rzeczy do zrobienia.");
		} else if (!player.isQuestCompleted(QUEST_SLOT)) {
			// not yet finished
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Coralii " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else if (isRepeatable(player)) {
			// may be repeated now
			res.add("Mnięło trochę czasu odkąd przyniosłem Coralii świeże owoce do jej kapelusza. Ciekaw jestem czy już zeschły?");
		} else {
			// not (currently) repeatable
			res.add("Przyniosłem Coralii owoce, których potrzebowała do swojego kapelusza, a ona zastąpiła nimi stare, które utraciły blask.");
		}
		return res;
	}

	public void prepareQuestStep() {
		SpeakerNPC npc = npcs.get("Coralia");
		
		// various quest introductions
		
		// offer quest first time
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("fruit", "owoc")),
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"Czy byłbyś na tyle miły i przyniósłbyś dla mnie trochę świeżych owoców do mojego kapelusza? Byłabym wdzięczna!",
			null);
			
		// ask for quest again after rejected
		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("hat", "kapelusz")),
			new QuestInStateCondition(QUEST_SLOT, "rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Czy chcesz teraz poszukać świeżych owoców do mojego kapelusza?",
			null);
			
		// repeat quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("hat", "kapelusz")),
			new AndCondition(
				new QuestCompletedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
			ConversationStates.QUEST_OFFERED,
			"Przykro mi, ale owoce, które mi przyniosłeś do mojego kapelusza  nie są zbyt świeże. " +
			"Czy byłbyś tak miły i znalazł ich trochę więcej?",
			null);

		// quest inactive
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("hat", "kapelusz")),
			new AndCondition(
				new QuestCompletedCondition(QUEST_SLOT),
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
			ConversationStates.ATTENDING,
			"Czy mój kapelusz nie wygląda świeżo? Teraz nie potrzebuję świeżych owoców, ale dziękuję za troskę!",
			null);

		// end of quest introductions
		
		
		// introduction chat
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("hat", "kapelusz"),
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new QuestNotInStateCondition(QUEST_SLOT, "rejected")),
			ConversationStates.ATTENDING,
			"Co za szkoda, że wszystkie uschły. Potrzebuję trochę świeżych #owoców...",
			null);
		
		// accept quest response
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.QUESTION_1,
			null,
			new MultipleActions(
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "To wspaniale! Chciałabym te świeże owoce: [items].")));
		
		// reject quest response
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ten egzotyczny kapelusz już się nie trzyma. Wiesz...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
		
		// meet again during quest
		npc.add(ConversationStates.IDLE, 
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
				new QuestActiveCondition(QUEST_SLOT),
				new GreetingMatchesNameCondition(npc.getName())),
			ConversationStates.ATTENDING,
			"Witaj ponownie. Jeżeli przyniosłeś świeże owoce na mój kapelusz to z radością je wezmę!",
			null);

		

		// specific fruit info
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("apple", "jabłko"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Lśniące, błszczące jabłka! Te, które mam pochodzą gdzieś ze wschodniego Semos.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("banana", "banan"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Jest jedna egzotyczna wyspa z bananami.. Kieruj się prosto na zachód. Powiedzmy, że banany nie są zbyt mięsiste dla tych na wschodzie.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("cherry", "wisienka"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Jest starsza pani w Fado, która sprzedaje najpiękniejsze wisienki.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("grapes", "winogrona"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Na północ od Semos znajduje się piękna mała świątynia pokryta winoroślami!  Słyszałam także o starym domku w górach Or'ril.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("pear", "gruszka"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Sądzę, że widziałam pare gruszy w północnych górach nie daleko pięknego wodospadu.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("watermelon", "arbuz"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Jeden ogromny arbuz z ogrodów Kalavan mógłby się pięknie prezentować na moim kapeluszu.",
			null);
		
		npc.add(ConversationStates.QUESTION_1,
			Arrays.asList("pomegranate", "granat"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			"Nigdy nie widziałam dziko rosnących drzew granatów, ale słyszałam o człowieku żyjącym na południe od wielkiej rzeki uprawiającym w ogrodzie te drzewa.",
			null);
	}


	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Coralia");
		
		// ask for required items
		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, Arrays.asList("hat", "kapelusz")),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null,
			new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż chciałabym [items]. Przyniosłeś któryś?"));

		// player says he didn't bring any items
		npc.add(ConversationStates.QUESTION_2, 
			ConversationPhrases.NO_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_1,
			null,
			new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Oh co za szkoda, powiedz mi, gdy znajdziesz kilka. Wciąż potrzebuję [items]."));
		
		// player says he has a required item with him
		npc.add(ConversationStates.QUESTION_2,
			ConversationPhrases.YES_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			"Cudownie jakie jeszcze świeże przysmaki mi przyniosłeś?",
			null);
		
		// set up next step
		ChatAction completeAction = new  MultipleActions(
			new SetQuestAction(QUEST_SLOT, "done"),
			new SayTextAction("Mój kapelusz jeszcze nigdy nie wyglądał tak wybornie! Bardzo ci dziękuję! Przyjmij tą nagrodę."),
			new IncreaseXPAction(300),
			new IncreaseKarmaAction(5),
			new EquipRandomAmountOfItemAction("naleśniki z polewą czekoladową", 1, 5),
			new EquipRandomAmountOfItemAction("mały eliksir", 2, 8),
			new SetQuestToTimeStampAction(QUEST_SLOT, 1)
		);
		
		// add triggers for the item names
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_2,
				item.getKey(),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2,
				null,
				new CollectRequestedItemsAction(item.getKey(),
					QUEST_SLOT,
					"Wspaniale! Czy przyniosłeś coś jeszcze?", "Już mam ich wystarczająco dużo.",
					completeAction,
					ConversationStates.ATTENDING));
		}
	}

	@Override
	public String getNPCName() {
		return "Coralia";
	}
}