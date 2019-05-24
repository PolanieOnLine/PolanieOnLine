/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a npc in Ados (name:Florence Boullabaisse) who is a fish soup maker on the market
 *
 * @author Krupi (fish soup idea) Vanessa Julius (implemented)
 *
 */
public class FishSoupMakerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Florence Boullabaisse") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(63, 14));
				nodes.add(new Node(70, 14));
                nodes.add(new Node(70, 10));
                nodes.add(new Node(67, 10));
                nodes.add(new Node(67, 14));
                nodes.add(new Node(64, 14));
                nodes.add(new Node(64, 10));
                nodes.add(new Node(63, 10));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				//addGreeting();
				addHelp("Mogę ugotować pyszną zupę rybną. Jeżeli nie lubisz ryb to mogę cię zapoznać z moją przyjaciółką Old Mother Helena w Fado. Robi najlepsze zupy warzywne w całym Faiumoni!");

				addQuest("Nie mam dla Ciebie zadań, ale mogę zaoferować świeżo ugotowaną zupę rybną na twoje podróże.");
				addJob("Jestem wyspecjalizowany w zupach. Moją ulubioną zupą jest fish soup, ale lubię także inne...");
				addOffer("Jeżeli jesteś naprawdę głodny lub potrzebyjesz jedzenia na swoje podróże to mogę ugotować tobie smaczną zupę rybną, ale dopiero, gdy przyniesze potrzebne produkty wg receptury.");
				addGoodbye("Życzę miłego pobytu na rynku w Ados!");
				addEmotionReply("cuddle", "przytula");
			}
		};

		npc.setDescription("Oto Florence Boullabaisse. Jest doskonałym szefem od zup.");
		npc.setEntityClass("fishsoupmakernpc");
		npc.setPosition(63, 14);
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
