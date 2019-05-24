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
package games.stendhal.server.maps.mithrilbourgh.stores;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds an NPC to buy previously un bought weapons.
 * He is the QM of the Mithrilbourgh Army, who are short of boots and helmets
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
		final SpeakerNPC npc = new SpeakerNPC("Diehelm Brui") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(10, 4));
				nodes.add(new Node(10, 8));
				nodes.add(new Node(15, 8));
				nodes.add(new Node(15, 2));
				nodes.add(new Node(3, 2));
				nodes.add(new Node(3, 7));
				nodes.add(new Node(10, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj w sklepie dla Wojska Mithrilbourghtów.");
				addJob("Jestem dumny, że jestem Kwatermistrzem Wojska Mithrilbourghtów. Mamy mnóstwo #amunicji. Jednak mamy braki w #butach i #hełmach.");
				addReply(Arrays.asList("boots", "butach", "buty"), "Wydaję stone boots regularnie, ale nasi żołnierze zawsze je gubią. Kupię każde dobre buty, które możesz zaoferować ( #offer ). Zobacz niebieską książkę, aby poznać cennik.");
				addReply(Arrays.asList("helmets", "hełmach", "hełmy"), "Nie mam dobrego źródła, z którego mógłbym brać hełmy. Każdy, który mi zaoferujesz ( #trade ) będzie doceniony. W tym momencie mamy dość dla poruczników, a żadnego dla żołnierzy. W czerwonej książce znajdziesz szczegóły.");
				addReply(Arrays.asList("ammunition", "amunicja"), "Sprzedaję arrows, wooden arrows są najtańsze, power arrows najdroższe. Na tablicy masz wszystkie ceny.");
				addHelp("Jako kwatermistrz zbieram #oferty towarów, których nam brakuje.");
				addOffer("Kupię #buty i #hełmy w imieniu Wojska Mithrilbourghtów.");
				addQuest("Wojsko Mithrilbourghtów nie potrzebuje twoich usług.");
				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("boots&helm")), false);
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("sellarrows")), false);
				addGoodbye("Dowidzenia.");
			}
		};
		npc.setDescription("Oto Diehelm Brui, Kwatermistrz.");
		npc.setEntityClass("recruiter3npc");
		npc.setPosition(10, 4);
		npc.initHP(100);
		zone.add(npc);

		// Add a book with the shop offers
		final Sign book = new Sign();
		book.setPosition(12, 3);
		book.setText(" -- Skupuję -- \n buty żelazne\t 1000\n złote buty\t 1500\n buty cieni\t 2000\n buty kamienne\t 2500\n buty chaosu\t 4000\n buty z zielonego potwora\t 6000\n buty xenocyjskie\t 8000");
		book.setEntityClass("book_blue");
		book.setResistance(10);
		zone.add(book);

		final Sign book2 = new Sign();
		book2.setPosition(13, 4);
		book2.setText(" -- Skupuję -- \n złoty hełm\t 3000\n hełm cieni\t 4000\n złoty hełm wikingów 5000\n hełm chaosu\t 6000\n magiczny hełm kolczy\t 8000\n czarny hełm\t 10000");
		book2.setEntityClass("book_red");
		book2.setResistance(10);
		zone.add(book2);
	}
}
