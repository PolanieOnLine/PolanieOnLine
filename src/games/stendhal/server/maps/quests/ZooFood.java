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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Zoo Food
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Katinka, the keeper at the Ados Wildlife Refuge
 * <li> Dr.Feelgood, the veterinary
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Katinka asks you for food for the animals.
 * <li> You get the food, e.g. by killing other animals ;) or harvesting it
 * <li> You give the food to Katinka.
 * <li> Katinka thanks you.
 * <li> You can then buy cheap medicine from Dr. Feelgood.
 * </ul>
 *
 * REWARD: <ul>
 * <li> 500 XP
 * <li> 10 Karma
 * <li> Supply for cheap medicine and free pet healing for one week
 * </ul>
 * REPETITIONS: - Once per week.
 */
public class ZooFood extends AbstractQuest {

	private static final int REQUIRED_HAM = 10;
	private static final int DELAY = MathHelper.MINUTES_IN_ONE_WEEK;

	private static final String QUEST_SLOT = "zoo_food";

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
		res.add("Spotkałem Katinkę w zoo");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie mam czasu na zwierzęta i ich problemy z jedzeniem");
			return res;
		}
		res.add("Nie chcę widzieć jak te biedne zwierzęta umierają! Pomogę im w zdobyciu jedzenia!");
		if (questState.startsWith("start;")) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,1);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,1);
			if (!player.isEquipped(questItem, amount)) {
				res.add(String.format("Zostałem poproszony, aby przynieść " +Grammar.quantityplnoun(amount, questItem, "a") + " dla zwierząt."));
			} else {
				res.add(String.format("Mam " +Grammar.quantityplnoun(amount, questItem, "a") + " dla zwierząt, muszę to zanieść do schroniska w Ados."));
			}
		}
		if (isCompleted(player)) {
			if(new TimePassedCondition(QUEST_SLOT, 1, DELAY).fire(player, null, null)) {
				res.add("Zwierzęta znów są głodne! Muszę zapytać Katinki czego potrzebują.");
			} else {
				res.add("Zwierzęta nie są głodne!");
			}
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Katinka");

        // Player has never done the zoo quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witam w schronisku dla dzikich zwierząt w Ados! Mamy wiele zwierząt do wykarmienia. Potrzebujemy pomocy... mam dla ciebie #zadanie do zrobienia.",
				null
		);

        // Player returns within one week of completing quest
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
                ConversationStates.ATTENDING, "Witam ponownie w schronisku dla dzikich zwierząt w Ados! Jeszcze raz dziękuję za uratowanie naszych zwierząt!",
				null
		);

        // Player returns and longer than a week has passed, ask to help again
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new TimePassedCondition(QUEST_SLOT, 1, DELAY)),
				ConversationStates.QUEST_OFFERED, "Witam ponownie w schronisku dla dzikich zwierząt w Ados. "
                + " Nasze zwierzęta są znowu głodne, pomożesz ponownie?",
                null);


        // Player has never done the zoo quest, player asks what the task was
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT), 
				ConversationStates.QUEST_OFFERED, "Nasze zwierzęta sę głodne. Potrzebujemy więcej " +
						"jedzenia do ich wykarmienia. Pomożesz nam?",
				null);

	    final Map<String,Integer> items = new HashMap<String, Integer>();		
		items.put("jabłko",10);
		items.put("chleb",3);
		items.put("pieczarka",8);
		items.put("marchew",10);
		items.put("ser",10);
		items.put("wisienka",5);
		items.put("jajo",5);
		items.put("zboże",20);
		items.put("szynka",10); 
		items.put("miód",5); 
		items.put("mięso",15);
		items.put("borowik",5);
		items.put("płotka",3);
		items.put("sałata",10);
		items.put("szpinak",7);

        // Player has done quest before and agrees to help again
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING, null,
                new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start;", 5.0),
				new StartRecordingRandomItemCollectionAction(QUEST_SLOT, 1, items, "Dziękuję! Proszę" 
                + " przynieś [item] lub tyle ile dasz rady."))
		);

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING, "Och... Chyba będziemy musieli nakarmić je jeleniami...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0)
		);
		
        // Player returns within one week of completing quest
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT), 
                                 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
				ConversationStates.ATTENDING, null, 
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY,  "Dziękuję, ale nie mamy teraz problemów."));

		// player requests quest while quest still active
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT, 1, "Już jesteś w trakcie zbierania [item]."));
	}

	private void step_2() {
		// Just find the food somewhere. It isn't a quest
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Katinka");

		// compatibility with old quests:
		// player returns while initial quest is still active, set it to match the new way
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Witaj ponownie! Czy przyniosłeś nam "
						+ Grammar.quantityplnoun(REQUIRED_HAM, "szynka") + "?",
			new SetQuestAction(QUEST_SLOT,"start;ham=10"));

		// player returns while quest is active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start;")),
				ConversationStates.QUEST_ITEM_BROUGHT,
				null,
				new SayRequiredItemAction(QUEST_SLOT, 1, "Witaj ponownie! Czy przyniosłeś nam [item]?"));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,1));
		actions.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done;1", 5.0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncreaseXPAction(500));
	
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES, 
			new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,1),
			ConversationStates.ATTENDING, "Dziękuję! Uratowałeś nasze rzadkie zwierzęta.",
			new MultipleActions(actions));

        npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
        		ConversationPhrases.YES_MESSAGES,
        		new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,1)),
        		ConversationStates.ATTENDING, null,
        		new SayRequiredItemAction(QUEST_SLOT, 1, "*westchnienie* SPECCJALNIE powiedziałam, że potrzebujemy [item]!")
        		);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Pospiesz się! Te rzadkie zwierzęta umierają z głodu!",
				null);
	}

	private void step_4() {
		final SpeakerNPC npc = npcs.get("Dr. Feelgood");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT), 
						new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY))),
			ConversationStates.ATTENDING, "Cześć! Teraz zwierzęta mają wystarczająco dużo jedzenia. Już nie chorują tak łatwo i mam czas na inne rzeczy. W czym mogę pomóc?",
				null
		);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new OrCondition(new QuestNotCompletedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT, 1, DELAY))
						)),
				ConversationStates.IDLE, "Przepraszam, ale nie mogę teraz rozmawiać. Wszystkie zwierzęta są chore ponieważ nie mają jedzenia. Nie chciałbyś się rozejrzeć?",
				null
		);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Jedzenie dla Zoo",
				"Zwierzęta w zoo są głodne! Muszę dostarczyć im coś do jedzenia!",
				true);
		step_1();
		step_2();
		step_3();
		step_4();
	}

	@Override
	public String getName() {
		return "ZooFood";
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,DELAY)).fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Katinka";
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
