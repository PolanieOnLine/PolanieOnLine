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
package games.stendhal.server.maps.ados.church;

import static games.stendhal.server.maps.quests.AGrandfathersWish.QUEST_SLOT;
import static games.stendhal.server.maps.quests.AGrandfathersWish.canRequestHolyWater;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;

/**
 * Priest to make holy water for An Old Man's Wish quest.
 */
public class PriestNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		zone.add(buildNPC());
	}

	private SpeakerNPC buildNPC() {
		final SpeakerNPC priest = new SpeakerNPC("Ojciec Calenus");
		priest.setEntityClass("priest2npc");

		priest.addGreeting("Witaj moje dziecko. W czym mogę Ci #pomóc?");
		priest.addGoodbye("Idź w pokoju.");
		priest.addJob("Jestem zarządcą tego świętego domu.");
		priest.addHelp("Jeśli potrzebujesz błogosławieństwa, mogę zaoferować"
				+ " ci trochę #'wody święconej'.");
		priest.addOffer("Znajdź wewnętrzny spokój. Tylko wtedy zrozumiesz wartość życia.");

		priest.add(
			ConversationStates.ATTENDING,
			Arrays.asList("holy water", "woda święcona", "wody święconej"),
			new AndCondition(
				new NotCondition(canRequestHolyWater()),
				new QuestNotInStateCondition(QUEST_SLOT, 2, "holy_water:bring_items")),
			ConversationStates.ATTENDING,
			"Woda święcona jest konsekrowana, aby pomóc cierpiącym"
				+ " i potrzebującym błogosławieństwa.",
			null);

		final List<Node> nodes = new LinkedList<Node>();
		nodes.add(new Node(16, 4));
		nodes.add(new Node(24, 4));
		priest.setPathAndPosition(new FixedPath(nodes, true));
		priest.setCollisionAction(CollisionAction.STOP);

		return priest;
	}
}