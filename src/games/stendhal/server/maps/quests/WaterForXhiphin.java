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
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Quest to fetch water for a thirsty person.
 * You have to check it is clean with someone knowledgeable first
 *
 * @author kymara
 * 
 * 
 * QUEST: Water for Xhiphin
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Xhiphin Zohos </li>
 * <li> Stefan </li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Ask Xhiphin Zohos for a quest. </li>
 * <li> Get some fresh water. </li>
 * <li> Xhiphin Zohos wants to assure that the water is clean. Show the water to Stefan and he will check it. </li>
 * <li> Return the water to Xhiphin Zohos who will then enjoy it. </li> 
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 250 XP </li>
 * <li> some karma (5 + (5 | -5)) </li>
 * <li> 3 potions </li>
 * </ul>
 * 
 * REPEATABLE: 
 */

public class WaterForXhiphin extends AbstractQuest {

	// constants
	private static final String QUEST_SLOT = "water_for_xhiphin";
	
	/** To combine with the quest triggers */
	private static final String EXTRA_TRIGGER = "woda";
	
	/** The delay between repeating quests.
	 * 7200 minutes */
	private static final int REQUIRED_MINUTES = 7200;
	
	/** How the water is marked as clean */
	private static final String CLEAN_WATER_INFOSTRING = "clean";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private void requestStep() {
		final SpeakerNPC npc = npcs.get("Xhiphin Zohos");
		
		// player asks about quest for first time (or rejected)
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED, 
				"Jestem bardzo spragniony czy mógłbyś przynieść mi trochę świeżej wody?",
				null);
		
