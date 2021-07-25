/***************************************************************************
 *                   (C) Copyright 2019-2021 - Stendhal                    *
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
import games.stendhal.common.constants.Occasion;
import games.stendhal.common.constants.Testing;
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

public class WeeklyItemWieliczkaQuest extends AbstractQuest {
	private static final String QUEST_SLOT = "weekly_item_wieliczka";
	private final SpeakerNPC npc = npcs.get("Zbigniew");

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK * 3;

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
		items.put("cuha góralska",1);
		items.put("zbroja lazurowa",1);
		items.put("zbroja szamana barbarzyńców",1);

		// axe
		items.put("czarna halabarda",1);
		items.put("topór chaosu",1);
		items.put("ciupaga",1);
		items.put("złota ciupaga",1);

		// boots
		items.put("buty z zielonego potwora",1);
		items.put("buty elfickie",1);
		items.put("buty karmazynowe",1);
		items.put("buty cieni",1);
		items.put("buty barbarzyńcy",1);
		items.put("buty kamienne",1);
		items.put("buty wampirze",1);

		// cloak
		items.put("czarny płaszcz smoczy",1);
		items.put("lodowy płaszcz",1);
		items.put("złoty płaszcz",1);
		items.put("płaszcz chaosu",1);
		items.put("płaszcz licha",1);
		items.put("płaszcz z otchłani",1);
		items.put("szmaragdowy płaszcz smoczy",1);

		// club
		items.put("kij z czaszką",1);
		items.put("młot chaosu",1);
		items.put("kropacz",1);

		// drinks
		items.put("zabójcza trucizna",5);
		items.put("śmiertelna trucizna",5);
		items.put("gigantyczny eliksir",3);
		items.put("zupa rybna",3);

		// helmet
		items.put("kapelusz elficki",1);
		items.put("hełm nabijany ćwiekami",1);
		items.put("hełm barbarzyńcy",1);
		items.put("góralski kapelusz",1);
		items.put("misiurka",1);
		items.put("hełm mainiocyjski",1);
		items.put("hełm cieni",1);

		// jewellery
		items.put("ametyst",1);
		items.put("rubin",3);
		items.put("obsydian",2);
		items.put("diament",1);
		items.put("szmaragd",5);

		// legs
		items.put("góralska biała spódnica",1);
		items.put("spodnie krasnoludzkie",1);
		items.put("lodowe spodnie",1);
		items.put("spodnie wampirze",1);
		items.put("spodnie nabijane klejnotami",1);
		items.put("spodnie pustynne",1);

		// shield
		items.put("tarcza chaosu",1);
		items.put("tarcza elficka",1);
		items.put("szmaragdowa tarcza smocza",1);
		items.put("tarcza piaskowa",1);
		items.put("tarcza jednorożca",1);
		items.put("tarcza królewska",1);
		items.put("tarcza licha",1);
		items.put("polska tarcza ciężka",1);
		items.put("polska tarcza drewniana",1);
		items.put("polska tarcza lekka",1);
		items.put("polska tarcza kolcza",1);
		items.put("ognista tarcza",1);
		items.put("lodowa tarcza",1);
		items.put("lwia tarcza",1);

		// sword
		items.put("chopesz",1);
		items.put("miecz elficki",1);
		items.put("miecz chaosu",1);
		items.put("klinga orków",1);
		items.put("złota klinga orków",1);
		items.put("miecz cieni",1);
		items.put("miecz zaczepny",1);
		items.put("miecz ognisty",1);
		items.put("nihonto",1);
		items.put("piekielny sztylet",1);
		items.put("miecz lodowy",1);
		items.put("miecz nieśmiertelnych",1);
		items.put("ognisty miecz demonów",1);
		
		// belt
		items.put("złoty pas",1);
		items.put("wzmocniony pas skórzany",1);
		items.put("pas wampirzy",1);
		items.put("pas karmazynowy",1);
		items.put("pas krasnoludzki",1);
		items.put("pas elficki",1);
		items.put("pas olbrzymi",1);
		items.put("pas zbójnicki",1);
		
		// glove
		items.put("rękawice karmazynowe",1);
		items.put("rękawice lazurowe",1);
		items.put("lodowe rękawice",1);
		items.put("ogniste rękawice",1);
		items.put("rękawice cieni",1);
		items.put("rękawice płytowe",1);
		items.put("kamienne rękawice",1);
		
		// wand
		items.put("różdżka Strzyboga",1);
		items.put("różdżka Wołosa",1);

		// necklace
		items.put("korale",1);
		items.put("elficki naszyjnik",1);

		// ranged
		items.put("klejony łuk",1);
		items.put("kusza",1);
		items.put("kusza łowcy",1);
		items.put("lodowa kusza",1);
	}

	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Dostarcz mi [item]"
				+ " i powiedz #'załatwione', gdy przyniesiesz."));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		return new MultipleActions(actions);
	}

	private void getQuest() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już otrzymałeś zadanie, aby przynieść [item]"
						+ ". Powiedz #zakończone jeżeli zdobędziesz już ten przedmiot."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"Już otrzymałeś zadanie, aby przynieść [item]"
						+ ". Powiedz #zakończone jeżeli będziesz miał [item] ze sobą. Być może teraz ten przedmiot występuje rzadko. Mogę dać Tobie #inne zadanie lub możesz wrócić z tym, o które prosiłem Cię wcześniej."));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "Mogę Cię prosić o przyniesienie przedmiotu tylko raz w tygodniu. Wróć za "));

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
				goldamount = 500 * Rand.roll1D20();
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
		res.add("Spotkałem burmistrza Zbigniewa w ratuszu w Wieliczce.");
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
				res.add(String.format("Zostałem poproszony, aby przynieść " +Grammar.quantityplnoun(amount, questItem, "") + " do ratusza w Wieliczce."));
			} else {
				res.add(String.format("Mam " + Grammar.quantityplnoun(amount, questItem, "a") + " dla burmistrza Wieliczki. Muszę to im zanieść."));
			}
		}
		if (isRepeatable(player)) {
			res.add("Zaniosłem wartościowy przedmiot do Zbigniewa i burmistrz zlecił mi znalezienie następnego.");
		} else if (isCompleted(player)) {
			res.add("Pomogłem Wieliczce. Za 7 dni zajrze tam znów. Być może potrzebować będą mojej pomocy.");
		}
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("Przyniosłem już "
					+ Grammar.quantityplnoun(repetitions, "") + " wartościowych przedmiotów do ratusza w Wieliczce.");
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
		return "Zbigniew";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}
}
