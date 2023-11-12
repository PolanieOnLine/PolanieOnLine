/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.magic.city;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a Wizard NPC who explains about the city.
 *
 * @author kymara
 */
public class GreeterNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Erodel Bmud") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(99, 111));
				nodes.add(new Node(106, 111));
				nodes.add(new Node(106, 98));
				nodes.add(new Node(105, 98));
				nodes.add(new Node(105, 89));
				nodes.add(new Node(107, 89));
				nodes.add(new Node(107, 44));
				nodes.add(new Node(104, 44));
				nodes.add(new Node(104, 40));
				nodes.add(new Node(57, 40));
				nodes.add(new Node(57, 51));
				nodes.add(new Node(93, 51));
				nodes.add(new Node(57, 51));
				nodes.add(new Node(57, 40));
				nodes.add(new Node(104, 40));
				nodes.add(new Node(104, 44));
				nodes.add(new Node(107, 44));
				nodes.add(new Node(107, 89));
				nodes.add(new Node(105, 89));
				nodes.add(new Node(105, 98));
				nodes.add(new Node(106, 98));
				nodes.add(new Node(106, 111));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj wędrowcze.");
				addJob("Jestem czarodziejem jak każdy, który mieszka w tym podziemnym magicznym mieście. Praktykujemy tutaj #magię.");
				addReply(Arrays.asList("magic", "magię"), "W rzeczywistości czary takie jak Sunlight Spell służą tutaj do utrzymania trawy i kwiatków. Wygląda na to, że zastanawiasz się dlaczego tradycyjni wrogowie tacy jak mroczne i zielone elfy żyją tutaj razem. Pozwól mi #wyjaśnić.");
				addReply(Arrays.asList("explain", "wyjaśnić"), "Jako miasto tylko dla czarodziei mamy dużo do nauczenia się od innych. Dlatego stare zwady są zapominane i dzięki temu żyjemy tutaj w pokoju.");
				addHelp("To jest część mojej #pracy, aby #oferować ( #offer ) Ci zaczarowane zwoje do podróżowania do każdego miasta w Faiumoni. Posiadam też zapas zwojów, które możesz zapisać i trochę zwoi do wywoływania potworów. Uważaj. Nie są tanie.");
				addQuest("Nikt nie może żyć, gdy inny przetrwał! Lord ciemności musi zginąć... nie... czekaj... to innym razem. Wybacz mi za zmylenie Ciebie. Niczego nie potrzebuję.");
				addGoodbye("Żegnaj.");
			}
		};

		npc.setDescription("Oto Erodel Bmud, przyjacielsko nastawiony starszy czarownik.");
		npc.setEntityClass("friendlywizardnpc");
		npc.setGender("M");
		npc.setPosition(99, 111);
		zone.add(npc);
	}
}
