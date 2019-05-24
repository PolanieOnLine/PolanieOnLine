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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Erastus, the archmage of the Wizards Tower
 *
 * see games.stendhal.server.maps.quests.ArchmageErastusQuest
 */
public class BlueArchmageNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildErastus(zone);
	}

	private void buildErastus(final StendhalRPZone zone) {
		final SpeakerNPC erastus = new SpeakerNPC("Erastus") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(21, 37));
				nodes.add(new Node(13, 37));
				nodes.add(new Node(13, 32));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(24, 25));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(33, 32));
				nodes.add(new Node(32, 32));
				nodes.add(new Node(32, 33));
				nodes.add(new Node(32, 32));
				nodes.add(new Node(22, 32));
				nodes.add(new Node(22, 25));
				nodes.add(new Node(24, 25));
				nodes.add(new Node(20, 25));
				nodes.add(new Node(20, 32));
				nodes.add(new Node(8, 32));
				nodes.add(new Node(11, 32));
				nodes.add(new Node(11, 35));
				nodes.add(new Node(13, 35));
				nodes.add(new Node(13, 37));
				nodes.add(new Node(22, 37));
				nodes.add(new Node(22, 40));
				nodes.add(new Node(26, 40));
				nodes.add(new Node(26, 36));
				nodes.add(new Node(26, 37));
				nodes.add(new Node(28, 37));
				nodes.add(new Node(25, 37));
				nodes.add(new Node(25, 40));
				nodes.add(new Node(22, 40));
				nodes.add(new Node(22, 37));
				nodes.add(new Node(21, 37));
				nodes.add(new Node(21, 36));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Bądź pozdrowiony, odkrywco!");
				addHelp("Każdy czar ma swoją #antymagię by zachować równowagę. Pamiętaj o tym podróżując po świecie.");
				addJob("Nazwyam się Erastus.  Moim zadaniem jest zjednoczyć i poprowadzić krąg czarodziejów.");
				addOffer("Nie mam nic do zaoferowania. Ale myślę, że Zekiel i czarodzieje z pewnością Ci pomogą.");
				addReply(Arrays.asList("opposite", "antymagię"), "Jeśli chcesz walczyć z ogniem, musisz użyć wody. " +
						"Ale są wielcy magowie, którzy potrafią połączyć żywioły i stworzyć czar tak potężny, jakiego ten świat jeszcze nie widział. Arcymagowie.");
				addQuest("Tak, mam zadanie dla Ciebie, ale najpierw musisz dowiedzieć się więcej o magii i kręgu czarodziejów. Dam Ci je, gdy ukończysz wszystkie zadania, jakie inni magowie Ci zlecą.");
				addGoodbye("Do zobaczenia, odkrywco!");

			} //remaining behaviour defined in maps.quests.ArchmageErastusQuest
		};

		erastus.setDescription("Widzisz Erastusa, wielkiego mistrza wszystkich rodzajów magii.");
		erastus.setEntityClass("blueoldwizardnpc");
		erastus.setPosition(21, 36);
		erastus.initHP(100);
		zone.add(erastus);
	}
}
