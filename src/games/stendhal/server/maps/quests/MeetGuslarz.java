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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.behaviour.impl.TeleporterBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Meet Guslarz anywhere around the World.
 * <p>
 *
 * PARTICIPANTS:<ul><li> Guslarz</ul>
 *
 * STEPS: <ul><li> Find Guslarz <li> Say hi <li> Get reward </ul>
 *
 * REWARD: <ul><li> a skrzynka which can be opened to obtain a random good reward: food,
 * money, potions, items, etc...</ul>
 *
 * REPETITIONS: None
 */
public class MeetGuslarz extends AbstractQuest {
	// quest slot changed ready for 2012
	private static final String QUEST_SLOT = "meet_guslarz_13";

	/** the Guslarz NPC. */
	protected SpeakerNPC guslarz;

	private StendhalRPZone zone;
	
	private TeleporterBehaviour teleporterBehaviour;
	
	/** the name of the quest */
	public static final String QUEST_NAME = "MeetGuslarz";

	// The default is 100 (30 seconds) so make ours half this
	private static final int TIME_OUT = 50;

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	private SpeakerNPC createguslarz() {
		guslarz = new SpeakerNPC("Guślarz") {
			@Override
			protected void createPath() {
				// npc does not move
				setPath(null);
			}

			@Override
			protected void createDialog() {
				// Greet players who have a basket but go straight back to idle to give others a chance
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(super.getName()),
								new QuestCompletedCondition(QUEST_SLOT)),
						ConversationStates.IDLE,
						"Witaj ponownie! Co za dużo to nie zdrowo!", null);

				final List<ChatAction> reward = new LinkedList<ChatAction>();
				reward.add(new EquipItemAction("skrzynka"));
				reward.add(new SetQuestAction(QUEST_SLOT, "done"));

				// Give unmet players a basket
				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(super.getName()),
							new QuestNotCompletedCondition(QUEST_SLOT)),
					ConversationStates.IDLE,
					"Witaj! Co nieszczęsny robisz w tych stronach? Ale skoro już mnie spotkałeś mam coś dla ciebie.",
					new MultipleActions(reward));
			}
		};
		
		guslarz.setEntityClass("npcguslarz");
		guslarz.initHP(100);
		// times out twice as fast as normal NPCs
		guslarz.setPlayerChatTimeout(TIME_OUT); 
		guslarz.setDescription("Oto Guślarz odprawiający gusła, aby przywołać z zaświatów wielkie moce!.");
		// start in int_admin_playground
		zone = SingletonRepository.getRPWorld().getZone("int_admin_playground");
		guslarz.setPosition(17, 13);
		zone.add(guslarz);

		return guslarz;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Guślarza",
				"Odprawia gusła, aby przywołać z zaświatów wielkie moce...",
				false);

		if (System.getProperty("stendhal.guslarz") != null) {
			createguslarz();
			teleporterBehaviour = new TeleporterBehaviour(guslarz, null, "0", "Strzeż się! Mocniejsze potwory zewsząd atakują!", false); 
		}
	}
		
	/**
	 * removes a quest from the world.
	 *
	 * @return true, if the quest could be removed; false otherwise.
	 */
	@Override
	public boolean removeFromWorld() {
		removeNPC("Guślarz");
		// remove the turn notifiers left from the TeleporterBehaviour
		SingletonRepository.getTurnNotifier().dontNotify(teleporterBehaviour);
		return true;
	}
	
	/**
	 * removes an NPC from the world and NPC list
	 *
	 * @param name name of NPC
	 */
	private void removeNPC(String name) {
		SpeakerNPC npc = NPCList.get().get(name);
		if (npc == null) {
			return;
		}
		npc.getZone().remove(npc);
	}

	@Override
	public String getName() {
		return QUEST_NAME;
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
	public String getNPCName() {
		return "Guślarz";
	}

}
