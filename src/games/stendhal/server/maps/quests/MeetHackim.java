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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Speak with Hackim
 *
 * PARTICIPANTS: - Hackim Easso, the blacksmith's assistant
 *
 * STEPS: - Talk to Hackim to activate the quest and keep speaking with Hackim.
 *
 * REWARD: - 350 XP - 75 gold coins
 *
 * REPETITIONS: - As much as wanted, but you only get the reward once.
 */
public class MeetHackim extends AbstractQuest {

	private static final String QUEST_SLOT = "meet_hackim";
	List<String> yesTrigger;


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
		res.add("Rozmawiałem z Hackim, bardzo miły pomocnik kowala Semos.");
		if (isCompleted(player)) {
			res.add("Wysłuchałem jego przydatnych informacji na temat Xin Blanca, faceta z tawerny Semos.");
		}
		return res;
	}

	private void prepareHackim() {

		final SpeakerNPC npc = npcs.get("Hackim Easso");

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"Nie powinniśmy sprzedawać broni łowcom przygód w dzisiejszych czasach. Pracujemy nad produkcją ekwipunku dla Imperialnych Wojsk Deniran. Walczą oni z mrocznymi legionami Blordroughtów na południu. (Ciii... możesz podejść bliżej, abym mógł Tobie coś powiedzieć?)",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.INFORMATION_2,
			"*szept* Idź do oberży i porozmawiaj z człowiekiem zwącym się #Xin #Blanca... kupuje i sprzedaje ekwipunek, który mógłby Cię zainteresować. Chcesz usłyszeć więcej?",
			null);

		npc.add(
			ConversationStates.INFORMATION_2,
			yesTrigger,
			null,
			ConversationStates.INFORMATION_3,
			"Zapytaj go co ma do #zaoferowania i zobacz co możesz #kupić i #sprzedać. Na przykład jeżeli masz studded shield, którego nie chcesz to możesz sprzedać mówiąc #'sprzedam tarcza ćwiekowa'.",
			null);

		final String answer = "Zgadnij kto zaopatruje Xin Blanca w broń, którą sprzedaje? Cóż to ja! Ponieważ muszę unikać wzbudzania podejrzeń przemycam tylko małe bronie. Jeżeli potrzebujesz czegoś mocniejszego to musisz zejść do podziemi i zabić jakiegoś potwora.\n";

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("money", 75));
		reward.add(new IncreaseXPAction(350));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.INFORMATION_3,
				Arrays.asList("buy", "sell", "offer", "sell studded shield", "kupię", "sprzedam", "oferta", "sprzedam tarcza ćwiekowa"),
				new QuestNotCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				answer + "Jeżeli ktoś Cię zapyta to nie znasz mnie!",
				new MultipleActions(reward));

		npc.add(ConversationStates.INFORMATION_3,
				Arrays.asList("buy", "sell", "offer", "sell studded shield", "kupię", "sprzedam", "oferta", "sprzedam tarcza ćwiekowa"),
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.IDLE,
				answer + "Gdzie dostałeś tę broń? W sklepie z zabawkami?",
				null);

		npc.add(new ConversationStates[] {
					ConversationStates.ATTENDING,
					ConversationStates.INFORMATION_1,
					ConversationStates.INFORMATION_2,
					ConversationStates.INFORMATION_3 },
				ConversationPhrases.NO_MESSAGES,
    			null,
    			ConversationStates.ATTENDING,
    			"Pamiętaj wszystkie bronie są policzone i lepiej je zostawić w spokoju.",
    			null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Hackima Easso",
				"Asystent kowala Hackim Easso posiada pewne użyteczne informacje.",
				false);
 	  yesTrigger = new LinkedList<String>(ConversationPhrases.YES_MESSAGES);
		yesTrigger.add("Xin Blanca");
		yesTrigger.add("Blanca");
		yesTrigger.add("Xin");
		prepareHackim();
	}

	@Override
	public String getName() {
		return "MeetHackim";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Hackim Easso";
	}
}
