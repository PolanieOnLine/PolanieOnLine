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
// Based on Herbs For Carmen 

package games.stendhal.server.maps.quests;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * QUEST: Herbs For Jadzka
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Carmen (the healer in Zakopane)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Jadzka introduces herself and asks for some items to help her heal people.</li>
 * <li>You collect the items.</li>
 * <li>Jadzka sees yours items, asks for them then thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>500 XP</li>
 * <li>4 antidote</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class HerbsForJadzka extends AbstractQuest {

	public static final String QUEST_SLOT = "herbs_for_jadzka";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "arandula=5;borowik=1;jabłko=3;polano=2;pieczarka=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Gaździna Jadźka poprosiła mnie o zebranie składników, aby pomóc jej nadal leczyć innych.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę, pomóc Gaździnie. Myślę, że ona znajdzie kogoś, kto jej pomoże.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("Wciąż muszę przynieść Gaździnie " + Grammar.enumerateCollection(missingItems.toStringList()) + ".");
		} else {
			res.add("Pomogłem Gaździnie i ona może teraz dalej uzdrawiać.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1, 
			"Hej ty! Tak, do ciebie mówię! Znasz mnie?", null);

		npc.add(ConversationStates.ATTENDING, 
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT,"rejected"),
			ConversationStates.QUEST_OFFERED, 
			"Hej, chcesz mi jakoś pomóc?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Świetnie, wiesz co robię. Moje zapasy ziół #leczniczych się wyczerpują.",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Jestem Gaździna Jadźka. Mogę uleczyć cię za darmo, dopóki twoje wymagania nie będą zbyt duże. Wielu wojowników prosi o moją pomoc. Dlatego moje zapasy ziół #leczniczych powoli się kończą i potrzebuję je uzupełnić.",
			null);

		npc.add(
			ConversationStates.ATTENDING,(Arrays.asList
			("ingredients", "leczniczych", "lecznicze")),
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Tak wielu wojowników prosi mnie o pomoc. Do leczenia potrzebne jest wiele składników a moje zapasy są na wyczerpaniu. Możesz mi przynieść brakujące zioła?",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, NEEDED_ITEMS, 5.0),
								new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Jak miło! Proszę przynieś mi: [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Ech... Cóż, niedobrze... Taka twoja wola. Pamiętaj jednak, że wkrótce zacznę odmawiać potrzebującym leczenia. Będę musiała im powiedzieć, że to przez TWOJE lenistwo.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.add(
			ConversationStates.ATTENDING,
			"jabłko",
			null,
			ConversationStates.ATTENDING,
			"jabłko mają wiele witamin. Rosną na wschód od Semos, ale ich pełen talerz jest także w Matinternecie.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"polano",
			null,
			ConversationStates.ATTENDING,
			"polano to świetny materiał. Można go wykorzystać na wiele sposobów. Znajdź drzewo w lesie i je zetnij.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("pieczarka","borowik"),
			null,
			ConversationStates.ATTENDING,
			"Na własne oczy widziałam całe polany grzybów w Zakopanem.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"arandula",
			null,
			ConversationStates.ATTENDING,
			"Na północ od Semos, niedaleko młodniaka rośnie ponoć zioło arandula. Oto rycina, na której zobaczysz jak wygląda.",
			new ExamineChatAction("arandula.png", "Rysunek Jadźki", "Arandula"));

	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Gaździna Jadźka");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Witaj ponownie. Czy przyniosłeś mi #zapasy ziół leczniczych?",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.ATTENDING, "zapasy", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz coś ze sobą?"));

		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUESTION_2, 
			null, new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Potrzebuję [items]. Czy masz to?"));		

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "Dobrze, a co jeszcze masz?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("Wspaniale! Znów mogę leczyć czcigodnych wojowników bez opłat! Dziękuję. Przyjmij ode mnie podarek za swoją pracę."),
				new IncreaseXPAction(500),
				new IncreaseKarmaAction(10),
				new EquipItemAction("antidotum", 7)
				);

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

			npc.add(ConversationStates.QUESTION_2, sl, null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							itemName, QUEST_SLOT,
							"Dobra, masz coś jeszcze?"," " +
							Grammar.quantityplnoun(entry.getValue(), itemName) + " już przyniosłeś dla mnie, ale dziękuję i tak.",
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

    /* says quest and quest can't be started nor is active*/
		npc.add(ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.ATTENDING, 
				"Nic nie potrzebuję teraz, dziękuję.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zioła dla Jadźki",
				"Zakopiański uzdrowiciel Gaździna Jadźka poszukuje składników do eliksirów i lekarstw. Czy moższe jej przynieść jej zioła, które potrzebuje?",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "HerbsForJadzka";
	}

	public String getTitle() {
		
		return "Zioła dla Jadzki";
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
}
