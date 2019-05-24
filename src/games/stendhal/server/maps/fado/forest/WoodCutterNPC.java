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
package games.stendhal.server.maps.fado.forest;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to buy previously un bought axes
 * He is a wood cutter.
 *
 * @author kymara
 */
public class WoodCutterNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Woody") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(55, 84));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(68, 84));
				nodes.add(new Node(68, 71));
				nodes.add(new Node(57, 71));
				nodes.add(new Node(57, 76));
				nodes.add(new Node(57, 75));
				nodes.add(new Node(53, 75));
				nodes.add(new Node(53, 82));
				nodes.add(new Node(55, 82));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witam w lesie na południe od rzeki Or'ril.");
				addJob("Z zawodu jestem drwalem. Czy możesz mi #zaoferować jakieś topory?");
				addHelp("Czasami możesz zbierać drewno rozrzucone po lesie. Aha i możesz mi #zaoferować jakieś dobre topory na sprzedaż.");
				addOffer("Moje topory powinny być przytępione i szybkie. Sprawdź moją tabliczkę, którą postawiłem na zewnątrz domu, aby zobaczyć jakie topory skupuję.");
				addQuest("Co powiedziałeś? Niczego nie potrzebuję. Może Sally po drugiej stronie rzeki potrzebuje twojej pomocy.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyaxe")), false);
				addGoodbye("Dowidzenia.");
			}
		};

		npc.setDescription("Oto drwal Woody.");
		npc.setEntityClass("woodcutternpc");
		npc.setPosition(55, 84);
		npc.initHP(100);
		zone.add(npc);
	}
}
