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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
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
	
	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK * 6;
	
	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK; 
	
	/**
	 * All items which are hard enough to find but not tooo hard and not in Daily quest. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();
		
		// armor
		items.put("zbroja barbarzyńcy",1);
		items.put("zbroja chaosu",1);
		items.put("zbroja krasnoludzka",1);
		items.put("złota zbroja",1);
		items.put("lodowa zbroja",1);
		items.put("magiczna zbroja płytowa",1);
		items.put("zbroja mainiocyjska",1);
		items.put("zbroja cieni",1);
		items.put("kamienna zbroja",1);
		items.put("zbroja xenocyjska",1);

		// axe
		items.put("topór oburęczny złoty",1);
		items.put("topór oburęczny magiczny",1);

		// boots
		items.put("buty chaosu",1);
		items.put("złote buty",1);
		items.put("buty mainiocyjskie",1);
		items.put("buty cieni",1);
		items.put("buty żelazne",1);
		items.put("buty kamienne",1);
		items.put("buty xenocyjskie",1);


		// cloak
		items.put("lazurowy płaszcz smoczy",1);
		items.put("prążkowany płaszcz lazurowy",1);
		items.put("płaszcz chaosu",1);
		items.put("złoty płaszcz",1);
		items.put("płaszcz mainiocyjski",1);
		items.put("karmazynowy płaszcz smoczy",1);
		items.put("płaszcz cieni",1);
		items.put("płaszcz xenocyjski",1);

		// club
		items.put("kij z czaszką",1);

		// drinks
		items.put("wielki eliksir",5);
		items.put("zupa rybna",3);

		// helmet
		items.put("hełm chaosu",1);
		items.put("złoty hełm",1);
		items.put("złoty hełm wikingów",1);
		items.put("hełm mainiocyjski",1);
		items.put("hełm cieni",1);

		// jewellery
		items.put("diament",1);
		items.put("obsydian",1);

		// legs
		items.put("spodnie chaosu",1);
		items.put("spodnie krasnoludzkie",1);
		items.put("złote spodnie",1);
		items.put("spodnie mainiocyjskie",1);
		items.put("spodnie cieni",1);
		items.put("spodnie xenocyjskie",1);

		// misc
		items.put("serce olbrzyma",5);
		items.put("gruczoł jadowy",1);

		// resource
		items.put("sztabka mithrilu",3);
		items.put("bryłka mithrilu",5);
		items.put("gruczoł przędzy",7);

		// special
		items.put("czterolistna koniczyna",1);
		items.put("mityczne jajo",1);

		// shield
		items.put("tarcza chaosu",1);
		items.put("złota tarcza",1);
		items.put("szmaragdowa tarcza smocza",1);
		items.put("magiczna tarcza płytowa",1);
		items.put("tarcza mainiocyjska",1);
		items.put("tarcza cieni",1);
		items.put("tarcza xenocyjska",1);

		// sword
		items.put("sztylet mordercy",1);
		items.put("pogromca",1);
		items.put("miecz chaosu",1);
		items.put("sztylet mroku",1);
		items.put("miecz demonów",1);
		items.put("miecz elfów ciemności",1);
		items.put("miecz ognisty",1);
		items.put("półtorak",1);
		items.put("piekielny sztylet",1);
		items.put("miecz lodowy",1);
		items.put("miecz nieśmiertelnych",1);
		items.put("czarny sztylet",1);
		items.put("miecz xenocyjski",1);

	}
	
	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again
		
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Chcę, aby muzeum w Kirdneh było największe w krainie! Dostarcz mi [item]"
				+ " i powiedz #'załatwione', gdy przyniesiesz."));	
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		return new MultipleActions(actions);
	}
	
	private void getQuest() {
		final SpeakerNPC npc = npcs.get("Hazel");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie przyniesienia do muzeum [item]"
						+ ". Powiedz #zakończone jeżeli będziesz miał  ze sobą."));
		
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już masz zadanie przyniesienia do muzeum [item]"
						+ ". Powiedz #zakończone jeżeli będziesz miał  ze sobą. Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej."));
	
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Muzeum może Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za "));
		
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))), 
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}
	
	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("Hazel");
		
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
		actions.add(new IncreaseAtkXPDependentOnLevelAction(5.0/3.0, 290.0));
		actions.add(new IncreaseDefXPDependentOnLevelAction(5.0/3.0, 290.0));
		actions.add(new IncreaseKarmaAction(40.0));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
				int goldamount;
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager()
								.getItem("money");
				goldamount = 100 * Rand.roll1D6();
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
		final SpeakerNPC npc = npcs.get("Hazel");
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)), 
				ConversationStates.ATTENDING, 
				null, 
				startQuestAction());
		
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
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Hazela kuratora muzeum Kirdneh.");
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
				res.add(String.format("Zostałem poproszony, aby przynieść " +Grammar.quantityplnoun(amount, questItem, "") + " do muzeum w Kirdneh."));
			} else {
				res.add(String.format("Mam " + Grammar.quantityplnoun(amount, questItem, "a") + " dla muzeum w Kirdneh. Muszę to im zanieść."));
			}
		}
		if (isRepeatable(player)) {
			res.add("Zaniosłem wartościowy przedmiot do Hazela i muzeum zleciło mi znalezienie następnego.");
		} else if (isCompleted(player)) {
			res.add("Pomogłem muzeum. Za 7 dni zajrze tam znów. Byś może potrzebować będą mojej pomocy.");
		}
		// add to history how often player helped Hazel so far
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("Przyniosłem już "
					+ Grammar.quantityplnoun(repetitions, "") + " eksponatów do muzeum.");
		}

		return res;
	}
	
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Muzeum Kirdneh potrzebuje pomocy!",
				"Hazel, kurator Muzeum Kirdneh, chce aby było one największym w kraju i potrzebuje mojej pomocy raz na tydzień.",
				true);
		buildItemsMap();
		
		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "WeeklyItemQuest";
	}
	
	// the items requested are pretty hard to get, so it's not worth prompting player to go till they are higher level.
	@Override
	public int getMinLevel() {
		return 60;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}

	@Override
	public String getNPCName() {
		return "Hazel";
	}
}