		// player can repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED, 
				"Zaschło mi w gardle z tego gadania czy mógłbyś przynieść mi trochę wody?",
				null);	
		
		// player can't repeat quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT), new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING, 
				"Dzięukję. Nic teraz nie potrzebuję.",
				null);	
		
		// if the quest is active we deal with the response to quest/water in a following step
		
		// Player agrees to get the water
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, 
				null,
				ConversationStates.ATTENDING, 
				"Dziękuję! Naturalna świeża woda jest najlepsza. Rzece płynącej z Fado do Nalwor wodę zapewnia źródło.",
				new MultipleActions(
				        new SetQuestAction(QUEST_SLOT, 0, "start"),
				        new IncreaseKarmaAction(5.0)));
		
		// Player says no, they've lost karma
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, 
				null, 
				ConversationStates.IDLE,
				"Coż nie jest to zbyt uczynne.",
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT, 0, "rejected"),
						new DecreaseKarmaAction(5.0)));
	}
		
	
	private void checkWaterStep() {
		final SpeakerNPC waterNPC = npcs.get("Stefan");

		// player gets water checked
		// mark infostring of item to show it's good
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		// for now Stefan is just able to check one water at a time (even from a stack) and he always says it's fine and clean
		// if you go to him with one checked and one unchecked he might just check the checked one again - depends what sits first in bag
		actions.add(new DropItemAction("woda",1));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item water = SingletonRepository.getEntityManager().getItem("woda");
				water.setInfoString(CLEAN_WATER_INFOSTRING);
				water.setDescription("Oto świeża woda. Jest smaczna i ożeźwiająca. Stefan sprawdzał ją.");
				// remember the description
				water.setPersistent(true);
				player.equipOrPutOnGround(water);
			}
		});
		waterNPC.add(ConversationStates.ATTENDING, 
					Arrays.asList("water", "clean", "check", "woda", "czysta", "czystość", "sprawdzał"),
					new PlayerHasItemWithHimCondition("woda"),
					ConversationStates.ATTENDING, 
					"To woda jak dla mnie to wygląda na czystą! Musi być z dobrego źródła.",
					// take the item and give them a new one with an infostring or mark all?
					new MultipleActions(actions));
		
		// player asks about water but doesn't have it with them
		waterNPC.add(ConversationStates.ATTENDING, 
					Arrays.asList("water", "clean", "check", "woda", "czysta", "czystość", "sprawdzał"),
					new NotCondition(new PlayerHasItemWithHimCondition("woda")),
					ConversationStates.ATTENDING, 
					"Możesz zdobyć wodę ze źródła w górach lub z dużych źródeł w podliżu wodospadów. Jeżeli przyniesiesz mi to sprawdzę jej czystość.",
					null);

	}

	
	private void finishStep() {
		final SpeakerNPC npc = npcs.get("Xhiphin Zohos");
		
		// Player has got water and it has been checked
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		// make sure we drop the checked water not any other water
		reward.add(new DropInfostringItemAction("woda", CLEAN_WATER_INFOSTRING));
		reward.add(new EquipItemAction("eliksir", 3));
		reward.add(new IncreaseXPAction(250));
		reward.add(new IncrementQuestAction(QUEST_SLOT, 2, 1) );
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		reward.add(new IncreaseKarmaAction(5.0));
		reward.add(new InflictStatusOnNPCAction("woda"));
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasInfostringItemWithHimCondition("woda", CLEAN_WATER_INFOSTRING)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuję! To jest to co chciałem! Przyjmij te mikstury, które dała mi Sarzina.",
				new MultipleActions(reward));
		
        // player returns with no water at all. 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("woda"))),
				ConversationStates.ATTENDING, 
				"Poczekam, aż przyniesiesz mi trochę wody. To słońce strasznie grzeje.",
				null);
		
        // add the other possibilities
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("woda"),
						new NotCondition(new PlayerHasInfostringItemWithHimCondition("woda", CLEAN_WATER_INFOSTRING))),
				ConversationStates.ATTENDING, 
				"Hmm... to nie to. Nie ufam Tobie, ale nie jestem pewien czy ta woda nadaje się do picia. Czy mógłbyś się udać do #Stefana i poprosić go o #sprawdzenie?",
				null);
		
		npc.addReply("Stefan", "Stefan jest szefem restauracji w hotelu w Fado. Ufam mu w sprawie sprawdzania wody czy nadaje się do jedzenia lub picia. On jest profesjonalistą.");
		npc.addReply(Arrays.asList("check", "sprawdzenie"), "Przykro mi, ale nie jestem ekspertem w potrawach i napojach. Zapytaj #Stefana.");

	}
	
	

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Woda Dla Xhiphin Zohos",
				"Xhiphin Zohos potrzebuje trochę świeżej wody.",
				true);
		requestStep();
		checkWaterStep();
		finishStep();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Xhiphin Zohos jest spragniony od stania cały dzień w słońcu.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Powiedziałem Xhiphin Zohos, że nie przyniosę mu wody.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add("Zgodziłem się przynieść mu trochę wody, aby Xhiphin Zohos ugasił pragnienie.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") && player.isEquipped("woda") && new NotCondition(new PlayerHasInfostringItemWithHimCondition("woda", CLEAN_WATER_INFOSTRING)).fire(player, null, null) || isCompleted(player)) {
			res.add("Znalazłem źródło świeżej wody, ale nie jestem pewny czy jest bezpieczna do picia dla Xhiphina.");
		}
		if (new PlayerHasInfostringItemWithHimCondition("woda", CLEAN_WATER_INFOSTRING).fire(player, null, null) || isCompleted(player)) {
			res.add("Stefan, szef w hotelu w Fado sprawdził wodę, którą zebrałem i jest czysta i zdatna do picia.");
		}
		// checked water was clean?
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add("Wziąłem wodę do Xhiphin Zohos jakiśczas temu.");
            } else {
                res.add("Wziąłem wcześniej wodę do Xhiphin Zohos i otrzymałem od niego trochę mikstur.");
            }			
		}
		return res;
	}

	@Override
	public String getName() {
		return "WaterForXhiphin";
	}
	
	@Override
	public int getMinLevel() {
		return 5;
	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
				 new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "Xhiphin Zohos";
	}
	
}
