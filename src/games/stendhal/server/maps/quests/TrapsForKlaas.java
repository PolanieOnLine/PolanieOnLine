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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
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
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Traps for Klaas
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Klaas (the Seaman that takes care of Athor's ferry's cargo)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Klaas asks you to bring him rodent traps.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>1000 XP</li>
 * <li>5 greater antidote
 * <li>note to apothecary (disabled until Antivenom Ring quest is ready)
 * <li>Can sell rodent traps to Klaas</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Every 24 hours</li>
 * </ul>
 */
public class TrapsForKlaas extends AbstractQuest {

	public final int REQUIRED_TRAPS = 20;
	
    // Time player must wait to repeat quest (1 day)
    private static final int WAIT_TIME = 60 * 24;

	private static final String QUEST_SLOT = "traps_for_klaas";
	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Klaasem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę mieć nic do czynienia z gryzoniami.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Przyrzekłem zdobyć " + REQUIRED_TRAPS + " pułapki na gryzonie i dostarczyć je Klaasowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, 0, "done")) {
			res.add("Dałem pułapki na gryzonie Klaasowi. Zdobyłem trochę doświadczenia i antidota.");
		}
		if (isRepeatable(player)) {
		    res.add("Powinienem sprawdzić czy Klaas znów nie potrzebuje mojej pomocy.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Klaas");
		
		// Player asks for quest
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
		        new AndCondition(
		                new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
		                new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)
		                ),
			ConversationStates.QUEST_OFFERED, 
			"Szczury tutaj dostają się do spichlerza. Czy pomożesz mi w uwolnieniu nas od tego plugastwa?",
			null);
		
        // Player requests quest before wait period ended
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
                new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
			ConversationStates.ATTENDING,
			null, 
                new SayTimeRemainingAction(QUEST_SLOT, 1, WAIT_TIME, "Dziękuje za pułapki. Teraz jedzenie będzie bezpieczne, ale możliwe, że znów będę potrzbował twojej pomocy."));
		
		// Player asks for quest after already started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wiem, że już się ciebie pytałem o zdobycie " + REQUIRED_TRAPS + " pułapek na gryzonie.",
				null);
		
		// Player accepts quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. Potrzebuję, abyś przyniósł mi " + REQUIRED_TRAPS + " #pułapka #na #gryzonie. Proszę pospiesz się! Już nie możemy sobie pozwolić na większą utratę jedzenia.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));
		
		// Player rejects quest
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			// Klaas walks away
			ConversationStates.IDLE,
			"Nie marnuj mojego czasu. Muszę chronić ładunek.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
		
		// Player asks about rodent traps
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("rodent trap", "trap", "rodent traps", "traps", "pułapka na gryzonie", "pułapka", "pułapki na gryzonie", "pułapki", "pułapek na gryzonie", "pułapek"),
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Nie znam nikogo kto nimi handluje, ale słyszałem historię o jednym człowieku, który zabił wielkiego szczura i odkrył pułapkę, która zamyka się na jego nogach.",
			null);
		
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Klaas");
		
		// Reward
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("pułapka na gryzonie", 20));
		// Replacing "not to apothecary" reward with antidotes until Antivenom Ring quest is done.
		//reward.add(new EquipItemAction("liścik do aptekarza", 1, true));
		reward.add(new EquipItemAction("mocne antidotum", 5));
		reward.add(new IncreaseXPAction(1000));
		reward.add(new IncreaseKarmaAction(10));
        reward.add(new SetQuestAction(QUEST_SLOT, "done"));
        reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		
		// Player has all 20 traps
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("pułapka na gryzonie")),
				ConversationStates.QUEST_ITEM_BROUGHT, 
				"Przyniosłeś jakieś pułapki?",
				null);
		
		// Player is not carrying any traps
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie"))),
			ConversationStates.ATTENDING, 
			"Mógłbym użyć te #pułapki. W czym mogę ci pomóc?",
			null);
		
		// Player is not carrying 20 traps
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie"),
						new NotCondition(new PlayerHasItemWithHimCondition("pułapka na gryzonie", 20))),
				ConversationStates.ATTENDING,
				"Przykro mi, ale potrzebuję 20 #pułapek #na #gryzonie",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("pułapka na gryzonie", 20),
				ConversationStates.ATTENDING,
				// Not mentioning apothecary until Antivenom Ring quest is ready
				"Dziękuję! Muszę je teraz przygotować tak szybko jak to możliwe. Weź te antidota jako nagrodę.", // Znam starego #aptekarza. Zabierz do niego ten liścik. Może w czymś ci pomoże.",
				new MultipleActions(reward));
		
        // Player says did not bring items
		npc.add(
            ConversationStates.QUEST_ITEM_BROUGHT,
            ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Znam starego aptekarza, ale nie wiem gdzie teraz mieszka. Może ktoś w Ados będzie wiedział.",
			null);

		// Player asks about the apothecary
		/* Disabling until Antivenom Ring quest is ready
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("apothecary", "aptekarza", "aptekarz"),
			null,
			ConversationStates.ATTENDING,
			"Proszę pospiesz się! Odkryłem, że dobrały się do kolejnej skrzyni z jedzeniem.",
			null);
		
		// Player has lost note
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new NotCondition(new PlayerHasItemWithHimCondition("liścik do aptekarza")),
						new QuestCompletedCondition(QUEST_SLOT),
						new QuestNotStartedCondition("antivenom_ring")),
				ConversationStates.ATTENDING,
				"Zgubiłeś liścik? Cóż napiszę kolejny, ale bądź ostrożny tym razem.",
				new EquipItemAction("note to apothecary", 1, true));
		*/
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pułapki dla Klaasa",
				"Klaas opiekun towaru na promie Athor potrzebuje pułapek na gryzonie.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "TrapsForKlaas";
	}

	public String getTitle() {
		
		return "Pułapki dla Klaasa";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}
	
	@Override
	public String getNPCName() {
		return "Klaas";
	}
}
