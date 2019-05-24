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

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: Mrs Yeti Needs Help
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Mrs Yeti, who lives in a snowy dungeon</li>
 * <li>Salva Mattori, Healer at magic city</li>
 * <li>Hackim Easso, Blacksmith assistant semos</li>
 * </ul>
 *
 * STEPS:
 * Mrs. Yeti lifes in a cave somewhere in semos mountain. She is mournful, because Mr. Yeti turn away from her. Thats why she ask the player for help. She like to have a special potion and some other stuff as a present for her husband.
 *
 * There is only one witch who, who can make the special potion. Mrs. Yeti tell the player where she lives. The player go for the witch. Once he found her, she tell the player, that she will help, but need some ingriedents.
 * 
 * When the player is bringing in the collected stuff, she has to tell him, that her magic knife is damaged and she need a new one and send the player to a blacksmith. He has to craft a new magic knife for the witch.
 * 
 * The blacksmith is willing to help. But need some stuff too, to craft the magic knife. He sends the player to collect it. The player brings in the needed items and the blacksmith could start make the knife, but he is too hungry to start it right now. Player has to bring him some food and he starts crafting the knife. But the player has to wait a bit until he is ready with it.
 * 
 * After bring the knife to the witch, he tell the player that she forgot an important item. The player has to get it and bring it to here. After a while the special potion is ready. And the player can bring it to Mrs. Yeti.
 * 
 * Mrs. Yeti is very happy about the special potion. But she needs some other things to make her husband happy. The player has to collect a baby dragon for her. After player bring the baby dragon to her, she is happy as never befor.
 *
 * REWARD:
 * <ul>
 * <li> 1,000 XP </li>
 * <li> some karma (10 + (10 | -10)) </li>
 * <li> Can buy <item>roach</item> from Mrs. Yeti </li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */

 public class HelpMrsYeti extends AbstractQuest {

	private static final String QUEST_SLOT = "mrsyeti";
	private static final int DELAY_IN_MINUTES = 60*24;
 
	private static Logger logger = Logger.getLogger(HelpMrsYeti.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void startQuest() {
		final SpeakerNPC npc = npcs.get("Mrs. Yeti");	

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Jam jest w rozpaczy wielkiej, gdyż mąż mój Pan Yeti opuścił me progi. Trza mi mocnego eliksiru by na powrót szczęśliwym go uczynić i jakiś podarek by go obłaskawić. Czy mi pomożesz?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję ci za pomoc! Nareszcie ja i Pan Yeti w pełni szczęścia pozostaniemy.",
				null);


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Dziękuję ci za chęci! Musisz prędko udać się do Salva Mattori w magicznym mieście po #eliksir miłości.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Och, jakiś ty bezduszny...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void makePotion() {
	// player needs to bring some items to make the potion:
	// a 'magic' knife from a blacksmith
	// 3 lilia flowers
	// sclaria
	// wine
	// black pearl
	final SpeakerNPC npc = npcs.get("Salva Mattori");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("eliksir", "potion"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, "Pomogę ci i wykonam dla ciebie ten eliksir, gdyż Pani Yeti jest moją starą znajomą. Ale ostrze "
				+ " mojego magicznego mieczyka znów się ukruszyło. Potrzebuję nowego. Swój dostałam od Hackima Easso z Semos, czy pójdziesz do niego i "
				+ " poprosisz o nowy? Powiedz tylko moje imię: #Salva",
				new SetQuestAction(QUEST_SLOT, "hackim"));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("salva","mieczyk","nóż"),
			new NotCondition(new QuestInStateCondition(QUEST_SLOT, "mieczyk")),
			ConversationStates.ATTENDING,
			"Zanim ci pomogę musisz się udać do Hackima Easso i poprosić go o magiczny mieczyk dla mnie.",
			null);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva","mieczyk","eliksir", "eliksir","nóż"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "mieczyk"),
				new PlayerHasItemWithHimCondition("mieczyk")),
				ConversationStates.ATTENDING, "Bardzo dobrze! Teraz potrzebuję kilku rzeczy, aby przygotować ci miłosny #eliksir. Potrzebuję 3 kwiaty lilii, 1 kokudę, 1 kieliszek napoju z winogron i 1 czarną perłę. Przynieś mi wszystko za jednym razem, a przygotuję ci #eliksir.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "eliksir"), new DropItemAction("mieczyk")));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva","mieczyk","eliksir","potion"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "mieczyk"),
				new NotCondition(new PlayerHasItemWithHimCondition("mieczyk"))),
				ConversationStates.ATTENDING, "Rozumiem, że byłe s już u Hackima? Gdzie masz magiczny mieczyk?",
				null);

		final List<ChatAction> potionactions = new LinkedList<ChatAction>();
		potionactions.add(new DropItemAction("lilia",3));
		potionactions.add(new DropItemAction("kokuda"));
		potionactions.add(new DropItemAction("napój z winogron"));
		potionactions.add(new DropItemAction("czarna perła"));
		potionactions.add(new EquipItemAction("eliksir miłości"));
		potionactions.add(new IncreaseXPAction(100));
		potionactions.add(new SetQuestAction(QUEST_SLOT, "gotpotion"));

		// don't make player wait for potion - could add this in later if wanted
		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva","eliksir", "eliksir"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "eliksir"),
								 new PlayerHasItemWithHimCondition("lilia",3),
								 new PlayerHasItemWithHimCondition("kokuda"),
								 new PlayerHasItemWithHimCondition("napój z winogron"),
								 new PlayerHasItemWithHimCondition("czarna perła")),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłam. Odsuń się nieco, a ja wyszepczę tajemne zaklęcie... O! Proszę! Oto gotowy eliksir miłosny. Życz ode mnie Pani Yeti wiele szczęścia!",
				new MultipleActions(potionactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva","potion","eliksir"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "eliksir"),
								 new NotCondition(
												  new AndCondition(new PlayerHasItemWithHimCondition("lilia",3),
																   new PlayerHasItemWithHimCondition("kokuda"),
																   new PlayerHasItemWithHimCondition("napój z winogron"),
																   new PlayerHasItemWithHimCondition("czarna perła")))),
				ConversationStates.ATTENDING, "Potrzebuję 3 kwiaty lilii, 1 kokudę, 1 kieliszek wina i 1 czarną perłę, aby przygotować miłosny eliksir. Proszę przynieś mi wszystko naraz. Dziękuję!", null);


	}

	private void makeMagicKnife() {
		// although the player does end up just taking an ordinary knife to salva, this step must be completed 
		// (must be in quest state 'knife' when they take the knife)
	final SpeakerNPC npc = npcs.get("Hackim Easso");
		npc.add(ConversationStates.ATTENDING, "salva",
				new QuestInStateCondition(QUEST_SLOT, "hackim"),
				ConversationStates.ATTENDING, "Salva potrzebuje kolejny magiczny mieczyk? No cóż, oczywiście, że ci pomogę. Jestem jednak bardzo głodny. "
				+ "Muszę coś zjeść! Przynieś mi 5 #tart to ci pomogę!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "pies", 1.0));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva", "pies", "placek", "tart"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "pies"),
				new PlayerHasItemWithHimCondition("tarta",5)),
				ConversationStates.ATTENDING, "Pięknie ci dziękuję! Zdradzę ci mały sekret. Otóż tak na prawdę nie jestem jeszcze kowalem, "
				+ "tylko zwykłym czeladnikiem. Nie potrafię zrobić miecza! Sprzedałem Salvie zwykły mieczyk, ale taki jej był potrzebny! Więc zanieś jej "
				+ "zwykły mieczyk jaki można kupić od Xin Blanca w Tawernie w Semos. Ja jej powiem, że jest mojej roboty! A! I dziękuję za tartę!!!",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "mieczyk", 1.0), new DropItemAction("tarta",5)));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("salva", "pies", "placek", "tarta"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "pies"),
				new NotCondition(new PlayerHasItemWithHimCondition("tarta",5))),
				ConversationStates.ATTENDING, "Arlindo z Ados robi najlepszą tartę. Nie zapomnij przynieść mi pięciu sztuk, jestem bardzo głodny!",
				null);

	}

	private void bringPotion() {
	final SpeakerNPC npc = npcs.get("Mrs. Yeti");
		final String extraTrigger = "eliksir";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		final List<ChatAction> tookpotionactions = new LinkedList<ChatAction>();
		tookpotionactions.add(new DropItemAction("eliksir miłości"));
		tookpotionactions.add(new IncreaseKarmaAction(10.0));
		tookpotionactions.add(new IncreaseXPAction(1000));
		tookpotionactions.add(new SetQuestAction(QUEST_SLOT, "dragon"));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotpotion"),
				new PlayerHasItemWithHimCondition("eliksir miłości")),
				ConversationStates.ATTENDING, "Dziękuję! Wygląda, że jest mocny. Gotowa byłam zakochać się w tobie, gdy tylko wyczułam zapach eliksiru! Ale nie obawiaj się, zachowam go wyłącznie dla męża. Ale nie przyjmie go ode mnie jeżli go wcześniej nie obłaskawię. Wiem, że bardzo lubi małe #smoki. Gdybyś był tak miły i przyprowadził mi jednego.", 
				new MultipleActions(tookpotionactions));

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotpotion"), new NotCondition(new PlayerHasItemWithHimCondition("eliksir miłości"))),
			ConversationStates.ATTENDING,
			"Gdzie żeś zapodział mój eliksir miłosny?",
			null);
		
		npc.add(ConversationStates.ATTENDING,
				questTrigger, 
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, "start"), 
								new QuestInStateCondition(QUEST_SLOT, "pies"), 
								new QuestInStateCondition(QUEST_SLOT, "mieczyk")),
				ConversationStates.ATTENDING,
				"Czekam byś przyniósł mi eliksir miłosny. Poproś Salvę Mattori z magicznego miasta o miłosny #eliksir.",
				null);
	}

	private void bringDragon() {
	final SpeakerNPC npc = npcs.get("Mrs. Yeti");

		final String extraTrigger = "smoki";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		// easy to check if they have a pet or sheep at all
		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"), 
							 new NotCondition(new PlayerHasPetOrSheepCondition())),
			ConversationStates.ATTENDING,
			"Możesz zdobyć małego smoka tylko posiadając mityczne jajo. Możesz je zdobyć od Morgrina ze szkoły magicznej. "
			+ "Terry z jaskiń pod Semos wyhoduje ci z niego smoka.",
			null);

		// if they have any pet or sheep, then check if it's a baby dragon
		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"), 
							 new PlayerHasPetOrSheepCondition()),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence,
								final EventRaiser npc) {
					if(!player.hasPet()){
						npc.say("Jaką miĹłą owieczkę prowadzisz. Ja jednak potrzebuję małego smoka dla pana Yeti. Porozmawiaj z Morgrinem ze szkoły magicznej.");
						return;
					}
					Pet pet = player.getPet();
					String petType = pet.get("type");
					if("baby_dragon".equals(petType)) {
						player.removePet(pet);
						npc.say("Przyprowadziłeś małego smoka! Będzie z niego pyszny gulasz! Gulasz z małego smoka to moja specjalność, a pan Yeti uwielbia go! Uczyniłeś nas wielce szczęśliwymi! Wróć tu następnego dnia, a otrzymasz #nagrodę.");
						player.addKarma(5.0);
						player.addXP(500);
						pet.delayedDamage(pet.getHP(), "Mrs. Yeti");
						player.setQuest(QUEST_SLOT,"reward;"+System.currentTimeMillis());
						player.notifyWorldAboutChanges();
					} else {
						npc.say("Jakie miłe zwierzątko prowadzisz. Ja jednak potrzebuję małego smoka dla pana Yeti. Porozmawiaj z Morgrinem ze szkoły magicznej.");
					}
				}
			});

	}

	private void getReward() {

	final SpeakerNPC npc = npcs.get("Mrs. Yeti");

		final String extraTrigger = "nagroda";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"), 
							 // delay is in minutes, last parameter is argument of timestamp
							 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT,1,DELAY_IN_MINUTES,"Witaj! Szykuję mojemu mężowi gulasz z małego smoka, a nagrodą dla ciebie zajmę się dopiero za "));


		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"), 
							// delay is in minutes, last parameter is argument of timestamp
							new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
			ConversationStates.ATTENDING,
			"Dziękuję! Aby ci się odwdzięczyć chcę ci zaoferować możliwość kupna ryb. Powiedz #kupię #płotka, a sprzedam ci tanio. Mam ich mnóstwo, a ty pewnie z nich zrobisz użytek.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT,"done"), new IncreaseXPAction(1000)));

	}

	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pomoc Mrs Yeti",
				"Mrs Yeti jest nieszczęśliwa z miłością swojego życia, ponieważ jej mąż odwrócił się od niej. Teraz para jest w dużych kłopotach. Tylko specjalny napój miłosny może pomóc Mrs Yeti, aby odzyskać męża. Pomożesz jej?",
				true);
		startQuest();
		makePotion();
		makeMagicKnife();
		bringPotion();
		bringDragon();
		getReward();
	}

  @Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("Spotkałem panią Yeti w lodowych jaskiniach poniżej Semos Mountain.");
			res.add("Pani Yeti poprosiła mnie, aby przynieść od Salvy Mattori specjalny eliksir miłości dla jej męża.");
			if ("rejected".equals(questState)) {
				res.add("Nie chcę, mieszać się w historie miłosne ..");
				return res;
			} 
			if ("start".equals(questState)) {
				return res;
			} 
			res.add("Salva Mattori potrzebuje magiczny mieczyk od Hackim Easso, wtedy da mi napój.");
			if ("hackim".equals(questState)) {
				return res;
			} 
			res.add("Hackim jest głodny i chce 5 tart zanim mi pomoże.");
			if ("pies".equals(questState)) {
				return res;
			} 
			res.add("Hackim powiedział abym kupił zwykły mieczyk u Xin Blanca! Najwyraźniej nabirał Salvę przez te wszystkie lata. Ona wierzyła, że są magiczne...");
			if ("knife".equals(questState)) {
				return res;
			} 
			res.add("Eliksir miłości wymaga 3 kwiaty lili, 1 gałązka kokudy, 1 szklanka napoju z winogron i 1 czarną perłę.");
			if ("potion".equals(questState)) {
				return res;
			} 
			res.add("Muszę zanieść eliksir miłości do pani Yeti.");
			if ("gotpotion".equals(questState)) {
				return res;
			} 
			res.add("Pani Yeti potrzebuje czegoś, co udobrucha jej męża i poprosiła mnie, abym przyprowadził małego smoka.");
			if ("dragon".equals(questState)) {
				return res;
			} 
			res.add("Ojej! Ona zabiła tego smoka aby zrobić gulasz!");
			if (questState.startsWith("reward")) {
				if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
					res.add("Pani Yeti kazał mi wrócić za dzień, aby odebrać nagrodę to wydaje się tak odległe.");
				} else {
					res.add("Pani Yeti kazała mi wrócić za dzień, aby odebrać nagrodę, więc muszę czekać.");
				}
				return res;
			} 
			res.add("Pani Yeti jest bardzo zadowolona z wyniku mojej pomocy, a teraz sprzeda mi płotki bardzo tanio.");
			if (isCompleted(player)) {
				return res;
			}
			
			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Stan zadania to: " + questState);
			logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
			return debug;
	}
	
	@Override
	public String getName() {
		return "HelpMrsYeti";
	}

		@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "Mrs. Yeti";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_YETI_CAVE;
	}
}

