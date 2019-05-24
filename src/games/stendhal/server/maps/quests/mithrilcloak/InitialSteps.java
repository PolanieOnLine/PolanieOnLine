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
package games.stendhal.server.maps.quests.mithrilcloak;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
 * @author kymara
 */

class InitialSteps {

	private MithrilCloakQuestInfo mithrilcloak;
	
	private final NPCList npcs = SingletonRepository.getNPCList();

	public InitialSteps(final MithrilCloakQuestInfo mithrilcloak) {
		this.mithrilcloak = mithrilcloak;
	}

	private void offerQuestStep() {
		final SpeakerNPC npc = npcs.get("Ida");
		

		// player asks about quest, they haven't started it yet
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new OrCondition(new QuestNotStartedCondition(mithrilcloak.getQuestSlot()), new QuestInStateCondition(mithrilcloak.getQuestSlot(), "rejected")),				
				ConversationStates.QUEST_OFFERED, 
				"Moja maszyna do szycia jest zepsuta. Pomożesz mi ją naprawić?",
				null);

      final Map<String,Integer> items = new HashMap<String, Integer>();
		  items.put("skórzana zbroja",1);
		  items.put("olejek",1);
		  items.put("szpulka do maszyny",1);

		// Player says yes they want to help 
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			null,			
			new MultipleActions(new SetQuestAction(mithrilcloak.getQuestSlot(), "machine;"),
								new StartRecordingRandomItemCollectionAction(mithrilcloak.getQuestSlot(), 1, items, "Dziękuję! Aby naprawić potrzebuję [#item]. będę wdzięczna za Twoją pomoc.")));
		
		// player said no they didn't want to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"Nie wiem co mogłabym zrobić bez przyzwoitej maszyny do szycia. Nie obawiaj się już nie będę zawracać Ci głowy!",
			new SetQuestAndModifyKarmaAction(mithrilcloak.getQuestSlot(), "rejected", -5.0));


		// player asks for quest but they already did it	
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestCompletedCondition(mithrilcloak.getQuestSlot()),
				ConversationStates.ATTENDING, 
				"Już wykonałeś zadanie, które Ci dałam.",
				null);
		
		//player fixed the machine but hadn't got mithril shield. 
		// they return and ask for quest but they still haven't got mithril shield
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new NotCondition(new QuestCompletedCondition(mithrilcloak.getShieldQuestSlot())),
								 new OrCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_mithril_shield"),
												 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "fixed_machine"))
								 ),
				ConversationStates.ATTENDING, 
								 "Nie mam nic dla Ciebie dopóki nie udowodnisz, że jesteś godny noszenia przedmiotów z mithrilu zdobywając tarczę z mithrilu.",
				null);


		// player fixed the machine but hadn't got mithril shield at time or didn't ask to hear more about the cloak. 
		// when they have got it and return to ask for quest she offers the cloak
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(
								 new QuestCompletedCondition(mithrilcloak.getShieldQuestSlot()),
								 new OrCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_mithril_shield"),
												 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "fixed_machine"))
								 ),
				ConversationStates.QUEST_2_OFFERED, 
				"Gratulacje ukończyłeś zadanie na tarczę z mithrilu! Teraz mam dla Ciebie inne zadanie. Chcesz posłuchać?",
				null);

		npc.addReply(Arrays.asList("olejek", "can of oil", "can", "olej", "pojemnik na olej", "pojemnik"),"Jedyny olejek, który miałam to pachniał rybami. Domyślam się, że rybak go zrobił.");
		npc.addReply("szpulka do maszyny","Tylko krasnal kowal wyrabia je nikt po za nim nie ma tak sprawnych palców. Spróbuj u #Alraka.");
		npc.addReply("Alrak","Sądziłam, że wszystkie dzieci znają Alraka. To jedyny krasnal, którego lubią koboldy. Może jest jedynym krasnalem, który lubi koboldy. Nigdy nie byłam pewna, która ...");
		npc.addReply(Arrays.asList("skórzana zbroja", "suit of leather armor", "suit", "serdak", "zbroja"), "Tak, potrzebuję kawałek skóry do mechanizmu. A ten serdak na to nada się.");
	}

	
	private void fixMachineStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		// player returns who has agreed to help fix machine and prompts ida
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("sewing", "machine", "sewing machine", "task", "quest", "zadanie", "misja"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "machine"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Moja maszyna do szycia jest wciąż zepsuta. Przyniosłeś coś do naprawienia?",
				null);

			// we stored the needed part name as part of the quest slot
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
					ConversationPhrases.YES_MESSAGES,
					new PlayerHasRecordedItemWithHimCondition(mithrilcloak.getQuestSlot(),1),
					ConversationStates.QUEST_2_OFFERED,
					"Bardzo dziękuję! Słuchaj. Muszę się odwdzięczyć za przysługę i mam dobry pomysł. Chcesz wiedzieć więcej?",
					new MultipleActions(new DropRecordedItemAction(mithrilcloak.getQuestSlot(),1), 
							new SetQuestAction(mithrilcloak.getQuestSlot(), "fixed_machine"),
							new IncreaseXPAction(100)));
			
			// we stored the needed part name as part of the quest slot
			npc.add(ConversationStates.QUEST_ITEM_QUESTION,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(new PlayerHasRecordedItemWithHimCondition(mithrilcloak.getQuestSlot(),1)),
					ConversationStates.ATTENDING,
					null,
					new SayRequiredItemAction(mithrilcloak.getQuestSlot(),1,"Nie posiadasz [item] Co za szkoda."));
						

			// player doesn't have the item to fix machine yet
		   npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				   ConversationPhrases.NO_MESSAGES,
				   null,
				   ConversationStates.ATTENDING,
				   null,
				   new SayRequiredItemAction(mithrilcloak.getQuestSlot(),1,"Dobrze jeżeli jest coś w czym mogłabym pomóc to mów. Nie zapomnij przynieś [item] następnym razem!"));

		   //offer cloak
		   npc.add(ConversationStates.QUEST_2_OFFERED,
				   ConversationPhrases.YES_MESSAGES,
				   new QuestCompletedCondition(mithrilcloak.getShieldQuestSlot()),
				   ConversationStates.ATTENDING,
				   "Zrobię dla Ciebie niesamowity płaszcz z mithrilu. Musisz zdobyć tkaninę i narzędzia, których potrzebuję! Na początek przynieś mi kilka jardów " + mithrilcloak.getFabricName() + ". Ekspertem od tkanin jest czarodziej #Kampusch.",
				   new SetQuestAction(mithrilcloak.getQuestSlot(), "need_fabric"));
					

			// player asks for quest but they haven't completed mithril shield quest
			npc.add(ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(
								 new NotCondition(new QuestCompletedCondition(mithrilcloak.getShieldQuestSlot())),
								 new QuestStartedCondition(mithrilcloak.getShieldQuestSlot())
								 ),
				ConversationStates.ATTENDING, 
				"Widzę, że wykonujesz zadanie na tarczę z mithrilu. Widzisz miałam Ci zaoferować płaszcz z mithrilu, ale najpierw musisz ukończyć to zadanie. Wróć, gdy ukończysz zadanie na tarczę z mithrilu, a wtedy znowu porozmawiamy.",
				new SetQuestAction(mithrilcloak.getQuestSlot(), "need_mithril_shield"));
			
			// player asks for quest but they haven't completed mithril shield quest
			npc.add(ConversationStates.QUEST_2_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new QuestNotStartedCondition(mithrilcloak.getShieldQuestSlot()),
					ConversationStates.ATTENDING, 
					"Są legendy o czarodzieju zwanym Baldemar mieszkającym w słynnym podziemnym mieście magów, który produkuje tarczę z mithrilu dla tych co przyniosą mu to co potrzebuje. Powinieneś spotkać się z nim i zrobić to o co Cię poprosi. Gdy skończysz zadanie to wróć tutaj i porozmawiaj ze mną. Będę miała dla Ciebie następne zadanie.",
					new SetQuestAction(mithrilcloak.getQuestSlot(), "need_mithril_shield"));

			// player refused to hear more about another quest after fixing machine
			npc.add(ConversationStates.QUEST_2_OFFERED,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Dobrze. Wygląda na to, że nie potrzebujesz przedmiotów z mithrilu! Wybacz mi, że zaoferowałam Ci pomoc...!",
					null);

			// where to find wizard
			npc.addReply("Kampusch","Ma obsesję na punkcie antyków. Poszukaj go w antykwaricie lub muzeum.");
	
	}

	public void addToWorld() {
		offerQuestStep();
		fixMachineStep();
	}

}
