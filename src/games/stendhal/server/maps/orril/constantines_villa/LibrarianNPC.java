/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package games.stendhal.server.maps.orril.constantines_villa;

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
 * Builds a npc in Constantines Villa (name:Cameron) who is a librarian
 * 
 * @author storyteller (idea) and Vanessa Julius (implemented)
 *
 */

public class LibrarianNPC implements ZoneConfigurator {
    
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Cameron") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(11, 17));
				nodes.add(new Node(11, 7));
                nodes.add(new Node(18, 7));
                nodes.add(new Node(18, 17));  
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			
			//Greeting and goodbye message in quest code TheMissingBooks.java
			
			protected void createDialog() {
				addHelp("Hmm... Chyba jesteś jednym z przyjaciół Constantines. Mogę coś ci powiedzieć o ile podejdziesz #bliżej..."); 
				addReply(Arrays.asList("closer", "bliżej"), "Teraz lepiej! Powienieneś odwiedzić moją przyjaciółkę Imorgen jest gdzieś w lesie Fado... Jej babcia jest wciąż chora i może potrzebować #pomocy...");
				addReply(Arrays.asList("help", "pomocy"), "Ostatnio, gdy ją widziałem martwiła się o swoją babcię. Może w zamian za pomoc coś ci da...");
				addJob("Jestem bibliotekarzem #'Constantines'! Niestety zgubiłem parę cennych książek... Mam nadzieje, że nigdy się o tym nie dowie!");
				addReply(Arrays.asList("Constantine", "Constantines"),
		        "Jest moim szefem i właścicielem tej ogromnej willi. Nie widziałem go od pewnego czasu. Wygląda na to, że jest zajęty lub jest na urlopie...");
				addReply(Arrays.asList("vacation", "urlop", "urlopie"), "Nie pytaj gdzie jest. Jestem tutaj ostatnim, który może coś wiedzieć... Po za tym książki są dla mnie ważniejsze.");
				addOffer("Przepraszam, ale nie mam nic do zaoferowania.");
				addGoodbye("Czytamy...ehm...do zobaczenia wkrótce!");
				
			}
		};

		npc.setDescription("Cameron wygląda trochę na szalone, ale nie martw się:  Jest tylko bibliotekarze z paroma problemami związanymi z jego najważniejszym skrabem.");
		npc.setEntityClass("librarianconstantinenpc");
		npc.setPosition(11, 17);
		npc.initHP(100);
		zone.add(npc);
	}
}
