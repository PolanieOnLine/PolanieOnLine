/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
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

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.AddItemToCollectionAction;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartItemsCollectionWithLimitAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Restock the Flower Shop
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Seremela, the elf girl who watches over Nalwor's flower shop</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Seremela asks you to bring a variety of flowers to restock the flower shop and 15 bottles of water to maintain them</li>
 * <li>Bring the requested amounts water and each flower type to Seremela</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>1000 XP</li>
 * <li>25 karma</li>
 * <li>5 nalwor city scrolls</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Once every 3 days</li>
 * </ul>
 *
 * @author AntumDeluge
 *
 */
public class RestockFlowerShop extends AbstractQuest {
	public static final String QUEST_SLOT = "restock_flowershop";

	// Different types of flowers needed in quest
	private static final List<String> flowerTypes = Arrays.asList(
			"stokrotki", "lilia", "bratek", "róża", "bielikrasa");

	private static final int MAX_FLOWERS = flowerTypes.size() * 10;

	private static final int REQ_WATER = 15;

	// Time player must wait to repeat quest (3 days)
	private static final int WAIT_TIME = 3 * MathHelper.MINUTES_IN_ONE_DAY;

	// Quest NPC
	private final SpeakerNPC npc = npcs.get("Seremela");

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		String npcName = npc.getName();
		if (player.isQuestInState(QUEST_SLOT, 0, "rejected")) {
			res.add("Kwiatki wywołują u mnie kichanie.");
		} else if (!player.isQuestInState(QUEST_SLOT, 0, "done")) {
			String questState = player.getQuest(QUEST_SLOT);
			res.add("Zaoferowałem pomoc " + npcName + " w uzupełnieniu zapasów kwiaciarni.");

			final ItemCollection remaining = new ItemCollection();
			remaining.addFromQuestStateString(questState);

			// Check to avoid ArrayIndexOutOfBoundsException
			if (!remaining.isEmpty()) {
				String requestedFlowers = "Wciąż potrzebuję przynieść następujące kwiatki: " + Grammar.enumerateCollection(remaining.toStringList()) + ".";
				res.add(requestedFlowers);
			}
		} else {
            if (isRepeatable(player)) {
                res.add("Minęło trochę czasu od ostatniej pomocy " + npcName + ". Może znów potrzebuje mojej pomocy.");
            } else {
                res.add("Teraz" + npcName + " ma odpowiedni zapas kwiatów.");
            }
		}

