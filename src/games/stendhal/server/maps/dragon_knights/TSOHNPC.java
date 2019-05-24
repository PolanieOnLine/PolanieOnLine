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
package games.stendhal.server.maps.dragon_knights;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPCFactory;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;


/**
 * An old hero (original name: Hayunn Naratha) who players meet when they enter the semos guard house.
 *
 * @see games.stendhal.server.maps.quests.BeerForHayunn
 * @see games.stendhal.server.maps.quests.MeetHayunn
 */
public class TSOHNPC extends SpeakerNPCFactory {

	@Override
	public void createDialog(final SpeakerNPC npc) {
		// A little trick to make NPC remember if it has met
		// player before and react accordingly
		// NPC_name quest doesn't exist anywhere else neither is
		// used for any other purpose

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction("meet_tsoh", "start"));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestNotStartedCondition("meet_tsoh"),
				ConversationStates.ATTENDING,
				"Witaj! Widzę, że chcesz lepiej poznać klan The Soldiers Of Honor?",
				new MultipleActions(actions));

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Witaj! Założę się, że chcesz wstąpić do naszego klanu The Soldiers Of Honor.",
				null);

		npc.addHelp("Mogę ci opowiedzieć o TSOH, a jeżeli chcesz wiedzieć jeszcze więcej to zajrzyj na stronę naszego klanu http://www.thesolidersofhonor.pl.tl/.");
		npc.addJob("Pracuje dla elitarnego klanu The Soldiers Of Honor. Zajmuje się promocją mojego klanu.");
		npc.addGoodbye("Dowidzenia. Zawsze będziesz mile widziany i pomyśl nad wstąpieniem do naszego klanu.");
		// further behaviour is defined in quests.
	}
}
