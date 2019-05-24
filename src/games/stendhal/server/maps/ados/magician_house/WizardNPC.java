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
package games.stendhal.server.maps.ados.magician_house;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WizardNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildMagicianHouseArea(zone);
	}

	private void buildMagicianHouseArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Haizen") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(7, 2));
				nodes.add(new Node(7, 4));
				nodes.add(new Node(13, 4));
				nodes.add(new Node(13, 9));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(9, 8));
				nodes.add(new Node(9, 9));
				nodes.add(new Node(2, 9));
				nodes.add(new Node(2, 3));
				nodes.add(new Node(7, 3));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem czarodziejem, który sprzedaje #magiczne #zwoje. Zapytaj mnie o #ofertę!");
				addHelp("Możesz zabrać potężną magię na swoje przygody za pomocą moich #magicznych #zwojów!");

				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("scrolls")));

				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("magic", "scroll", "scrolls", "magiczne", "magicznych", "zwoje","zwojów"),
				        null,
				        ConversationStates.ATTENDING,
				        "#Oferuję zwoje, które pomagają w szybszym podróżowaniu: zwoje #tatrzańskie, zwoje #semos i #zaznaczone oraz #puste zwoje. Dla bardziej doświadczonego klienta mam zwoje #przywołania!",
				        null);
				add(ConversationStates.ATTENDING, Arrays.asList("semos", "semos city scroll"), null,
				        ConversationStates.ATTENDING,
				        "Semos zwoje zabierają natychmiast do Semos. Dobra droga ucieczki przed niebezpieczeństwem!", null);
				add(ConversationStates.ATTENDING, Arrays.asList("tatrzańskie", "zwój tatrzański"), null,
				        ConversationStates.ATTENDING,
				        "Tatrzańskie zwoje zabierają natychmiast do Zakopanego. Pomysłowe wyjście z niebezpiecznej sytuacji!", null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("empty", "marked", "empty scroll", "markable", "marked scroll", "zaznaczone", "puste"),
				        null,
				        ConversationStates.ATTENDING,
				        "Puste zwoje służą do oznaczenia pozycji. Tak zaznaczone zwoje mogą Cię zabrać z powrotem do tego miejsca, w którym je zaznaczyłeś. Są trochę drogie, ale warto je mieć.",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("summon", "przywołania", "summon scroll"),
				        null,
				        ConversationStates.ATTENDING,
				        "Zwój przywołania umożliwia Ci przywołanie zwierząt. Doświadczeni magowie będą mogli przywoływać silniejsze potwory niż inni. Oczywiście te zwoje mogą być groźne jeżeli się ich nadużywa.",
				        null);

				addGoodbye();
			}
		};

		npc.setEntityClass("wisemannpc");
		npc.setPosition(7, 2);
		npc.initHP(100);
		npc.setDescription("Oto potężny czarownik Haizen. Sprzedaje zwoje teleportu.");
		zone.add(npc);
	}
}
