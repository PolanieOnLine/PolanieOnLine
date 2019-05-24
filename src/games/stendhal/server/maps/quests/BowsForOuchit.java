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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Bows for Ouchit
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Ouchit, ranged items seller</li>
 * <li> Karl, farmer</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Ouchit asks for wood for his bows and arrows. </li>
 * <li> Puchit asks you to fetch horse hair from Karl also.</li>
 * <li> Return and you get some equipment as reward.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 1500 XP<li>
 * <li> Scale armor</li>
 * <li> Chain legs</li>
 * <li> Karma: 30<li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> None.</li>
 * </ul>
 */

public class BowsForOuchit extends AbstractQuest {

	public static final String QUEST_SLOT = "bows_ouchit";

	public void prepareQuestStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");

		/*
		 * Add a reply on the trigger phrase "quest" to Ouchit
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Czy jesteś tu, aby mi pomóc?",
				null);

		/*
		 * Player is interested in helping, so explain the quest.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Dobrze! Sprzedaję łuki i strzały. Byłoby super gdybyś mógł " +
				"przynieść 10 sztuk #polan. Możesz mi przynieść drewno?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Ehhh... dowidzenia.",
				null);

		/*
		 * Player agreed to get wood, so tell them what they'll need to say
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wspaniale! Jak wrócisz z nim powiedz #polano.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "wood", 3.0));

		/*
		 * Player asks about wood.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				"polano",
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Drzewo jest potrzebne do różnych rzeczy, znajdziesz go...  " +
				"najlepiej spytaj Drwala koło Zakopanego. Przyniesiesz mi 10 sztuk?",
				null);

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				"Dobrze, możesz wrócić później, jeżeli chcesz. Do zobaczenia.",
				null);
	}

	public void bringWoodStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");
		
		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"wood"),
				ConversationStates.ATTENDING,
				"Czekam na ciebie, aż mi przyniesiesz 10 sztuk drzewa.",
				null);
		
		/*
		 * Player asks about wood, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				"polano",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"wood"),
								 new NotCondition (new PlayerHasItemWithHimCondition("polano",10))),
				ConversationStates.ATTENDING,
				"Drzewo jest surowcem potrzebnym do wielu rzeczy. Jestem pewien, " +
				"że znajdziesz w lesie. Lepiej porozmawiaj z drwalem koło Zakopanego." +
				"I nie zapomnij powiedzieć #'polano', gdy wrócisz z nim.",
				null);

		/*
		 * Player asks about wood, and has collected some - take it and
ask for horse hair.
		 */
		npc.add(ConversationStates.ATTENDING,
				"polano",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"wood"),
								new PlayerHasItemWithHimCondition("polano",10)),
				ConversationStates.ATTENDING,
				"Bardzo dobrze, teraz mogę zrobić nowe strzały. Ale do łuku, który robię potrzebuję " +
				"cięciwę. Proszę idź do #Karl. Wiem, że posiada konie " +
				"powiedz moje imię, a da ci końskiego włosa. Z niego zrobię cięciwę.",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "hair", 2.0), new DropItemAction("polano", 10)));

		/*
		 * For simplicity, respond to 'Karl' at any time.
		 */
		npc.addReply("Karl", "Karl jest rolnikiem mieszkającym na wschód od Semos. Posiada sporo zwierząt na swojej farmie.");
	}

	public void getHairStep() {

		/*
		 * get a reference to the Karl NPC
		 */
		SpeakerNPC npc = npcs.get("Karl");

		npc.add(ConversationStates.ATTENDING,
				"Ouchit",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("koński włos",1))),
				ConversationStates.ATTENDING,
				"Witam, witam! Ouchit potrzebuje więcej włosia z moich koni? " +
				"Nie ma problemu. Weź to i przekaż Ouchitowi serdeczne pozdrowienia ode mnie.",
				new EquipItemAction("koński włos"));

	}

	public void bringHairStep() {

		/*
		 * get a reference to the Ouchit NPC
		 */
		SpeakerNPC npc = npcs.get("Ouchit");

		/*
		 * Player asks about quest, remind what they're doing
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT,"hair"),
				ConversationStates.ATTENDING,
				"Czekam tu na ciebie, abyś przyniósł mi #'włosie końskie'.",
				null);
		
		/*
		 * Player asks about horse hair, but hasn't collected any - remind them.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "horse", "włosie końskie", "włosie", "końskie", "koński włos"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new NotCondition (new PlayerHasItemWithHimCondition("koński włos"))),
				ConversationStates.ATTENDING,
				"Włos koński stosowany może być do cięciwy, a dostaniesz go od #Karl.",
				null);

		/*
		 * These actions are part of the reward
		 */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("zbroja łuskowa", 1, true));
		reward.add(new EquipItemAction("spodnie nabijane ćwiekami", 1, true));
		reward.add(new IncreaseXPAction(1500));
		reward.add(new DropItemAction("koński włos"));
		reward.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "done", 25.0));
		
		/*
		 * Player asks about horse hair, and has collected some - take it
and ask for horse hair.
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("hair", "horse", "horse hairs", "włosie końskie", "włosie", "końskie"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"hair"),
								new PlayerHasItemWithHimCondition("koński włos")),
				ConversationStates.ATTENDING,
				"Wspaniale, masz włos koński. Dziękuję bardzo. Karl jest bardzo miły. Proszę, " +
				"zostawił tutaj ten... Mi nie jest to potrzebne, a tobie może się przydać.",
				new MultipleActions(reward));
		
		/*
		 * Player asks about quest, and it is finished
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję za twoją pomoc. Jeżeli mogę zaoferować coś po prostu zapytaj o ofertę.",
				null);
		
	}

	@Override
	public void addToWorld() {
		prepareQuestStep();
		bringWoodStep();
		getHairStep();
		bringHairStep();
		fillQuestInfo(
				"Łuki dla Ouchita",
				"Ouchit zajmuje się robieniem łuków! Potrzebuje pomocy od Ciebie i Karla.",
				false);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Ouchit poprosił mnie o pomoc w celu uzupełnienia jego zapasów w łuki i strzały.");
		if (player.isQuestInState(QUEST_SLOT, "wood", "hair", "done")) {
			res.add("Najpierw muszę pszynieść Ouchit 10 sztuk drewna.");
		}
		if(player.isEquipped("polano", 10) && "polano".equals(questState)) {
			res.add("Pszyniosłem drewno dla Ouchit");
		}
		if(player.isQuestInState(QUEST_SLOT, "hair", "done")) {
			res.add("Muszę zdobyć trochę włosów konia, które Ouchit potrzebuje do cięciwy. Powiedziano mi, że rolnik Karl mi pomoże.");
		}
		if(player.isEquipped("koński włos") && "hair".equals(questState) || isCompleted(player)) {
			res.add("Karl był miły i dał mi włos koński.");
		}
		if (isCompleted(player)) {
			res.add("Ouchit dał mi nowy ekwipunek i podziękował za pomoc mu.");
		}
		return res;
	}
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "BowsForOuchit";
	}
	
	@Override
	public int getMinLevel() {
		return 0;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ouchit";
	}
}