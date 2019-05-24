/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class NaprawaLodzi extends AbstractQuest {
	
	private static final int ILOSC_DREWNA = 20;
	private static final int ILOSC_RYB = 3;

	private static final String QUEST_SLOT = "naprawa_lodzi";

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
			res.add("Spotkałem pewnego rybaka o imieniu Thomas, który chciałby wypłynąć w głębokie wody, ale nie może, ponieważ ma zepsutą łódź.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać rybakowi.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Obiecałem Thomasowi, że przyniąsę wystarczająco dużo drewna, aby mógł naprawić swoją łódź.");
		}
		if (questState.equals("start") && player.isEquipped("polano",
				ILOSC_DREWNA)
				|| questState.equals("done")) {
			res.add("Mam już wystarczająco dużo drewna. Teraz musiałbym zanieść je do Thomasa.");
		}
		if (questState.equals("start")
				&& !player.isEquipped("polano", ILOSC_DREWNA)) {
			res.add("Jeszcze nie zdobyłem wystarczająco dużo drewna dla rybaka.");
		}
		if (questState.equals("done")) {
			res.add("Zaniosłem drewno rybakowi i w nagrodę od niego otrzymałem rzadkie ryby!");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Thomas");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Mam nadzieje, że moja łódź się kolejny raz nie zepsuje.",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Potrzebuję pomocy w naprawie mojej łodzi, gdyż jestem rybakiem, a bez mojej łodzi nie mam jak wypłynąć w głębokie wody. Pomógłbyś mi?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Wspaniale! Przynieś mi bardzo dużo drewna, gdyż nie potrafię obliczyć ile będzie potrzebne do załatania wszystkich dziur.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT,"start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED, 
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Trudno... może inny rycerz mi pomoże.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Thomas");

		/** Complete the quest */
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("polano", ILOSC_DREWNA));
		reward.add(new EquipItemAction("błazenek", ILOSC_RYB, true));
		reward.add(new EquipItemAction("skrzydlica", ILOSC_RYB, true));
		reward.add(new EquipItemAction("pokolec", ILOSC_RYB, true));
		reward.add(new EquipItemAction("palia alpejska", ILOSC_RYB, true));
		reward.add(new EquipItemAction("dorsz", ILOSC_RYB, true));
		reward.add(new EquipItemAction("makrela", ILOSC_RYB, true));
		reward.add(new EquipItemAction("okoń", ILOSC_RYB, true));
		reward.add(new EquipItemAction("płotka", ILOSC_RYB, true));
		reward.add(new EquipItemAction("pstrąg", ILOSC_RYB, true));
		reward.add(new IncreaseXPAction(7500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new PlayerHasItemWithHimCondition("polano", ILOSC_DREWNA)),
			ConversationStates.ATTENDING,
			"Przyniosłeś drewno! Dziękuję. Teraz zajmę się naprawianiem łodzi, a oto twoja nagroda, przyjmij te egzotyczne ryby ode mnie.",
			new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				new NotCondition(new PlayerHasItemWithHimCondition("polano", ILOSC_DREWNA))),
			ConversationStates.ATTENDING,
			"Hej! Miałeś przynieść mi dużo, dużo więcej tego drewna!",
			null);
	}

	private void offerSteps() {
  		final SpeakerNPC npc = npcs.get("Thomas");

		// player returns after finishing the quest and says offer
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.OFFER_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Sprzedaję tuńczyk 5.",
			null);

		// player returns when the quest is in progress and says offer
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.OFFER_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Najpierw pomóż mi naprawić łódkę, a później będziesz mógł kupić ode mnie tuńczyka!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Naprawa łodzi",
				"Krakowski rybak potrzebuje pomocy w sprawie naprawienia łodzi.",
				false);
		step_1();
		step_2();
		offerSteps();
	}

	@Override
	public String getName() {
		return "NaprawaLodzi";
	}

	@Override
	public String getRegion() {
		return Region.KRAKOW_CITY;
	}

	@Override
	public String getNPCName() {
		return "Thomas";
	}
}