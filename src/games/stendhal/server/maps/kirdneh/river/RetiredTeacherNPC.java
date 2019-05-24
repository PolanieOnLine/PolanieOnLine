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
package games.stendhal.server.maps.kirdneh.river;

import games.stendhal.common.Direction;
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
 * Builds a npc in the house at Kirdneh River (name:Ortiv Milquetoast) who is a coward retired teacher
 * 
 * @author Vanessa Julius 
 *
 */
public class RetiredTeacherNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ortiv Milquetoast") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 28));
				nodes.add(new Node(27, 28));
                nodes.add(new Node(27, 19));
                nodes.add(new Node(26, 19));  
                nodes.add(new Node(26, 16));
                nodes.add(new Node(28, 16)); 
                nodes.add(new Node(28, 11)); 
                nodes.add(new Node(24, 11));
                nodes.add(new Node(24, 20));
                nodes.add(new Node(27, 20));
                nodes.add(new Node(27, 26));
                nodes.add(new Node(14, 26));
                nodes.add(new Node(14, 25));
                nodes.add(new Node(13, 25));
                nodes.add(new Node(13, 20));
                nodes.add(new Node(14, 20));
                nodes.add(new Node(14, 14));
                nodes.add(new Node(4, 14));
                nodes.add(new Node(4, 6));
                nodes.add(new Node(10, 6));
                nodes.add(new Node(10, 3));
                nodes.add(new Node(6, 3));
                nodes.add(new Node(6, 6));
                nodes.add(new Node(4, 6));
                nodes.add(new Node(4, 22));
                nodes.add(new Node(13, 22));
                nodes.add(new Node(13, 27));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Oh obcy znalazł mój ukryty dom, witam!");
				addHelp("Nigdy nie ładuj się w kłopoty z #zabójcami, bo są silniejsi niż ty!");
				addReply(Arrays.asList("assassins", "zabójcami"), "Przyjdą po Ciebie tak czy inaczej! Kilkoro z nich czeka na dole pod moją piwnicą!");
				addJob("Byłem nauczycielem alchemi, ale kilku z moich #studentów stało się brzydkimi bandytami i zabójcami...");
				addReply(Arrays.asList("students", "studentów"), "Nie wiem co się teraz dzieje w Faiumoni, ale przez cały dzień siedzę w tym bezpicznym domku...");
				addOffer("Przykro mi, ale nic nie mogę zaoferować Tobie... Mam głównie problemy w mojej piwnicy!");
				addQuest("Chcę przygotować miksturę trzymającą zabójców i bandytów w mojej komórce.");
				addGoodbye("Trzymaj się i często mnie odwiedzaj. Boję się być sam!");
			}
		};

		npc.setDescription("Oto Ortiv Milquetoast. Otacza go jakąś aura nauczyciela. Wygląda na wystaszonego i zdenerwowanego.");
		npc.setEntityClass("retiredteachernpc");
		npc.setPosition(15, 28);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
