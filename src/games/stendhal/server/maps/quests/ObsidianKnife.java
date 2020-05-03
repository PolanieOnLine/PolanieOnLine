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

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
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
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * QUEST: The Obsidian Knife.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Alrak, a dwarf who abandoned the mountain dwarves to live in Kobold City</li>
 * <li>Ceryl, the librarian in Semos</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Alrak is hungry and asks for 100 pieces of ham, cheese or meat</li>
 * <li>Then, Alrak is bored and asks for a book</li>
 * <li>Get the book from Ceryl, and remember the name of who it is for</li>
 * <li>Bring the book to Alrak - he reads it for 3 days</li>
 * <li>After 3 days Alrak has learned how to make a knife from obsidian</li>
 * <li>Provided you have high enough level, you can continue</li>
 * <li>Get obsidian for the blade and a fish for the fish bone handle</li>
 * <li>Alrak makes the knife for you</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>Obsidian Knife</li>
 * <li>11500 XP</li>
 * <li> lots of karma (total 85 + (5 | -5))
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ObsidianKnife extends AbstractQuest {
	
	// Please note, if you want to code a quest where you're asked to collect a number of some randomly picked item, like alrak asks you to initially, 
	// please use StartRecordingRandomItemCollectionAction, SayRequiredItemAction, PlayerHasRecordedItemWithHimCondition and DropRecordedItemAction
    // This quest was written before they were available and you should not use it as a template.
	
	private static final int MINUTES_IN_DAYS = 24 * 60;

	private static final int REQUIRED_FOOD = 100;
	
	// Required level to move from the finished reading stage to 
	// the offering knife stage
	private static final int REQUIRED_LEVEL = 50;
	
	private static final List<String> FOOD_LIST = Arrays.asList("szynka", "mięso", "ser");

	private static final int REQUIRED_DAYS = 3;

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "obsidian_knife";

	private static final String FISH = "dorsz";

	private static final String NAME = "Alrak";

	private static Logger logger = Logger.getLogger(ObsidianKnife.class);
	
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
		res.add("Spotkałem Alrak w kuźni w Wofol.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomóc Alrakowi.");
			return res;
		} 
		res.add("Alrak poprosił mnie o przyniesienie trochę jedzenia.");
		if (player.isQuestInState(QUEST_SLOT, "szynka", "mięso", "ser")) {
			res.add("Muszę przynieść " + Grammar.quantityplnoun(REQUIRED_FOOD, questState) + ", i powiedzieć " + questState + " gdy wrócę.");
			return res;
		}
		res.add("Przyniosłem Alrakowi jedzenie.");
		if (questState.equals("food_brought")) {
			return res;
		}
		res.add("Muszę poprosić w bibliotece o książkę o kamieniach dla Alrak.");
		if (questState.equals("seeking_book")) {
			return res;
		}
		res.add("Mam książkę, którą Alrak chciał.");
		if (questState.equals("got_book")) {
			return res;
		}
		res.add("Alrak czyta książkę, którą mu przyniosłem.");
		if (questState.startsWith("reading")) {
			return res;
		}
        res.add("Alrak powiedzial, ze ksiazka nauczy go jak wykonac saks, brzmialo to calkiem ciekawie.");
		if (questState.equals("book_read")) {
			return res;
		}
        res.add("Alrak powiedział, że jeśli zabiję czarnego smoka i przyniosę mu dorsza oraz obsydian to zrobi mi nóż.");
		if (questState.equals("knife_offered")
		&& !player.hasKilled("czarny smok")) {
			return res;
		}
		res.add("Zabiłem czarnego smoka.");
		if (questState.equals("knife_offered")
				&& player.hasKilled("czarny smok")) {
			return res;
		}
		res.add("Przyniosłem dorsza i obsydian.");
		if (questState.equals("knife_offered")
				&& player.isEquipped("obsydian")
				&& player.isEquipped(FISH))  {
			return res;
		}
		res.add("zaniosłem dorsza i obsydian do Alrak. Teraz wyrabia mój nóż.");
		if (questState.startsWith("forging")) {
			return res;
		}
		res.add("Odebrałem obsydianowego saksa. Jestem wniebowzięty!");
		if (questState.equals("done")) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}
	
	private void prepareQuestOfferingStep() {
		final SpeakerNPC npc = npcs.get("Alrak");

		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_ITEM_QUESTION, 
				"Wiesz, że tutaj trudno jest dostać jedzenie. Nie mam żadnych #zapasów na następny rok.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Jestem znowu zajęty pracą! Wyrabiam rzeczy dla Wrvila. Dziękuję za zainteresowanie.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestInStateCondition(QUEST_SLOT, "food_brought"),
				ConversationStates.QUEST_ITEM_BROUGHT, 
				"Już tak bardzo nie martwie się o jedzenie. Teraz się nudzę. Jest pewna #książka, którą lubię czytać.",
				null);
		
		// any other state than above
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestActiveCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "food_brought")),
				ConversationStates.ATTENDING, 
				"Już się Ciebie pytałem czy czegoś byś dla mnie nie zrobił.",
				null);
						

		/*
		 * Player agrees to collect the food asked for. his quest slot gets set
		 * with it so that later when he returns and says the food name, Alrak
		 * can check if that was the food type he was asked to bring.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String food = player.getQuest(QUEST_SLOT);
					npc.say("Dziękuję! Mam nadzieję, że zbieranie nie zajmie Ci zbyt wiele czasu. Nie zapomnij powiedzieć '"
						+ food + "' kiedy wrócisz.");
					// player.setQuest(QUEST_SLOT, food);
					player.addKarma(5.0);
					// set food to null?
				}
			});

		// Player says no. they might get asked to bring a different food next
		// time but they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Nie wiem jak przetrwam następny rok. Dowidzenia okrutna duszo.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// Player asks what supplies he needs, and a random choice of what he
		// wants is made.
		npc.add(ConversationStates.QUEST_ITEM_QUESTION, 
				Arrays.asList("supplies", "zapasów"), 
				null,
				ConversationStates.QUEST_OFFERED, 
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String food = Rand.rand(FOOD_LIST);
						player.setQuest(QUEST_SLOT, food);
						npc.say("Jeżeli mógłbyś zebrać " + REQUIRED_FOOD
								+ " kawałków " + food
								+ " to byłbym wdzięczny. Pomożesz mi?");
					}
				});
	}

	private void bringFoodStep() {
		final SpeakerNPC npc = npcs.get("Alrak");

		/** If player has quest and has brought the food, and says so, take it */
		for(final String itemName : FOOD_LIST) {
			final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						if (player.drop(itemName, REQUIRED_FOOD)) {
							npc.say("Wspaniale! Przyniosłeś " + itemName + "!");
						}
					} });
		reward.add(new IncreaseXPAction(1000));
		reward.add(new IncreaseKarmaAction(35.0));
		reward.add(new SetQuestAction(QUEST_SLOT, "food_brought"));

			npc.add(ConversationStates.ATTENDING, 
				itemName,
				new ChatCondition() {
					@Override
					public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
						return player.hasQuest(QUEST_SLOT)
								&& player.getQuest(QUEST_SLOT).equals(itemName)
								&& player.isEquipped(itemName, REQUIRED_FOOD);
					}
				}, 
				ConversationStates.ATTENDING, 
				null,
				new MultipleActions(reward));
		}
	}

	private void requestBookStep() {
		final SpeakerNPC npc = npcs.get("Alrak");

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				Arrays.asList("book", "książka"),
				null,
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Jest ona o kamieniach i minerałach. Założę się byłbyś zainteresowany ... sądzisz, ze mógłbyś ją zdobyć?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Szkoda. Chciałbym się więcej nauczyć o cennych kamieniach. Cześć, dowidzenia.",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				"Dziękuję. Zapytaj w bibliotece o 'książkę o kamieniach'.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "seeking_book", 10.0));
	}

	private void getBookStep() {
		final SpeakerNPC npc = npcs.get("Ceryl");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("książkę o kamieniach", "książka o kamieniach"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "seeking_book"), new QuestCompletedCondition("ceryl_book")),
				ConversationStates.ATTENDING,
				"Obecnie #'książka o kamieniach' jest dość popularna ...",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("książkę o kamieniach", "książka o kamieniach"),
				new QuestInStateCondition(QUEST_SLOT, "seeking_book"),
				ConversationStates.QUESTION_1,
				"Masz szczęście! Ognir przyniósł ją w tamtym tygodniu. Dla kogo ma być?",
				null);

		npc.add(ConversationStates.QUESTION_1, 
				NAME, 
				null,
				ConversationStates.ATTENDING, 
				"Ach górski ork! On uwielbia książki o kamieniach.",
				new MultipleActions(new EquipItemAction("księga lazurowa", 1, true), 
				new SetQuestAction(QUEST_SLOT, "got_book")));

		// allow to say goodbye while Ceryl is listening for the dwarf's name
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.IDLE,
				"Dowidzenia.", null);

		// player says something which isn't the dwarf's name.
		npc.add(ConversationStates.QUESTION_1, 
				"",
				new NotCondition(new TriggerInListCondition(NAME.toLowerCase())),
				ConversationStates.QUESTION_1,
				"Hm, lepiej sprawdź dobrze dla kogo ma być.",
				null);
	}

	private void bringBookStep() {
		final SpeakerNPC npc = npcs.get("Alrak");
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "got_book"), 
						new PlayerHasItemWithHimCondition("księga lazurowa")),
				ConversationStates.IDLE, 
				"Wspaniale! Sądzę, że będę ją czytał przez jakiś czas. Dowidzenia!",
				new MultipleActions(
						new DropItemAction("księga lazurowa"),
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "reading;"),
						new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(new QuestInStateCondition(QUEST_SLOT,"seeking_book"), new QuestInStateCondition(QUEST_SLOT, "got_book")), 
						new NotCondition(new PlayerHasItemWithHimCondition("księga lazurowa"))),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Mam nadzieję, że nie zapomniałeś o książce. Potrzebuję jej.",
				null);
	}

	private void offerKnifeStep() {
		final SpeakerNPC npc = npcs.get("Alrak");
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "reading;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS))),
				ConversationStates.IDLE, 
				null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS, "Nie skończyłem czytać książki. Może skończę ją za "));
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "reading;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_DAYS * MINUTES_IN_DAYS)),
				ConversationStates.QUEST_2_OFFERED, 
				"Przeczytałem! Była naprawdę interesująca. Nauczyłem się jak robić specjalny #nóż z kamieniem #obsydianu.", 
				new SetQuestAction(QUEST_SLOT, "book_read"));
						

		npc.add(ConversationStates.QUEST_2_OFFERED,
				"obsydianu",
				new LevelGreaterThanCondition(REQUIRED_LEVEL),
				ConversationStates.QUEST_2_OFFERED,
				"Książka opowiada o czarnym kamieniu zwanym obsydianem, który może zostać użyty do zrobienia ostro tnącego ostrza. Fascynujące! Jeżeli pokonasz czarnego smoka i znajdziesz u niego obsydian to zrobię dla Ciebie #saks.",
				new SetQuestAction(QUEST_SLOT, "knife_offered"));

		npc.add(ConversationStates.QUEST_2_OFFERED,
				Arrays.asList("knife", "saks","nóż"),
				new LevelGreaterThanCondition(REQUIRED_LEVEL),
				ConversationStates.QUEST_2_OFFERED,
				"Zrobię obsydianowy saks jeżeli zdobędziesz kamień, z którego wyrabia się ostrze. Przynieś mi też "
						+ FISH
						+ ", z jego ości zrobię rękojeść.",
				new SetQuestAction(QUEST_SLOT, "knife_offered"));
		
		npc.add(ConversationStates.QUEST_2_OFFERED,
				Arrays.asList("obsydian", "obsydianowy saks", "nóż"),
				new NotCondition(new LevelGreaterThanCondition(REQUIRED_LEVEL)),
				ConversationStates.ATTENDING,
				"Cóż nie sądzę, abyś był gotowy na tak groźną broń. Wrócisz, gdy osiągniesz " + Integer.toString(REQUIRED_LEVEL) + " poziom?",
				null);
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "book_read")),
				ConversationStates.QUEST_2_OFFERED,
				"Cześć! Może powinieneś przyjść ponownie i zapytać się o #nóż ... ",
				null);
		        
		// player says hi to NPC when equipped with the fish and the gem and
		// he's killed a czarny smok
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"), 
						new KilledCondition("czarny smok"),
						new PlayerHasItemWithHimCondition("obsydian"),
						new PlayerHasItemWithHimCondition(FISH)), 
				ConversationStates.IDLE, 
				"Znalazłeś kamień na ostrze oraz rybie ości na rękojeść! Zacznę pracę od razu. Wróć za "
				+ REQUIRED_MINUTES + " minutę" + ".",
				new MultipleActions(
				new DropItemAction("obsydian"),
				new DropItemAction(FISH),
				new SetQuestAction(QUEST_SLOT, "forging;"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		// player says hi to NPC when equipped with the fish and the gem and
		// he's not killed a czarny smok
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"), 
						new NotCondition(new KilledCondition("czarny smok")),
						new PlayerHasItemWithHimCondition("obsydian"),
						new PlayerHasItemWithHimCondition(FISH)),
				ConversationStates.ATTENDING,
				"Chyba mnie nie zrozumiałeś? Powiedziałem, abyś zgładził czarnego smoka dla obsydiana, a nie kupił! Skąd mam wiedzieć, że nie jest to fałszywy kamień? Nie robię specjalnego noża dla kogoś kto boi się stanąć twarzą w twarz ze smokiem.",
				null);

		// player says hi to NPC when not equipped with the fish and the gem
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "knife_offered"), 
						new NotCondition(
								new AndCondition(
										new PlayerHasItemWithHimCondition("obsydian"),
										new PlayerHasItemWithHimCondition(FISH)))),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Nie zapomnij, że zaproponowałem tobie zrobienie obsydianowego sakasa o ile przyniesiesz mi "
					+ FISH
					+ "a i obsydian z zabitego przez Ciebie czarnego smoka. W międzyczasie jeżeli potrzebujesz #pomocy to powiedz słowo.",
				null);

		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.IDLE, 
				null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES, "Jeszcze nie skończyłem noża. Proszę wróć za "));
		
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseXPAction(10000));
		reward.add(new IncreaseKarmaAction(40));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new EquipItemAction("obsydianowy saks", 1, true));
		
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
						new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.IDLE, 
				"Nóż jest gotowy! Wiesz, że była to przyjemność. Chyba znowu zacznę wyrabiać go masowo. Dziękuję!",
				new MultipleActions(reward));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Obsydianowy Saks",
				"Alrak potrzebuje pomocy, ale może on mi też pomoże.",
				false);
		prepareQuestOfferingStep();
		bringFoodStep();
		requestBookStep();
		getBookStep();
		bringBookStep();
		offerKnifeStep();
	}

	@Override
	public String getName() {
		return "ObsidianKnife";
	}
	
	@Override
	public int getMinLevel() {
		return REQUIRED_LEVEL;
	}

	@Override
	public String getNPCName() {
		return "Alrak";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_MINES;
	}
	
}
