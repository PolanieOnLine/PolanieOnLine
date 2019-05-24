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
// Based on HatForMonogenes.
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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Scythe For Fryderyk
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
 * <li>2200 XP</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS: - None.
 */
public class ScytheForFryderyk extends AbstractQuest {
	private static final String QUEST_SLOT = "scythe_fryderyk";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add("Spotkałem Fryderyka jakiś czas temu.");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Mam przynieść Fryderykowi kosę.");
		if ((player.isQuestInState(QUEST_SLOT, "start")
				&& player.isEquipped("kosa"))
				|| player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Znalazłem kosę.");
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add("Dostarczyłem kosę Fryderykowi.");
		}
		return res;
	}

	private void createRequestingStep() {
		final SpeakerNPC fryderyk = npcs.get("Fryderyk");

		fryderyk.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Czy mógłbyś przynieść mi #kosę? Potrzebuję ją do żniw.",
			null);

		fryderyk.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dziękuję za ofertę dobry człowieku, ale ta kosa wystarczy mi na pięć wiosen i nie potrzebuję ich więcej.",
			null);

		fryderyk.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję przyjacielu. Będę tutaj czekał na twój powrót!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		fryderyk.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Jestem pewien, że masz lepsze rzeczy do zrobienia. Będę stał tutaj, a moje plony zmarnieją... *sniff*",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		fryderyk.add(
			ConversationStates.QUEST_OFFERED,
			"kosa",
			null,
			ConversationStates.QUEST_OFFERED,
			"Nie wiesz co to jest kosa?! Jedyne narzędzie, którym można skosić plony na polu. Składa się z metalowego ostrza i drewnianego kija, a wszystko przypomina literę L. Zrobisz to dla mnie?",
			null);
	}

	private void createBringingStep() {
		final SpeakerNPC fryderyk = npcs.get("Fryderyk");

		fryderyk.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(fryderyk.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("kosa")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Hej! Czy ta kosa jest dla mnie?", null);

		fryderyk.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(fryderyk.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("kosa"))),
			ConversationStates.ATTENDING,
			"Hej mój przyjacielu. Pamiętasz o kosie, którą mi obiecałeś. Pytałem się o nią wcześniej? Moje plony czekają na mnie...",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kosa"));
		reward.add(new IncreaseXPAction(2200));
		reward.add(new IncreaseKarmaAction(25));
		reward.add(new EquipItemAction("skórzany hełm"));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// make sure the player isn't cheating by putting the
		// helmet away and then saying "yes"
		fryderyk.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("kosa"),
			ConversationStates.ATTENDING,
			"Niech Cię pobłogosławię mój dobry przyjacielu! Teraz będę wstanie zebrać moje plony.",
			new MultipleActions(reward));

		fryderyk.add(
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
				"Kosa dla Fryderyka",
				"Fryderyk potrzebuje do żniw kosę, dostarcz mu ją.",
				false);
		createRequestingStep();
		createBringingStep();
	}

	@Override
	public String getName() {
		return "ScytheForFryderyk";
	}
	@Override
	public String getNPCName() {
		return "Fryderyk";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
