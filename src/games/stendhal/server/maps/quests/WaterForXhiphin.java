/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.DropItemdataItemAction;
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
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

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
	private static final String QUEST_SLOT = "water_for_xhiphin";
	private final SpeakerNPC npc = npcs.get("Xhiphin Zohos");

	/** To combine with the quest triggers */
	private static final String EXTRA_TRIGGER = "woda";

	/**
	 * The delay between repeating quests.
	 * 7200 minutes
	 * */
	private static final int REQUIRED_MINUTES = 7200;

	/** How the water is marked as clean */
	private static final String CLEAN_WATER_ITEMDATA = "clean";

	private void requestStep() {
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
				"Dziękuję. Nic teraz nie potrzebuję.",
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
				"Cóż, nie jest to zbyt uczynne.",
				new MultipleActions(
						new SetQuestAction(QUEST_SLOT, 0, "rejected"),
						new DecreaseKarmaAction(5.0)));
	}

	private void checkWaterStep() {
		final SpeakerNPC waterNPC = npcs.get("Stefan");

		// player gets water checked
		// mark itemdata of item to show it's good
		final List<ChatAction> actions = new LinkedList<ChatAction>();
		// for now Stefan is just able to check one water at a time (even from a stack) and he always says it's fine and clean
		// if you go to him with one checked and one unchecked he might just check the checked one again - depends what sits first in bag
		actions.add(new DropItemAction("butelka wody",1));
		actions.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item water = SingletonRepository.getEntityManager().getItem("butelka wody");
				water.setItemData(CLEAN_WATER_ITEMDATA);
				water.setDescription("Oto świeża woda. Jest smaczna i ożeźwiająca. Stefan sprawdzał ją.");
				// remember the description
				water.setPersistent(true);
				player.equipOrPutOnGround(water);
			}
		});

		waterNPC.add(ConversationStates.ATTENDING, 
					Arrays.asList("water", "clean", "check", "woda", "czysta", "czystość", "sprawdzał"),
					new PlayerHasItemWithHimCondition("butelka wody"),
					ConversationStates.ATTENDING, 
					"Ta woda jak dla mnie, to wygląda na czystą! Musi być z dobrego źródła.",
					// take the item and give them a new one with an itemdata or mark all?
					new MultipleActions(actions));

		// player asks about water but doesn't have it with them
		waterNPC.add(ConversationStates.ATTENDING, 
					Arrays.asList("water", "clean", "check", "woda", "czysta", "czystość", "sprawdzał"),
					new NotCondition(new PlayerHasItemWithHimCondition("butelka wody")),
					ConversationStates.ATTENDING, 
					"Możesz zdobyć wodę ze źródła w górach lub z dużych źródeł w podliżu wodospadów. Jeżeli przyniesiesz mi to sprawdzę jej czystość.",
					null);
	}

	private void finishStep() {
		// Player has got water and it has been checked
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		// make sure we drop the checked water not any other water
		reward.add(new DropItemdataItemAction("butelka wody", CLEAN_WATER_ITEMDATA));
		reward.add(new EquipItemAction("eliksir", 3));
		reward.add(new IncreaseXPAction(250));
		reward.add(new IncrementQuestAction(QUEST_SLOT, 2, 1) );
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT,1));
		reward.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		reward.add(new IncreaseKarmaAction(5.0));
		reward.add(new InflictStatusOnNPCAction("butelka wody"));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemdataItemWithHimCondition("butelka wody", CLEAN_WATER_ITEMDATA)),
				ConversationStates.ATTENDING, 
				"Bardzo dziękuję! To jest to czego aktualnie potrzebowałem! Przyjmij te mikstury, które dała mi Sarzina.",
				new MultipleActions(reward));

        // player returns with no water at all. 
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("butelka wody"))),
				ConversationStates.ATTENDING, 
				"Poczekam, aż przyniesiesz mi trochę wody. To słońce strasznie grzeje.",
				null);

        // add the other possibilities
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.combine(ConversationPhrases.QUEST_MESSAGES, EXTRA_TRIGGER), 
				new AndCondition(
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("butelka wody"),
						new NotCondition(new PlayerHasItemdataItemWithHimCondition("butelka wody", CLEAN_WATER_ITEMDATA))),
				ConversationStates.ATTENDING, 
				"Hmm... to nie to. Nie ufam Tobie, ale nie jestem pewien czy ta woda nadaje się do picia. Czy mógłbyś się udać do #Stefana i poprosić go o #sprawdzenie?",
				null);

		npc.addReply("Stefan", "Stefan jest szefem restauracji w hotelu w Fado. Ufam mu w sprawie sprawdzania wody czy nadaje się do jedzenia lub picia. On jest profesjonalistą.");
		npc.addReply(Arrays.asList("check", "sprawdzenie"), "Przykro mi, ale nie jestem ekspertem w potrawach i napojach. Zapytaj #Stefana.");
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Woda Dla Xhiphina",
				"Xhiphin Zohos potrzebuje trochę świeżej wody.",
				true, 2);
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
			res.add(player.getGenderVerb("Powiedziałem") + " Xhiphin Zohos, że nie przyniosę mu wody.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") || isCompleted(player)) {
			res.add(player.getGenderVerb("Zgodziłem") + " się przynieść mu trochę wody, aby Xhiphin Zohos ugasił pragnienie.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start") && player.isEquipped("butelka wody") && new NotCondition(new PlayerHasItemdataItemWithHimCondition("butelka wody", CLEAN_WATER_ITEMDATA)).fire(player, null, null) || isCompleted(player)) {
			res.add(player.getGenderVerb("Znalazłem") + " źródło świeżej wody, ale nie jestem " + player.getGenderVerb("pewny") + " czy jest bezpieczna do picia dla Xhiphina.");
		}
		if (new PlayerHasItemdataItemWithHimCondition("butelka wody", CLEAN_WATER_ITEMDATA).fire(player, null, null) || isCompleted(player)) {
			res.add("Stefan, szef w hotelu w Fado sprawdził wodę, którą zebrałem i jest czysta i zdatna do picia.");
		}
		// checked water was clean?
        if (isCompleted(player)) {
            if (isRepeatable(player)) {
                res.add(player.getGenderVerb("Wziąłem") + " wodę do Xhiphin Zohos jakiś czas temu.");
            } else {
                res.add(player.getGenderVerb("Wziąłem") + " wcześniej wodę do Xhiphin Zohos i " + player.getGenderVerb("" + player.getGenderVerb("otrzymałem") + "") + " od niego trochę mikstur.");
            }			
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Woda Dla Xhiphina";
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
		return npc.getName();
	}
}
