/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tatry.kuznice.blacksmith;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

/**
 * @author KarajuSs
 */
public class ZygfrydNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildKuznia(zone);
	}

	private void buildKuznia(final StendhalRPZone zone) {
		final SpeakerNPC kuznia = new SpeakerNPC("Zygfryd") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 4));
				nodes.add(new Node(7, 7));
				nodes.add(new Node(13, 7));
				nodes.add(new Node(7, 7));
				nodes.add(new Node(7, 4));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Pomagam tutejszemu kowalowi. Nauczył mnie odlewać pewien minerał, sztabkę cieni.");
				addHelp("Odlewam sztabki cieni. Jeżeli chcesz, abym wykonał dla Ciebie powiedz mi tylko #odlej.");
				addOffer("Mogę odlać sztabkę cieni, jeśli przyniesiesz mi #polano, #'rudę cieni' oraz trochę pieniędzy. Powiedz tylko #odlej.");
				addGoodbye();

				addReply(Arrays.asList("polano", "drewno"),
						"Polano znajdziesz na obrzeżach lasów. Potrzebne mi są do podtrzymywania ognia czy wykonania rękojeści.");
				addReply(Arrays.asList("sztabka", "cieni", "sztabkę", "ruda", "rudę"),
						"Rudę cieni znajdziesz w górach Zakopanego. Uważaj tam na siebie!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 2);
				requiredResources.put("ruda cieni", 1);
				requiredResources.put("money", 70);

				final ProducerBehaviour behaviour = new ProducerBehaviour("zygfryd_cast_shadow",
						Arrays.asList("cast", "odlej"), "sztabka cieni", requiredResources, 7 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Cześć! Jestem pomocnikiem tutejszego kowala. Jeżeli będziesz chciał, abym odlał dla Ciebie #'sztabkę cieni' to daj znać!");
			}
		};

		kuznia.setDescription("Oto młody Zygfryd. Pomaga tutejszemu kowalowi w odlewaniu sztabek.");
		kuznia.setEntityClass("naughtyteennpc");
		kuznia.setGender("M");
		kuznia.setPosition(7, 4);
		zone.add(kuznia);
	}
}
