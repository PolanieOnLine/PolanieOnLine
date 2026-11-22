/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/** Compatibility for unfinished ingredient lists accepted before herbal orders were introduced. */
public class HerbsForJadzka extends AbstractQuest {
	public static final String QUEST_SLOT = "herbs_for_jadzka";
	private final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "arandula=5;borowik=1;jabłko=3;polano=2;pieczarka=1";
	private static final List<String> INGREDIENT_MESSAGES = Arrays.asList(
			"składniki", "skladniki", "zapasy", "ingredients");

	private void prepareBringingStep() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie! Jeśli masz dla mnie #składniki, podaj ich nazwy, a je przyjmę. Możesz też zapytać o brakujące #zapasy.",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT,
						"Potrzebuję jeszcze [items]. Podaj nazwę składnika, który chcesz mi przekazać."));

		npc.add(new ConversationStates[] {ConversationStates.ATTENDING, ConversationStates.QUESTION_2},
				ConversationPhrases.combine(ConversationPhrases.QUEST_FINISH_MESSAGES, INGREDIENT_MESSAGES),
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT,
						"Potrzebuję jeszcze [items]. Podaj nazwę składnika, który chcesz mi przekazać."));

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_2, "Podaj nazwę składnika, który chcesz mi przekazać, na przykład: #arandula.",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wspaniale! Znów mogę leczyć czcigodnych wojowników bez opłat! Dziękuję. Przyjmij ode mnie podarek za swoją pracę."),
				new IncreaseXPAction(500),
				new IncreaseKarmaAction(10),
				new EquipItemAction("antidotum", 7));

		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> entry : items.entrySet()) {
			String itemName = entry.getKey();

			String singular = Grammar.singular(itemName);
			List<String> sl = new ArrayList<String>();
			sl.add(itemName);

			// handle the porcino/porcini singular/plural case with item name "borowik"
			if (!singular.equals(itemName)) {
				sl.add(singular);
			}
			// also allow to understand the misspelled "porcinis"
			if (itemName.equals("borowik")) {
				sl.add("borowik");
			}

			npc.add(new ConversationStates[] {ConversationStates.ATTENDING, ConversationStates.QUESTION_2},
					sl, new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"Dziękuję. Brakuje mi jeszcze [items]. Jeśli masz coś z tej listy, podaj nazwę.",
							"Te składniki mam już od ciebie. Zapytaj o #składniki, żeby sprawdzić, czego jeszcze brakuje.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dobrze i daj mi znać jeśli mogę ci kiedyś #pomóc..", 
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ok, i daj mi znać jeśli mogę #pomóc w czymkolwiek innym.", null);


	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zioła dla Jadźki",
				"Gaździna Jadźka, uzdrowicielka z Zakopanego, potrzebuje składników do eliksirów i lekarstw. Pomóż jej uzupełnić zapasy.",
				false);
		prepareBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Gaździna Jadźka poprosiła mnie o zebranie składników, aby pomóc jej nadal leczyć innych.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomóc Jadźce. Będzie musiała znaleźć kogoś innego.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Gaździnie " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
			res.add("Aby oddać składniki, wystarczy po przywitaniu podać ich nazwy. Jadźka przyjmuje je także partiami.");
		} else {
			res.add(player.getGenderVerb("Pomogłem") + " Gaździnie i ona może teraz dalej uzdrawiać.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Zioła dla Jadźki";
	}

	@Override
	public int getMinLevel() {
		return 3;
	}
	
	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return "Gaździna Jadźka";
	}
	/** Existing ingredient lists remain finishable; new players receive the herbal order. */
	@Override
	public boolean isVisibleOnQuestStatus(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !"rejected".equals(player.getQuest(QUEST_SLOT));
	}

	@Override
	public games.stendhal.common.QuestMarker getNPCQuestMarker(final Player player, final SpeakerNPC speaker) {
		if (!getNPCName().equals(speaker.getName()) || !HerbalOrdersForJadzka.hasLegacyOrder(player)) return games.stendhal.common.QuestMarker.NONE;
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(player.getQuest(QUEST_SLOT));
		for (Map.Entry<String, Integer> item : items.entrySet()) {
			if (!player.isEquipped(item.getKey(), item.getValue())) return games.stendhal.common.QuestMarker.IN_PROGRESS;
		}
		return games.stendhal.common.QuestMarker.READY;
	}

}
