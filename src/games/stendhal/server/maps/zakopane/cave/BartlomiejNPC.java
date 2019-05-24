/* $Id: BartlomiejNPC.java,v 1.6 2010/09/19 02:28:01 Legolas Exp $ */
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
 // based on ../games/stendhal/server/maps/ados/abandonedkeep/OrcKillGiantDwarfNPC.java
package games.stendhal.server.maps.zakopane.cave;

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
 * Builds the orc kill diant dwarf NPC.
 *
 * @author Teiv
 */
public class BartlomiejNPC implements ZoneConfigurator {

	//
	// ZoneConfigurator
	//

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}


	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC bartlomiejNPC = new SpeakerNPC("Bartłomiej") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 59));
				nodes.add(new Node(11, 62));
				nodes.add(new Node(12, 62));
				nodes.add(new Node(12, 59));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj. Jestem kowalem pracującym dla zbójników.");
				addJob("Może potrzebujesz #'złoty róg'. Mogę porozmawiać z moim #bratem .");
				addReply(Arrays.asList("brat", "bratem"), "On jeden zna tajemnice wyrabiania złotego rogu. W zamian oczekuję, że przyniesiesz mi kilka #piórek powiedz tylko #zadanie.");
				addReply(Arrays.asList("feathers", "piórko", "piórek"), "Po zabiciu każdego anioła znajdziesz w jego zwłokach piórko");
				addHelp("Znam kogoś kto wyrabia złote rogi w zamian chcę kolekcję piórek.");
				addGoodbye("Życzę powodzenia i szczęścia na wyprawach.");
			}
		};

		bartlomiejNPC.setDescription("Oto Kowal Bartłomiej. Znalazł pracę u herszta zbójników!");
		bartlomiejNPC.setEntityClass("beardmannpc");
		bartlomiejNPC.setPosition(11, 59);
		bartlomiejNPC.initHP(1000);
		zone.add(bartlomiejNPC);
	}
}
