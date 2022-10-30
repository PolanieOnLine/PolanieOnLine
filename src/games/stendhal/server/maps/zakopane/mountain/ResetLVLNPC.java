/***************************************************************************
 *                 (C) Copyright 2018-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.mountain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;

/**
 * @author KarajuSs 00:34:21 11-07-2018
 */
public class ResetLVLNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Yerena") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 18));
				nodes.add(new Node(16, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(new GreetingMatchesNameCondition(getName()),
						new QuestInStateCondition("reborn_extra_reward3", "start")),
					ConversationStates.INFORMATION_1,
					"Dobrze, że jesteś... Zauważyłam w szczelinie pewne sztylety, mogą Ci się przydać w walce ze złem!",
					new MultipleActions(
						new EquipItemAction("sztylet leworęczny", 1, true),
						new EquipItemAction("sztylet praworęczny", 1, true),
						new SetQuestAction("reborn_extra_reward3", "done")));

				addGreeting("Witam Cię dzielny wojaku.");
				addJob("Zajmuję się resetowaniem mocy u wojowników. Powiedz mi tylko #'zadanie'.");
				addOffer("Mogę ci zaoferować zadanie, które po wykonaniu udowodnisz, czy rzeczywiście chcesz zresetować swój poziom.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Yerena. Smok, który włada czasem.");
		npc.setEntityClass("dragon3npc");
		npc.setGender("F");
		npc.setPosition(16, 18);
		zone.add(npc);
	}
}
