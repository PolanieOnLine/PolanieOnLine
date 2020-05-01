/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityinterior.potionsshop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.ShopList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.PlaySoundAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.adder.BuyerAdder;
import games.stendhal.server.entity.npc.behaviour.adder.SellerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;
import games.stendhal.server.entity.npc.condition.PlayerNextToCondition;
import games.stendhal.server.entity.player.Player;

public class PotionsDealerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Wanda");

		final List<Node> nodes = Arrays.asList(
				new Node(6, 5),
				new Node(9, 5)
		);

		final Map<String, Integer> pricesBuy = new HashMap<String, Integer>() {{
			put("mandragora", 300);
			//put("kokuda", 5000);
			put("muchomor", 60);
			put("trucizna", 40);
			put("mocna trucizna", 60);
			put("bardzo mocna trucizna", 500);
			put("śmiertelna trucizna", 1000);
			put("zabójcza trucizna", 1200);
			put("środek uspokajający", 200);
			put("gruczoł jadowy", 800);
		}};

		final Map<String, Integer> pricesSell = new HashMap<String, Integer>() {{
			put("mały eliksir", 150);
			put("eliksir", 300);
			put("duży eliksir", 600);
			put("wielki eliksir", 1650);
			put("antidotum", 100);
			put("mocne antidotum", 150);
			put("środek uspokajający", 400);
		}};

		new BuyerAdder().addBuyer(npc, new BuyerBehaviour(pricesBuy));
		new SellerAdder().addSeller(npc, new SellerBehaviour(pricesSell));

		// add Wanda's shop to the general shop list
		final ShopList shops = ShopList.get();
		for (final String key: pricesBuy.keySet()) {
			shops.add("deniranpotionsbuy", key, pricesBuy.get(key));
		}
		for (final String key: pricesSell.keySet()) {
			shops.add("deniranpotionssell", key, pricesSell.get(key));
		}

		npc.addGreeting("Witamy w sklepie z miksturami w mieście Deniran.");
		npc.addJob("Zarządzam tym sklepem z miksturami. Zapytaj mnie o moje #ceny.");
		npc.addHelp("Jeśli chcesz coś sprzedać, zapytaj mnie o moje #ceny, a powiem ci, co oferuję.");
		npc.addQuest("Nie mam dla ciebie nic do zrobienia. Lecz próbuję uzupełnić moje zapasy. Jeśli chcesz pomóc,"
				+ " po prostu zapytaj, a powiem ci o #cenach, które płacę za mikstury i trucizny.");

		npc.add(ConversationStates.ANY,
				Arrays.asList("price", "prices", "cena", "ceny", "cenach"),
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final int buyCount = pricesBuy.size();
						final int sellCount = pricesSell.size();

						final StringBuilder sb = new StringBuilder("Skupuję");
						int idx = 0;
						for (final String itemName: pricesBuy.keySet()) {
							if (buyCount > 1 && idx == buyCount - 1) {
								sb.append(" oraz");
							}
							sb.append(" " + itemName + " za " + Integer.toString(pricesBuy.get(itemName)));
							if (buyCount > 1 && idx < buyCount - 1) {
								sb.append(",");
							}
							idx++;
						}
						sb.append("\n\nSprzedaję również");
						idx = 0;
						for (final String itemName: pricesSell.keySet()) {
							if (sellCount > 1 && idx == sellCount - 1) {
								sb.append(" oraz");
							}
							sb.append(" " + itemName + " za " + Integer.toString(pricesSell.get(itemName)));
							if (sellCount > 1 && idx < sellCount - 1) {
								sb.append(",");
							}
							idx++;
						}

						raiser.say(sb.toString());
					}
				});

		npc.add(ConversationStates.ANY,
				ConversationPhrases.GOODBYE_MESSAGES,
				null,
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						new PlaySoundAction("kiss-female-01"),
						new SayTextAction("Proszę przyjść ponownie.")
				)
		);

		npc.add(ConversationStates.IDLE,
				Arrays.asList("kiss", "buziak", "buziaczek", "pocałunek", "całus"),
				new PlayerNextToCondition(),
				ConversationStates.IDLE,
				null,
				new MultipleActions(
						new PlaySoundAction("kiss-female-01"),
						new NPCEmoteAction("buziaki", "w policzek")
				));

		npc.setPosition(nodes.get(0).getX(), nodes.get(0).getY());
		npc.setPath(new FixedPath(nodes, true));
		npc.setCollisionAction(CollisionAction.STOP);
		npc.setOutfit(new Outfit("body=7,head=16,dress=29,mask=1,hair=20"));

		zone.add(npc);
	}
}