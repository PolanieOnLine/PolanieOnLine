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
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
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
 * QUEST: Armor for Dagobert
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Dagobert, the consultant at the bank of Semos</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Dagobert asks you to find a leather cuirass.</li>
 * <li>You get a leather cuirass, e.g. by killing a cyclops.</li>
 * <li>Dagobert sees your leather cuirass and asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>100 gold</li>
 * <li>Karma: 10</li>
 * <li>Access to vault</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ArmorForDagobert extends AbstractQuest {

	private static final String QUEST_SLOT = "armor_dagobert";

	

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Dagobert. Jest konsultantem w banku w Semos.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Poprosił mnie o znalezienie skórzanego kirysu, ale odrzuciłem jego proźbę.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add("Przyrzekłem, że znajdę dla niego skórzany kirys ponieważ został okradziony.");
		}
		if ("start".equals(questState) && (player.isEquipped("skórzany kirys") || player.isEquipped("skórzany kirys z naramiennikami")) || "done".equals(questState)) {
			res.add("Znalazłem skórzany kirys i zabiorę go do Dagoberta.");
		}
		if ("done".equals(questState)) {
			res.add("Wziąłem skórzany kirys do Dagoberta. Podziękował i dał mi nagrodę.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Dagobert");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Obawiam się, że zostałem okradziony. Nie mam żadnej ochrony. Czy mógłbyś mi pomóc?",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Dziękuję za zbroję, ale nie mam więcej zadań dla Ciebie.",
			null);

		// player is willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Raz miałem #'skórzany kirys', ale został zniszczony podczas ostatniej kradzieży. Jeżeli znajdziesz nowy to dam Tobie nagrodę.",
			new SetQuestAction(QUEST_SLOT, "start"));

		// player is not willing to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Cóż będę musiał się ukryć..",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		// player wants to know what a leather cuirass is
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("skórzany kirys", "leather", "cuirass"),
			null,
			ConversationStates.ATTENDING,
			"Skórzany kirys jest tradycyjną zbroją cyklopów. Kilka cyklopów mieszka w podziemiach głęboko pod miastem.",
			null);
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Dagobert");

		// player returns while quest is still active
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"), 
				new OrCondition(
					new PlayerHasItemWithHimCondition("skórzany kirys"),
					new PlayerHasItemWithHimCondition("skórzany kirys z naramiennikami"))),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			"Przepraszam! Zauważyłem, że masz przy sobie skórzany kirys. Jest dla mnie?",
			null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
				new QuestInStateCondition(QUEST_SLOT, "start"),  
				new NotCondition(new OrCondition(
					new PlayerHasItemWithHimCondition("skórzany kirys"),
					new PlayerHasItemWithHimCondition("skórzany kirys z naramiennikami")))),
			ConversationStates.ATTENDING, 
			"Na szczęście nie zostałem obrabowany, gdy Ciebie nie było. Chciałbym się odwdzięczyć za zbroję. Poza tym może potrzebujesz #pomocy?",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 100));
		reward.add(new IncreaseXPAction(500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(15));

		final List<ChatAction> reward1 = new LinkedList<ChatAction>(reward);
		reward1.add(new DropItemAction("skórzany kirys"));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the armor
			// away and then saying "yes"
			new PlayerHasItemWithHimCondition("skórzany kirys"), 
			ConversationStates.ATTENDING, "Oh, jestem Ci tak wdzięczny, proszę weź to złoto, które znalazłem...ehm..gdzieś.",
			new MultipleActions(reward1));

		final List<ChatAction> reward2 = new LinkedList<ChatAction>(reward);
		reward2.add(new DropItemAction("skórzany kirys z naramiennikami"));
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the armor
			// away and then saying "yes"
			new AndCondition(
				new NotCondition(new PlayerHasItemWithHimCondition("skórzany kirys")),
				new PlayerHasItemWithHimCondition("skórzany kirys z naramiennikami")), 
			ConversationStates.ATTENDING, "Oh, jestem Ci tak wdzięczny, proszę weź to złoto, które znalazłem...ehm..gdzieś. Teraz gdy udowodniłeś, że jesteś zaufanym klientem, możesz mieć dostęp do prywatnej skrzyni bankowej kiedy tylko zechcesz.",
			new MultipleActions(reward2));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Cóż mam nadzieję, że znajdziesz i dasz mi inny nim zostanę ponownie obrabowany.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zbroja dla Dagoberta",
				"Dagobert konsultant w banku w Semos poprosił mnie o znalezienie dla niego skórzanego kirysu.",
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
		return "ArmorForDagobert";
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
		return "Dagobert";
	}
}
