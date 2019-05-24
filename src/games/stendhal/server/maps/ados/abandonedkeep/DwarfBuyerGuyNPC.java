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
package games.stendhal.server.maps.ados.abandonedkeep;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Inside Ados Abandoned Keep - level -3 .
 */
public class DwarfBuyerGuyNPC implements ZoneConfigurator  {
	
    private final ShopList shops = SingletonRepository.getShopList();

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Ritati Dragontracker") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(25,32));
				nodes.add(new Node(38,32));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			public void createDialog() {

				addGreeting("Czego potrzebujesz?");
				addJob("Skupuję graty i różne drobiazgi. Ktoś musi to robić.");
				addHelp("Spójrz na mnie! Jestem zmuszony do skupowania świecidełek! W czym mogę TOBIE pomóc?");
				addOffer("Nie zawracaj mi głowy dopóki nie będziesz miał czegoś co ja potrzebuję! Spójrz na tablicę i zobacz ceny.");
				addQuest("O ile nie będziesz chciał #wynająć tego miejsca to nic nie będziesz mógł zrobić dla mnie.");
				addGoodbye("Zmiataj stąd!");
			    addReply(Arrays.asList("own", "wynająć"), "Co? Dlaczego nie możesz przyjść do mnie z wystarczającą ilością pieniędzy!");
			    // see games.stendhal.server.maps.quests.mithrilcloak.GettingTools for further behaviour
			    addReply(Arrays.asList("buy", "kupić"), "Niczego nie sprzedaję, ale możesz spojżeć na moją tablicę co skupuję. Albo zapytaj o #specjały.");
			    addReply(Arrays.asList("YOU", "TOBIE"), "Tak mówię do CIEBIE! Z kim jeszcze miałbym rozmawiać!");

				new BuyerAdder().addBuyer(this, new BuyerBehaviour(shops.get("buyoddsandends")), false);			    
			}};
			    
			npc.setPosition(25, 32);
			npc.setEntityClass("olddwarfnpc");
			npc.setDescription("Oto Ritati Dragontracker, który skupuje graty i drobiazgi.");
			zone.add(npc);		
			   	}
}