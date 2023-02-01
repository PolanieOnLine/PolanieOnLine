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
package games.stendhal.server.maps.ados.bakery;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Ados Bakery (Inside / Level 0).
 *
 * @author hendrik
 */
public class BakerNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBakery(zone);
	}

	private void buildBakery(final StendhalRPZone zone) {
		final SpeakerNPC baker = new SpeakerNPC("Arlindo") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(15, 3));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(13, 9));
				nodes.add(new Node(13, 10));
				nodes.add(new Node(10, 10));
				nodes.add(new Node(10, 12));
				nodes.add(new Node(7, 12));
				nodes.add(new Node(7, 10));
				nodes.add(new Node(3, 10));
				nodes.add(new Node(3, 4));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(5, 3));
				nodes.add(new Node(5, 4));
				nodes.add(new Node(15, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addJob("Jestem lokalnym piekarzem. Mimo iż dostajemy większość zapasów z Semos to i tak jest sporo pracy do zrobienia.");
				addReply(Arrays.asList("mąka", "mięso", "marchew"),
						"W Ados brakuje zapasów. Dostajemy większość jedzenia z Semos, które jest na zachód stąd.");
				addReply(Arrays.asList("button mushroom", "pieczarka"),
						"Doszły nas słuchy, że w kuchni brakuje żywności. Postanowiliśmy powiększyć zapasy grzybów. Teraz znajdziesz ich większą ilość w lasach. ");
				addHelp("Jeżeli posiadasz mnóstwo mięsa lub sera to możesz sprzedać Siandrze w barze Ados.");
				addGoodbye();
			}
		};

		baker.setDescription("Oto Arlindo, który jest oficjalnym piekarzem w Ados. Znany jest z przepysznego placka");
		baker.setEntityClass("bakernpc");
		baker.setGender("M");
		baker.setDirection(Direction.DOWN);
		baker.setPosition(15, 3);
		zone.add(baker);
	}
}