		return res;
	}


	private void setupBasicResponses() {

		List<List<String>> keywords = Arrays.asList(
				Arrays.asList("flower", "kwiat"),
				ConversationPhrases.HELP_MESSAGES);
		List<String> responses = Arrays.asList(
				"Czyż kwiatki nie są piękne?",
				"Hmmmm, Nie sądzę, aby było coś w czym mógłbym pomóc.");

		for (int i = 0; i < responses.size(); i++) {
			npc.add(ConversationStates.ANY,
					keywords.get(i),
					new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
					ConversationStates.ATTENDING,
					responses.get(i),
					null);
		}
	}

	private void setupActiveQuestResponses() {

		// Player asks to be reminded of remaining flowers required
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flower", "remind", "what", "item", "list", "something", "kwiat", "przypomnij", "co", "przedmiot", "lista", "coś"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż potrzebuję [items]. Czy przyniosłeś mi?"));

        npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("flower", "remind", "what", "item", "list", "something", "kwiat", "przypomnij", "co", "przedmiot", "lista", "coś"),
                new QuestActiveCondition(QUEST_SLOT),
                ConversationStates.QUESTION_1,
                null,
                new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż potrzebuję [items]. Czy przyniosłeś mi?"));

        // Player asks to be reminded of remaining flowers required
        npc.add(ConversationStates.QUESTION_1,
				Arrays.asList("flower", "remind", "what", "item", "list", "kwiat", "przypomnij", "co", "przedmiot", "lista"),
                new QuestActiveCondition(QUEST_SLOT),
                ConversationStates.QUESTION_1,
                null,
                new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wciąż potrzebuję [items]. Czy przyniosłeś mi?"));

		List<List<String>> keywords = Arrays.asList(
				Arrays.asList("daisy", "bunch of daisies", "bunches of daisies", "lilia", "pansy", "stokrotki", "bukiet stokrotek", "bukiety stokreotek", "Lilia", "bratek"),
				Arrays.asList("rose", "róża"),
				Arrays.asList("zantedeschia", "bielikrasa"),
				Arrays.asList("water", "bottle of water", "woda", "butelka wody"),
				Arrays.asList("who", "where", "kto", "gdzie"),
				Arrays.asList("jenny"),
				Arrays.asList("fleur"),
				Arrays.asList("flask", "butelka"),
				ConversationPhrases.HELP_MESSAGES);
		List<String> responses = Arrays.asList(
						"#Jenny ma nasiona tego typu kwiatków.",
						"#Fleur zawsze posiada najpiękniejsze róże.",
						"Bielikrasa to mój ulubiony kwiat. Niektórzy nazywają je arum lub lilie choć nimi nie są. Zapytaj #Jenny czy nie ma jakiś bulw.",
						"Potrzebuję wody, aby moje #kwiaty były świeże. Musisz znaleść źródło wody i wziąść jej trochę do #butelek. Może jest toś kto sprzedaje wodę.",
						"#Jenny dużo wie o kwiatach. Może też porozmawiasz z #Fleur.",
						"Możesz spotkać Jenny w pobliżu młyna obok Semos. Tam gdzie mieli się zboże na mąkę.",
						"Fleur pracuje na targu w Kirdneh.",
						"Zapytaj barmanki w Semos.",
						"Mogę ci #przypomnieć, które #kwiaty potrzebuję. także pomogę ci wskazać miejsce #gdzie trochę ich występuje.");

		for (int f = 0; f < responses.size(); f++) {
			npc.add(ConversationStates.ANY,
					keywords.get(f),
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.ATTENDING,
					responses.get(f),
					null);
		}
	}

	private void prepareRequestingStep() {

		// Player requests quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
						new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
				ConversationStates.QUEST_OFFERED,
				"Kwiaciarnia ma problem z kwiatami. Pomożesz mi uzupełnić zapasy?",
				null);

		// Player requests quest after started
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Wciąż nie kupiłeś mi #kwiatów, o które prosiłam.",
				null);

		// Player requests quest before wait period ended
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, WAIT_TIME, "Kwiaty, które kupiłeś szybko się sprzedają. Może znów będę potrzebowała twojej pomocy"));

		// Player accepts quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						new IncreaseKarmaAction(5.0),
						new StartItemsCollectionWithLimitAction(QUEST_SLOT, 0, flowerTypes, MAX_FLOWERS),
						new AddItemToCollectionAction(QUEST_SLOT, "woda", REQ_WATER),
						new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Wspaniale! To jest to czego potrzebuję: [items]."))
		);

		// Player rejects quest
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Przykro mi to słyszeć.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}


	private void prepareBringingStep() {
		List<String> requestedItems = new ArrayList<>(flowerTypes);
		requestedItems.add("woda");

		ChatAction rewardAction = new MultipleActions(
				new IncreaseXPAction(1000),
				new IncreaseKarmaAction(25.0),
				new EquipItemAction("zwój nalwor", 5),
				new SetQuestAction(QUEST_SLOT, "done"),
				new SetQuestToTimeStampAction(QUEST_SLOT, 1),
				new SayTextAction("Bardzo dziękuję! Teraz mogę zrealizować wszystkie zamówienia."));

		/* add triggers for the item names */
		for (String item : requestedItems) {
			npc.add(ConversationStates.QUESTION_1,
					item,
					new QuestActiveCondition(QUEST_SLOT),
					ConversationStates.QUESTION_1,
					null,
					new CollectRequestedItemsAction(
							item,
							QUEST_SLOT,
							"Dziękuję! Co jeszcze przyniosłeś?",
							"Nie potrzebuję już tego więcej.",
							rewardAction,
							ConversationStates.IDLE
							));
		}

		// NPC asks if player brought items
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Czy przyniosłeś #coś dla sklepu?",
				null);

		// Player confirms brought flowers
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Co przyniosłeś?",
				null);

		// Player didn't bring flowers
		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Nie przestawaj wąchać róż# Zamówienia wracają. Mogę ci #przypomnieć co masz przynieść.",
				null);

		// Player offers item that wasn't requested
		npc.add(ConversationStates.QUESTION_1,
				"",
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.QUESTION_1,
				"Nie sądzę, aby to dobrze wyglądało w sklepie.",
				null);

		// Player says "bye" or "no" while listing flowers
		List<String> endDiscussionPhrases = new ArrayList<>(ConversationPhrases.NO_MESSAGES);
		endDiscussionPhrases.addAll(ConversationPhrases.GOODBYE_MESSAGES);

		npc.add(ConversationStates.QUESTION_1,
				endDiscussionPhrases,
				null,
				ConversationStates.IDLE,
				"Proszę wróć, gdy znajdziesz jakieś kwiaty.",
				null);
	}

	@Override
	public boolean isRepeatable(Player player) {
		return new AndCondition(
				new NotCondition(new QuestActiveCondition(QUEST_SLOT)),
				new TimePassedCondition(QUEST_SLOT, 1, WAIT_TIME)).fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "RestockFlowerShop";
	}

	public String getTitle() {
		return "Odnowienie Zapasów Kwiaciarni";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.NALWOR_CITY;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				getTitle(),
				getNPCName() + " potrzebuje odnowić zapasy kwiaciarni w mieście Nalwor.",
				true);
		setupBasicResponses();
		setupActiveQuestResponses();
		prepareRequestingStep();
		prepareBringingStep();
	}
}
