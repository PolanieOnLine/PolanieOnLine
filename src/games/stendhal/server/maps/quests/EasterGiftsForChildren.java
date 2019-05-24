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

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Easter gifts for children
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Caroline who is working in her tavern in Ados city</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Caroline wants to make children around Faiumoni happy with gifting easter baskets for them.</li>
 * <li>Players have to bring Caroline sweets like chocolate bars and chocolate eggs, as well as some fruit.</li>
 * <li>Children around Faiumoni will be happy with Carolines baskets.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>100 XP</li>
 * <li>5 Ados city scrolls</li>
 * <li>2 home scrolls</li>
 * <li>Karma: 50</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class EasterGiftsForChildren extends AbstractQuest {

	private static final String QUEST_SLOT = "easter_gifts_[year]";

	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Rozmawiałem z Caroline w Ados. Pracuje tam w swojej tawernie.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem jej przyniesienia słodyczy.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Przyrzekłem, że przyniosę Caroline trochę słodyczy dla dzieci z okolic Faiumoni jako Wielkanocny prezent.");
		}
		if ("start".equals(questState) && player.isEquipped("tabliczka czekolady", 5)  && player.isEquipped("małe jajo wielkanocne", 1) && player.isEquipped("jabłko", 5)  && player.isEquipped("wisienka", 5) || "done".equals(questState)) {
			res.add("Mam wszystkie słodycze i zabiorę je do Caroline.");
		}
		if ("done".equals(questState)) {
			res.add("Wziąłem słodycze do Caroline. W zamian dała mi prezent Wielkanocny na moje podróże jako prawdziwemu bohaterowi. :)");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Caroline");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Potrzebuję pomocy w pakowaniu Wielkanocnych koszyków dla dzieci z Faiumoni. Wiem, że odwiedzi je zajączek, ale są takie kochane tak, że chcę je uszczęśliwić. Czy możesz mi pomóc?",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Bardzo dziękuję za słodycze! Już rozdałam wszystkie koszyki Wielkanocne dzieciom z Faiumoni i są teraz szczęśliwe! :) Niestety w tym momencie nie ma innych zadań dla ciebie. Życzę szczęśliwych świąt Wielkanocnych!",
			null);
		
		// Player asks for quests after it is already started
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Zapomniałeś? Już cię pytałam o przyniesienie trochę #słodyczy",
			null);

		// player is willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Potrzebuję trochę #słodyczy do moich koszyków Wielkanocnych. Jeśli przyniesiesz 5 #tabliczka #czekolady, #małe #jajo #wielkanocne, 5 #jabłko i 5 #wisienka to dam ci miłą nagrodę Wielkanocną.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// player is not willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Co za szkoda! Biedne dzieci nie dotaną wspaniałego koszyka. Może znajdę kogoś innego i zapytam go o pomoc.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player wants to know what sweets she is referring to
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("sweets", "słodyczy"),
			null,
			ConversationStates.ATTENDING,
			"Jest wiele czekoladowych słodkości, ale chciałabym także zapełnić mój koszyk owocami.", null);
		
		// player wants to know where he can get this sweets from
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("chocolate bar", "chocolate bars", "chocolate", "tabliczka czekolady", "tabliczka", "czekolady"),
				null,
				ConversationStates.ATTENDING,
				"Tabliczka czekolady jest sprzedawana w tawernach i słyszałem też, że pare złych dzieci też je nosi. Jeśli znajdziesz je to pamiętaj, że Elizabeth w Kirdneh też uwielbia czekoladę. :)", null);
		
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("apple", "apples", "jabłko", "jabłka"),
				null,
				ConversationStates.ATTENDING,
				"Jabłka można znaleść na farmie na wschód od miasta. Są naprawdę zdrowe i możesz z nich upiec wspaniały jabłecznik. Można też dostać z ogrodów Marthy w mieście Kalavan.", null);
		
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("cherry", "cherries", "wisienka", "wisienki"),
				null,
				ConversationStates.ATTENDING,
				"Old Mother Helena w Fado sprzedaje najpiękniejsze wisienki. Są naprawdę pyszne! Mam nadzieję, że spróbowałeś wspaniałego ciasta z wiśniami upieczonego przez Gerthę będącą w ogrodach miasta Kalavan.", null);
		
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("small easter egg", "chocolate egg", "małe jajo wielkanocne", "czekoladowe jajko"),
				null,
				ConversationStates.ATTENDING,
				"Małe jajo wielkanocne jest specjalnością zajączka Wielkanocnego, który kica podczas dni Wielkanocy. Może spotkasz go na swojej drodze. :)", null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Caroline");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new AndCondition(
					new PlayerHasItemWithHimCondition("tabliczka czekolady", 5),
					new PlayerHasItemWithHimCondition("małe jajo wielkanocne",1),
					new PlayerHasItemWithHimCondition("jabłko", 5),
					new PlayerHasItemWithHimCondition("wisienka", 5))),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Jak miło! Widzę, że masz ze sobą słodycze. Są one dla koszyka Wielkanocnego, który teraz przygotowuję?",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"), 
				new NotCondition(new AndCondition(
					new PlayerHasItemWithHimCondition("tabliczka czekolady", 5),
					new PlayerHasItemWithHimCondition("małe jajo wielkanocne",1),
					new PlayerHasItemWithHimCondition("jabłko", 5),
					new PlayerHasItemWithHimCondition("wisienka", 5)))),
			ConversationStates.ATTENDING, 
			"Oh nie. Wciąż brakuję słodyczy, których potrzebuję do koszyka Wielkanocnego. Mam nadzieję, że wkrótce znajdziesz je...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("tabliczka czekolady", 5));
		reward.add(new DropItemAction("małe jajo wielkanocne", 1));
		reward.add(new DropItemAction("jabłko", 5));
		reward.add(new DropItemAction("wisienka",5));
		reward.add(new EquipItemAction("zwój ados", 5));
		reward.add(new EquipItemAction("zwój tatrzański", 2));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(50));
		
		
		
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the sweets
			// away and then saying "yes"
			
			new AndCondition(
					new PlayerHasItemWithHimCondition("tabliczka czekolady", 5),
					new PlayerHasItemWithHimCondition("małe jajo wielkanocne", 1),
					new PlayerHasItemWithHimCondition("jabłko", 5),
					new PlayerHasItemWithHimCondition("wisienka", 5)),

			ConversationStates.ATTENDING, "Jak wspaniale! Teraz mogę wypełnić koszyk dla dzieci! Będą szczęśliwe! Bardzo dziękuję za twoją pomoc i życzę wesołych Świąt Wielkanocnych! Proszę przyjmij te zwoje w dowód wdzięczności. :)",
			new MultipleActions(reward));


		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Mam nadzieję, że znajdziesz jakieś słodycze przed Wielkanocą, bo dzieci będą smutne.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Prezent Wielkanocny Dla Dzieci",
				"Caroline miła właścicielka tawerny w mieście Ados chce uszczęśliwić dzieci podczas świąt Wielkanocnych.",
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
		return "EasterGiftsForChildren";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
	
	@Override
	public String getNPCName() {
		return "Caroline";
	}
}
