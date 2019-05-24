/* $Id$ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerCanEquipItemCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * QUEST: The Elf Princess
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Tywysoga, the Elf Princess in Nalwor Tower</li>
 * <li>Rose Leigh, the wandering flower seller.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>The princess asks you for a rare flower</li>
 * <li>Find the wandering flower seller</li>
 * <li>You are given the flower, provided you've already been asked to fetch it</li>
 * <li>Take flower back to princess</li>
 * <li>Princess gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>5000 XP</li>
 * <li>Some gold bars, random between 5,10,15,20,25,30.</li>
 * <li>Karma: 15</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Unlimited, provided you've activated the quest by asking the princess
 * for a task again</li>
 * </ul>
 */
public class ElfPrincess extends AbstractQuest {

    /* delay in minutes */
	private static final int DELAY = 5;
	private static final String QUEST_SLOT = "elf_princess";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Tywysoga");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
			ConversationStates.QUEST_OFFERED,
			"Znajdziesz wędrowną sprzedawczynię kwiatów Różę Kwiaciarkę i weźmiesz od niej orchideę mój ulubiony kwiatek?",
			null);

        // shouldn't happen: is a repeatable quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition("QUEST_SLOT"),
			ConversationStates.ATTENDING,
			"Dziękuję, ale mam pełno kwiatków.", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, 0, "flower_brought"),
			ConversationStates.QUEST_OFFERED,
			"Ostatnią orchideę, którą mi przyniosłeś była taka piękna. Przyniesiesz mi następną od Róży Kwiaciarki?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new OrCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"), new QuestInStateCondition(QUEST_SLOT, 0, "got_flower")),
			ConversationStates.ATTENDING,
			"Kochane są te kwiatki od Róży Kwiaciarki ...",
			null);

		// Player agrees to collect flower
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuję! Gdy będziesz miał to powiedz #kwiatek, a wtedy będę wiedziała, że go masz. W zamian dam Ci nagrodę.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "start"),
								new IncreaseKarmaAction(10.0)));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
			"Och nieważne. Żegnaj.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT, 0, "rejected"),
					new DecreaseKarmaAction(10.0)));
	}

	private void getFlowerStep() {
		final SpeakerNPC rose = npcs.get("Róża Kwiaciarka");

        // give the flower if it's at least 5 minutes since the flower was last given, and set the time slot again
		rose.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(rose.getName()),
							 new QuestInStateCondition(QUEST_SLOT, 0, "start"),
							 new PlayerCanEquipItemCondition("orchidea"),
                             new TimePassedCondition(QUEST_SLOT, 1, DELAY)),
			ConversationStates.IDLE, 
			"Witaj. Mój daleki wzrok powiedział mi, że potrzebujesz kwiatek dla pięknej dziewczyny. Oto on i dozobaczenia.",
			new MultipleActions(new EquipItemAction("orchidea", 1, true), 
                                new SetQuestAction(QUEST_SLOT, 0, "got_flower"), 
                                new SetQuestToTimeStampAction(QUEST_SLOT, 1)));

		// don't put the flower on the ground - if player has no space, tell them
		rose.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(rose.getName()),
								 new QuestInStateCondition(QUEST_SLOT, 0, "start"),
                                 new TimePassedCondition(QUEST_SLOT, 1, DELAY),
								 new NotCondition(new PlayerCanEquipItemCondition("orchidea"))),
				ConversationStates.IDLE, 
				"Szkoda, że nie masz miejsca, aby wziąść ode mnie te piękny kwiat. Wróć, gdy będziesz mógł wziąść ten cenny kwiat bez uszkadzania płatków.",
				null);
		
        // don't give the flower if one was given within the last 5 minutes
        rose.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(rose.getName()),
								 new QuestInStateCondition(QUEST_SLOT, 0, "start"),
                                 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
			ConversationStates.IDLE,
				"Dałam tobie kwiat pięć minut temu! Jej Królewska Wysokość może się nimi cieszyć przez jakiś czas.",
				null);
	    
	    final ChatCondition lostFlowerCondition = new AndCondition(new GreetingMatchesNameCondition(rose.getName()),
				 // had got the flower before and was supposed to take it to the princess next
	    		 new QuestInStateCondition(QUEST_SLOT, 0, "got_flower"),
				 // check chest and so on first - maybe the player does still have it (though we can't check house chests or the floor)
				 new ChatCondition() {
				     @Override
				     public boolean fire(final Player player, final Sentence sentence, final Entity entity) { 
				    	 return player.getTotalNumberOf("orchidea") == 0;
				     }
				 },
				// just to check there is space
				new PlayerCanEquipItemCondition("orchidea"),
				// note: older quest slots will pass this automatically, but they are old now.
                new TimePassedCondition(QUEST_SLOT, 1, 12*MathHelper.MINUTES_IN_ONE_WEEK));
	   	    
	    // if the player never had a timestamp stored (older quest) we have now added timestamp 1.
	    // but that was a while ago that we changed it (November 2010?)
		rose.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			lostFlowerCondition,
			ConversationStates.QUESTION_1, 
			"Cześć. Zgubiłeś kwiatek, który dałam ostatnio? Jeżeli potrzebujesz następny to powiedz #tak, ale to strata dla mnie, że muszę dać ci kolejny więc lepiej bądź pewny!",
			null);
		
		rose.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				lostFlowerCondition,
				ConversationStates.IDLE,
				"Oto nowe kwiatki dla pięknej pani, ale nie zgub ich.",
				new MultipleActions(new EquipItemAction("orchidea", 1, true), 
                        new SetQuestAction(QUEST_SLOT, 0, "got_flower"), 
                        // dock some karma for losing the flower
                        new IncreaseKarmaAction(-20.0), 
                        new SetQuestToTimeStampAction(QUEST_SLOT, 1)));
		
		rose.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				lostFlowerCondition,
				ConversationStates.IDLE,
				"Nie martw się. Na pewno gdzieś je masz!",
				null);
	    
        // don't give the flower if the quest state isn't start
        // unless it's been over 12 weeks and are in state got_flower?
	    rose.add(ConversationStates.IDLE,
		    	ConversationPhrases.GREETING_MESSAGES,
		    	new AndCondition(new GreetingMatchesNameCondition(rose.getName()),
		    					 new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
		    					 new NotCondition(lostFlowerCondition)),
		    	ConversationStates.IDLE,
		    	"Dziś nie mam nic dla Ciebie, przykro mi. Wyruszam teraz w dalszą drogę. Dowidzenia.",
			    null);
	}

	private void bringFlowerStep() {
		final SpeakerNPC npc = npcs.get("Tywysoga");
		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of goldbars
				final StackableItem goldbars = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("sztabka złota");
				int goldamount;
				goldamount = 5 * Rand.roll1D6();
				goldbars.setQuantity(goldamount);
				// goldbars.setBoundTo(player.getName()); <- not sure
				// if these should get bound or not.
				player.equipOrPutOnGround(goldbars);
				npc.say("Dziękuję! Weź te " + Integer.toString(goldamount) + " sztabek złota. Mam ich mnóstwo i słuchaj: jeżeli chciałbyś mi przynieść następny kwiatek to wcześniej zapytaj mnie. Róża Kwiaciarka jest podejrzliwa. Nie sprzedam kwiatka byle komu i bez powodu.");
			}
		};
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flower", "orchidea", "kwiatek"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "got_flower"), new PlayerHasItemWithHimCondition("orchidea")),
				ConversationStates.ATTENDING, null,
				new MultipleActions(new DropItemAction("orchidea"), 
                                    new IncreaseXPAction(5000), 
                                    new IncreaseKarmaAction(15),
									addRandomNumberOfItemsAction, 
									new SetQuestAction(QUEST_SLOT, 0, "flower_brought"),
									new IncrementQuestAction(QUEST_SLOT, 2, 1)));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("flower", "orchidea", "kwiatek"),
			new NotCondition(new PlayerHasItemWithHimCondition("orchidea")),
			ConversationStates.ATTENDING,
			"Widzę, że nie masz przy sobie orchidei. Róża Kwiaciarka przemierza całą wyspę i jestem pewna, że pewnego dnia ją spotkasz!",
			null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Orchidea dla Księżniczki Elfów",
				"Tywysoga Księżniczka Elfów z Nalwor Tower, zleciła znalezienie cudownej sprzedawczyni Róży Kwiaciarki, aby odebrać od niej cenną orchideę.",
				false);
		offerQuestStep();
		getFlowerStep();
		bringFlowerStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Dzielnie dostałem się na górę Nalwor Tower, aby spotkać się z Princess Tywysoga.");
        // todo split on ; to put the 0th part in questState		
        final String questStateFull = player.getQuest(QUEST_SLOT);
        final String[] parts = questStateFull.split(";");
        final String questState = parts[0];
		if ("rejected".equals(questState)) {
			res.add("Księżniczka Elfów poprosiła mnie o piękny kwiatek, ale nie mogę sobie zawracać tym głowy. Chce pokonać kilku elfów!");
		}
		if ("start".equals(questState) || "got_flower".equals(questState) || isCompleted(player)) {
			res.add("Księżnika zleciła mi znalezienie cudownej Róży Kwiaciarki, aby odebrać cenną orchideę dla niej.");
		}
		if ("got_flower".equals(questState) || isCompleted(player)) {
			res.add("Znalzłem Różę Kwiaciarkę i mam kwiatek, który muszę doręczyć Princess Tywysoga.");
		}
        if (isRepeatable(player)) {
            res.add("Wziąłem kwiatek do Księżniczki, a ona dała mi sztabki złota. Jeżeli chcę jej znowu sprawić radość to mogę znowu wziąść kolejne zadanie.");
        } 
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("Zaniosłem juz Princess Tywysoga " + Grammar.quantityplnoun(repetitions, "cenny kwiat", "one") + ".");
		}
		return res;
	}
	
	@Override
	public String getName() {
		return "ElfPrincess";
	}
	
	@Override
	public int getMinLevel() {
		return 60;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new QuestInStateCondition(QUEST_SLOT,0,"flower_brought").fire(player,null, null);
	}
	
	// The quest may have been completed a few times already and then re-opened as it's repeatable
	// since this method is used to separate open quests from completed quests, we'll say that being completed
	// means it's done and not re-opened
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestInStateCondition(QUEST_SLOT,0,"flower_brought").fire(player,null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}
	@Override
	public String getNPCName() {
		return "Tywysoga";
	}
}
