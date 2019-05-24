/* $Id$ */
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
package games.stendhal.server.maps.semos.jail;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Jail;
import games.stendhal.server.entity.player.Player;

/**
 * The prison guard (original name: Marcus) who's patrolling along the cells.
 * 
 * @author hendrik
 */
public class GuardNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Marcus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9,7));
				nodes.add(new Node(21,7));
				nodes.add(new Node(21,8));
				nodes.add(new Node(9,8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Pozdrawiam! Jakiej #pomocy oczekujesz?");
		
				add(ConversationStates.ATTENDING,
						ConversationPhrases.JOB_MESSAGES,
						new NotInJailCondition(),
				        ConversationStates.ATTENDING,
		        		"Jestem strażnikiem więziennym.",
				        null);
		
				add(ConversationStates.ATTENDING,
						ConversationPhrases.JOB_MESSAGES,
						new InJailCondition(),
				        ConversationStates.ATTENDING,
		        		"Jestem strażnikiem więziennym. Zostałeś tutaj zamknięty za złe zachowanie.",
				        null);
		
				add(ConversationStates.ATTENDING,
		        		ConversationPhrases.HELP_MESSAGES,
				        new InJailCondition(),
				        ConversationStates.ATTENDING,
		        		"Zaczekaj na administratora, aby zdecydował co z tobą zrobić. Przy okazji - stąd nie uciekniesz.",
				        null);
		
				add(ConversationStates.ATTENDING,
						ConversationPhrases.HELP_MESSAGES,
						new NotInJailCondition(),
				        ConversationStates.ATTENDING,
		        		"Wiesz, że możesz poznać lokalne prawo przez wpisanie /rules? Ci przestępcy w celach na pewno nie znają.",
				        null);
		
				addGoodbye();
			}};
			npc.setPosition(9, 7);
			npc.setDescription("Oto jeden ze strażników więziennych Semos zwany Marcus.");
			npc.setEntityClass("youngsoldiernpc");
			zone.add(npc);		
	}

	/**
	 * Is the player speaking to us in jail?
	 */
	public static class InJailCondition implements ChatCondition {

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return Jail.isInJail(player);
		}
	}

	/**
	 * Is the player speaking to us not in jail?
	 */
	public static class NotInJailCondition implements ChatCondition {

		@Override
		public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
			return !Jail.isInJail(player);
		}
	}
}
