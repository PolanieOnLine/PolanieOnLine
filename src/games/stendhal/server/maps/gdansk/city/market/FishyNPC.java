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
package games.stendhal.server.maps.gdansk.city.market;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

/**
 * @author zekkeq
 */
public class FishyNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

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
		final SpeakerNPC npc = new SpeakerNPC("Joachim") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37, 75));
				nodes.add(new Node(37, 79));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ahoj! Widzę, że wyglądasz na groźnego.");
				addJob("Na moce! Ja będę skupował. Ty będziesz sprzedawał?");
				addReply(Arrays.asList("yes", "tak"), "Cóż dreszcz mnie przeszedł! Sprawdź tablicę, aby zobaczyć po jakiej cenie i co skupuję");
				addReply("aye", "Cóż dreszcz mnie przeszedł! Sprawdź tablicę, aby zobaczyć po jakiej cenie i co skupuję");
				addReply(Arrays.asList("nie", "no"), "Ty tchórzliwa lilio, łobuzie! Dlaczego marnujesz mój czas?");
				addHelp("Co sobie myślisz, że taki jak ja stary wyjadacz potrzebuje pomocy?");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("skupryb")), false);
				addOffer("Sprawdź na tablicy ile dudków mogę dać.");
				addQuest("Nie masz towaru, którego ja potrzebuję.");
				addGoodbye("Arrgh, do zobaczenia!");
			}
		};

		npc.setDescription("Oto Joachim. Marynarz, który skupuje ryby z całego świata.");
		npc.setEntityClass("sailor1npc");
		npc.setGender("M");
		npc.setPosition(37, 75);
		zone.add(npc);
	}
}
