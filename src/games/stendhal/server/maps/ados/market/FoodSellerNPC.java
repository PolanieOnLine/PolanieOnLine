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
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Food and drink seller,  at Ados Market
 */
public class FoodSellerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final SpeakerNPC npc = new SpeakerNPC("Adena") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(31, 8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Mamy świeże jabłka i marchwie z kilku #farm niedaleko Semos");
				addReply(Arrays.asList("Semos Farm", "Semos", "Farm", "farms", "farmy Semos", "farma", "farmy", "Semos"), "Całe nasz jedzenie dostajemy z farm w Semos, ale droga jest #niebezpieczna.");
				addReply(Arrays.asList("dangerous", "expensive", "niebezpieczna"), "Całe wojsko walczy w wielkiej bitwie co ma wpływ na drogę Semos, która jest pozostawiona bez ochrony. Obawiam się, że ceny są relatywnie wysokie.");
				addJob("Sprzedaję produkty z #farm niedaleko Semos tak szybko jak je otrzymujemy.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("adosfoodseller")));
				addGoodbye();
			}
		};

		npc.setEntityClass("marketsellernpc");
		npc.setDescription("Adena przyjaźnie uśmiechnięta,a także ciężko pracująca.");
		npc.setPosition(31, 8);
		npc.initHP(100);
		npc.setDirection(Direction.DOWN);
		zone.add(npc);
	}
}
