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
import java.util.Arrays;
import java.util.List;

import games.stendhal.common.constants.SoundID;
import games.stendhal.common.constants.SoundLayer;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.SoundEvent;
import games.stendhal.server.maps.Region;

/**
 * QUEST: McPegleg's IOU
 *
 * PARTICIPANTS: - a corpse in kanmararn - McPegleg
 *
 * NOTE: The corpse with contains the IOU is created in KanmararnSoldiers.java
 * Without it this quest cannot be started (so the player won't notice the
 * problem at all).
 *
 * STEPS: - find IOU in a corpse in kanmararn - bring it to McPegleg
 *
 * REWARD: - 250 money
 *
 * REPETITIONS: - None.
 */
public class McPeglegIOU extends AbstractQuest {
	private static final String QUEST_SLOT = "IOU";
	private final SpeakerNPC npc = npcs.get("McPegleg");

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("iou", "henry", "charles", "note", "notatka","karteczka"),
			new QuestNotCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING, null,
			new ChatAction() {

				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					// from all notes that the player is carrying, try to
					// find the IOU note
					final List<Item> notes = player.getAllEquipped("karteczka");
					Item iouNote = null;
					for (final Item note : notes) {
						if ("charles".equalsIgnoreCase(note.getItemData())) {
							iouNote = note;
							break;
						}
					}
					if (iouNote != null) {
						raiser.say("Skąd to wziąłeś? Nie ważne tutaj są twoje pieniądze. *westchnienie*");
						raiser.addEvent(new SoundEvent(SoundID.COMMERCE, SoundLayer.CREATURE_NOISE));
						player.drop(iouNote);
						final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
						money.setQuantity(250);
						player.equipToInventoryOnly(money);
						player.setQuest(QUEST_SLOT, "done");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					} else {
						raiser.say("Nie widzę abyś miał ważny czek z moim podpisem!");
					}
				}
			});

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("iou", "henry", "charles", "note", "notatka", "czek", "karteczka"),
			new QuestCompletedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Dostałeś już pieniądze za ten przeklęty czek!", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kupon IOU",
				"Czy znalazłeś karteczke z imieniem McPegleg? Może McPegleg wie co z tym zrobić...",
				false);
		step_1();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			// only valid if player started the kanmararn soldiers quest
			if(player.isQuestCompleted("soldier_henry")) {
				res.add("Henry dał mi karteczkę z imieniem McPegleg na jej temat.");
			}
			if (isCompleted(player)) {
				res.add("McPegleg poznała karteczkę z jej imieniem - " + player.getGenderVerb("dostałem") + " za nią 250 money!");
			}
			return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Kupon IOU";
	}

	@Override
	public int getMinLevel() {
		return 40;
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
