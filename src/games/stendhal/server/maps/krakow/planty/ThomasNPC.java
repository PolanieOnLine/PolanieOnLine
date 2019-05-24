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
package games.stendhal.server.maps.krakow.planty;

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
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.QuestCompletedSellerBehaviour;

/**
 * Build a NPC
 *
 * @author KarajuSs
 */
public class ThomasNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Thomas") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(78, 51));
				nodes.add(new Node(78, 53));
				nodes.add(new Node(77, 53));
				nodes.add(new Node(77, 55));
				nodes.add(new Node(73, 55));
				nodes.add(new Node(75, 55));
				nodes.add(new Node(75, 54));
				nodes.add(new Node(77, 54));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Interesuje się ogólnie rybactwem, ale ostatnio mi się łódka popsuła i nie mam jak wypłynąć w szerokie wody.");
				addOffer("Jeżeli pomożesz mi naprawić łódkę to będziesz mógł u mnie kupić tuńczyka!");
				// tuńczyk - 5;
				new SellerAdder().addSeller(this, new QuestCompletedSellerBehaviour("naprawa_lodzi", "Najpierw pomóż mi naprawić łódkę, a później będziesz mógł kupić ode mnie tuńczyka!", shops.get("sellthomas")), false);
				addGoodbye();
			}
		};

		npc.setDescription("Oto Thomas. Zajmuje się łowieniem ryb rzecznych, jednak codziennie wypływa również do portu, gdzie czeka na niego świeża dostawa tuńczyka. Tuńczyk to morska ryba, której nie złowi się w rzece, a jest na niego spore zapotrzebowanie.");
		npc.setEntityClass("fishermannpc");
		npc.setPosition(79, 51);
		npc.initHP(100);
		zone.add(npc);
	}
}
