/***************************************************************************
 *                   (C) Copyright 2003-2019 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.plains;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
//import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

/**
 * Provides A man hoeing the farm ground near the Mill north of Semos
 * @see games.stendhal.server.maps.quests.AdMemoriaInPortfolio
 *
 * Jingo Radish offers a quest to unlock the portfolio container
 * Involves Hazen in Kirdneh
 *
 * @author omero
 *
 */
public class HoeingManNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
			// TODO: NPC Hazen does not exist, and should probably not be named so similar to Haizen.
			/*
			final String[] rambles = {
				"... This is so relaxing... Hoeing and seeding... Hoeing and seeding... ",
				"... This is so relaxing... #Hazen... #Kirdneh... Ohh my poor #memory... ",
				"... This is so relaxing... #Kirdneh... #Hazen... Ahh my fainting  #memory...",
				"... This is so relaxing... Hoeing and seeding... #Hazen... #Memory... #Kirdneh...",
				"... This is so relaxing... Hoeing and seeding... #Kirdneh... Oh poor #memory... #Hazen...",
				"... This is so relaxing... Hoeing and seeding... #Hazen... #Kirdneh...  Where is my #memory gone... ",
	            "... This is so relaxing... Hoeing and seeding... If only I could remember... What happened to #Hazen... Where is #Kirdneh... Ah my poor #memory..."
			};
			//1,2,3,4,5 minutes
			
			new MonologueBehaviour(buildNPC(zone),rambles, 1);
			*/
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
			final SpeakerNPC npc = new SpeakerNPC("Jingo Radish") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(48, 62));
				nodes.add(new Node(43, 76));
				nodes.add(new Node(43, 62));

				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {
				addGreeting("Co za spotkanie, drogi wędrowcze!");
				addJob("Widzisz? Ciągle pozbywam się chwastów przy pomocy #motyki, lecz one odrastają za każdym razem...");
				addHelp("Nie spiesz się! Przejdź się po okolicy. Na północy znajduje się młyn, na wschodzie zaś - tereny rolnicze. Przyjemna i bogata wieś, gdzie można zacząć polowanie na jedzenie!");
				addReply(Arrays.asList("hoe", "motyka"),
	                    "Oh cóż nie ma nic specjalnego w mojej motyce... Jeżeli potrzebujesz dobrego narzędzia rolniczego jak #kosa to odwiedź kowala w pobliskim miaście Semos!");
				addReply(Arrays.asList("scythe", "kosa"),
	                    "Ach cóż... Jeżeli potrzebujesz dobrego narzędzia rolniczego jak #kosa to odwiedź kowala w pobliskim miaście Semos!");
				/**
				addReply("hazen",
	                    "Ja... ja mam siostrę... Pamiętam miasto... Kirdneh");
				addReply("kirdneh",
	                    "Ja... pamiętam nazwę miasta... Kirdneh... Mam tam siostrę... Hazen!");
				addReply("name",
	                    "Ja... nie pamiętam... Co to było... Nazwa... Nazwa #zadania...");
				addOffer("Zrobisz dla mnie #zadanie?");
				*/
				addGoodbye("Żegnaj! Może Twoja droga będzie wolna od chwastów.");
			}
		};

		// Finalize Jingo Radish, the hoeing man near the Mill north of Semos
		npc.setEntityClass("hoeingmannpc");
		npc.setDescription("Widzisz człowieka z motyką, który zajęty jest odchwaszczaniem...");
		npc.setPosition(48,62);
		npc.initHP(100);
		zone.add(npc);

		return npc;
	}
}
