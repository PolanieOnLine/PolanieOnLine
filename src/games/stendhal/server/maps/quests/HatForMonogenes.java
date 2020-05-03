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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Hat For Monogenes 
 * 
 * PARTICIPANTS: 
 * <ul>
 * <li>Monogenes, an old man in Semos city.</li>
 * </ul>
 * 
 * STEPS:
 * <ul> 
 * <li> Monogenes asks you to buy a hat for him.</li>
 * <li> Xin Blanca sells you a leather helmet.</li>
 * <li> Monogenes sees your leather helmet and asks for it and then thanks you.</li>
 * </ul>
 * 
 * REWARD: 
 * <ul>
 * <li>300 XP</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS: - None.
 */
public class HatForMonogenes extends AbstractQuest {
	private static final String QUEST_SLOT = "hat_monogenes";


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add("Spotkałem Monogenes na wiosnę w wiosce Semos");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Muszę znaleźć jakiś skórzany kapelusz, który trzymałby ciepło.");
		if (player.isQuestInState(QUEST_SLOT, "start") 
				&& player.isEquipped("skórzany hełm")
				|| player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Znalazłem kapelusz.");
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Dałem kapelusz Monogenesowi i nagrodził mnie 10 pd.");
		}
		return res;
	}

	private void createRequestingStep() {
		final SpeakerNPC monogenes = npcs.get("Monogenes");

		monogenes.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Czy mógłbyś przynieść mi #kapelusz do zakrycia mojej łysinki? Brrrrr! Dni w Semos robią się coraz chłodniejsze...",
			null);

		monogenes.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za ofertę dobry człowieku, ale ten kapelusz wystarczy mi na pięć zim i nie potrzebuję ich więcej.",
			null);

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję przyjacielu. Będę tutaj czekał na twój powrót!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Jestem pewien, że masz lepsze rzeczy do zrobienia. Będę stał tutaj i zamarzał na śmierć... *sniff*",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		monogenes.add(
			ConversationStates.QUEST_OFFERED,
			"hat",
			null,
			ConversationStates.QUEST_OFFERED,
			"Nie wiesz co to jest kapelusz?! Wszystko co może zakryć moją świecącą głowę jak na przykład skóra. Zrobisz to dla mnie?",
			null);
	}

	private void createBringingStep() {
		final SpeakerNPC monogenes = npcs.get("Monogenes");

		monogenes.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(monogenes.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("skórzany hełm")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Hej! Czy ten skórzany hełm jest dla mnie?", null);

		monogenes.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(monogenes.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("skórzany hełm"))),
			ConversationStates.ATTENDING,
			"Hej mój przyjacielu. Pamiętasz ten skórzany hełm, który mi obiecałeś. Pytałem się o niego wcześniej? Tutaj wciąż jest zimno...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("skórzany hełm"));
		reward.add(new IncreaseXPAction(300));
		reward.add(new IncreaseKarmaAction(10));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// make sure the player isn't cheating by putting the
		// helmet away and then saying "yes"
		monogenes.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("skórzany hełm"),
			ConversationStates.ATTENDING,
			"Niech Cię pobłogosławię mój dobry przyjacielu! Teraz mojej głowie będzie wygodnie i ciepło.",
			new MultipleActions(reward));

		monogenes.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ktoś miał dzisiaj dużo szczęścia... *Apsik*.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kapelusz dla Monogenesa",
				"Monogenes potrzebuje kapelusza, który trzymałby ciepło podczas zimy.",
				false);
		createRequestingStep();
		createBringingStep();
	}
	
	@Override
	public String getName() {
		return "HatForMonogenes";
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Monogenes";
	}
}
