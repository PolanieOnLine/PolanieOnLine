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
// Based on LearnAboutOrbs.
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Wawel brama
 * 
 
 * </ul>
 */
public class WawelBrama extends AbstractQuest {

	private static final String QUEST_SLOT = "brama_wawel";

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
		res.add("Stanąłem przed bramą Wawelu.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("done")) {
			res.add("Mam prawo wejść na teren Zamku i stanąć przed królem.");
		}
		return res;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Strażnik");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Na teren Wawelu mogą #wejść tylko osoby wyższego stanu.", null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, 
			"Chciałbyś #wejść i pokłonić się naszemu Królowi.", null);

		// player interested in wejscie
		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("enter", "wejść", "wejście", "wejścia"), 
			new LevelGreaterThanCondition(99),
			ConversationStates.QUESTION_1,
			"Jeżeli chcesz przejść to powiedz #tak, nie zapomnij pokłonić się naszemu Królowi",
			null);

		// player interested in wejscie but level < 99
		npc.add(ConversationStates.QUEST_OFFERED,
			Arrays.asList("enter", "wejść", "wejście", "wejścia"), 
			new NotCondition(new LevelGreaterThanCondition(99)),
			ConversationStates.ATTENDING,
			"Zdobądź sławę jeżeli chcesz pokłonić się naszemu Królowi, to nie miejsce dla chłopów i kmieci.",
			null);

		// player wants reminder on Use
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("enter", "wejść ", "wejście", "wejścia"),
			null,
			ConversationStates.ATTENDING,
			"Możesz przejść", null);

		// player got message from wejscie
		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Jesteś godny aby iść i pokłonić się naszemu Królowi",
			new MultipleActions(new IncreaseXPAction(5000), new SetQuestAction(QUEST_SLOT, "done")));

		// player didn't get message, try again
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES,
			null, ConversationStates.QUESTION_1,
			"Cóż musisz stanąć obok. Podejdź bliżej i powiedz #wejście.", null);

	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Straż przed Bramą na Wawel.",
			"Strażnik Sprawdza kto może wejść na teren zamku. Wpuszcza od 100 poziomu.",
			false);

		step1();
	}

	@Override
	public String getName() {
		return "WawelBrama";
	}
	@Override
	public String getNPCName() {
		return "Strażnik";
	}
}
