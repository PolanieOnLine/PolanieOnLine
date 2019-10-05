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
package games.stendhal.server.maps.gdansk.cavehut;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.HealerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author KarajuSs
 */

public class WitchNPC implements ZoneConfigurator {
	private final ShopList shops = SingletonRepository.getShopList();
	/**
	 * Configure a zone.
	 *
	 * @param zone
	 *            The zone to be configured.
	 * @param attributes
	 *            Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Xylona") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(24, 7));
				nodes.add(new Node(21, 7));
				nodes.add(new Node(21, 9));
				nodes.add(new Node(15, 9));
				nodes.add(new Node(15, 12));
				nodes.add(new Node(13, 12));
				nodes.add(new Node(13, 27));
				nodes.add(new Node(22, 27));
				nodes.add(new Node(13, 27));
				nodes.add(new Node(13, 12));
				nodes.add(new Node(15, 12));
				nodes.add(new Node(15, 9));
				nodes.add(new Node(21, 9));
				nodes.add(new Node(21, 7));
				nodes.add(new Node(24, 7));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem czarownicą skoro pytasz. Hoduję #collard jako hobby.");
				addReply("collard",	"To ta kapusta w doniczce. Bądź ostrożny z tym!");

				addHelp("Mogę Cię uleczyć. Powiedz tylko #ulecz. Jeżeli szukać pracy to powiedz tylko #'zadanie'.");
				new SellerAdder().addSeller(this, new SellerBehaviour(shops.get("xylona")));
				new HealerAdder().addHealer(this, 500);
				add(ConversationStates.ATTENDING,
				        Arrays.asList("magic", "scroll", "scrolls", "magia", "magiczne", "zwój", "zwoje"),
				        null,
				        ConversationStates.ATTENDING,
				        "#Oferuję zwoje, które pomogą Tobie w podróży: #'zwój gdańska', #'zwój tatrzański', #'zwój krakowski', #'zwój wieliczka' i #'niezapisany zwój'. Dla bardziej doświadczonych klientów mam #'zwój przywołania'!",
				        null);
				add(ConversationStates.ATTENDING, Arrays.asList("tatrzański", "zakopane", "zwój tatrzański"), null,
				        ConversationStates.ATTENDING,
				        "Tatrzańskie zwoje zabierają natychmiast spokojnego miejsca położonego niedaleko gór jakim jest Zakopane. Dobry drogą wyjścia z niebezpieczeństwa!", null);
				add(ConversationStates.ATTENDING, Arrays.asList("krakowski", "kraków", "zwój krakowski"), null,
				        ConversationStates.ATTENDING,
				        "Krakowskie zwoje zabierają natychmiast do pięknego zielonego miejsca jakim jest Kraków, ale również niebiezpieczego!", null);
				add(ConversationStates.ATTENDING, Arrays.asList("wieliczka", "zwój wieliczka"), null,
				        ConversationStates.ATTENDING,
				        "Zwoje wieliczki zabierają natychmiast do Wieliczki. Idealne miejsce na zwiedzanie kopalni soli!", null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("empty", "marked", "niezapisany zwój", "markable", "marked scroll"),
				        null,
				        ConversationStates.ATTENDING,
				        "Puste zwoje są używane do oznaczania pozycji. Zaznaczone zwoje mogą Cię zabrać z powrotem do tej lokacji. Są trochę drogie.",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        "zwój przywołania",
				        null,
				        ConversationStates.ATTENDING,
				        "Zwoje przywołania służą do przywoływania zwierząt. Doświadczeni magowie będą mogli przywoływać silniejsze potwory niż inni. Oczywiście te zwoje mogą być niebezpieczne jeżeli będą nadużywane.",
				        null);
				addGoodbye("Dowidzenia - ostrożnie nie dotykaj tej kuli. Prowadzi do bardzo niebezpiecznego miejsca!");
			}
		};

		npc.setEntityClass("witchnpc");
		npc.setPosition(24, 7);
		npc.initHP(100);
		npc.setDescription("Oto wiedźma zwana imieniem Xylona. Lata na miotle.");
		npc.setSounds(Arrays.asList("witch-cackle-1"));
		zone.add(npc);
	}
}
