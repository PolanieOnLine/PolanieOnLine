/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
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
import java.util.List;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Jailed Dwarf
 *
 * PARTICIPANTS: - Hunel, the guard of the Dwarf Kingdom's Prison
 *
 * STEPS:
 * <ul>
 * <li> You see Hunel locked in the cell. </li>
 * <li> Gget the key by killing the duergar king. </li>
 * <li> Speak to Hunel when you have the key. </li>
 * <li> Hunel wants to stay in, he is afraid. </li>
 * <li> You can then sell chaos equipment to Hunel. </li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 8,000 XP </li>
 * <li> some karma (20) </li>
 * <li> everlasting place to sell chaos equipment </li>
 * </ul>
 *
 * REPETITIONS: - None.
 */
public class JailedDwarf extends AbstractQuest {
	private static final String QUEST_SLOT = "jailed_dwarf";
	private final SpeakerNPC npc = npcs.get("Hunel");

	private void step_1() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Cześć. Jak widzisz wciąż jestem zbyt zdenerwowany, aby wyjść ...",
				null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new NotCondition(new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn"))),
				ConversationStates.IDLE,
				"Pomocy! Duergars najechały na więzienie i zamknęły mnie tutaj! Powinienem być Strażnikiem! To chaos.",
				new SetQuestAction(QUEST_SLOT, "start"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition(QUEST_SLOT),
						new PlayerHasItemWithHimCondition("klucz do więzienia Kanmararn")),
				ConversationStates.ATTENDING,
				"Masz klucz, aby mnie wypuścić! *mamrocze*  Errrr ... nie wygląda, aby było tam dla mnie bezpiecznie ... Chyba zostanę tutaj ... może ktoś #zaoferuje mi dobry ekwipunek ... ",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "done"),
						 			 new IncreaseXPAction(8000),
						 			 new IncreaseKarmaAction(20)));
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Uwięziony Krasnal",
				"Na dole Kanmararn znajdziesz przrażonego, uwięzionego w celi krasnala czekającego na odwiedziny. Powinien być strażnikiem, ale duergary napadły na więzienie. Może potrzebować zbroi do ucieczki.",
				false);
		step_1();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("Muszę zdobyć klucz aby uwolnić Hunela.");
			if (isCompleted(player)) {
				res.add(player.getGenderVerb("Zabiłem") + " króla Duergars i zdobyłem klucz do celi Hunela. Teraz jest zbyt przestraszony aby wyjść. Kupi każdą ilość zbroi. Biedny Hunel.");
			}
			return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Uwięziony Krasnal";
	}

	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_DUNGEONS;
	}
}
