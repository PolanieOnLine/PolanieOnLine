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
package games.stendhal.server.maps.semos.wizardstower;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Elana, the life wizard of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.WizardElanaPlainQuest
 */
public class WhiteLifeSorceressNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildElana(zone);
	}

	private void buildElana(final StendhalRPZone zone) {
		final SpeakerNPC elana = new SpeakerNPC("Elana") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(39, 18));
				nodes.add(new Node(39, 17));
				nodes.add(new Node(35, 17));
				nodes.add(new Node(35, 15));
				nodes.add(new Node(35, 20));
				nodes.add(new Node(33, 20));
				nodes.add(new Node(33, 25));
				nodes.add(new Node(32, 25));
				nodes.add(new Node(32, 27));
				nodes.add(new Node(32, 25));
				nodes.add(new Node(31, 25));
				nodes.add(new Node(32, 25));
				nodes.add(new Node(32, 27));
				nodes.add(new Node(32, 25));
				nodes.add(new Node(33, 25));
				nodes.add(new Node(33, 21));
				nodes.add(new Node(30, 21));
				nodes.add(new Node(40, 21));
				nodes.add(new Node(40, 29));
				nodes.add(new Node(39, 29));
				nodes.add(new Node(39, 27));
				nodes.add(new Node(36, 27));
				nodes.add(new Node(38, 27));
				nodes.add(new Node(38, 25));
				nodes.add(new Node(40, 25));
				nodes.add(new Node(40, 20));
				nodes.add(new Node(39, 20));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Miło Cię widzieć!");
				addHelp("Proszę, wybacz mi, ale jestem bardzo zajęta. Biali magowie w kręgu czarodziejów są pod moją opieką.");
				addJob("Nazywam się Elana. Reprezentuję białych magów z #Lavitae w kręgu czarodziejów.");
				addOffer("Proszę, wybacz mi, ale jestem bardzo zajęta. Biali magowie w kręgu czarodziejów są pod moją opieką.");
				addQuest("Magia kiedyś opanuje świat. Narazie to dopiero jej początki... Ale jestem przez to strasznie zjęta. Na dodatek muszę dbać o białych magów z #Lavitae. Gdy będę potrzebować pomocy, odezwę się w sprawie zadania.");
				addReply("Lavitae", "Lavitae to szkoła białej magii. Jest niedaleko, znajduje się w pobliżu Ados.");
				addGoodbye("Żegnaj!");

			} //remaining behaviour defined in maps.quests.WizardElanaPlainQuest
		};

		elana.setDescription("Widzisz Elanę, białą wróżkę życia.");
		elana.setEntityClass("whitesorceressnpc");
		elana.setPosition(40, 18);
		elana.initHP(100);
		zone.add(elana);
	}
}
