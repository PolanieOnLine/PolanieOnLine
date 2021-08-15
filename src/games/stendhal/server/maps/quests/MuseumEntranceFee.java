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

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * QUEST: Museum Entrance Fee
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Iker</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Talk to Iker and pay the fee</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> Admittance to the museum</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> At any time</li>
 * </ul>
 * 
 * @author kribbel
 */
public class MuseumEntranceFee extends AbstractQuest {
	private static final String QUEST_SLOT = "museum_entrance_fee";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Iker");

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("visit", "wstęp", "wizyta", "zwiedzić", "wejście", "wejść"),
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Otrzymasz dostęp po opłaceniu wpisowego w wysokości 10 money. Czy chcesz teraz zapłacić?",
				null);

		// Player asks to visit while already paid
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("visit", "wizyta", "zwiedzić", "wejście", "wejść"),
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Zapłaciłeś już za wstęp. Wejdź i ciesz się wizytą.",
				null);

		// Player wants to visit, but has not enough money
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new NotCondition(new PlayerHasItemWithHimCondition("money", 10)),
				ConversationStates.ATTENDING,
				"Przepraszam, nie mogę zaoferować zniżki. Musisz mi dać 10 money.",
				null);
		
		// Player wants to visit, and has enough money. Entering the portal will set quest slot to null and so end (and delete) it. Look at deniran.xml
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new PlayerHasItemWithHimCondition("money", 10),
				ConversationStates.ATTENDING,
				"Dziękuję, wejdź i życzę miłej wizyty.",
				new MultipleActions(new DropItemAction("money", 10),new SetQuestAction(QUEST_SLOT, "start"))); 

		// Player doesn't want to visit
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Wstyd! Brakuje ci pewnych doświadczeń swojego życia.",
				null);
	}

	@Override
	public void addToWorld() {
		step_1();
/*		fillQuestInfo(
				"Opłata Za Wstęp Do Muzeum", null,
				//"Iker, chłopak z Deniran.",
				true);
*/
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getName() {
		return "Opłata Za Wstęp Do Muzeum";
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return true;
	}

	@Override
	public String getNPCName() {
		return "Iker";
	}
}
