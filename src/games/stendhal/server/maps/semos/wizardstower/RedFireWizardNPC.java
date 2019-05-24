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
 * Malleus, the fire wizard of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.WizardMalleusPlainQuest
 */
public class RedFireWizardNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMalleus(zone);
	}

	private void buildMalleus(final StendhalRPZone zone) {
		final SpeakerNPC malleus = new SpeakerNPC("Malleus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 43));
				nodes.add(new Node(5, 42));
				nodes.add(new Node(11, 42));
				nodes.add(new Node(9, 42));
				nodes.add(new Node(9, 33));
				nodes.add(new Node(10, 33));
				nodes.add(new Node(9, 33));
				nodes.add(new Node(9, 37));
				nodes.add(new Node(11, 37));
				nodes.add(new Node(9, 37));
				nodes.add(new Node(9, 33));
				nodes.add(new Node(10, 33));
				nodes.add(new Node(9, 33));
				nodes.add(new Node(9, 42));
				nodes.add(new Node(3, 42));
				nodes.add(new Node(3, 41));
				nodes.add(new Node(2, 41));
				nodes.add(new Node(2, 36));
				nodes.add(new Node(6, 36));
				nodes.add(new Node(4, 36));
				nodes.add(new Node(4, 33));
				nodes.add(new Node(2, 33));
				nodes.add(new Node(2, 41));
				nodes.add(new Node(3, 41));
				nodes.add(new Node(3, 43));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj!");
				addHelp("Przepraszam, ale jestem bardzo zajęty. Czarnoksiężnicy podczas spotkania kręgu czarodziejów są pod moją opieką.");
				addJob("Jestem Malleus. Reprezentuję czarnoksiężników z #Yrafear w kręgu czarodziejów.");
				addOffer("Przepraszam, ale jestem bardzo zajęty. Czarnoksiężnicy podczas spotkania kręgu czarodziejów są pod moją opieką.");
				addQuest("Magia stawia pierwsze kroki na tym świecie. Jestem zajęty, czarnoksiężnicy z #Yrafear eż są obecni na spotkaniu kręgu czarodziejów. Gdy będę miał jakieś zadanie dla Ciebie, powiem Ci o tym.");
				addReply("Yrafear", "Yrafear, szkoła magii ognia, leży głęboko pod ziemiami Faiumoni.");
				addGoodbye("Żegnaj!");

			} //remaining behaviour defined in maps.quests.WizardMalleusPlainQuest
		};

		malleus.setDescription("Oto Malleus, mistrz magii destrukcji.");
		malleus.setEntityClass("reddarkwizardnpc");
		malleus.setPosition(2, 43);
		malleus.initHP(100);
		zone.add(malleus);
	}
}
