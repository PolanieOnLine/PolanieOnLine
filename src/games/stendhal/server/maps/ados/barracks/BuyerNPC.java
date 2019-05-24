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
package games.stendhal.server.maps.ados.barracks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/**
 * Builds an NPC to buy previously unbought armor.
 *
 * @author kymara
 */
public class BuyerNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Mrotho") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(45, 49));
				nodes.add(new Node(29, 49));
				nodes.add(new Node(29, 57));
				nodes.add(new Node(45, 57));
				nodes.add(new Node(19, 57));
				nodes.add(new Node(19, 49));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Pozdrawiam. Przyszedłeś zaciągnąć się do wojska?");
				addReply(ConversationPhrases.YES_MESSAGES, "Ha! Cóż nie pozwolę Ci zapisać się do wojska, ale możesz nam #zaoferować jakąś zbroję...");
				addReply(ConversationPhrases.NO_MESSAGES, "Dobrze! I tak nigdy nie chciałbyś się tutaj dostać.");
				addJob("Poszukuję broni. Brakuje nam jej tutaj. Posiadamy dużo amunicji a mało zbroi. Widzę, że masz ze sobą jakąś zbroję, którą mógłbyś nam #zaoferować.");
				addHelp("Skupuję zbroje jeżeli coś masz to #zaoferuj mi to.");
				addOffer("Spójrz na tablicę, aby zobaczyć czego nam brakuje i ile za to płacimy. Sprzedaję również różne strzały.");
				addQuest("O, dziękuję, ale niczego już nie potrzebuję.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyrare3")), false);
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellarrows")), false);
				addGoodbye("Dowidzenia kolego.");
			}
		};

		npc.setDescription("Oto Mrotho, strzeże baraków w Ados.");
		npc.setEntityClass("barracksbuyernpc");
		npc.setPosition(45, 49);
		npc.initHP(500);
		zone.add(npc);
	}
}
