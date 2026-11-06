/***************************************************************************
 *                   (C) Copyright 2007-2021 - Stendhal                    *
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

import games.stendhal.common.constants.ItemRarity;

import java.util.ArrayList;
import games.stendhal.server.core.rule.quest.StarterQuestLine;
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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

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
	private final SpeakerNPC npc = npcs.get("Fryderyk");

	private void createRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Spójrz na to pole. Wszystko do zebrania, a moja kosa już do niczego się nie nadaje. Nim przyjdą przymrozki, plony muszą być pod dachem. Zdobędziesz dla mnie #kosę?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Twoja kosa dobrze mi służy. Na razie niczego więcej nie potrzebuję, dziękuję.",
			null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję. Będę na polu. Oby tylko pogoda się utrzymała.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Szkoda. Może sąsiad pożyczy mi swoją, gdy skończy żniwa.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(ConversationStates.QUEST_OFFERED,
			"kosa",
			null,
			ConversationStates.QUEST_OFFERED,
			"Potrzebuję zwykłej kosy, z porządnym ostrzem. Nie musi być nowa, byle dało się nią pracować. Poszukasz takiej?",
			null);
	}

	private void createBringingStep() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("kosa")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Hej! Czy ta kosa jest dla mnie?", null);

		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(new PlayerHasItemWithHimCondition("kosa"))),
			ConversationStates.ATTENDING,
			"Dobrze cię widzieć. Udało ci się znaleźć kosę? Bez niej nie ruszę z robotą, a dni coraz krótsze.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("kosa"));
		reward.add(new IncreaseXPAction(2200));
		reward.add(new IncreaseKarmaAction(25));
		reward.add(new EquipItemAction("skórzany hełm", 1, false));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// make sure the player isn't cheating by putting the
		// helmet away and then saying "yes"
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("kosa"),
			ConversationStates.ATTENDING,
			"No, taką to można pracować! Zdążę zebrać plony przed chłodami. Weź ten hełm, mnie na polu nie jest potrzebny. Stasek też szukał kogoś do pomocy przy opale. Ma sklep obok mostu w centrum Zakopanego. Powiem mu, że można na tobie polegać.",
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Rozumiem, zatrzymujesz ją dla siebie. Daj znać, jeśli znajdziesz drugą.",
			null);
	}

	@Override
	public void addToWorld() {
    	fillQuestInfo(
			"Kosa Fryderyka",
			"Fryderyk potrzebuje kosy, aby zebrać plony i przygotować zapasy przed zimą.",
			false);
        questInfo.setMainQuest(StarterQuestLine.CHAPTER, 3, StarterQuestLine.FRYDERYK);
		questInfo.setRewards(new QuestRewards(2200, 0)
				.item("skórzany hełm", 1, ItemRarity.COMMON, false)
				.build());
		createRequestingStep();
		createBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.hasQuest(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Spotkałem") + " Fryderyka jakiś czas temu.");
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Mam przynieść Fryderykowi kosę.");
		if ((player.isQuestInState(QUEST_SLOT, "start")
				&& player.isEquipped("kosa"))
				|| player.isQuestCompleted(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Znalazłem") + " kosę.");
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Dostarczyłem") + " kosę Fryderykowi.");
            res.add("Fryderyk może przygotować zapasy. Wspomniał o Stasku, który potrzebuje opału na zimę.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Kosa Fryderyka";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
