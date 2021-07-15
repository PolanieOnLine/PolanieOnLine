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
package games.pol.server.maps.tatry.kuznice.blacksmith;

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
public class BlacksmithNPC implements ZoneConfigurator {
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
		final SpeakerNPC kuznia = new SpeakerNPC("Kowal Markusław") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(18, 2));
				nodes.add(new Node(25, 2));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj!");
				addJob("Jestem tutejszym kowalem. Mogę dla Ciebie #odlać sztabkę platyny.");
				addHelp("Odlewam sztabki platyny. Jeżeli chcesz, abym wykonał dla Ciebie powiedz mi tylko #odlej.");
				addOffer("Mogę odlać sztabkę platyny, jeśli przyniesiesz mi #polano, #'rudę platyny' oraz trochę pieniędzy. Powiedz tylko #odlej.");
				addGoodbye();

				addReply(Arrays.asList("polano", "drewno"),
						"Polano znajdziesz na obrzeżach lasów. Potrzebne mi są do podtrzymywania ognia czy wykonania rękojeści.");
				addReply(Arrays.asList("sztabka", "platyna", "sztabkę platyny", "ruda platyny", "rudę platyny", "ruda"),
						"Rudę platyny znajdziesz w górach Zakopanego. Uważaj tam na siebie!");

				final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
				requiredResources.put("polano", 3);
				requiredResources.put("ruda platyny", 1);
				requiredResources.put("money", 125);

				final ProducerBehaviour behaviour = new ProducerBehaviour("markuslaw_cast_platinum",
						Arrays.asList("cast", "odlej"), "sztabka platyny", requiredResources, 12 * 60);

				new ProducerAdder().addProducer(this, behaviour,
				        "Cześć! Jestem tutejszym kowalem. Jeżeli będziesz chciał, abym odlał dla Ciebie #'sztabkę platyny' to daj znać!");
			}
		};

		kuznia.setDescription("Oto kowal Markusław. Jest strasznie zapracowany!");
		kuznia.setEntityClass("goldsmithnpc");
		kuznia.setGender("M");
		kuznia.setPosition(18, 2);
		zone.add(kuznia);
	}
}