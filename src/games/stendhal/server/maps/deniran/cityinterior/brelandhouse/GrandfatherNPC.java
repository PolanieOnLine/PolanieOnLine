/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.brelandhouse;

import static games.stendhal.server.maps.quests.AGrandfathersWish.QUEST_SLOT;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestRegisteredCondition;

public class GrandfatherNPC implements ZoneConfigurator  {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC elias = new SpeakerNPC("Elias Breland");

		elias.addGreeting("Witaj młody.");
		elias.addGoodbye("Do widzenia.");
		elias.addHelp("Życzę sobie, abym potrzebował pomocy. Niestety, ale nie.");
		elias.addJob("Jestem starym człowiekiem, który lubi opiekować się moim ogródkiem.");
		elias.addOffer("Nie mam nic do zaoferowania.");
		elias.add(ConversationStates.ANY,
				ConversationPhrases.QUEST_MESSAGES,
				new NotCondition(new QuestRegisteredCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"Jest pewna sprawa, co na mnie ciąży. Ale nie jestem"
					+ " gotowy na pomoc. Może mógłbyś wrócić później.",
				null);

		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(11, 16));
		nodes.add(new Node(3, 16));
		nodes.add(new Node(3, 3));
		nodes.add(new Node(11, 3));
		nodes.add(new Node(11, 6));
		nodes.add(new Node(27, 6));
		nodes.add(new Node(11, 6));
		elias.setPathAndPosition(new FixedPath(nodes, true));
		elias.setCollisionAction(CollisionAction.STOP);

		elias.setEntityClass("oldman2npc");
		elias.setDescription("Oto " + elias.getName() + ", który przechadza się po swoim domu.");

		zone.add(elias);
	}
}