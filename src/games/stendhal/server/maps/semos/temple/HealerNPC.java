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
package games.stendhal.server.maps.semos.temple;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HealerNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTempleArea(zone);
	}

	private void buildSemosTempleArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ilisa") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(9, 6));
				nodes.add(new Node(14, 6));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Moje specjalne moce pomagają mi w leczeniu rannych ludzi. Także sprzedaję mikstury i antidota.");
				addHelp("Mogę Cię uleczyć ( #heal ) za darmo lub możesz wziąć jeden przygotowany przeze mnie medykament na podróż. Chcesz wiedzieć jaka jest moja #oferta?");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("healing")));
				// charge (2*the player level + 1) to heal
				new HealerAdder().addHealer(this, -2);
				addGoodbye();
			}
		};

		npc.setEntityClass("welcomernpc");
		npc.setDescription("Oto Ilisa. Jest młoda, ładna i otacza ją magiczna aura.");
		npc.setPosition(9, 6);
		npc.initHP(100);
		zone.add(npc);
	}
}
